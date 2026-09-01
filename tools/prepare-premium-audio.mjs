import fs from "node:fs";
import path from "node:path";
import { createHash } from "node:crypto";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const recipe = JSON.parse(fs.readFileSync(path.join(root, "audio/premium-sfx.json"), "utf8"));
const destination = path.join(root, "apps/mobile/android/app/src/main/assets/premium-sfx");
const hash = (data) => createHash("sha256").update(data).digest("hex");
const expectedIds = [437, 459, 531, 1928, 1934, 2011, 2012];
if (JSON.stringify(recipe.assets.map((asset) => asset.id).sort((a, b) => a - b)) !== JSON.stringify(expectedIds)) {
  throw new Error("El catálogo debe contener exactamente los siete audios aprobados; nunca Mixkit 462.");
}

export function verifyPreparedAudio() {
  for (const asset of recipe.assets) {
    const file = path.join(destination, `${asset.id}.pcm`);
    if (!fs.existsSync(file)) throw new Error("Faltan los efectos con licencia. Ejecutá npm run audio:prepare -- --download, previa lectura de docs/AUDIO_LICENSES.md.");
    const pcm = fs.readFileSync(file);
    if (pcm.length !== Math.floor(recipe.sampleRate * asset.durationMillis / 1000) * 2) {
      throw new Error(`Duración PCM incorrecta: ${asset.id}`);
    }
    if (hash(pcm) !== asset.pcmSha256) throw new Error(`Hash PCM incorrecto: ${asset.id}`);
  }
  if (fs.readdirSync(destination).some((name) => !expectedIds.some((id) => name === `${id}.pcm`))) {
    throw new Error("Hay archivos no aprobados en premium-sfx; no empaquetarlos.");
  }
  console.log("Siete clips PCM aprobados verificados (duración y SHA-256).");
}

async function prepare() {
  const args = process.argv.slice(2);
  if (args.includes("--verify")) return verifyPreparedAudio();
  const sourceIndex = args.indexOf("--source-dir");
  const sourceDir = sourceIndex >= 0 ? args[sourceIndex + 1] : null;
  if (!sourceDir && !args.includes("--download")) {
    throw new Error("Usá --source-dir <carpeta de preescuchas> o --download. La licencia prohíbe subir los clips al repo público.");
  }
  const cache = path.join(root, "artifacts/audio-source-cache");
  fs.mkdirSync(cache, { recursive: true });
  fs.mkdirSync(destination, { recursive: true });
  for (const asset of recipe.assets) {
    const sourceFile = sourceDir ? path.join(sourceDir, asset.previewFile) : path.join(cache, `${asset.id}.mp3`);
    if (!sourceDir && !fs.existsSync(sourceFile)) {
      const response = await fetch(`https://assets.mixkit.co/active_storage/sfx/${asset.id}/${asset.id}-preview.mp3`, { signal: AbortSignal.timeout(30000) });
      if (!response.ok) throw new Error(`Mixkit ${asset.id}: HTTP ${response.status}`);
      fs.writeFileSync(sourceFile, Buffer.from(await response.arrayBuffer()));
    }
    if (hash(fs.readFileSync(sourceFile)) !== asset.sourceSha256) throw new Error(`La fuente ${asset.id} difiere de la aprobada; no continuar.`);
    const samples = Math.floor(recipe.sampleRate * asset.durationMillis / 1000);
    const outputFile = path.join(destination, `${asset.id}.pcm`);
    const result = spawnSync("ffmpeg", ["-hide_banner", "-loglevel", "error", "-y", "-i", sourceFile,
      "-ac", "1", "-ar", String(recipe.sampleRate), "-af",
      `atrim=start=${asset.sourceOffsetSeconds}:duration=${asset.durationMillis / 1000},asetpts=PTS-STARTPTS,aresample=${recipe.sampleRate},aformat=sample_fmts=s16:channel_layouts=mono,apad=whole_len=${samples},atrim=end_sample=${samples}`,
      "-f", "s16le", "-acodec", "pcm_s16le", "-map_metadata", "-1", outputFile], { stdio: "inherit", shell: false });
    if (result.error) throw result.error;
    if (result.status !== 0) throw new Error(`FFmpeg falló para ${asset.id}`);
    console.log(`${asset.id}: ${hash(fs.readFileSync(outputFile))}`);
  }
  verifyPreparedAudio();
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) await prepare();
