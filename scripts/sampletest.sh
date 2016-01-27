#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."

java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg retrieve eddie &

sleep 1s

java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg retrieve debbie &

sleep 1s

java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg retrieve charlie &
