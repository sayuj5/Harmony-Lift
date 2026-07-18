# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 2.0.x | ✅ Active support |
| 1.x.x | ❌ No longer supported |

---

## 🔒 Security Practices

Harmony-Lift is designed with security and privacy as foundational principles:

### Offline-First Architecture
All AI inference, audio processing, and data handling happens **entirely on-device**. There is no external server, no API endpoint, and no cloud dependency of any kind for core functionality.

### Audio Privacy
- Microphone audio is captured for **real-time analysis only**.
- Audio frames are processed in memory and immediately discarded.
- **No audio is ever written to disk** (except explicitly exported user files).
- **No audio is ever transmitted** over any network.

### Data Storage
- User preferences are stored via **DataStore** (encrypted shared preferences).
- No personally identifiable information (PII) is collected or stored.
- Exported files (PDF/TXT) are created **in the app cache directory** and only shared via the Android Share Sheet on user request.

### Network
- The **only** network operation is the initial model download from HuggingFace.
- After download, the app is **fully air-gap capable**.
- No telemetry, analytics, crash reporting, or tracking endpoints exist in the codebase.

### Native Code (JNI)
- The llama.cpp JNI bridge is sandboxed within the `tutor` module.
- Native memory is managed by `LocalModelManager` with proper `close()` / `release()` lifecycle.
- GGUF model files are validated by magic byte header before loading into native memory.

---

## 📣 Reporting a Vulnerability

We take security reports seriously. If you discover a security vulnerability, please **do not** open a public GitHub issue.

### Responsible Disclosure Process

1. **Email** the maintainer at: `security@harmonylift.app`
2. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)
3. You will receive an acknowledgement within **48 hours**.
4. We aim to release a fix within **14 days** for critical issues.
5. You will be credited in the security advisory (unless you prefer anonymity).

### Scope

In-scope vulnerabilities:
- Data leakage to external servers
- Unauthorized access to device storage
- JNI/native memory vulnerabilities
- Privilege escalation via exported components
- Insecure FileProvider configuration

Out-of-scope:
- Physical device access attacks
- Social engineering
- Issues in third-party libraries (report those upstream)

---

## 📬 Contact

Security: `security@harmonylift.app`  
General: Open a [GitHub Issue](https://github.com/sayuj5/Harmony-Lift/issues)
