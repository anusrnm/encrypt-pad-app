#!/bin/bash

LOGFILE=~/setup.log
exec > >(tee -a "$LOGFILE") 2>&1  # Redirect stdout and stderr to the log file

set -e  # Exit immediately if a command exits with a non-zero status.

echo "==================== INSTALLATION STARTED ===================="

echo "Updating package list..."
apt-get update && apt-get install -y wget unzip

echo "Downloading Android command line tools..."
wget https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -O tools.zip

echo "Extracting tools..."
unzip tools.zip -d ~/android_sdk

echo "Cleaning up..."
rm tools.zip

echo "Renaming cmdline-tools..."
mv ~/android_sdk/cmdline-tools ~/android_sdk/cmdline-tools-old

echo "Creating latest directory..."
mkdir ~/android_sdk/cmdline-tools/latest

echo "Moving contents to latest directory..."
mv ~/android_sdk/cmdline-tools-old/* ~/android_sdk/cmdline-tools/latest/

echo "Updating PATH..."
echo 'export ANDROID_HOME="~/android_sdk"' >> ~/.bashrc
echo 'export PATH="~/android_sdk/cmdline-tools/latest/bin:$PATH"' >> ~/.bashrc

echo "Sourcing .bashrc..."
source ~/.bashrc

echo "Accepting SDK licenses..."
~/android_sdk/cmdline-tools/latest/bin/sdkmanager --licenses

echo "Installing SDK components..."
~/android_sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=~/android_sdk --install 'platform-tools' 'build-tools;33.0.0'

echo "==================== INSTALLATION COMPLETED ===================="
