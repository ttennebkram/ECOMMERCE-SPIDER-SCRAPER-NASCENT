#!/bin/bash

java="c:/Progra~1/Java/jre6/bin/java.exe"
newpig_indexer="config/newpig_indexer.jar"

scraper_data_folder="$1/newpig"

function usage {
	echo "usage"
}

# fix_slashes - change Unix-style /path/to/file paths into \\path\\to\\file
function fix_slashes {
	arg="$1"
	echo $arg | sed -e 's/\//\\\\/g'
}

if [ $# -lt 1 ]; then
	usage
	exit 1
fi

command_arg="\"./newpig\" \"$scraper_data_folder\""
command="cmd /c mklink /d `fix_slashes \"$command_arg\"`"
eval $command

command_arg="\"$newpig_indexer\" \"$scraper_data_folder\""
command="$java -jar `fix_slashes \"$command_arg\"`"
eval $command