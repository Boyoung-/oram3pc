#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.TestCLI -test_alg retrieve -eddie_ip 52.42.255.187 -debbie_ip 52.38.255.37 charlie
