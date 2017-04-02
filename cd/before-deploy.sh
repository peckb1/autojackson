#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_844fcf1f452a_key -iv $encrypted_844fcf1f452a_iv -in cd/codesigning.asc.enc -out cd/signingkey.asc -d
    gpg --fast-import --batch --verbose cd/signingkey.asc
fi