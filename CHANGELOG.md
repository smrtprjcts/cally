# Changelog

Усі помітні зміни — тут. Формат: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), версіонування — [SemVer](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Додано
-

### Змінено
-

### Виправлено
-

### Безпека
-

### Видалено
-

### Застаріле (Deprecated)
-

## [0.2.0] — 2026-04-28

### Додано
- First-run legal disclaimer (ModalBottomSheet) з 3-tier таксономією юрисдикцій: (1) one-party consent — без сповіщення, (2) all-party + implied consent — достатньо попередити, (3) explicit consent — потрібна явна згода (DE/AT/BE). Кожен tier розгортається тапом, відкриваючи legal references (§ 201 StGB, 18 U.S.C. § 2511, CA Penal Code § 632 тощо). Завжди видимий tech-блок про неможливість beep-сповіщення через uplink.
- Settings → Про додаток → "Юридичне попередження" — повторне відкриття того самого sheet'а як read-only.
- Версійований DataStore-флаг `disclaimer_accepted_v1` для майбутнього re-prompt при істотних змінах тексту.
- WAV encoder перезаписує RIFF-хедер кожен ~1 МБ — hard-killed запис залишається відтворюваним.
- DB↔FS reconciliation pass позначає записи, чий аудіофайл був видалений поза додатком.
- Cloud STT API ключ зашифрований at rest через Android Keystore-backed AES/GCM.

### Змінено
- README: секцію «Користувачам поза Україною» переписано в 3-tier структуру (без сповіщення / достатньо попередити / explicit consent) з матрицею-резюме «що дозволено в кожному рівні».
- Номери телефонів і контактні імена більше не з'являються у release logcat на рівні INFO (тільки DEBUG).
- AAC encoder ретраїть EOS input slot до 5 разів під час close — moov atom MP4-контейнера тепер завжди записується.
- Recorder pump joins подовжено до 5 с; якщо pump усе ще заблокований — pipe FD force-close.
- Strategy ladder чесно завершується, коли Shizuku daemon binder вмирає посеред спроби, замість рапортувати "all silent".

### Виправлено
- Release-білди тепер активують verifyCaller signing-cert pin (signingSha256 раніше був порожній).
- Cleanup-запити пропускають записи, що ще тривають (ended_at IS NULL).
- WRITE_SECURE_SETTINGS прибрано з UserService grant allow-list — у v0.2.0 жоден шлях його не використовує.
- Strategy ladder більше не поміщає робочу стратегію у `knownFailedInit`, коли Shizuku daemon binder вмер посеред спроби — кеш вибору залишається валідним.

### UserService
- `userServiceVersion` 10 → 11. AudioRecorderJob.stop() тепер force-close pipe FD при таймауті join, і RecorderService прибрав `WRITE_SECURE_SETTINGS` з allow-list. Daemon з `daemon=true` після оновлення APK respawn'иться автоматично через version mismatch у `ShizukuClient.onServiceConnected`.

### Заплановано
- SAF integration (`OpenDocumentTree` + MediaStore mirror)
- Англомовна локаль (`values-en/strings.xml`)
- Encrypted vault (AES-GCM, PIN/biometric)
- GitHub Actions CI (lint + test + assembleDebug)
- Material 3 ButtonGroup overload migration (5 callsites — `overflowIndicator` parameter)

## [0.1.0] — 2026-04-27

Перший публічний реліз. MVP scaffold.

### Додано
- AIDL-міст `:aidl/IRecorderService` між app-процесом і Shizuku UserService.
- Shizuku UserService з `daemon=true`: процес живе після свопу додатку з recents.
- Privileged recorder у shell-процесі (UID 2000) через `WrappedShellContext` (impersonation `com.android.shell` для проходження AudioFlinger gate).
- 5-step fallback ladder з live-audibility verification, кеш по `Build.FINGERPRINT`:
  1. `VOICE_UPLINK` + `VOICE_DOWNLINK` (dual paralel)
  2. `MIC` + `VOICE_DOWNLINK` (Samsung-friendly)
  3. `VOICE_CALL` stereo (L=uplink/R=downlink)
  4. `VOICE_CALL` mono
  5. `MIC` only (last resort)
- AAC-в-MP4 encoder за замовчуванням (`MediaCodec` + `MediaMuxer`), WAV опційно.
- Foreground service `type=specialUse` + invisible 1×1 overlay для FGS-from-background bypass на Android 14+.
- Material 3 Expressive UI (theme, motion, shape morphing, wavy LoadingIndicator, FloatingToolbar).
- 4 екрани: Onboarding, Primary (recordings list), Playback, Settings.
- Auto-start recording на `OFFHOOK` через `CallStateReceiver`.
- Контакти і call log resolution для metadata записів.
- Mix-to-stereo export з кастомним balance (для share-діалогу плеєра).
- Waveform view (peak-amplitude reducer + Canvas, тап і драг для seek).
- Транскрипція через user-configured OpenAI-compatible endpoint (default OpenRouter+Gemini Flash); opt-in, вимкнена без API ключа.
- Tap any transcript bubble для seek+play цього сегменту.
- MediaSession + MediaStyle notification з transport buttons і BT-headset media keys.
- Notification про збережений запис після кожного дзвінка.
- `verifyCaller()` на КОЖНИЙ AIDL-виклик: UID + SHA-256 release-cert pin.
- Per-app мовний конфіг (Android 13+) — наразі лише uk-UA.

### Безпека
- `cleartextTrafficPermitted=false` у `network_security_config.xml`.
- `data_extraction_rules.xml` + `backup_rules.xml` блокують adb-backup і cloud-restore.
- Hidden API exemptions scoped до 5 framework prefixes (defence-in-depth, не `""`).
- Жодного Firebase / Crashlytics / Sentry / analytics.

### Відомі обмеження
- Тільки uk-UA локаль.
- VoIP (WhatsApp/Telegram/Viber/Signal) принципово не підтримується — telephony audio path не задіяний.
- Bluetooth-гарнітура під час дзвінка може зламати запис на деяких HAL.
- Samsung One UI 5.1+ потребує fallback на MIC-only стратегії — VOICE_* з shell UID повертає тишу.

[Unreleased]: https://github.com/LyoSU/cally/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/LyoSU/cally/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/LyoSU/cally/releases/tag/v0.1.0
