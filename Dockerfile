FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_SDK_ROOT=/android-sdk \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    PATH=$PATH:/usr/lib/jvm/java-17-openjdk-amd64/bin:/android-sdk/cmdline-tools/latest/bin:/android-sdk/platform-tools

# Install dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    git wget unzip curl build-essential ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create SDK directory
RUN mkdir -p /android-sdk && chmod 777 /android-sdk

# Download and install Android SDK
RUN cd /tmp && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q commandlinetools-linux-11076708_latest.zip && \
    mkdir -p /android-sdk/cmdline-tools/latest && \
    mv cmdline-tools/* /android-sdk/cmdline-tools/latest/ && \
    rm -rf /tmp/commandlinetools*

# Accept licenses before installing
RUN mkdir -p /android-sdk/licenses && \
    echo -e "\n8933bad161af4d5fe346e853d06471adccd34239" > /android-sdk/licenses/android-sdk-license && \
    echo -e "\n79c1eadc40104d0288e186abc968ff4f" > /android-sdk/licenses/android-sdk-preview-license && \
    chmod -R 777 /android-sdk/licenses

# Install SDK components
RUN /android-sdk/cmdline-tools/latest/bin/sdkmanager \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "platform-tools" \
    || true

# Work directory
WORKDIR /build

# Copy and prepare
COPY . /build/
RUN chmod 777 /build && \
    echo "sdk.dir=/android-sdk" > /build/local.properties && \
    chmod +x /build/gradlew

# Build with error tolerance
RUN cd /build && \
    /build/gradlew clean assembleRelease -Dorg.gradle.jvmargs="-Xmx4g" 2>&1 || true

# Prepare outputs
RUN mkdir -p /build/outputs && \
    (cp /build/app/build/outputs/apk/release/*.apk /build/outputs/ 2>/dev/null || true) && \
    ls -lah /build/outputs/

CMD ["/bin/bash"]
