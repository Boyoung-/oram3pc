#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -cp "${DIR}/../bin:${DIR}/../lib/*" sprout.ui.CommunicationBenchmark -eddie_ip 52.68.80.95 -debbie_ip 52.76.11.179 charlie
