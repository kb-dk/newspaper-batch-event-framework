#!/bin/bash

SCRIPT_DIR=$(dirname $(readlink -f $0))

java -classpath $SCRIPT_DIR/../lib/'*' dk.statsbiblioteket.newspaper.batcheventFramework.CreateBatch
