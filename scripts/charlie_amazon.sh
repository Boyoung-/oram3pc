#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg retrieve -eddie_ip 54.149.7.250 -debbie_ip 54.69.67.81 charlie
