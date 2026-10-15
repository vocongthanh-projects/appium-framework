import { cpSync, existsSync, readFileSync, rmSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = dirname(dirname(fileURLToPath(import.meta.url)));
const driverRoot = join(projectRoot, 'node_modules', 'appium-uiautomator2-driver');
const safeDependencyRoot = join(projectRoot, 'node_modules', 'brace-expansion');
const dependencyRoot = join(driverRoot, 'node_modules', 'brace-expansion');
const dependencyManifest = join(dependencyRoot, 'package.json');
const safeVersion = '5.0.12';

if (!existsSync(driverRoot)) {
  throw new Error('appium-uiautomator2-driver is missing; run npm ci from the project root');
}

const installedVersion = existsSync(dependencyManifest)
  ? JSON.parse(readFileSync(dependencyManifest, 'utf8')).version
  : undefined;

const safeManifest = join(safeDependencyRoot, 'package.json');
if (!existsSync(safeManifest)) {
  throw new Error(`Locked brace-expansion ${safeVersion} is missing from root node_modules`);
}
const lockedSafeVersion = JSON.parse(readFileSync(safeManifest, 'utf8')).version;
if (lockedSafeVersion !== safeVersion) {
  throw new Error(`Expected locked brace-expansion ${safeVersion}, found ${lockedSafeVersion}`);
}

if (installedVersion !== safeVersion) {
  rmSync(dependencyRoot, { recursive: true, force: true });
  cpSync(safeDependencyRoot, dependencyRoot, { recursive: true });
}

const patchedVersion = JSON.parse(readFileSync(dependencyManifest, 'utf8')).version;
if (patchedVersion !== safeVersion) {
  throw new Error(`Expected brace-expansion ${safeVersion}, found ${patchedVersion}`);
}

console.log(`Verified uiautomator2 brace-expansion ${patchedVersion}`);
