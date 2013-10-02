#!/bin/sh

SCRIPT_DIR=$(dirname "$0")

java -classpath "$SCRIPT_DIR/../lib/*" dk.statsbiblioteket.newspaper.autonomous.SampleComponent -c $SCRIPT_DIR/../conf/config.properties