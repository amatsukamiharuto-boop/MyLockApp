# MyLockApp
Open this folder in Android Studio (Koala+), sync Gradle, run.

## Replace placeholder assets (res/raw)
- access_granted.wav, access_denied.wav, alarm_siren.wav  -> beep placeholders. Delete and add your
  .mp3/.wav under the SAME base names (a duplicate base name with a different extension breaks the build).
- data_match.json -> simple placeholder Lottie ring. Drop in your own HUD animation.

## First run
1. Enable the Accessibility Service, grant overlay permission.
2. Set a 6-digit PIN, enroll face, add package names to lock.
3. Open a locked app -> Biometric -> PIN -> Face -> Matching -> Granted/Denied.

## Known limitations
- ML Kit detects faces; it does NOT identify them. FaceSignature is a landmark-ratio placeholder.
  Use a TFLite embedding model (MobileFaceNet) for real matching/anti-spoofing.
- BiometricPrompt confirms "an enrolled device fingerprint", not whose.
- Accessibility-service apps are restricted on Google Play (needs policy declaration).
- Device-admin/Force-stop/uninstall protection is not included.
