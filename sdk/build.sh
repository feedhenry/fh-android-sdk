#!/bin/bash
FH_SDK_VERSION=`head -1 VERSION.txt`
DIST_DIR="./dist"

rm -rf "$DIST_DIR/*.jar"
#First, run the ant task to generate the jar file
ant clean
ant jar
ant doc

# For some environments, you may need to specify an sdk.dir
# e.g.
# ant jar -Dsdk.dir=/usr/local/Cellar/android-sdk/r22.0.1

if [ "$?" != "0" ]; then
  echo "Ant task failed";
  exit 1;
fi

cd ..
#then, replace the jar file in the starter project's libs directory with the new one
rm -rf "FHStarterProject/libs/*.jar"
cp  "sdk/dist/fh-$FH_SDK_VERSION.jar" "FHStarterProject/libs/"

#zip the starter project
rm -rf "sdk/dist/fh-starter-project-*.zip"

rm -rf "FHStarterProject/bin/"
rm -rf "FHStarterProject/gen/"

zip -9ry "sdk/dist/fh-starter-project-$FH_SDK_VERSION.zip" "FHStarterProject"



