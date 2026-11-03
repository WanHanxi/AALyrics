#!/usr/bin/env python3
"""Check the signed personal distribution artifact before publishing it."""

import argparse
from pathlib import Path
import re
import subprocess
import zipfile


def verify(apk, build_tools, version_name, version_code, repository):
    badging = subprocess.check_output(
        [str(build_tools / "aapt"), "dump", "badging", str(apk)], text=True
    )
    package = re.search(
        r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'",
        badging,
        re.MULTILINE,
    )
    expected = ("io.github.wanhanxi.aalyrics", str(version_code), version_name)
    if package is None or package.groups() != expected:
        raise ValueError(f"APK package/version mismatch; expected {expected}")
    if "application-debuggable" in badging:
        raise ValueError("A Release APK must not be debuggable")

    certificates = subprocess.check_output(
        [str(build_tools / "apksigner"), "verify", "--print-certs", str(apk)],
        text=True,
    )
    fingerprints = re.findall(
        r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})$",
        certificates,
        re.MULTILINE,
    )
    expected_fingerprint = (
        repository / ".github/release-signing-certificate.sha256"
    ).read_text().strip().lower()
    if fingerprints != [expected_fingerprint]:
        raise ValueError("APK certificate does not match the personal release identity")

    changelog = (repository / "CHANGELOG.md").read_bytes()
    heading = re.search(rb"^## \[([^\]]+)\]", changelog, re.MULTILINE)
    if heading is None or heading.group(1).decode() != version_name:
        raise ValueError("Newest Changelog heading does not match the APK version")
    with zipfile.ZipFile(apk) as archive:
        if archive.read("assets/aalyrics_changelog.md") != changelog:
            raise ValueError("Bundled Changelog differs from the checked-in release notes")
    print(f"Verified {apk.name}: {expected[0]}, {version_name}, versionCode {version_code}")
    print(f"Release certificate SHA-256: {expected_fingerprint}")
    print("Signature, non-debuggable build and bundled Changelog checks passed.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("--build-tools", type=Path, required=True)
    parser.add_argument("--version-name", required=True)
    parser.add_argument("--version-code", type=int, required=True)
    args = parser.parse_args()
    try:
        verify(
            args.apk,
            args.build_tools,
            args.version_name,
            args.version_code,
            Path(__file__).resolve().parents[1],
        )
    except (OSError, ValueError, KeyError, zipfile.BadZipFile, subprocess.CalledProcessError) as error:
        parser.exit(1, f"Release APK verification failed: {error}\n")


if __name__ == "__main__":
    main()
