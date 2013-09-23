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
	echo $batch_dirname
	# TODO valideer

	$batch_id=`echo "$batch_dirname" | sed -r -e '^B\([^-]+[-]RT[0-9]+\)$'`

	#echo `expr "$batch_dirname" : '^B\([^-]+[-]RT[0-9]+\)$'`
done



