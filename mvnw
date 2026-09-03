#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script
# ----------------------------------------------------------------------------

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-`pwd`}"
export MAVEN_PROJECTBASEDIR

WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ -f "$WRAPPER_JAR" ]; then
    exec java -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -cp "$WRAPPER_JAR" "$WRAPPER_LAUNCHER" "$@"
else
    echo "Error: Could not find $WRAPPER_JAR" >&2
    exit 1
fi
