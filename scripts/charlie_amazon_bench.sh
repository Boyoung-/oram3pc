#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -cp "${DIR}/../bin:${DIR}/../lib/*" sprout.ui.CommunicationBenchmark -eddie_ip 54.84.164.75 -debbie_ip 52.8.180.138 charlie
