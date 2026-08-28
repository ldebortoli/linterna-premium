import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const versionProperties = Object.fromEntries(
  fs.readFileSync(path.join(root, "version.properties"), "utf8")
    .trim()
    .split(/\r?\n/)
    .map((line) => line.split("=")),
);
const rootPackage = JSON.parse(fs.readFileSync(path.join(root, "package.json"), "utf8"));
const mobilePackage = JSON.parse(fs.readFileSync(path.join(root, "apps/mobile/package.json"), "utf8"));
const appConfig = JSON.parse(fs.readFileSync(path.join(root, "apps/mobile/app.json"), "utf8"));

const expectedName = versionProperties.versionName;
const expectedCode = Number(versionProperties.versionCode);
const errors = [];

if (!/^\d+\.\d+\.\d+$/.test(expectedName)) errors.push("versionName debe usar SemVer X.Y.Z");
if (!Number.isInteger(expectedCode) || expectedCode < 1) errors.push("versionCode debe ser un entero positivo");
if (rootPackage.version !== expectedName) errors.push("package.json no coincide con version.properties");
if (mobilePackage.version !== expectedName) errors.push("apps/mobile/package.json no coincide con version.properties");
if (appConfig.app.version !== expectedName) errors.push("apps/mobile/app.json no coincide con version.properties");
if (appConfig.app.android.versionCode !== expectedCode) errors.push("android.versionCode no coincide con version.properties");

if (errors.length > 0) {
  console.error(errors.map((error) => `- ${error}`).join("\n"));
  process.exit(1);
}

console.log(`Version verificada: ${expectedName} (${expectedCode})`);

