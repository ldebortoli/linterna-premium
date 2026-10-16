import assert from "node:assert/strict";
import { createHash } from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import test from "node:test";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const inventory = JSON.parse(fs.readFileSync(path.join(root, "docs/LANGUAGE_INVENTORY.json"), "utf8"));
const directory = path.join(root, "apps/mobile/android/app/src/main/java/com/linternapremium/app/localization");
const languagesSource = fs.readFileSync(path.join(directory, "AppLanguage.kt"), "utf8");
const decode = (literal) => JSON.parse(literal.replaceAll("\\$", "$"));
const languages = [...languagesSource.matchAll(/^\s*(\w+)\(("(?:[^"\\]|\\.)*"), ("(?:[^"\\]|\\.)*")\)[,;]/gm)]
  .map(([, name, code, label]) => ({ name, code: decode(code), label: decode(label) }));
const keys = Object.keys(inventory.sourceCatalog);
const catalogs = new Map();
for (const file of fs.readdirSync(directory).filter((file) => /^ExtendedCatalogBatch\d+\.kt$/.test(file))) {
  const source = fs.readFileSync(path.join(directory, file), "utf8").replaceAll("\r\n", "\n");
  for (const [, name, block] of source.matchAll(/AppLanguage\.(\w+) to translationCatalog\(TextKey.entries,\n([\s\S]*?)\n    \)/g)) {
    const values = [...block.matchAll(/^\s*("(?:[^"\\]|\\.)*"),$/gm)].map(([, literal]) => decode(literal));
    assert.equal(catalogs.has(name), false, `Duplicate catalog ${name}`);
    catalogs.set(name, values);
  }
}

test("the frozen inventory preserves every original option and distinguishes approved and blocked candidates", () => {
  assert.equal(inventory.baselineCount, 21);
  assert.equal(inventory.sourceCandidates.length, 251);
  assert.equal(inventory.nativeReview, false);
  assert.deepEqual(languages.slice(0, 21).map(({ code, label }) => ({ code, label })),
    inventory.preserved.map(({ code, label }) => ({ code, label })));
  assert.equal(new Set(languages.map(({ code }) => code)).size, languages.length);
  assert.equal(new Set(languages.map(({ label }) => label)).size, languages.length);
  assert.equal(languages.length, inventory.publishedCount);
  const portable = JSON.parse(fs.readFileSync(path.join(root, "docs/LANGUAGE_PORTABLE.json"), "utf8"));
  assert.deepEqual(portable.languages.map(({ code, displayName }) => ({ code, label: displayName })),
    languages.map(({ code, label }) => ({ code, label })));
  assert.equal(portable.nativeReview, false);
  const approved = inventory.candidates.filter(({ status }) => status === "approved-machine");
  assert.equal(approved.length + 21, inventory.publishedCount);
  assert.equal(catalogs.size, approved.length);
  const rtlBlock = languagesSource.match(/rightToLeftCodes = setOf\(([^\n]+)\)/)[1];
  const rtlCodes = [...rtlBlock.matchAll(/"([^"]+)"/g)].map(([, code]) => code).sort();
  assert.deepEqual(rtlCodes, approved.filter(({ rtl }) => rtl).map(({ sourceCode }) => sourceCode).sort());
  for (const candidate of inventory.candidates.filter(({ status }) => status !== "approved-machine")) {
    assert.equal(languages.some(({ code }) => code === candidate.sourceCode), false, candidate.sourceCode);
    assert.ok(candidate.sourceFragmentKeys.length || candidate.unchangedSourceKeys.length);
  }
});

test("every new offline catalog matches its recorded keys, source and translation hash", () => {
  const sourceHash = createHash("sha256").update(Object.values(inventory.sourceCatalog).join("\n")).digest("hex");
  for (const candidate of inventory.candidates.filter(({ status }) => status === "approved-machine")) {
    const language = languages.find(({ code }) => code === candidate.sourceCode);
    assert.ok(language, candidate.sourceCode);
    const values = catalogs.get(language.name);
    assert.equal(values.length, keys.length, language.code);
    assert.equal(candidate.keyCount, keys.length, language.code);
    assert.equal(candidate.sourceSha256, sourceHash, language.code);
    const catalog = Object.fromEntries(keys.map((key, index) => [key, values[index]]));
    assert.equal(createHash("sha256").update(JSON.stringify(catalog)).digest("hex"), candidate.catalogSha256, language.code);
    for (const key of keys) {
      assert.ok(catalog[key].trim(), `${language.code}/${key}`);
      assert.notEqual(catalog[key].toLowerCase(), inventory.sourceCatalog[key].toLowerCase(), `${language.code}/${key}`);
    }
    assert.deepEqual(candidate.sourceFragmentKeys, []);
    assert.deepEqual(candidate.unchangedSourceKeys, []);
  }
});
