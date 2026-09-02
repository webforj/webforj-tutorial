import { closeSync, existsSync, mkdirSync, openSync, readFileSync, readdirSync } from 'node:fs';
import { createServer } from 'node:net';
import { basename, dirname, join, resolve } from 'node:path';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const repositoryRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..');
const stepDirectories = new Map([
  ['1', '1-creating-a-basic-app'],
  ['2', '2-working-with-data'],
  ['3', '3-routing-and-composites'],
  ['4', '4-observers-and-route-parameters'],
  ['5', '5-validating-and-binding-data'],
  ['6', '6-integrating-an-app-layout']
]);

const rawArguments = process.argv.slice(2);
const requestedSteps = [];
const playwrightArguments = [];
let skipBuild = false;

for (let index = 0; index < rawArguments.length; index += 1) {
  const argument = rawArguments[index];

  if (argument === '--skip-build') {
    skipBuild = true;
  } else if (argument === '--step' && rawArguments[index + 1]) {
    requestedSteps.push(rawArguments[index + 1]);
    index += 1;
  } else if (argument.startsWith('--step=')) {
    requestedSteps.push(argument.substring('--step='.length));
  } else if (/^[1-6]$/.test(argument)) {
    requestedSteps.push(argument);
  } else {
    playwrightArguments.push(argument);
  }
}

const steps = requestedSteps.length > 0
  ? [...new Set(requestedSteps)]
  : [...stepDirectories.keys()];

for (const step of steps) {
  if (!stepDirectories.has(step)) {
    throw new Error(`Unknown tutorial step: ${step}. Expected a number from 1 to 6.`);
  }
}

function run(command, args, options = {}) {
  return new Promise((resolvePromise, rejectPromise) => {
    const childCommand = process.platform === 'win32'
      ? (process.env.ComSpec ?? 'cmd.exe')
      : command;
    const childArguments = process.platform === 'win32'
      ? ['/d', '/s', '/c', `${command}.cmd`, ...args]
      : args;
    const child = spawn(childCommand, childArguments, {
      cwd: repositoryRoot,
      stdio: 'inherit',
      ...options
    });

    child.on('error', rejectPromise);
    child.on('exit', (code, signal) => {
      if (code === 0) {
        resolvePromise();
      } else {
        rejectPromise(new Error(
          `${basename(command)} exited with ${code ?? signal ?? 'an unknown status'}`));
      }
    });
  });
}

function canListen(port) {
  return new Promise(resolvePromise => {
    const server = createServer();
    server.unref();
    server.once('error', () => resolvePromise(false));
    server.listen({ host: '127.0.0.1', port }, () => {
      server.close(() => resolvePromise(true));
    });
  });
}

async function findFreePort() {
  for (const preferredPort of [8080, 8090]) {
    if (await canListen(preferredPort)) {
      return preferredPort;
    }
  }

  return new Promise((resolvePromise, rejectPromise) => {
    const server = createServer();
    server.unref();
    server.once('error', rejectPromise);
    server.listen({ host: '127.0.0.1', port: 0 }, () => {
      const address = server.address();
      const port = typeof address === 'object' && address ? address.port : 0;
      server.close(() => resolvePromise(port));
    });
  });
}

function findApplicationJar(stepDirectory) {
  const targetDirectory = join(repositoryRoot, stepDirectory, 'target');
  const jars = readdirSync(targetDirectory)
    .filter(file => file.endsWith('.jar') && !file.endsWith('.jar.original'));

  if (jars.length !== 1) {
    throw new Error(`Expected exactly one application JAR in ${targetDirectory}, found ${jars.length}.`);
  }

  return join(targetDirectory, jars[0]);
}

function tail(file, lines = 80) {
  if (!existsSync(file)) {
    return '(server log was not created)';
  }

  return readFileSync(file, 'utf8').split(/\r?\n/).slice(-lines).join('\n');
}

async function waitForApplication(url, application, logFile) {
  const deadline = Date.now() + 180_000;

  while (Date.now() < deadline) {
    if (application.exitCode !== null) {
      throw new Error(`Application exited before becoming ready.\n${tail(logFile)}`);
    }

    try {
      const response = await fetch(url, { redirect: 'manual' });
      if (response.status < 500) {
        return;
      }
    } catch {
      // The server is still starting.
    }

    await new Promise(resolvePromise => setTimeout(resolvePromise, 500));
  }

  throw new Error(`Application did not become ready within 180 seconds.\n${tail(logFile)}`);
}

async function stopApplication(application) {
  if (application.exitCode !== null) {
    return;
  }

  application.kill('SIGTERM');
  await Promise.race([
    new Promise(resolvePromise => application.once('exit', resolvePromise)),
    new Promise(resolvePromise => setTimeout(resolvePromise, 5_000))
  ]);

  if (application.exitCode === null) {
    application.kill('SIGKILL');
  }
}

async function runStep(step) {
  const stepDirectory = stepDirectories.get(step);
  const pom = join(stepDirectory, 'pom.xml');

  console.log(`\n=== Tutorial step ${step}: ${stepDirectory} ===`);

  if (!skipBuild) {
    await run('mvn', ['-ntp', '-f', pom, '-DskipTests', 'package']);
  }

  const jar = findApplicationJar(stepDirectory);
  const port = await findFreePort();
  const baseURL = `http://127.0.0.1:${port}`;
  const artifactDirectory = join(repositoryRoot, 'test-results', `step-${step}`);
  const logFile = join(artifactDirectory, 'server.log');
  mkdirSync(artifactDirectory, { recursive: true });
  const logDescriptor = openSync(logFile, 'w');

  console.log(`Starting ${stepDirectory} on ${baseURL}`);

  const application = spawn('java', [
    '-jar', jar,
    `--server.port=${port}`,
    '--server.address=127.0.0.1',
    '--spring.jpa.hibernate.ddl-auto=create-drop',
    '--webforj.devtools.browser.open=false',
    '--webforj.devtools.livereload.enabled=false',
    '--webforj.devtools.livereload.static-resources-enabled=false',
    '--webforj.devtools.craftforj.enabled=false',
    '--webforj.debug=false'
  ], {
    cwd: join(repositoryRoot, stepDirectory),
    stdio: ['ignore', logDescriptor, logDescriptor]
  });

  try {
    await waitForApplication(baseURL, application, logFile);
    await run('npx', [
      '--no-install',
      'playwright',
      'test',
      ...playwrightArguments
    ], {
      env: {
        ...process.env,
        E2E_STEP: step,
        E2E_BASE_URL: baseURL
      }
    });
  } catch (error) {
    console.error(`\nServer log: ${logFile}`);
    throw error;
  } finally {
    await stopApplication(application);
    closeSync(logDescriptor);
  }
}

const failures = [];

for (const step of steps) {
  try {
    await runStep(step);
  } catch (error) {
    failures.push({ step, error });
    console.error(`Step ${step} failed: ${error.message}`);
  }
}

if (failures.length > 0) {
  console.error(`\n${failures.length} tutorial step(s) failed: ${failures.map(({ step }) => step).join(', ')}`);
  process.exitCode = 1;
} else {
  console.log(`\nAll requested tutorial steps passed: ${steps.join(', ')}`);
}
