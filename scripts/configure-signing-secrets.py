#!/usr/bin/env python3
"""Upload an existing personal signing identity to this fork using authenticated gh."""

import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("config", type=Path, help="Private JSON configuration outside the repository")
    parser.add_argument("--dry-run", action="store_true", help="Verify the key without uploading")
    args = parser.parse_args()
    repository = Path(__file__).resolve().parents[1]
    try:
        config = json.loads(args.config.read_text())
        keystore = Path(config["AALYRICS_RELEASE_STORE_FILE"]).expanduser().resolve()
        if args.config.resolve().is_relative_to(repository) or keystore.is_relative_to(repository):
            raise ValueError("Keep the private config and keystore outside the repository")
        cert = subprocess.check_output(
            [
                "keytool", "-exportcert", "-keystore", str(keystore),
                "-alias", config["AALYRICS_RELEASE_KEY_ALIAS"],
                "-storepass:env", "AALYRICS_PRIVATE_STORE_PASSWORD",
            ],
            env=dict(os.environ, AALYRICS_PRIVATE_STORE_PASSWORD=config["AALYRICS_RELEASE_STORE_PASSWORD"]),
            stderr=subprocess.PIPE,
        )
        expected = (repository / ".github/release-signing-certificate.sha256").read_text().strip()
        if hashlib.sha256(cert).hexdigest() != expected:
            raise ValueError("Keystore does not match the pinned personal signing certificate")
        # Exporting a certificate does not unlock the private key. Sign and discard
        # a local certificate request to validate its password without changing the key.
        subprocess.run(
            [
                "keytool", "-certreq", "-keystore", str(keystore),
                "-alias", config["AALYRICS_RELEASE_KEY_ALIAS"],
                "-storepass:env", "AALYRICS_PRIVATE_STORE_PASSWORD",
                "-keypass:env", "AALYRICS_PRIVATE_KEY_PASSWORD",
            ],
            env=dict(
                os.environ,
                AALYRICS_PRIVATE_STORE_PASSWORD=config["AALYRICS_RELEASE_STORE_PASSWORD"],
                AALYRICS_PRIVATE_KEY_PASSWORD=config["AALYRICS_RELEASE_KEY_PASSWORD"],
            ),
            stdin=subprocess.DEVNULL, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE,
            check=True,
        )
        values = {
            "AALYRICS_RELEASE_KEYSTORE_BASE64": base64.b64encode(keystore.read_bytes()).decode(),
            **{key: config[key] for key in (
                "AALYRICS_RELEASE_STORE_PASSWORD",
                "AALYRICS_RELEASE_KEY_ALIAS",
                "AALYRICS_RELEASE_KEY_PASSWORD",
            )},
        }
        if any(not value for value in values.values()):
            raise ValueError("The signing configuration contains an empty value")
        if not args.dry_run:
            subprocess.run(["gh", "auth", "status", "--hostname", "github.com"], check=True)
            for name, value in values.items():
                subprocess.run(
                    ["gh", "secret", "set", name, "--repo", "WanHanxi/AALyrics"],
                    input=value, text=True, check=True,
                )
        action = "Validated (no upload)" if args.dry_run else "Uploaded"
        print(f"{action}: {len(values)} signing secrets for WanHanxi/AALyrics")
        print(f"Public certificate SHA-256: {expected}")
        print("PetitLyrics credentials are separate; this command does not configure them.")
    except (OSError, ValueError, KeyError, subprocess.CalledProcessError) as error:
        # Do not include config values, subprocess input or stderr in the failure log.
        parser.exit(1, f"Signing setup failed ({type(error).__name__}); check paths, key and gh authentication.\n")


if __name__ == "__main__":
    main()
