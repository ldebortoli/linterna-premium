"""Opt-in runtime direction checks for every supported RTL language."""
import json
import sys
import time
import qa
import matrix

CODES = ["ar", "bal", "bm-Nkoo", "ckb", "dv", "fa", "fa-AF", "he", "ms-Arab", "pa-Arab", "ps", "sd", "ug", "ur", "yi"]
slug = sys.argv[1]
qa.verify_emulator()
output = qa.PROJECTS[slug] / "reports" / qa.RUN
qa.shell("wm", "size", "reset")
qa.shell("cmd", "overlay", "disable", "com.android.internal.display.cutout.emulation.hole")
qa.profile("es-AR", 1, 0, "gestural")
records = []
try:
    for code in CODES:
        qa.language(slug, code)
        matrix.start(slug)
        qa.ensure_rotation(slug, 0)
        capture = qa.save(slug, "rtl-" + code)
        nodes = qa.own(slug, capture)
        brand = next(node for node in nodes if node["text"] == slug.upper())
        left, top, right, bottom = brand["bounds"]
        assert (left + right) / 2 > capture["screen"][0] / 2, "Brand did not move to the RTL side"
        assert any("[" + code + "]" in node["text"] for node in nodes), "Selected language label missing"
        stored = qa.shell("run-as", qa.pkg(slug), "cat", "shared_prefs/app_language.xml")
        assert ">" + code + "<" in stored, "Selected language did not persist"
        records.append({"code": code, "direction": "rtl", "persisted": True, "status": "passed"})
        (output / "rtl.json").write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")
        print(slug, code, "RTL PASS", flush=True)
finally:
    qa.shell("input", "keyevent", "3")
    time.sleep(.2)
    qa.shell("am", "force-stop", qa.pkg(slug))
