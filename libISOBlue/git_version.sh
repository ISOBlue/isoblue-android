#!/bin/sh
# Generate Androing string resource whose value is a git description.
# This is for Eclipse that is lame and does not use ant.

GIT_VER=`git describe --tags --dirty --abbrev=7 --always | sed 's/-/\&#8211;/g'`
FILE=res/values/git_version.xml

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > ${FILE}
echo "<resources>" >> ${FILE}
echo "" >> ${FILE}
echo "	<string name=\"libisoblue_version\">${GIT_VER}</string>" >> ${FILE}
echo "" >> ${FILE}
echo "</resources>" >> ${FILE}

