#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -cp "${DIR}/../bin:${DIR}/../lib/*" sprout.ui.CommunicationBenchmark -eddie_ip 52.25.176.85 -debbie_ip 52.25.176.171 charlie
