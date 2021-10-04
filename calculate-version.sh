#!/bin/sh

# get the last tag that is a semver, e.g. 0.1.0
LAST_TAG=`git tag | grep -Eo [0-9]+\.[0-9]+\.[0-9]+ | sort -t "." -k1,1n -k2,2n -k3,3n | tail -1`

# split the tag into major minor and patch 
VERSION_ARRAY=$(echo $LAST_TAG | sed 's/\./\ /g')
LAST_MAJOR=$(echo $VERSION_ARRAY | awk '{print $1;}')
LAST_MINOR=$(echo $VERSION_ARRAY | awk '{print $2;}')
LAST_PATCH=$(echo $VERSION_ARRAY | awk '{print $3;}')

# print calculated
echo "$LAST_MAJOR.$LAST_MINOR.$(( $LAST_PATCH + 1 ))"
