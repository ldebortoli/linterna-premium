import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const read = (relativePath) => fs.readFileSync(path.join(root, relativePath), "utf8");
const requiredFiles = [
  "version.properties",
  "apps/mobile/app.json",
  "apps/mobile/android/app/src/main/AndroidManifest.xml",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/domain/LinternaEngine.kt",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/ui/LinternaPremiumScreen.kt",
  "apps/mobile/android/app/src/demo/AndroidManifest.xml",
  "apps/mobile/android/app/src/play/AndroidManifest.xml",
  "docs/CONFIGURACION_GOOGLE.md",
  "docs/PRIVACIDAD.md",
];
const errors = [];

for (const relativePath of requiredFiles) {
  if (!fs.existsSync(path.join(root, relativePath))) errors.push(`Falta ${relativePath}`);
}

if (errors.length === 0) {
  const engine = read("apps/mobile/android/app/src/main/java/com/linternapremium/app/domain/LinternaEngine.kt");
  const premiumMethod = engine.slice(engine.indexOf("fun pressPremium"), engine.indexOf("fun confirmPremiumPurchase"));
  const confirmationMethod = engine.slice(engine.indexOf("fun confirmPremiumPurchase"), engine.indexOf("fun dismissPurchase"));
  if (!premiumMethod.includes("torch.turnOff()")) errors.push("El apagado Premium debe cortar la linterna antes de ofrecer la compra");
  if (premiumMethod.includes("PremiumEffect.LaunchGooglePlay")) errors.push("Google Play debe abrirse recien despues de confirmar la compra");
  if (!confirmationMethod.includes("showPurchaseDialog") || !confirmationMethod.includes("PremiumEffect.LaunchGooglePlay")) {
    errors.push("La compra debe exigir la confirmacion previa antes de abrir Google Play");
  }
  if (!engine.includes("fun turnOffNormally")) errors.push("Falta el apagado normal gratuito");

  const playManifest = read("apps/mobile/android/app/src/play/AndroidManifest.xml");
  if (playManifest.includes("ca-app-pub-3940256099942544")) {
    errors.push("La variante Play no puede contener identificadores publicitarios de prueba");
  }

  const manifest = read("apps/mobile/android/app/src/main/AndroidManifest.xml");
  if (!manifest.includes("android.permission.CAMERA")) errors.push("Falta el permiso de camara para controlar el flash");
  if (manifest.includes("READ_CONTACTS") || manifest.includes("ACCESS_FINE_LOCATION")) {
    errors.push("La app solicita permisos fuera de alcance");
  }
}

if (errors.length > 0) {
  console.error(errors.map((error) => `- ${error}`).join("\n"));
  process.exit(1);
}

console.log("Estructura, seguridad de apagado y separacion demo/Play verificadas.");
