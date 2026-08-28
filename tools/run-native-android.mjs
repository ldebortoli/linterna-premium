import { spawnSync } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";

const toolsDirectory = path.dirname(fileURLToPath(import.meta.url));
const gradleRunner = path.join(toolsDirectory, "run-gradle.mjs");
const install = spawnSync(process.execPath, [gradleRunner, "installDemoDebug"], {
  cwd: path.resolve(toolsDirectory, ".."),
  env: process.env,
  stdio: "inherit",
  shell: false,
});
if (install.error) throw install.error;
if (install.status !== 0) process.exit(install.status ?? 1);

const androidHome = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
if (!androidHome) throw new Error("ANDROID_HOME no esta configurado.");
const adb = path.join(androidHome, "platform-tools", process.platform === "win32" ? "adb.exe" : "adb");
const serialArgs = process.env.ANDROID_SERIAL ? ["-s", process.env.ANDROID_SERIAL] : [];
const launch = spawnSync(
  adb,
  [...serialArgs, "shell", "am", "start", "-n", "com.linternapremium.app.demo.debug/com.linternapremium.app.MainActivity"],
  { stdio: "inherit", shell: false },
);
if (launch.error) throw launch.error;
process.exit(launch.status ?? 1);

