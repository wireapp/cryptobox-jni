# cryptobox-jni

JNI bindings for the [cryptobox](https://github.com/romanb/cryptobox) with support for cross-compilation to Android.

## Building

The project can be built on OSX and most Linux distributions.

### Host Architecture

Besides common OS-specific development tooling, the following prerequisites
are needed to build for the host architecture:

  * A Rust compiler (1.4 Beta or newer).
  * A Java compiler (1.6 or later).

With that in place

    make dist

will leave a tarball in the `dist` directory containing all the binaries for
your host architecture in the form of shared libraries, as well as a `.jar`
file and the corresponding `javadoc` output.

### Android

Besides common OS-specific development tooling, the following prerequisites
are needed to build for Android:

  * The [Android SDK](http://developer.android.com/sdk/index.html) (The Android Studio IDE is not required).

  * The [Android NDK](https://developer.android.com/ndk/downloads/index.html) (`r10d` or newer).

  * [NDK standalone toolchains](https://developer.android.com/ndk/guides/standalone_toolchain.html) for the following architectures:
      * `armeabi-v7a`
      * `arm64-v8a`
      * `x86`

  * A Java compiler (1.6 or later).

  * A Rust compiler (1.4 Beta or newer) that can cross-compile to the following
    targets corresponding to the aforementioned NDK standalone toolchains:
      * `arm-linux-androideabi`
      * `aarch64-linux-android`
      * `i686-linux-android`
    
    Typically such a compiler needs to be built from source, e.g.:

        ./configure \
            --prefix=/where/to/install \
            --arm-linux-androideabi-ndk=/path/to/android-ndk-toolchain-armeabi-v7a \
            --aarch64-linux-android-ndk=/path/to/android-ndk-toolchain-arm64-v8a \
            --i686-linux-android-ndk=/path/to/android-ndk-toolchain-x86 \
            --target=arm-linux-androideabi,aarch64-linux-android,i686-linux-android
        make -j4
        make install

  * The `ANDROID_NDK_HOME` environment variable must be set and point to the
    home directory of the NDK installation.

  * The `ANDROID_NDK_TOOLCHAIN_ARM` environment variable must be set and point
    to the home directory of the `armeabi-v7a` standalone toolchain.

  * The `ANDROID_NDK_TOOLCHAIN_X86` environment variable must be set and point
    to the home directory of the `x86` standalone toolchain.

  * The `ANDROID_NDK_TOOLCHAIN_AARCH64` environment variable must be set and point
    to the home directory of the `arm64-v8a` standalone toolchain.

With the prerequisites in place, the Android build can be run with:

    cd android && make dist

The distribution artifacts will be in the `android/dist` directory, which includes
an [Android Library Archive](http://tools.android.com/tech-docs/new-build-system/aar-format) (`.aar`).

## Sample Application

This project has a simple Android sample application that can be installed
on a connected Android device or emulator:

    cd android-example && make install

Look for an application named `CryptoBoxExample`.

## Tests

Currently this project's tests run only on Android and require a connected
Android device or emulator:

    cd android-example && make test

The test project is located in the `android-example/tests` directory.

## Contribute

For any problems, comments, or feedback please create an issue [here on GitHub](https://github.com/romanb/cryptobox-jni/issues).

## Licence

This project is released under the [Mozilla Public License Version 2.0](http://www.mozilla.org/MPL/).
