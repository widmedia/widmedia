"""
Capture a screenshot from the Android device via ADB (used by scrcpy),
save it as a cropped PNG (324x576 px).

Crop: cut 52 px from top, 104 px from bottom.

Requirements:
    pip install Pillow
    adb must be on PATH (comes with Android SDK platform-tools or scrcpy bundle),
    or set ADB_PATH below to the full path of adb.exe.
"""

import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Pillow is required: pip install Pillow")
    sys.exit(1)


SCRIPT_DIR  = Path(__file__).parent
GRADLE_FILE = SCRIPT_DIR / "../../app/build.gradle.kts"


def read_version_name() -> str:
    text = GRADLE_FILE.read_text(encoding="utf-8")
    m = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    if not m:
        print(f"ERROR: versionName not found in {GRADLE_FILE}")
        sys.exit(1)
    return m.group(1)


VERSION_NAME = read_version_name()


def next_output_path() -> Path:
    for i in range(100):
        p = SCRIPT_DIR / f"TagesWert_{VERSION_NAME}_{i:02d}.png"
        if not p.exists():
            return p
    print("ERROR: All 100 filename slots are taken (_00 … _99).")
    sys.exit(1)


OUTPUT_CROPPED = next_output_path()

# Set to the full path of adb.exe if it is not on your PATH, e.g.:
ADB_PATH = r"C:\temp\scrcpy\scrcpy-win64-v4.0\adb.exe"
# ADB_PATH = shutil.which("adb") or "adb"

CROP_TOP    = 140
CROP_BOTTOM = 280


def capture_via_adb() -> Image.Image:
    """Save a screencap on the device, pull it locally, then delete it."""
    if not shutil.which(ADB_PATH) and not Path(ADB_PATH).is_file():
        print(
            f"ERROR: adb not found ('{ADB_PATH}').\n"
            "  Either add adb to your PATH or set ADB_PATH in this script.\n"
            "  adb.exe is usually in the scrcpy folder or in:\n"
            "  %LOCALAPPDATA%\\Android\\Sdk\\platform-tools\\"
        )
        sys.exit(1)

    DEVICE_PATH = "/sdcard/capture_tmp.png"

    def run(*args):
        r = subprocess.run([ADB_PATH, *args], capture_output=True, timeout=30)
        if r.returncode != 0:
            print(f"ERROR: adb {' '.join(args)} failed: {r.stderr.decode(errors='replace').strip()}")
            sys.exit(1)
        return r

    # 1. Capture on device
    run("shell", "screencap", "-p", DEVICE_PATH)

    # 2. Pull to a temp file (binary-safe file transfer, no stdout corruption)
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        tmp_path = Path(tmp.name)
    try:
        run("pull", DEVICE_PATH, str(tmp_path))
        img = Image.open(tmp_path).convert("RGB")
        img.load()  # force read before file is deleted
    finally:
        tmp_path.unlink(missing_ok=True)

    # 3. Delete from device
    run("shell", "rm", DEVICE_PATH)

    return img


def main():
    print("Capturing screenshot via ADB...")
    img = capture_via_adb()

    # Crop: remove CROP_TOP from top, CROP_BOTTOM from bottom
    # PIL crop box: (left, upper, right, lower)
    cropped = img.crop((0, CROP_TOP, img.width, img.height - CROP_BOTTOM))
    cropped.save(OUTPUT_CROPPED, "PNG")
    print(f"Cropped         : {OUTPUT_CROPPED}  ({cropped.width}x{cropped.height} px)")


if __name__ == "__main__":
    main()
