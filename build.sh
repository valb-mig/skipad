#!/usr/bin/env bash
set -euo pipefail

# --- Configuração ---
export ANDROID_HOME="${ANDROID_HOME:-/home/valb/Android/Sdk}"
BT="$ANDROID_HOME/build-tools/36.1.0"
AJAR="$ANDROID_HOME/platforms/android-36.1/android.jar"
MIN_SDK=24
TARGET_SDK=34

PROJ="$(cd "$(dirname "$0")" && pwd)"
BUILD="$PROJ/build"
KEYSTORE="$PROJ/debug.keystore"

rm -rf "$BUILD"
mkdir -p "$BUILD/classes" "$BUILD/gen"

echo "==> [1/6] Compilando recursos (aapt2 compile)"
"$BT/aapt2" compile --dir "$PROJ/res" -o "$BUILD/res.zip"

echo "==> [2/6] Linkando recursos + manifest (aapt2 link)"
"$BT/aapt2" link \
    -o "$BUILD/base.apk" \
    -I "$AJAR" \
    --manifest "$PROJ/AndroidManifest.xml" \
    --java "$BUILD/gen" \
    "$BUILD/res.zip" \
    --min-sdk-version "$MIN_SDK" \
    --target-sdk-version "$TARGET_SDK"

echo "==> [3/6] Compilando Java (javac)"
find "$PROJ/src" "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
javac -source 8 -target 8 -d "$BUILD/classes" -cp "$AJAR" @"$BUILD/sources.txt"

echo "==> [4/6] Gerando dex (d8)"
find "$BUILD/classes" -name '*.class' > "$BUILD/classes.txt"
"$BT/d8" --min-api "$MIN_SDK" --lib "$AJAR" --output "$BUILD" @"$BUILD/classes.txt"

echo "==> [5/6] Empacotando dex no APK + alinhando"
jar uf "$BUILD/base.apk" -C "$BUILD" classes.dex
"$BT/zipalign" -f 4 "$BUILD/base.apk" "$BUILD/aligned.apk"

echo "==> [6/6] Assinando (apksigner)"
if [ ! -f "$KEYSTORE" ]; then
    echo "    (gerando debug.keystore)"
    keytool -genkeypair -v \
        -keystore "$KEYSTORE" \
        -alias skipad -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=SkipAd, OU=Dev, O=valb, C=BR" >/dev/null 2>&1
fi
"$BT/apksigner" sign \
    --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android \
    --out "$PROJ/skipad.apk" \
    "$BUILD/aligned.apk"

echo ""
echo "OK -> $PROJ/skipad.apk"
