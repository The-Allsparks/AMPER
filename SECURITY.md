# Security Policy

## Supported versions

| Version | Supported |
| ------- | --------- |
| 0.1.x   | Yes       |

## Reporting a vulnerability

Please do **not** open a public issue for security problems that could put robots, students, or machines at risk.

**Private channel (preferred):** use [GitHub Private Vulnerability Reporting](https://github.com/The-Allsparks/AMPER/security/advisories/new) for this repository. Maintainers receive the report privately and can coordinate a fix before any public disclosure.

Do not file a public issue for exploitable robot-safety bugs (for example unexpected motor motion or bypass of gravity-hold protections).

Include:

- A description of the issue
- Steps to reproduce
- Impact assessment (for example: unexpected motor motion, unsafe disable of gravity holds, credential exposure)

## Safety expectations for this project

AMPER intentionally:

- Keeps **motor intervention disabled by default**
- Treats missing or stale sensing as a reason to **avoid** active limiting, not invent values
- Documents that software cannot fix bad batteries, connectors, or wiring

If you discover a path that enables output intervention without an explicit feature flag, or that drops gravity-critical holds below a declared safe minimum, treat it as a safety defect.

## Secrets

Never store passwords, Wi-Fi credentials, API keys, or tokens in the repository, issues, or exported logs.
