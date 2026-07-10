#!/bin/sh
JAVA_HOME=${JAVA_HOME:-/home/user/.sdkman/candidates/java/current}
DIRNAME=$(dirname "$0")
exec "$JAVA_HOME/bin/java" -Xmx4g -Xms256m -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"