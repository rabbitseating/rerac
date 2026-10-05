"""Check tracked text and optional reachable history without printing secret values.

Heuristic guard for this portfolio repository, not a guarantee that all secrets
are detectable. Intentionally permit the documented example placeholders.
"""
import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(
        ['git', '-c', f'safe.directory={ROOT.as_posix()}', '-C', str(ROOT), *args]
    )


PATTERNS = {
    'provider token': re.compile(
        r'\b(?:pk\.|sk\.)[A-Za-z0-9_.-]{20,}|\bgh[pousr]_[A-Za-z0-9]{20,}'
        r'|\bgithub_pat_[A-Za-z0-9_]{20,}|\bAKIA[0-9A-Z]{16}'
        r'|\bAIza[0-9A-Za-z_-]{30,}|\bxox[baprs]-[A-Za-z0-9-]{15,}'
    ),
    'private key': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----'),
    'credential URL': re.compile(r'\b(?:mysql|postgres(?:ql)?|mongodb(?:\+srv)?|https?)://[^\s/:]+:[^\s/@]+@'),
    'credential literal': re.compile(
        r'''(?im)\b[\w.-]*(?:password|passwd|secret|token|api_?key)[\w.-]*["']?\s*[:=]\s*["']([^"'\r\n]+)["']'''
    ),
    'credential property': re.compile(
        r'(?im)^(?:MAPBOX_DOWNLOADS_TOKEN|MAPBOX_ACCESS_TOKEN|OPENWEATHER_API_KEY|DB_PASSWORD)\s*=\s*(\S+)'
    ),
    'XML credential': re.compile(
        r'(?im)<string\s+name="(?:mapbox_access_token|api_key|openweather_api_key|password)"[^>]*>([^<]+)</string>'
    ),
}


def allowed_reference(value):
    return (
        value.lower().startswith(('your_', 'your-', '<put_your_'))
        or value.startswith(('${', '$env:', '{{'))
        or value in ('MAPBOX_DOWNLOADS_TOKEN', 'MAPBOX_ACCESS_TOKEN', 'OPENWEATHER_API_KEY', 'DB_PASSWORD')
    )


def inspect(path, raw, label):
    if b'\0' in raw:
        return []
    try:
        text = raw.decode('utf-8')
    except UnicodeDecodeError:
        return []
    findings = []
    for category, pattern in PATTERNS.items():
        for match in pattern.finditer(text):
            value = match.group(1) if match.lastindex else match.group()
            if match.lastindex and allowed_reference(value):
                continue
            line = text.count('\n', 0, match.start()) + 1
            findings.append(f'{label}:{path}:{line}: possible {category}')
    return findings


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--history', action='store_true')
    args = parser.parse_args()
    findings = []
    tracked = git('ls-files', '-z').decode().split('\0')
    count = 0
    for path in filter(None, tracked):
        name = Path(path).name.lower()
        if ((name.startswith('.env') and name != '.env.example')
                or (name.startswith('local.properties') and name != 'local.properties.example')
                or Path(path).suffix.lower() in ('.jks', '.keystore', '.p12', '.pfx', '.pem', '.key')):
            findings.append(f'working tree:{path}: local credential/configuration file is tracked')
        file = ROOT / path
        if file.is_file():
            findings.extend(inspect(path, file.read_bytes(), 'working tree'))
            count += 1
    history_count = 0
    if args.history:
        for row in git('rev-list', '--objects', '--all').decode().splitlines():
            parts = row.split(' ', 1)
            if len(parts) != 2:
                continue
            oid, path = parts
            if git('cat-file', '-t', oid).strip() != b'blob':
                continue
            findings.extend(inspect(path, git('cat-file', 'blob', oid), f'history {oid[:12]}'))
            history_count += 1
    if findings:
        print('\n'.join(sorted(set(findings))))
        print('Review these locations locally. Matched values were not printed.')
        return 1
    print(f'Credential checks passed: {count} tracked files, {history_count} historical blobs.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
