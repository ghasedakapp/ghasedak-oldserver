#!/bin/bash
SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
java ${SBT_OPTS} -jar `dirname $0`/sbt-launch.jar "$@"