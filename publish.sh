#!/bin/bash

# Extract version from gradle.properties
VERSION=$(grep "^version=" gradle.properties | cut -d'=' -f2)
TAG="v$VERSION"

echo "Current version: $VERSION"

# Check if tag already exists
if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Tag $TAG already exists."
    read -p "Push to master anyway? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git push origin master
    fi
else
    echo "Creating and pushing tag $TAG..."
    git add .
    git commit -m "Release $VERSION"
    git tag "$TAG"
    git push origin master --tags
fi
