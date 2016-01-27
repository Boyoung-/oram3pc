ORAM3P
======

Three-party ORAM implementation

1. To compile:
$ ant

2. To rebuild:
$ ant clean
$ ant

3. To run local 3-party testing, enter the following in order in different consoles:
$ scripts/eddie.sh retrieve
$ scripts/debbie.sh retrieve
$ scripts/charlie.sh retrieve

4. To do a sample run:
$ scripts/sampletest.sh