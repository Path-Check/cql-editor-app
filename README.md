# CQL Editor App

<img align="right" src="./docs/screenshots/1.Home.png" data-canonical-src="./docs/screenshots/1.Home.png" width="350px"/>

A simple Android native application to Compile CQL. 

# Releases 

Check the latest on the [Release Directory](https://github.com/Path-Check/cql-editor-app/releases)

# Development Overview

## Setup

Make sure to have the following pre-requisites installed:
1. Java 11
2. Android Studio Artic Fox+
3. Android 7.0+ Phone or Emulation setup

Fork and clone this repository and import into Android Studio
```bash
git clone git@github.com:Path-Check/cql-editor-app.git
```

Use one of the Android Studio builds to install and run the app in your device or a simulator.

## Building
Build the app:
```bash
./gradlew assembleDebug
```

## Testing
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Installing on device
```bash
./gradlew installDebug
```

## How to Deploy

1. Generate a new signing key 
```
keytool -genkey -v -keystore <my-release-key.keystore> -alias <alias_name> -keyalg RSA -keysize 2048 -validity 10000
```
2. Create 4 Secret Key variables on your GitHub repository and fill in with the signing key information
   - `KEY_ALIAS` <- `<alias_name>`
   - `KEY_PASSWORD` <- `<your password>`
   - `KEY_STORE_PASSWORD` <- `<your key store password>`
   - `SIGNING_KEY` <- the data from `<my-release-key.keystore>`
3. Change the `versionCode` and `versionName` on `app/build.gradle`
4. Commit and push. 
5. Tag the commit with `v{x.x.x}`
6. Let the [Create Release GitHub Action](https://github.com/Path-Check/cql-editor-app/actions/workflows/create-release.yml) build a new `aab` file. 
7. Add your CHANGE LOG to the description of the new release
8. Download the `aab` file and upload it to the` PlayStore. 

# Contributing

[Issues](https://github.com/Path-Check/cql-editor-app/issues) and [pull requests](https://github.com/Path-Check/cql-editor-app/pulls) are very welcome.

# License

Copyright 2021 PathCheck Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
