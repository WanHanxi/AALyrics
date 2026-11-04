"""Exercise signing setup with a real temporary JKS and a local gh recorder."""

import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest


class ConfigureSigningSecretsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory(prefix="aalyrics-signing-test-")
        cls.addClassCleanup(cls.temporary.cleanup)
        root = Path(cls.temporary.name)
        repository = root / "repository"
        (repository / "scripts").mkdir(parents=True)
        (repository / ".github").mkdir()
        cls.script = repository / "scripts/configure-signing-secrets.py"
        shutil.copyfile(Path(__file__).resolve().parents[1] / cls.script.name, cls.script)
        cls.keystore = root / "test.jks"
        cls.config_path = root / "signing.json"
        cls.calls_path = root / "gh-calls.jsonl"
        cls.config = {
            "AALYRICS_RELEASE_STORE_FILE": str(cls.keystore),
            "AALYRICS_RELEASE_STORE_PASSWORD": "test-store-password",
            "AALYRICS_RELEASE_KEY_ALIAS": "test-release-key",
            "AALYRICS_RELEASE_KEY_PASSWORD": "different-test-key-password",
        }
        key_env = dict(
            os.environ,
            TEST_STORE_PASSWORD=cls.config["AALYRICS_RELEASE_STORE_PASSWORD"],
            TEST_KEY_PASSWORD=cls.config["AALYRICS_RELEASE_KEY_PASSWORD"],
        )
        subprocess.run(
            [
                "keytool", "-genkeypair", "-keystore", str(cls.keystore),
                "-storetype", "JKS", "-alias", cls.config["AALYRICS_RELEASE_KEY_ALIAS"],
                "-storepass:env", "TEST_STORE_PASSWORD", "-keypass:env", "TEST_KEY_PASSWORD",
                "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
                "-dname", "CN=AALyrics signing test", "-noprompt",
            ],
            env=key_env, check=True, capture_output=True,
        )
        cert = subprocess.check_output(
            [
                "keytool", "-exportcert", "-keystore", str(cls.keystore),
                "-alias", cls.config["AALYRICS_RELEASE_KEY_ALIAS"],
                "-storepass:env", "TEST_STORE_PASSWORD",
            ],
            env=key_env, stderr=subprocess.PIPE,
        )
        (repository / ".github/release-signing-certificate.sha256").write_text(
            hashlib.sha256(cert).hexdigest() + "\n",
        )
        binary_directory = root / "bin"
        binary_directory.mkdir()
        gh = binary_directory / "gh"
        gh.write_text(
            f"#!{sys.executable}\n"
            "import json, os, sys\n"
            "if sys.argv[1:3] == ['secret', 'set']:\n"
            "    if not sys.stdin.read():\n"
            "        sys.exit(1)\n"
            "with open(os.environ['TEST_GH_CALLS'], 'a') as output:\n"
            "    output.write(json.dumps(sys.argv[1:]) + '\\n')\n",
        )
        gh.chmod(0o700)
        cls.env = dict(
            os.environ,
            PATH=str(binary_directory) + os.pathsep + os.environ["PATH"],
            TEST_GH_CALLS=str(cls.calls_path),
        )

    def run_setup(self, *, dry_run=False, key_password=None):
        config = dict(self.config)
        if key_password is not None:
            config["AALYRICS_RELEASE_KEY_PASSWORD"] = key_password
        self.config_path.write_text(json.dumps(config))
        self.calls_path.unlink(missing_ok=True)
        return subprocess.run(
            [sys.executable, str(self.script), str(self.config_path)]
            + (["--dry-run"] if dry_run else []),
            env=self.env, stdin=subprocess.DEVNULL, capture_output=True, text=True,
        )

    def assert_passwords_hidden(self, result, *extra):
        output = result.stdout + result.stderr
        for password in (
            self.config["AALYRICS_RELEASE_STORE_PASSWORD"],
            self.config["AALYRICS_RELEASE_KEY_PASSWORD"],
            *extra,
        ):
            self.assertNotIn(password, output)

    def test_distinct_correct_passwords_pass_dry_run_without_changing_the_key(self):
        original = self.keystore.read_bytes()
        result = self.run_setup(dry_run=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("Validated (no upload)", result.stdout)
        self.assertFalse(self.calls_path.exists())
        self.assertEqual(original, self.keystore.read_bytes())
        self.assert_passwords_hidden(result)

    def test_wrong_private_key_password_fails_before_validation_or_any_upload(self):
        wrong_password = "mistyped-test-key-password"
        for dry_run in (True, False):
            with self.subTest(dry_run=dry_run):
                result = self.run_setup(dry_run=dry_run, key_password=wrong_password)
                self.assertNotEqual(0, result.returncode)
                self.assertNotIn("Validated", result.stdout)
                self.assertNotIn("Uploaded", result.stdout)
                self.assertFalse(self.calls_path.exists())
                self.assert_passwords_hidden(result, wrong_password)

    def test_valid_configuration_uploads_only_the_four_personal_signing_secrets(self):
        result = self.run_setup()
        self.assertEqual(0, result.returncode, result.stderr)
        calls = [json.loads(line) for line in self.calls_path.read_text().splitlines()]
        self.assertEqual([
            ["auth", "status", "--hostname", "github.com"],
            *[
                ["secret", "set", name, "--repo", "WanHanxi/AALyrics"]
                for name in (
                    "AALYRICS_RELEASE_KEYSTORE_BASE64",
                    "AALYRICS_RELEASE_STORE_PASSWORD",
                    "AALYRICS_RELEASE_KEY_ALIAS",
                    "AALYRICS_RELEASE_KEY_PASSWORD",
                )
            ],
        ], calls)
        self.assert_passwords_hidden(result)


if __name__ == "__main__":
    unittest.main()
