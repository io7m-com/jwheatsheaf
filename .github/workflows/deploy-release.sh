#!/bin/sh
#
#  Automatically generated: DO NOT EDIT.
#
#  Generation code: https://www.github.com/io7m-com/.github/
#

fatal()
{
  echo "fatal: $1" 1>&2
  exit 1
}

if [ -z "${MAVEN_CENTRAL_USERNAME}" ]
then
  fatal "MAVEN_CENTRAL_USERNAME is not set."
fi

if [ -z "${MAVEN_CENTRAL_PASSWORD}" ]
then
  fatal "MAVEN_CENTRAL_PASSWORD is not set."
fi

(cat <<EOF
<settings
  xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <profiles>
    <profile>
      <id>io7m</id>
      <properties>
        <gpg.useagent>true</gpg.useagent>
        <gpg.keyname>github-ci-maven-rsa-key</gpg.keyname>
        <io7m.deployment>true</io7m.deployment>
      </properties>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>${MAVEN_CENTRAL_USERNAME}</username>
      <password>${MAVEN_CENTRAL_PASSWORD}</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>${MAVEN_CENTRAL_USERNAME}</username>
      <password>${MAVEN_CENTRAL_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF
) > "$HOME/.m2/settings.xml" || fatal "could not update $HOME/.m2/settings.xml"

exec mvn \
-Dio7m.release=true \
-Dio7m.deployment=true \
--batch-mode \
--strict-checksums \
-Denforcer.skip=true \
-DskipTests=true \
-DskipITs=true deploy
