#!/bin/sh -ex

RENDERDOC_VERSION="1.41"

(cat <<EOF
5b64338eeafc755737d89136118dc3820b5cbd8a87b958547596cb911f54a481  renderdoc.tar.gz
EOF
) > renderdoc.tar.gz.sha256

wget -O renderdoc.tar.gz "https://renderdoc.org/stable/${RENDERDOC_VERSION}/renderdoc_${RENDERDOC_VERSION}.tar.gz"
sha256sum -c renderdoc.tar.gz.sha256
tar -x -z -f renderdoc.tar.gz --strip-components=2 "renderdoc_${RENDERDOC_VERSION}/lib/librenderdoc.so"
rm -f renderdoc.tar.gz
rm -f renderdoc.tar.gz.sig
