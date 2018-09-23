#!/bin/bash

set -e

version=$1
git_user=$2
git_mail=$3

git config user.email $git_mail
git config user.name $git_user

print_usage() {
	echo ""
	echo "usage: perform_release.sh <version>"
	echo ""
	echo "  version     The version of the release."
	echo ""
}

# Advances the last number of the given version string by one.
function advance_version () {
    local v=$1
    # Get the last number. First remove any suffixes (such as '-SNAPSHOT').
    local cleaned=`echo $v | sed -e 's/[^0-9][^0-9]*$//'`
    local last_num=`echo $cleaned | sed -e 's/[0-9]*\.//g'`
    local next_num=$(($last_num+1))
    # Finally replace the last number in version string with the new one.
    echo $v | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}

if [ -z "${version}" ]; then
	echo "ERROR: Version parameter is missing!"
	print_usage
	exit 1
fi

echo "Updating versions to ${version}"

mvn -f $(pwd)/org.structs4java.parent/pom.xml org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=${version} -Dmaven.repo.local=/workspace/.m2

mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/org.structs4java.parent/pom.xml versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-with-dependencies/pom.xml versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-core/pom.xml versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-maven-plugin/pom.xml versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-maven-plugin-test/pom.xml versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2

mvn versions:use-releases -DallowSnapshots=true -DexcludeReactor=false -DgenerateBackupPoms=false -Dmaven.repo.local=/workspace/.m2

git add --all
git commit -m"Released version ${version}."
git tag ${version}

next_version=$(advance_version $version)
echo "Bumping version ${version} up to ${next_version}"

mvn -f $(pwd)/org.structs4java.parent/pom.xml org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=${next_version}.qualifier -Dmaven.repo.local=/workspace/.m2

mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/org.structs4java.parent/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-with-dependencies/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-core/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-maven-plugin/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2
mvn -f $(pwd)/structs4java-maven-plugin-test/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=${next_version}-SNAPSHOT -Dmaven.repo.local=/workspace/.m2

git add --all
git commit -m"Bumped version ${version} for next development up to ${next_version}."

echo "Release done."
echo "You can now push your master and newly create tag ${version}"
echo "Simply run: git push origin master --tags"
