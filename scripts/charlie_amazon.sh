#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg "$@" -eddie_ip 52.68.80.95 -debbie_ip 52.69.231.166 charlie
