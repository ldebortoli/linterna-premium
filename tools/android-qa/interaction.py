"""Keyboard, real language selection and first/last menu reachability checks."""
import json
import sys
import time
import qa
import matrix
from audit import contains

slug = sys.argv[1]
output = qa.PROJECTS[slug] / "reports" / qa.RUN
qa.profile()
qa.shell("wm", "size", "reset")
qa.shell("cmd", "overlay", "disable", "com.android.internal.display.cutout.emulation.hole")
qa.language(slug, "es-AR")


def restart():
    qa.shell("input", "keyevent", 3)
    time.sleep(.2)
    qa.shell("am", "force-stop", qa.pkg(slug))
    matrix.start(slug)
    return qa.ensure_rotation(slug, 0)


def stored(code):
    deadline = time.monotonic() + 5
    while time.monotonic() < deadline:
        xml = qa.shell("run-as", qa.pkg(slug), "cat", "shared_prefs/app_language.xml")
        if ">" + code + "<" in xml:
            break
        time.sleep(.1)
    else:
        raise RuntimeError("Language selection callback did not persist " + code)
    capture = restart()
    label = {"en": "English", "zu": "Zulu [zu]"}[code]
    assert any("🌐" in node["text"] and label in node["text"] for node in qa.own(slug, capture)), "Selection lost after process restart"


try:
    matrix.start(slug)
    qa.ensure_rotation(slug, 0)
    # Android keyboard navigation must reach the language button without touch.
    for _ in range(12):
        qa.shell("input", "keyevent", 61)
        capture = qa.probe()
        focused = [node for node in qa.own(slug, capture) if node["focused"]]
        if any(node["text"] and "🌐" in node["text"] for node in focused):
            break
        if any(contains(node["bounds"], label["bounds"]) for node in focused for label in qa.own(slug, capture) if "🌐" in label["text"]):
            break
    else:
        raise RuntimeError("Keyboard focus did not reach language control")
    qa.shell("input", "keyevent", 66)
    capture = qa.save(slug, "keyboard-menu")
    assert any("Español (Argentina)" in node["text"] for node in qa.own(slug, capture)), "Enter did not open the language menu"
    qa.shell("input", "keyevent", 111)
    assert not any(node["text"] == "✓ Español (Argentina)" for node in qa.probe()["nodes"]), "Escape did not dismiss menu"
    capture = qa.menu(slug, qa.probe())
    english = next(node for node in qa.own(slug, capture) if node["text"] == "English")
    qa.tap(english)
    stored("en")
    capture = qa.menu(slug, qa.probe())
    nodes = qa.own(slug, capture)
    assert any(contains(node["bounds"], label["bounds"]) for node in nodes if node["checked"] or node["selected"]
               for label in nodes if label["text"] == "✓ English"), "Selected language has no accessible state"
    for _ in range(70):
        capture = qa.probe()
        last = [node for node in qa.own(slug, capture) if node["text"] == "Zulu [zu]" and node["visible"]]
        if last:
            break
        qa.scroll(slug, capture)
    else:
        raise RuntimeError("Last language is not reachable")
    qa.save(slug, "language-menu-last")
    qa.tap(last[0])
    stored("zu")
    qa.save(slug, "language-last-persisted")
    result = {"status": "passed", "keyboardTabEnterEscape": True,
              "firstAndLastMenuEntriesReachable": True, "realSelectionPersistsAfterRestart": ["en", "zu"],
              "selectedLanguageAccessibleState": True}
    (output / "interaction.json").write_text(json.dumps(result, indent=2), encoding="utf-8")
    print(slug, "INTERACTION PASS", flush=True)
finally:
    qa.shell("input", "keyevent", 3)
    time.sleep(.2)
    qa.shell("am", "force-stop", qa.pkg(slug))
