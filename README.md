# Wire

This repository is part of the source code of Wire. You can find more information at [wire.com](https://wire.com) or by contacting opensource@wire.com.

You can find the published source code at [github.com/wireapp](https://github.com/wireapp).

For licensing information, see the attached LICENSE file and the list of third-party licenses at [wire.com/legal/licenses/](https://wire.com/legal/licenses/).

# cryptobox-jni

JNI bindings for the [cryptobox](https://github.com/wireapp/cryptobox) with support for cross-compilation to Android.

## Building

### Host Architecture

Besides common OS-specific development tooling, the following prerequisites
are needed to build for the host architecture:

  * A Rust compiler (1.16.0 exactly).
  * A Java compiler (1.6 or later).

With that in place

    make dist

will leave a tarball in the `dist` directory containing all the binaries for
your host architecture in the form of shared libraries, as well as a `.jar`
file and the corresponding `javadoc` output.

### Android

Besides common OS-specific development tooling, the following prerequisites
are needed to build for Android:

  * The [Android SDK](http://developer.android.com/sdk/index.html) (The Android Studio IDE is not required).

  * The [Android NDK](https://developer.android.com/ndk/downloads/index.html) (`r10d` or newer).

  * A Java compiler (1.6 or later).

  * A Rust compiler (1.16.0) that can cross-compile to the following
    targets corresponding to the NDK toolchains:
      * `armv7-linux-androideabi`
      * `aarch64-linux-android`
      * `i686-linux-android`
      * `x86_64-unknown-linux-gnu`

    It is recommended to use [rustup](https://github.com/rust-lang-nursery/rustup.rs) to
    manage multiple Rust compiler toolchains. Using rustup, the following commands
    will install the necessary target-specific Rust binaries needed for Android:

        rustup target add armv7-linux-androideabi
        rustup target add i686-linux-android
        rustup target add aarch64-linux-android
        rustup target add x86_64-unknown-linux-gnu

    Alternatively a Rust compiler that supports the necessary targets can be built from source, e.g.:

        ./configure \
            --prefix=/where/to/install \
            --arm-linux-androideabi-ndk=/path/to/android-ndk-toolchain-armeabi-v7a \
            --aarch64-linux-android-ndk=/path/to/android-ndk-toolchain-arm64-v8a \
            --i686-linux-android-ndk=/path/to/android-ndk-toolchain-x86 \
            --x86_64-linux-android-ndk=/path/to/android-ndk-toolchain-x86_64 \
            --target=arm-linux-androideabi,aarch64-linux-android,i686-linux-android,x86_64-unknown-linux-gnu
        make -j4
        make install

  * The `ANDROID_NDK_HOME` environment variable must be set and point to the
    home directory of the NDK installation.

With the prerequisites in place, the Android build can be run with:

    cd android && make dist

The distribution artifacts will be in the `android/dist` directory, which includes
an [Android Library Archive](http://tools.android.com/tech-docs/new-build-system/aar-format) (`.aar`).

If [Maven](https://maven.apache.org) is installed (availble on [homebrew](https://formulae.brew.sh/formula/maven)), you can publish the aar to a your local Maven repository with the command:

```
mvn install:install-file \
	-Dfile="<path to aar>" \
	-DgroupId=com.wire \
	-DartifactId=cryptobox-android \
	-Dpackaging=aar \
	-Dversion=<version number>
```

### Windows

You need:

  * [MSYS2](http://msys2.github.io/) with MinGW-w64 toolchains

  * The pkg-config from MinGW-w64 toolchain

        pacman -S mingw-w64-x86_64-pkg-config

  * A Java compiler (1.6 or later)

  * A Rust compiler (1.6 or newer) with GNU ABI

  * The `JAVA_HOME` environment variable must be set correctly for MSYS2

        export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_rev"

  * The `PATH` environment variable must include JDK and Rust for MSYS2

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

For any problems, comments, or feedback please create an issue [here on GitHub](https://github.com/wireapp/cryptobox-jni/issues).

## Licence

This project is released under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl-3.0.en.html).
