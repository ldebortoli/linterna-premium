"""Exercise the compact large-text free, purchase and celebration flows on a QA APK."""
import json
import sys
import time
import qa
import matrix
from audit import contains, safe_rect

slug = sys.argv[1]
qa.verify_emulator()
rotation = int(sys.argv[2]) if len(sys.argv) > 2 else 0
prefix = "scene-" + str(rotation)
output = qa.PROJECTS[slug] / "reports" / qa.RUN
qa.shell("wm", "size", "640x960")
qa.shell("cmd", "overlay", "disable", "com.android.internal.display.cutout.emulation.hole")
qa.profile("es-AR", 2, rotation, "threebutton")
qa.language(slug, "es-AR")
if slug == "gemidos":
    # Seed and verify the isolated fixture instead of assuming a previous run's
    # volume (a force-killed test process cannot execute its Android lifecycle).
    qa.shell("cmd", "media_session", "volume", "--stream", "3", "--set", "5")
    assert "volume is 5 " in qa.shell("cmd", "media_session", "volume", "--stream", "3", "--get")
# This file contains only the simulated entitlement of the demo.debug test fixture.
qa.shell("run-as", qa.pkg(slug), "rm", "-f", "shared_prefs/premium_entitlement.xml")
if slug == "linterna":
    qa.shell("pm", "grant", qa.pkg(slug), "android.permission.CAMERA")


def find(text):
    for _ in range(16):
        capture = qa.probe()
        matches = [node for node in qa.own(slug, capture) if node["text"] == text and node["visible"]]
        if matches:
            return matches[0]
        qa.scroll(slug, capture)
    raise RuntimeError("Control not reachable: " + text)


checks = {}
try:
    matrix.start(slug)
    qa.ensure_rotation(slug, rotation)
    if slug == "gemidos":
        time.sleep(11)
        capture = qa.save(slug, prefix + "-playing-top")
        assert any(node["text"] == "Apagado plebeyo" and node["visible"] for node in qa.own(slug, capture)), "Free stop left the viewport while audio was playing"
        checks["freeStopVisibleWithoutScrolling"] = True
    else:
        qa.tap(find("Encender linterna"))
        time.sleep(.5)
        qa.tap(find("Apagar linterna como un plebeyo"))
        qa.tap(find("Encender linterna"))
        checks["normalOffAndRestart"] = True

    target = find("APAGADO PREMIUM")
    qa.save(slug, prefix + "-active-actions")
    qa.tap(target)
    time.sleep(.3)
    capture = qa.save(slug, prefix + "-purchase-top")
    assert any(node["scrollable"] for node in qa.own(slug, capture)), "Purchase content is not scrollable"
    qa.tap(find("Ahora no"))
    time.sleep(.3)
    assert not any(node["text"] == "Prueba local" for node in qa.probe()["nodes"]), "Cancel did not close the purchase confirmation"
    checks["purchaseCanBeCancelled"] = True

    qa.tap(find("APAGADO PREMIUM"))
    capture = qa.probe()
    qa.scroll(slug, capture)
    qa.save(slug, prefix + "-purchase-middle")
    target = find("Simular compra Premium")
    qa.save(slug, prefix + "-purchase-bottom")
    qa.tap(target)
    deadline = time.monotonic() + 10
    while time.monotonic() < deadline:
        capture = qa.probe()
        regions = [node for node in qa.own(slug, capture) if node.get("liveRegion") == 1 and "felicitaciones" in node["description"].lower()]
        if regions:
            break
        time.sleep(.2)
    else:
        raise RuntimeError("Accessible celebration announcement did not appear")
    assert len(regions) == 1, "Celebration must expose one polite announcement"
    capture = qa.save(slug, prefix + "-celebration")
    width, height = capture["screen"]
    assert (width > height) == bool(rotation), "Scene was captured in the wrong orientation"
    window = (output / (prefix + "-celebration-window.txt")).read_text(encoding="utf-8")
    safe = safe_rect(window, width, height)
    region = next(node for node in qa.own(slug, capture) if node.get("liveRegion") == 1)
    assert contains(safe, region["bounds"]), "Celebration extends into runtime system insets"
    assert not any(node["text"] in ("7", "★", "♥", "★★★★★") for node in qa.own(slug, capture)), "Decorative reels leaked into the accessibility tree"
    checks["onePoliteCelebrationInsideInsets"] = True

    if slug == "gemidos":
        target = next(node for node in qa.own(slug, capture) if node["text"] == "Apagado plebeyo" and node["visible"])
        qa.tap(target)
        time.sleep(.5)
        volume = qa.shell("cmd", "media_session", "volume", "--stream", "3", "--get")
        assert "volume is 5 " in volume, "Normal stop did not restore the QA baseline multimedia volume"
        assert not any(node.get("liveRegion") == 1 for node in qa.probe()["nodes"]), "Normal stop did not dismiss the celebration"
        checks["stopCancelsCelebrationAndRestoresVolume"] = True

    qa.shell("input", "keyevent", "3")
    time.sleep(.2)
    qa.shell("am", "force-stop", qa.pkg(slug))
    matrix.start(slug)
    qa.ensure_rotation(slug, rotation)
    entitlement = qa.shell("run-as", qa.pkg(slug), "cat", "shared_prefs/premium_entitlement.xml")
    assert "true" in entitlement, "Simulated entitlement did not survive restart"
    checks["simulatedEntitlementPersists"] = True
    (output / (prefix + "-receipt.json")).write_text(json.dumps({"status": "passed", "rotation": rotation, "checks": checks}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(slug, prefix, "PASS", checks, flush=True)
finally:
    qa.shell("input", "keyevent", "3")
    time.sleep(.2)
    qa.shell("am", "force-stop", qa.pkg(slug))
