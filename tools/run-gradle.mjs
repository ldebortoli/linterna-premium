import { spawnSync } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";

const toolsDirectory = path.dirname(fileURLToPath(import.meta.url));
const androidDirectory = path.resolve(toolsDirectory, "../apps/mobile/android");
const tasks = process.argv.slice(2);

if (tasks.length === 0) {
  throw new Error("Indica al menos una tarea de Gradle.");
}

const isWindows = process.platform === "win32";
const command = isWindows ? (process.env.ComSpec || "cmd.exe") : "./gradlew";
const args = isWindows
  ? ["/d", "/s", "/c", "gradlew.bat", ...tasks, "--stacktrace"]
  : [...tasks, "--stacktrace"];

const result = spawnSync(command, args, {
  cwd: androidDirectory,
  env: process.env,
  stdio: "inherit",
  shell: false,
});

if (result.error) throw result.error;
process.exit(result.status ?? 1);

