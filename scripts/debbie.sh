#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -debug ${DIR}/logs/debbie.log "$@" debbie
