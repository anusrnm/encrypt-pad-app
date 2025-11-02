FROM mcr.microsoft.com/devcontainers/java:17

# Noninteractive apt
ENV DEBIAN_FRONTEND=noninteractive

# Android SDK location
ENV ANDROID_SDK_ROOT=/opt/android/sdk
ENV PATH="$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools"

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget unzip zip git ca-certificates \
    && mkdir -p ${ANDROID_SDK_ROOT} /tmp/android-sdk \
    # Download Android command line tools
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline.zip \
    && unzip -q /tmp/cmdline.zip -d /tmp/android-sdk \
    && mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && mv /tmp/android-sdk/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm -rf /tmp/cmdline.zip /tmp/android-sdk \
    # Accept licenses and install essential SDK packages (platform-tools, platforms and build-tools)
    && yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses || true \
    && ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} "platform-tools" "platforms;android-34" "build-tools;34.0.0" \
    && apt-get remove -y wget unzip git-lfs\
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

CMD ["sleep", "infinity"]
