import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const read = (relativePath) => fs.readFileSync(path.join(root, relativePath), "utf8");
const requiredFiles = [
  "version.properties",
  "apps/mobile/app.json",
  "apps/mobile/android/app/src/main/AndroidManifest.xml",
  "apps/mobile/android/app/src/main/res/values/strings.xml",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/domain/LinternaEngine.kt",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/domain/PremiumSequenceRunner.kt",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/ui/LinternaPremiumScreen.kt",
  "apps/mobile/android/app/src/main/java/com/linternapremium/app/localization/LinternaLocalization.kt",
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
  const premiumSequence = read("apps/mobile/android/app/src/main/java/com/linternapremium/app/domain/PremiumSequenceRunner.kt");
  const mainActivity = read("apps/mobile/android/app/src/main/java/com/linternapremium/app/MainActivity.kt");
  const screen = read("apps/mobile/android/app/src/main/java/com/linternapremium/app/ui/LinternaPremiumScreen.kt");
  const localization = read("apps/mobile/android/app/src/main/java/com/linternapremium/app/localization/LinternaLocalization.kt");
  const gradle = read("apps/mobile/android/app/build.gradle.kts");
  const mobilePackage = JSON.parse(read("apps/mobile/package.json"));
  const appConfig = JSON.parse(read("apps/mobile/app.json"));
  const strings = read("apps/mobile/android/app/src/main/res/values/strings.xml");
  const premiumMethod = engine.slice(engine.indexOf("fun pressPremium"), engine.indexOf("fun confirmPremiumPurchase"));
  const confirmationMethod = engine.slice(engine.indexOf("fun confirmPremiumPurchase"), engine.indexOf("fun dismissPurchase"));
  if (!premiumMethod.includes("torch.turnOff()")) errors.push("El apagado Premium debe cortar la linterna antes de ofrecer la compra");
  if (premiumMethod.includes("PremiumEffect.LaunchGooglePlay")) errors.push("Google Play debe abrirse recien despues de confirmar la compra");
  if (!confirmationMethod.includes("showPurchaseDialog") || !confirmationMethod.includes("PremiumEffect.LaunchGooglePlay")) {
    errors.push("La compra debe exigir la confirmacion previa antes de abrir Google Play");
  }
  if (!engine.includes("fun turnOffNormally")) errors.push("Falta el apagado normal gratuito");
  if (!premiumSequence.includes("finally") || !premiumSequence.includes("torch.turnOff()")) {
    errors.push("La secuencia Premium debe apagar el flash incluso si se interrumpe o falla");
  }
  if (!mainActivity.includes("if (!BuildConfig.DEMO_BILLING) return") || !screen.includes("isDemo && state.isPremiumOwned")) {
    errors.push("El restablecimiento de Premium debe quedar limitado a la variante demo");
  }
  if (appConfig.app.name !== "Linterna PREMIUM" || !strings.includes(">Linterna PREMIUM<") || !screen.includes('text = "PREMIUM"')) {
    errors.push("El nombre visible debe ser Linterna PREMIUM en metadatos, launcher e interfaz");
  }
  if (screen.toLowerCase().includes("mortal") || engine.toLowerCase().includes("mortal") || localization.toLowerCase().includes("mortal")) {
    errors.push("La experiencia activa debe usar plebeyo en lugar de mortal");
  }
  if (!localization.includes("EDICIÓN PLEBEYA") || !localization.includes("Apagar linterna como un plebeyo")) {
    errors.push("Faltan la insignia o el apagado normal con terminologia plebeya");
  }
  for (const code of [
    "es-AR",
    "es-ES",
    "en",
    "ru",
    "la",
    "ja",
    "it",
    "fr",
    "de",
    "nl",
    "zh-Hans",
    "zh-Hant",
    "pt-BR",
    "pt-PT",
    "ca",
    "eu",
    "gn",
    "quz",
    "cmn-Hans",
    "yue-Hant",
    "ko",
  ]) {
    if (!localization.includes(`(\"${code}\",`)) errors.push(`Falta el idioma ${code}`);
  }
  if (!screen.includes("LanguageSelector(") || !mainActivity.includes("PreferencesLanguageStore")) {
    errors.push("Falta el selector persistente de idiomas en la pantalla principal");
  }
  if (
    !screen.includes("horizontalArrangement = Arrangement.End") ||
    !screen.includes("scrollState = rememberScrollState()") ||
    !screen.includes("heightIn(max = 360.dp)")
  ) {
    errors.push("El selector de idiomas debe abrir debajo de su boton y limitarse con scroll");
  }
  if (
    !gradle.includes("APPS_DASHBOARD_ANDROID_TEST_KEYSTORE_PATH") ||
    mobilePackage.scripts["build:apk"] !== "node ../../tools/run-gradle.mjs assembleDemoRelease"
  ) {
    errors.push("La APK de prueba debe ser demoRelease con la firma QA estable");
  }

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
