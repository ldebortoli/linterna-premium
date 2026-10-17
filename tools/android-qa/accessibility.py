"""Exercise platform accessibility actions while real TalkBack remains enabled.

Install/enable TalkBack in the isolated AVD first. This does not certify voice
pronunciation or physical audio; the emulator is deliberately muted.
"""
import json
import sys
import time
import qa
import matrix
from audit import contains

slug = sys.argv[1]
qa.verify_emulator()
assert "com.google.android.marvin.talkback" in qa.shell("settings", "get", "secure", "enabled_accessibility_services"), "Enable real TalkBack before this suite"
output = qa.PROJECTS[slug] / "reports" / qa.RUN
qa.profile("es-AR", 1.3, 0, "gestural")
qa.shell("wm", "size", "reset")
qa.language(slug, "es-AR")
qa.shell("run-as", qa.pkg(slug), "rm", "-f", "shared_prefs/premium_entitlement.xml")
if slug == "gemidos":
    qa.shell("cmd", "media_session", "volume", "--stream", "3", "--set", "5")
else:
    qa.shell("pm", "grant", qa.pkg(slug), "android.permission.CAMERA")


def act(query, action):
    for _ in range(18):
        capture = qa.probe(query, action)
        actions = [node for node in capture["nodes"] if "performed" in node]
        if actions:
            assert actions[0]["performed"], "Accessibility action rejected: " + query + "/" + action
            if action == "focus":
                assert actions[0]["targetAccessibilityFocused"], "Platform accessibility focus was not assigned"
            return actions[0]
        scroll = qa.probe("", "scroll")
        assert any(node.get("performed") for node in scroll["nodes"]), "No accessible scroll reaches " + query
    raise RuntimeError("Accessible control not reached: " + query)


def assert_button_role(capture, label):
    # Compose may expose Button as a virtual child of a generic clickable
    # parent. Validate the role around its label rather than its click target.
    nodes = qa.own(slug, capture)
    assert any(contains(node["bounds"], text["bounds"]) for node in nodes if node["class"] == "android.widget.Button"
               for text in nodes if text["text"] == label), "Free stop must expose Button role"


try:
    matrix.start(slug)
    qa.ensure_rotation(slug, 0)
    act("🌐", "focus")
    qa.save(slug, "talkback-language-focus")
    act("🌐", "click")
    capture = qa.save(slug, "talkback-menu")
    assert any(node["checked"] or node["selected"] for node in qa.own(slug, capture)), "Menu selection state missing"
    act("English", "focus")
    act("English", "click")
    deadline = time.monotonic() + 5
    while ">en<" not in qa.shell("run-as", qa.pkg(slug), "cat", "shared_prefs/app_language.xml"):
        assert time.monotonic() < deadline, "Accessible language callback did not persist"
        time.sleep(.1)
    # Restore Spanish through the actual menu callback, preserving TalkBack.
    act("🌐", "click")
    act("Español (Argentina)", "click")
    if slug == "linterna":
        act("Encender linterna", "focus")
        act("Encender linterna", "click")
        act("Apagar linterna como un plebeyo", "focus")
        capture = qa.save(slug, "talkback-stop-focus")
        assert_button_role(capture, "Apagar linterna como un plebeyo")
        act("Apagar linterna como un plebeyo", "click")
    else:
        time.sleep(11)
        act("Apagado plebeyo", "focus")
        capture = qa.save(slug, "talkback-stop-focus")
        assert_button_role(capture, "Apagado plebeyo")
        act("Apagado plebeyo", "click")
        assert "volume is 5 " in qa.shell("cmd", "media_session", "volume", "--stream", "3", "--get"), "Free stop did not restore volume"
    service = qa.shell("dumpsys", "accessibility")
    (output / "talkback-service.txt").write_text(service, encoding="utf-8")
    assert "TalkBackService" in service, "TalkBack service disappeared"
    result = {"status": "passed", "realTalkBackEnabled": True, "platformFocusAndClick": True,
              "languageCallbackAndSelectedState": True, "freeStopRoleAndActivation": True,
              "voiceQualityEvaluated": False}
    (output / "accessibility.json").write_text(json.dumps(result, indent=2), encoding="utf-8")
    print(slug, "TALKBACK PASS", flush=True)
finally:
    qa.shell("input", "keyevent", 3)
    time.sleep(.5)
    qa.shell("am", "force-stop", qa.pkg(slug))
