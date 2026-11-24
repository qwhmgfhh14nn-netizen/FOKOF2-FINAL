#!/usr/bin/env sh
# FOKOF2 checksum-pinned Gradle bootstrapper.
# This script downloads the exact Gradle distribution from gradle-wrapper.properties.
# It intentionally does not pretend to be the upstream gradle-wrapper.jar implementation.
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
if [ ! -f "$PROPERTIES" ]; then
  echo "ERROR: Missing $PROPERTIES" >&2
  exit 1
fi

property() {
  key=$1
  sed -n "s/^${key}=//p" "$PROPERTIES" | head -n 1 | sed 's/\\:/:/g; s/\\\\/\\/g'
}

DIST_URL=$(property distributionUrl)
DIST_SHA=$(property distributionSha256Sum)
case "$DIST_URL" in
  https://services.gradle.org/distributions/gradle-*-bin.zip) ;;
  *) echo "ERROR: Unexpected Gradle distribution URL: $DIST_URL" >&2; exit 1 ;;
esac
case "$DIST_SHA" in
  *[!0-9a-fA-F]*|'') echo "ERROR: Invalid distributionSha256Sum in wrapper properties" >&2; exit 1 ;;
esac
if [ "${#DIST_SHA}" -ne 64 ]; then
  echo "ERROR: distributionSha256Sum must be 64 hexadecimal characters" >&2
  exit 1
fi

ARCHIVE=${DIST_URL##*/}
DIST_NAME=${ARCHIVE%.zip}
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
CACHE_DIR="$GRADLE_USER_HOME/wrapper/dists/$DIST_NAME/fokof2-bootstrap"
GRADLE_HOME="$CACHE_DIR/$DIST_NAME"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  TMP_ZIP="$CACHE_DIR/$ARCHIVE.tmp.$$"
  TMP_DIR="$CACHE_DIR/unpack.$$"
  trap 'rm -f "$TMP_ZIP"; rm -rf "$TMP_DIR"' EXIT HUP INT TERM
  echo "Downloading $DIST_NAME from the official Gradle distribution service..."
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 3 --connect-timeout 20 "$DIST_URL" -o "$TMP_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget --https-only -O "$TMP_ZIP" "$DIST_URL"
  else
    echo "ERROR: Install curl or wget to bootstrap Gradle." >&2
    exit 1
  fi

  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL_SHA=$(sha256sum "$TMP_ZIP" | awk '{print $1}')
  elif command -v shasum >/dev/null 2>&1; then
    ACTUAL_SHA=$(shasum -a 256 "$TMP_ZIP" | awk '{print $1}')
  else
    echo "ERROR: sha256sum or shasum is required to verify the Gradle download." >&2
    exit 1
  fi
  if [ "$(printf '%s' "$ACTUAL_SHA" | tr 'A-F' 'a-f')" != "$(printf '%s' "$DIST_SHA" | tr 'A-F' 'a-f')" ]; then
    echo "ERROR: Gradle archive SHA-256 mismatch. Download deleted." >&2
    exit 1
  fi

  mkdir -p "$TMP_DIR"
  unzip -q "$TMP_ZIP" -d "$TMP_DIR"
  if [ ! -x "$TMP_DIR/$DIST_NAME/bin/gradle" ]; then
    echo "ERROR: Gradle distribution archive has an unexpected structure." >&2
    exit 1
  fi
  rm -rf "$GRADLE_HOME"
  mv "$TMP_DIR/$DIST_NAME" "$GRADLE_HOME"
  rm -f "$TMP_ZIP"
  rm -rf "$TMP_DIR"
  trap - EXIT HUP INT TERM
fi

exec "$GRADLE_BIN" "$@"
