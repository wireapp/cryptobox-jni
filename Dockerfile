FROM rust

USER root

ENV PATH $PATH:/usr/local/sbin:/usr/sbin:/sbin

####### BASE TOOLS #######
RUN apt update
RUN apt upgrade -y
RUN apt install -qqy --no-install-recommends \
	git \
	wget \
	build-essential \
	gcc \
	software-properties-common \
	unzip \
	clang \
	vim \
	pkg-config \
	strace \
	less \
	g++-multilib \
	libc6-dev-i386 \
	sudo

RUN apt install -qqy --no-install-recommends openjdk-11-jre-headless openjdk-11-jdk-headless

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

######## ANDROID #########

RUN wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
RUN mv android-sdk_r24.4.1-linux.tgz /opt/
RUN cd /opt && tar xzvf ./android-sdk_r24.4.1-linux.tgz
ENV ANDROID_HOME /opt/android-sdk-linux
ENV PATH $ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
RUN echo y | android update sdk --no-ui --all --filter tools
RUN echo y | android update sdk --no-ui --all --filter platform-tools
RUN echo y | android update sdk --no-ui --all --filter android-27
RUN echo y | android update sdk --no-ui --all --filter build-tools-27.0.3
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository
RUN echo y | android update sdk --no-ui --all --filter extra-google-m2repository
RUN echo y | android update sdk --no-ui --all --filter extra-google-google_play_services
RUN echo y | android update sdk --no-ui --all --filter addon-google_apis-google-23

ENV ANDROID_NDK_HOME /opt/android-ndk
ENV ANDROID_NDK_VERSION r20
RUN mkdir /opt/android-ndk-tmp && \
    cd /opt/android-ndk-tmp && \
    wget -q https://dl.google.com/android/repository/android-ndk-${ANDROID_NDK_VERSION}-linux-x86_64.zip && \
# uncompress
    unzip -q android-ndk-${ANDROID_NDK_VERSION}-linux-x86_64.zip && \
# move to its final location
    mv ./android-ndk-${ANDROID_NDK_VERSION} ${ANDROID_NDK_HOME} && \
# remove temp dir
    cd ${ANDROID_NDK_HOME} && \
    rm -rf /opt/android-ndk-tmp

# add to PATH
ENV PATH ${PATH}:${ANDROID_NDK_HOME}

# # Install gradle
# RUN apt-get install -y unzip
# #ADD https://services.gradle.org/distributions/gradle-2.14.1-bin.zip /opt/
# ADD gradle-2.14.1-bin.zip /opt/
# RUN unzip /opt/gradle-2.14.1-bin.zip -d /opt
# ENV GRADLE_HOME /opt/gradle-2.14.1
# ENV PATH $GRADLE_HOME/bin:$PATH

######### RUST ############
RUN useradd rust -m
USER rust

RUN rustup install 1.36.0
RUN rustup default 1.36.0
RUN rustup target add armv7-linux-androideabi
RUN rustup target add i686-linux-android
RUN rustup target add aarch64-linux-android
RUN rustup target add x86_64-linux-android

ENV RUST_HOME ~/.rust

ENV PKG_CONFIG_PATH=/home/rust/cryptobox-jni/android/build/libsodium-android-armv7-a/lib/pkgconfig

######### ADDITIONAL TOOLS ############
USER root
RUN apt-get install -qqy --no-install-recommends \
	zip
USER rust

######### Build ##############
WORKDIR /home/rust
COPY --chown=rust . cryptobox-jni
WORKDIR cryptobox-jni/android
RUN make dist || echo "FAILED TO BUILD!!"

