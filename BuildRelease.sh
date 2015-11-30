#! /bin/bash

SDK_VERSION=$1
if [[ -z "$SDK_VERSION" ]] || [[ ${SDK_VERSION:0:1} == "v" ]]; then
  echo "Please provide sdk version in format of 2.0.0"
  exit 1
fi

echo "==============> Update release repo"
git submodule update --init

cd "src"

set ANDROID_HOME=/home/user/android-studio/sdk

echo "==============> Make jar"
./gradlew :lib:makeJar

echo "==============> Copy to tmpDist"
cd ..
rm -rf tmpDist
mkdir tmpDist

# TODO Filtering unnecessary libs
cp -rf src/lib/build/outputs/jars tmpDist/libs
cp -rf src/app tmpDist/demo
mv tmpDist/libs/tuisongbao.jar "tmpDist/libs/tuisongbao_realtime_engine_client_android_v$SDK_VERSION.jar"

echo "==============> Done"
