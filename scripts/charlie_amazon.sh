#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg "$@" -eddie_ip 52.26.47.130 -debbie_ip 52.26.15.76 charlie
