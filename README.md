# Wire

This repository is part of the source code of Wire. You can find more information at [wire.com](https://wire.com) or by contacting opensource@wire.com.

You can find the published source code at [github.com/wireapp](https://github.com/wireapp).

For licensing information, see the attached LICENSE file and the list of third-party licenses at [wire.com/legal/licenses/](https://wire.com/legal/licenses/).

# cryptobox-jni

JNI bindings for the [cryptobox](https://github.com/wireapp/cryptobox) with support for cross-compilation to Android.

## Building

There is a Docker file that create an image to cross compile on all necessary platforms. You need to have Docker running on your machine.

Run `./docker-build.sh` to start the build. It will download Android SDK and NDK, so it will take a while.

Once the script is completed, you will find the result of the compilation copied to the `output/` folder.

### Publishing

#### Locally

If [Maven](https://maven.apache.org) is installed (availble on [homebrew](https://formulae.brew.sh/formula/maven)), you can publish the aar to a your local Maven repository with the command:

```
mvn install:install-file \
	-Dfile="<path to aar>" \
	-DgroupId=com.wire \
	-DartifactId=cryptobox-android \
	-Dpackaging=aar \
	-Dversion=<version number>
```

#### Bintray
In order to publish the binary to Bintray, you need to install the JFrog CLI (`jfrog-cli-go`) tool.

- Create a version using `jfrog` CLI
- Upload files to that version using the `jfrog` CLI. E.g:
```
/usr/local/bin/jfrog bt u "cryptobox-android-1.1.1.*" \
	"wire-android/releases/cryptobox-android/1.1.1" \
	"com/wire/cryptobox-android/1.1.1/"
```

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
