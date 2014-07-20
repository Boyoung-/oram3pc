#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/libraries/*" sprout.ui.AccessCLI -debug ${DIR}/logs/charlie.log "$@" charlie
