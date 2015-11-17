# Project Structure

Following introduction is according to the structure of `Project` -> `Android` in Android Studio.

- `app`: Demo related
    - `manifests`: `AndroidManifest.xml`
    - `java`: java files
    - `jniLibs`: `.so` files for Push
    - `res`: xml files for UI
    - `assets`: push config file
- `lib`: SDK related
    - `manifests`: `AndroidManifest.xml`
    - `java`: java files
        - `channel`: pub/sub feature related
        - `chat`: chat feature related
        - `common`: Base and common classes
        - `connection`: manage connection
        - `download`: manage download tasks, start, cancel, progress...
        - `engineio`: engineio server lib
        - `http`: send http requests and base host configs
        - `utils`: common utils, like string, log...
        - `Engine.java`: entrance of Engine
        - `EngineOptions.java`: options for initializing `Engine`, like `appId` and `authEndPoint`
    - `res`: xml files for UI
- `Gradle Scripts`

# Third Party Dependencies

## Demo

## SDK

In `build.gradle` (Module: lib), it define all third party we used:

```
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:22.2.0'
    compile 'com.github.nkzawa:engine.io-client:0.5.1'
    compile 'com.google.code.gson:gson:2.3.1'
    compile 'com.qiniu:qiniu-android-sdk:7.0.7.1'
}
```

- `engine.io-client`: client side of engineio
- `gson`: Java serialization library that can convert Java Objects into JSON and back
- `qiniu-android-sdk`: upload file to Qiniu

# Develop Environment Setup

- Download and install [Android Studio IDE](https://developer.android.com/sdk/index.html).
- Download [Android SDK](https://developer.android.com/sdk/index.html).
- Download and insatll [JDK](http://docs.oracle.com/javase/7/docs/webnotes/install/linux/linux-jdk.html).
- Config Java Path in `~/.bashrc`, for example:

    ```bash
    export JAVA_HOME=/root/java/jdk1.8.0_05/
    export JRE_HOME=${JAVA_HOME}/jre
    ```

- Read `Install-Linux-tar.txt` under `android-studio` path and start `Android Studio`

# Style Guide

- java

    Go to `Settings` and configure below items:

    - Strip tailing spaces on save: All
    - Ensure line feed at file end on save: true
    - Tabs and Indents:
        - Tab size: 4
        - Indent size: 4

- git commit

    ```
    scope: subject

    description
    ```

    scope is like category of features, modules or code structure, for example, login, logsViewer, resqueMonitor, play, angular, codingStyle, etc. Be consistent with others if possible.

# Abbreviations

# Books & References & Tools

## Android Gradle Plugin

- [MUST READ]http://tools.android.com/tech-docs/new-build-system/user-guide

    As the new Android SDK build system is working in progress, some items in this article may be a bit of out-of-date, you can also refer to the official doc, [Configuring Gradle](https://developer.android.com/tools/building/configuring-gradle.html) and [Android Plugin for Gradle](https://developer.android.com/tools/building/plugin-for-gradle.html), almost same thing.

- http://blog.danlew.net/2015/04/27/victor/, after reading this article, you should basically know how to write a plugin for gradle.

## Run Gradle in terminal

Gradle is installed when installing Android Studio by default, the gradle executable is usually installed at `<user_home>/.gradle/wrapper/dists/gradle-<version>-bin/<some_key>/gradle-<version>/bin/`, you have to add this path to `.bashrc`, for instance:

```bash
# gradle
export GRADLE=/root/.gradle/wrapper/dists/gradle-2.5-all/d3xh0kipe7wr2bvnx5sk0hao8/gradle-2.5/bin/
export PATH=$PATH:$GRADLE
```

then, you can run `gradle` in terminal directly.
