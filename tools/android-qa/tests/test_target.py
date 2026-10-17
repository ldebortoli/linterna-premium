"""Offline safety checks: these tests never invoke ADB or start an emulator."""
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

HARNESS = Path(__file__).resolve().parents[1] / "qa.py"


def load_target(avd="Premium_QA_API36", serial="emulator-5556"):
    spec = importlib.util.spec_from_file_location("premium_qa_target_test", HARNESS)
    module = importlib.util.module_from_spec(spec)
    with patch.dict(os.environ, PREMIUM_QA_AVD=avd, PREMIUM_QA_SERIAL=serial,
                    PREMIUM_QA_RUN="offline-test"):
        spec.loader.exec_module(module)
    return module


class TargetSafetyTest(unittest.TestCase):
    def test_existing_aosp_target_remains_supported(self):
        qa = load_target()
        with patch.object(qa, "adb", return_value="Premium_QA_API36\nOK"):
            self.assertEqual(qa.verify_emulator(), "Premium_QA_API36")

    def test_original_google_play_target_requires_explicit_selection(self):
        qa = load_target("AppAlimentos_Pixel_API36", "emulator-5554")
        with patch.object(qa, "adb", return_value="AppAlimentos_Pixel_API36\nOK"):
            self.assertEqual(qa.verify_emulator(), "AppAlimentos_Pixel_API36")

    def test_unrelated_avd_is_rejected_before_any_adb_call(self):
        with self.assertRaises(ValueError):
            load_target("Another_AVD")

    def test_phone_serial_is_rejected(self):
        with self.assertRaises(ValueError):
            load_target("AppAlimentos_Pixel_API36", "phone-serial")

    def test_port_with_a_different_avd_is_rejected(self):
        qa = load_target("AppAlimentos_Pixel_API36", "emulator-5554")
        with patch.object(qa, "adb", return_value="Premium_QA_API36\nOK"):
            with self.assertRaises(RuntimeError):
                qa.verify_emulator()

    def test_only_this_projects_demo_debug_package_is_targeted(self):
        qa = load_target("AppAlimentos_Pixel_API36")
        self.assertEqual(qa.pkg(qa.SLUG), f"com.{qa.SLUG}premium.app.demo.debug")
        for slug in ("another", "gemidos" if qa.SLUG == "linterna" else "linterna"):
            with self.assertRaises(ValueError):
                qa.pkg(slug)

    def test_wrong_avd_prevents_probe_and_screenshot(self):
        qa = load_target("AppAlimentos_Pixel_API36")
        with patch.object(qa, "adb", return_value="Premium_QA_API36\nOK"), \
             patch.object(qa, "probe") as probe, \
             patch.object(qa, "screenshot") as screenshot:
            with self.assertRaises(RuntimeError):
                qa.save(qa.SLUG, "blocked")
        probe.assert_not_called()
        screenshot.assert_not_called()

    def test_evidence_records_the_actual_verified_avd_and_serial(self):
        qa = load_target("AppAlimentos_Pixel_API36", "emulator-5554")
        with tempfile.TemporaryDirectory() as folder:
            qa.PROJECTS = {qa.SLUG: Path(folder)}
            with patch.object(qa, "adb", return_value="AppAlimentos_Pixel_API36\nOK"), \
                 patch.object(qa, "probe", return_value={"nodes": []}), \
                 patch.object(qa, "screenshot", return_value=(640, 960)), \
                 patch.object(qa, "shell", return_value="window state"):
                capture = qa.save(qa.SLUG, "sample")
            evidence = json.loads((Path(folder) / "reports/offline-test/sample.json").read_text(encoding="utf-8"))
            self.assertEqual(capture["emulator"], evidence["emulator"])
            self.assertEqual(evidence["emulator"], {
                "avd": "AppAlimentos_Pixel_API36", "serial": "emulator-5554"})


if __name__ == "__main__":
    unittest.main()
