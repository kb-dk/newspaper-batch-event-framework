#!/bin/bash
#
# Check for newly arrived newspaper batch(es), and initiate creation in DOMS
#

CONFIG_FILENAME="$1"
if [ -z "$CONFIG_FILENAME" ]; then
	echo 'config-filename not received' >&2
	exit 1
fi

# Get config from file
source "$CONFIG_FILENAME"

cd "$PATH_TO_DIR_OF_BATCHES"

for batch_dirname in *; do
	# Check format of dirname
	if [[ ! "$batch_dirname" =~ ^B[^-]+\-RT[0-9]+$ ]]; then
	#if [[ "$batch_dirname" =~ "^B[^-]+\-RT[0-9]+$" ]]; then
		# Dirname not recognized as a batch, skip it
		continue
	fi
	echo "'$batch_dirname'"

	batch_id=`echo "$batch_dirname" | sed -r 's/^B([^-]+).+/\1/g'`
	roundtrip=`echo "$batch_dirname" | sed -r 's/^B[^-]+\-RT([0-9]+)/\1/g'`
done

