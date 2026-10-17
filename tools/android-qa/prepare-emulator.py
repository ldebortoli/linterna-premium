"""Prepare only the isolated, root-capable Android QA image; never a phone."""
import os
import subprocess
import time

SERIAL = os.environ.get("PREMIUM_QA_SERIAL", "emulator-5556")
if not SERIAL.startswith("emulator-"):
    raise RuntimeError("Physical devices are outside this harness")


def adb(*arguments, timeout=45):
    result = subprocess.run(
        ["adb", "-s", SERIAL, *arguments], capture_output=True, text=True, timeout=timeout
    )
    if result.returncode:
        raise RuntimeError(result.stderr[-500:])
    return result.stdout.strip()


adb("wait-for-device", timeout=240)
if adb("emu", "avd", "name").splitlines()[0] != "Premium_QA_API36":
    raise RuntimeError("Only the isolated PREMIUM QA AVD may be configured")
print(adb("root"), flush=True)
adb("wait-for-device")
if adb("shell", "id", "-u") != "0":
    raise RuntimeError("This QA image must support adb root")
multiplier = adb("shell", "getprop", "ro.hw_timeout_multiplier")
if not multiplier:
    adb("shell", "stop")
    try:
        adb("shell", "setprop", "sys.boot_completed", "0")
        adb("shell", "setprop", "ro.hw_timeout_multiplier", "3")
        if adb("shell", "getprop", "ro.hw_timeout_multiplier") != "3":
            raise RuntimeError("Slow-VM startup configuration was not applied")
    finally:
        adb("shell", "start")
elif multiplier != "3":
    raise RuntimeError("Unexpected read-only timeout configuration; use a clean QA boot")

deadline = time.monotonic() + 240
while time.monotonic() < deadline:
    if adb("shell", "getprop", "sys.boot_completed") == "1":
        break
    time.sleep(4)
else:
    raise RuntimeError("Android framework startup exceeded four minutes")
adb("shell", "settings", "put", "system", "font_scale", "1.0")
adb("shell", "settings", "put", "system", "screen_off_timeout", "1800000")
adb("shell", "settings", "put", "global", "window_animation_scale", "0")
adb("shell", "settings", "put", "global", "transition_animation_scale", "0")
# Keep the app's fifteen-second animations enabled while disabling OS window transitions.
adb("shell", "settings", "put", "global", "animator_duration_scale", "1")
adb("shell", "svc", "power", "stayon", "true")
adb("shell", "input", "keyevent", "224")
adb("shell", "wm", "dismiss-keyguard")
print("QA ready: AOSP API36, finite timeout multiplier=3, snapshots disabled", flush=True)
