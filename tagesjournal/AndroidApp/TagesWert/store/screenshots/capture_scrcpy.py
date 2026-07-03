"""
Capture 5 screenshots from the Android device via ADB at defined app states.

Steps:
  1. Lock screen   - immediate screenshot, then prompt user to unlock manually.
  2. Main view     - user has unlocked; take screenshot immediately.
  3. Calendar day  - click today's day-number button, take screenshot.
  4. Tutorial      - click 'Speichern', click 'Tutorial neu starten', take screenshot.
  5. Import/Export - click 'Tutorial überspringen', click 'Import / Export', take screenshot.

Each run creates a new series: TagesWert_{version}_{series:02d}_{step}.png

Requirements:
    pip install Pillow
    adb must be on PATH (comes with Android SDK platform-tools or scrcpy bundle),
    or set ADB_PATH below to the full path of adb.exe.
"""

import datetime
import re
import shutil
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    from PIL import Image  # type: ignore
except ImportError:
    print("Pillow is required: pip install Pillow")
    sys.exit(1)


SCRIPT_DIR  = Path(__file__).parent
GRADLE_FILE = SCRIPT_DIR / "../../app/build.gradle.kts"

# Set to the full path of adb.exe if it is not on your PATH.
ADB_PATH = r"C:\temp\scrcpy\scrcpy-win64-v4.0\adb.exe"

CROP_TOP    = 76
CROP_BOTTOM = 344

STEP_LABELS = ["1_lock", "2_main", "3_calendar", "4_tutorial", "5_import_export"]


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def read_version_name() -> str:
    text = GRADLE_FILE.read_text(encoding="utf-8")
    m = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    if not m:
        print(f"ERROR: versionName not found in {GRADLE_FILE}")
        sys.exit(1)
    return m.group(1)


VERSION_NAME = read_version_name()


def series_output_paths() -> list[Path]:
    """Find the first free series index and return all 5 output paths."""
    for i in range(100):
        paths = [SCRIPT_DIR / f"TagesWert_{VERSION_NAME}_{i:02d}_{lbl}.png"
                 for lbl in STEP_LABELS]
        if not any(p.exists() for p in paths):
            return paths
    print("ERROR: All 100 series slots are taken.")
    sys.exit(1)


def _run(*args) -> subprocess.CompletedProcess:
    """Run an adb command; exit on failure."""
    r = subprocess.run([ADB_PATH, *args], capture_output=True, timeout=30)
    if r.returncode != 0:
        print(f"ERROR: adb {' '.join(str(a) for a in args)} failed:\n"
              f"  {r.stderr.decode(errors='replace').strip()}")
        sys.exit(1)
    return r


def _check_adb() -> None:
    if not shutil.which(ADB_PATH) and not Path(ADB_PATH).is_file():
        print(
            f"ERROR: adb not found ('{ADB_PATH}').\n"
            "  Either add adb to your PATH or set ADB_PATH in this script.\n"
            "  adb.exe is usually in the scrcpy folder or in:\n"
            "  %LOCALAPPDATA%\\Android\\Sdk\\platform-tools\\"
        )
        sys.exit(1)


def capture_and_crop() -> Image.Image:
    """Screencap on device, pull, crop, return PIL image."""
    DEVICE_PATH = "/sdcard/capture_tmp.png"
    _run("shell", "screencap", "-p", DEVICE_PATH)
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        tmp_path = Path(tmp.name)
    try:
        _run("pull", DEVICE_PATH, str(tmp_path))
        img = Image.open(tmp_path).convert("RGB")
        img.load()
    finally:
        tmp_path.unlink(missing_ok=True)
    _run("shell", "rm", DEVICE_PATH)
    cropped = img.crop((0, CROP_TOP, img.width, img.height - CROP_BOTTOM))
    return cropped


def save_screenshot(out_path: Path) -> None:
    img = capture_and_crop()
    img.save(out_path, "PNG")
    print(f"  Saved: {out_path.name}  ({img.width}x{img.height} px)")


def _dump_ui_tree() -> ET.ElementTree:
    """Dump the UI hierarchy from the device and return the parsed XML tree."""
    DEVICE_XML = "/sdcard/ui_dump.xml"
    _run("shell", "uiautomator", "dump", DEVICE_XML)
    with tempfile.NamedTemporaryFile(suffix=".xml", delete=False) as tmp:
        tmp_path = Path(tmp.name)
    try:
        _run("pull", DEVICE_XML, str(tmp_path))
        tree = ET.parse(tmp_path)
    finally:
        tmp_path.unlink(missing_ok=True)
    _run("shell", "rm", DEVICE_XML)
    return tree # type: ignore


def _find_node(tree: ET.ElementTree, text: str) -> "ET.Element | None":
    for elem in tree.iter():
        if elem.attrib.get("text") == text:
            return elem
    return None


def _tap_node(node: ET.Element, label: str) -> None:
    bounds = node.attrib.get("bounds", "")
    m = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
    if not m:
        print(f"ERROR: Could not parse bounds '{bounds}' for '{label}'.")
        sys.exit(1)
    x1, y1, x2, y2 = int(m.group(1)), int(m.group(2)), int(m.group(3)), int(m.group(4))
    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
    print(f"  Tapping '{label}' at ({cx}, {cy})...")
    _run("shell", "input", "tap", str(cx), str(cy))


def click_button_by_text(text: str, max_scrolls: int = 10) -> None:
    """Find a button by visible text (scrolling if needed) and tap it."""
    tree = _dump_ui_tree()
    node = _find_node(tree, text)
    for attempt in range(max_scrolls):
        if node is not None:
            break
        print(f"  '{text}' not visible – scrolling down ({attempt + 1}/{max_scrolls})...")
        _run("shell", "input", "swipe", "500", "700", "500", "300", "400")
        time.sleep(0.5)
        node = _find_node(_dump_ui_tree(), text)
    if node is None:
        print(f"ERROR: '{text}' not found after {max_scrolls} scroll attempts.")
        sys.exit(1)
    _tap_node(node, text)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    _check_adb()
    today_day = str(datetime.date.today().day)
    paths = series_output_paths()

    # -- Step 1: lock screen screenshot, then prompt user to unlock ----------
    print("\n[Step 1] Taking lock-screen screenshot...")
    save_screenshot(paths[0])
    input("\n  >>> Please unlock the app on the device, then press Enter here to continue... ")

    # -- Step 2: main view screenshot ----------------------------------------
    print("\n[Step 2] Taking main view screenshot...")
    time.sleep(1)  # let the UI fully render
    save_screenshot(paths[1])

    # -- Step 3: click today's day in the calendar, take screenshot ----------
    print(f"\n[Step 3] Clicking day '{today_day}' in calendar view...")
    click_button_by_text(today_day)
    time.sleep(1)
    save_screenshot(paths[2])

    # -- Step 4: speichern -> Tutorial neu starten, take screenshot ----------
    print("\n[Step 4] Clicking 'Speichern'...")
    click_button_by_text("Speichern")
    time.sleep(0.5)
    print("  Clicking 'Tutorial neu starten'...")
    click_button_by_text("Tutorial neu starten")
    time.sleep(1)
    save_screenshot(paths[3])

    # -- Step 5: Tutorial überspringen -> Import / Export, take screenshot ---
    print("\n[Step 5] Clicking 'Tutorial überspringen'...")
    click_button_by_text("Tutorial überspringen")
    time.sleep(0.5)
    print("  Clicking 'Import / Export'...")
    click_button_by_text("Import / Export")
    time.sleep(1)
    save_screenshot(paths[4])

    # -- Final: press the back arrow to return to the main view --------------
    print("\n[Final] Pressing back arrow...")
    _run("shell", "input", "keyevent", "KEYCODE_BACK")

    print("\nDone. 5 screenshots saved.")


if __name__ == "__main__":
    main()

