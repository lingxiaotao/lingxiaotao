#!/usr/bin/env bash
# Build the QR encrypt Android APK without Gradle.
#
# Requirements:
#   - JDK 17 (JAVA_HOME or java/javac/keytool on PATH)
#   - Android build-tools (aapt2, d8, zipalign, apksigner) and a platform android.jar
#
# Tool locations can be overridden via environment variables:
#   BT       directory containing build-tools binaries (default /opt/android-sdk/android-14)
#   PLATFORM directory containing android.jar          (default /opt/android-sdk/android-34-ext12)
#
# Usage: bash build.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BT="${BT:-/opt/android-sdk/android-14}"
PLATFORM="${PLATFORM:-/opt/android-sdk/android-34-ext12}"
OUT="$ROOT/build"
APK_NAME="qrencrypt.apk"

if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

mkdir -p "$OUT"
cd "$OUT"

# 1. Signing keystore
KEYSTORE="$ROOT/signing/my.keystore"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -alias mykey -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass android -keypass android \
    -dname "CN=QRApp, O=Example, C=CN"
fi

# 2. Compile resources
"$BT/aapt2" compile --dir "$ROOT/app/res" -o res.zip

# 3. Link resources
"$BT/aapt2" link -o base.apk -I "$PLATFORM/android.jar" \
  --manifest "$ROOT/app/AndroidManifest.xml" --java gen res.zip --auto-add-overlay

# 4. Compile Java
mkdir -p classes
javac -source 8 -target 8 \
  -bootclasspath "$PLATFORM/android.jar" \
  -classpath "$PLATFORM/android.jar:$ROOT/app/libs/zxing-core.jar" \
  -d classes \
  "$ROOT/app/src/com/example/qrencrypt/MainActivity.java" \
  gen/com/example/qrencrypt/R.java

# 5. Dex
mkdir -p dexout
"$BT/d8" --release --lib "$PLATFORM/android.jar" --min-api 21 \
  --output dexout classes/com/example/qrencrypt/*.class "$ROOT/app/libs/zxing-core.jar"

# 6. Package, align and sign
cp base.apk unsigned.apk
(cd dexout && zip -q "$OUT/unsigned.apk" classes.dex)
"$BT/zipalign" -f 4 unsigned.apk aligned.apk
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias mykey --min-sdk-version 21 \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out "$OUT/$APK_NAME" aligned.apk

cp "$OUT/$APK_NAME" "$ROOT/$APK_NAME"

echo "Built: $ROOT/$APK_NAME"
