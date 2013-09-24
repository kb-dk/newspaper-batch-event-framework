#!/bin/bash
#
# Check for newly arrived newspaper batch(es), and initiate creation in DOMS
#

donedir='batches-done'

config_filename="$1"
if [ -z "$config_filename" ]; then
	echo 'config-filename not received' >&2
	exit 1
fi

# Get config from file
source "$config_filename"

# Get path to directory of this script
my_path="`dirname \"$0\"`"              # relative
my_path="`( cd \"$my_path\" && pwd )`"  # absolutized and normalized
if [ -z "$my_path" ] ; then
	echo 'could not access path' >&2
    exit 1
fi

cd "$path_to_dir_of_batches"

for batch_dirname in *; do
	# Check format of dirname
	if [[ ! "$batch_dirname" =~ ^B[^-]+\-RT[0-9]+$ ]]; then
		# Dirname not recognized as a batch, skip it
		continue
	fi

	# Skip batches that are already done
	if [ -f "$my_path/$donedir/$batch_dirname" ]; then
		continue
	fi

	batch_id=`echo "$batch_dirname" | sed -r 's/^B([^-]+).+/\1/g'`
	roundtrip=`echo "$batch_dirname" | sed -r 's/^B[^-]+\-RT([0-9]+)/\1/g'`

	# Create batch in DOMS
	java -classpath $my_path/../lib/'*' dk.statsbiblioteket.newspaper.batcheventFramework.CreateBatch "$batch_id" "$roundtrip" "$trigger_name" "$url_to_doms" "$doms_username" "$doms_password"

	# Mark batch as done, by creating an empty file with the batch's name
	touch "$my_path/$donedir/$batch_dirname"
done
