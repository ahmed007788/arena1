#!/bin/sh
JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/jdk-11}
DIRNAME=$(dirname "$0")
exec "$JAVA_HOME/bin/java" -Xmx4g -Xms256m -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"