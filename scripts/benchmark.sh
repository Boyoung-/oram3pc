#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.CommunicationBenchmark "$@" eddie 2>&1 | tee bench-eddie.log &

java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.CommunicationBenchmark "$@" debbie 2>&1 | tee bench-debbie.log &

java -cp "${DIR}/bin:${DIR}/lib/*" sprout.ui.CommunicationBenchmark "$@" charlie 2>&1 | tee bench-charlie.log &
