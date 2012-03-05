#!/bin/bash

# list of programs we can run- for each program {X} there must be a 
# run_{X} function that does whatever's necessary to run that spider
programs="airgas airgas-complete airgas-images airgas-extract-images airgasnonmed \
			airgasnonmed-complete airgasnonmed-images airgasnonmed-extract-images \
			cdw dell newpig selenium"

# path to the directories in use
src_path="src"
log_path="log"
data_path="data"

# path to the top folder, *relative to an instance's data path*
path_data_relative="../../.."

# the name of the configuration directory in a spider's src_path, made available
# to instances of that spider at run-time (holds id files, mappings etc.)
confdir_name="config"

# start_cmd.bat
start_bat="_start_cmd.bat"

# python (with -u for unbuffered output), java, bash
python="c:/Python27/python.exe -u"
java="c:/Progra~1/Java/jre6/bin/java.exe"
bash="c:/cygwin/bin/bash.exe"

# usage - print a help message if there are no command-line arguments
function usage {
	echo -e "\n  *** usage:"
	echo -e "\n\t"`basename $0` "[ spider_to_run ] [ arg ] ..."
	echo -e "\texample:" `basename $0` "airgas"
	echo -e "\n\tavailable programs:\n"
	for name in $programs; do
		echo -e "\t   $name"
	done
	echo -e ""
}

# fix_slashes - change Unix-style /path/to/file paths into \\path\\to\\file
function fix_slashes {
	arg="$1"
	echo $arg | sed -e 's/\//\\\\/g'
}

# make_cmd - run a program, setting up its logfile and data directory, and a
# symlink to its configuration directory
# 	@param $1:	name
#	@param $2:	instance name (used for logging)
#	@param $3:	executable (e.g. $python, $java, $bash from above)
#	@param $4:	program to be executed
#	@param $5:	arguments to the program
function make_cmd {
	spider_name="$1"
	spider_instance_name="$2"
	spider_exec="$3"
	spider_program="$4"
	spider_program_args="$5"
	
	# check to see if we need to make this instance's log dir, data dir, and 
	# whether we need to symlink its conf dir from the program's path under $src_path
	date=`date '+%m%d%y'`
	datadir="$data_path/$spider_name/$spider_name-$date"
	if [ ! -d $datadir ]; then
		mkdir $datadir
	fi
	logdir="$log_path/$spider_name/$spider_name-$date"
	if [ ! -d $logdir ]; then
		mkdir $logdir
	fi
	confdir="`dirname $spider_program`/$confdir_name"
	if [ -d $confdir ]; then
		if [ ! -d $datadir/$confdir_name ]; then
			command_arg="\"$datadir/$confdir_name\" \"$path_data_relative/$confdir\""
			command="cmd /c mklink /d `fix_slashes \"$command_arg\"`"
			eval $command
		fi
	fi
	
	logdir_data_relative="$path_data_relative/$logdir"
	logfile="$logdir_data_relative/$spider_instance_name-$date-`date '+%H%M%S'`.log"
	
	# redefine spider_program to be run from within the data directory
	spider_program="$path_data_relative/$spider_program"
	
	# make the new command, run it
	command_arg="\"$start_bat\" \"$spider_instance_name\" \"$datadir\" \"$spider_exec \
				$spider_program $spider_program_args 2>&1\" \"$logfile\""
	command="cmd /c `fix_slashes \"$command_arg\"`"
	eval $command
	
	# print this program's datadir, *must be the last line of output*
	echo -ne "\nDATA=`fix_slashes \"$datadir\"`"
}

function _run_ag_template {
	site="$1"
	airgas_exec="$python"
	airgas_program="$src_path/airgas/airgas_spider.py"
	
	for i in {0..5}; do
		airgas_args="$site $i"
		make_cmd "$site" "$site$i" "$airgas_exec" "$airgas_program" "$airgas_args"
	done
}

function _run_ag-complete_template {
	site="$1"
	args="$2"
	airgas_complete_exec="$python"
	airgas_complete_program="$src_path/airgas/airgas_spider.py"
	
	airgas_complete_args="$site complete $args"
	make_cmd "$site-complete" "$site-complete" "$airgas_complete_exec" "$airgas_complete_program" \
				"$airgas_complete_args"
}

function _run_ag-images_template {
	site="$1"
	args="$2"
	airgas_images_exec="$python"
	airgas_images_program="$src_path/airgas-images/airgas_images.py"
	
	for i in {0..6}; do
		airgas_images_args="$site $i $args"
		make_cmd "$site-images" "$site-images$i" "$airgas_images_exec" "$airgas_images_program" \
					"$airgas_images_args"
	done
}

function _run_ag-extract-images_template {
	site="$1"
	args="$2"	
	airgas_extract_images_exec="$bash"
	airgas_extract_images_program="$src_path/airgas-extract-images/airgas_extract_images.sh"
	
	make_cmd "$site-extract-images" "$site-extract-images" "$airgas_extract_images_exec" \
				"$airgas_extract_images_program" "$site $args"
}

function run_airgas {
	arg="$1"
	_run_ag_template "airgas" $arg
}
function run_airgasnonmed {
	arg="$1"
	_run_ag_template "airgasnonmed" $arg
}
function run_airgas-complete {
	arg="$1"
	_run_ag-complete_template "airgas" $arg
}
function run_airgasnonmed-complete {
	arg="$1"
	_run_ag-complete_template "airgasnonmed" $arg
}
function run_airgas-images {
	arg="$1"
	_run_ag-images_template "airgas" $arg
}
function run_airgasnonmed-images {
	arg="$1"
	_run_ag-images_template "airgasnonmed" $arg
}
function run_airgas-extract-images {
	arg="$1"
	_run_ag-extract-images_template "airgas" $arg
}
function run_airgasnonmed-extract-images  {
	arg="$1"
	_run_ag-extract-images_template "airgasnonmed" $arg
}

# run_cdw - run the CDW spider
function run_cdw {
	cdw_exec="scrapy runspider"
	cdw_program="$src_path/cdw/cdw_spider.py"
	
	make_cmd "cdw" "cdw" "$cdw_exec" "$cdw_program" ""
}

# run_dell - run the Dell spider
function run_dell {
	dell_exec="$python"
	dell_program="$src_path/dell/dell_spider.py"
	
	make_cmd "dell" "dell" "$dell_exec" "$dell_program" ""
}

# run_selenium - open a selenium server
function run_selenium {
	selenium_exec="$java -jar"
	selenium_program="$src_path/selenium-server-standalone-2.9.0.jar"
	
	make_cmd "selenium" "selenium" "$selenium_exec" "$selenium_program" ""
}

function main {
	# print the help message if there aren't any command-line arguments
	if [ $# -lt 1 ]; then
		usage
		exit 1
	fi

	# is this argument-string one of the valid program names?
	spider_name="$1"
	spider_arg="$2"
	found=0
	for j in $programs; do
		if [ "$spider_name" == "$j" ]; then
			# it's a valid string– run this spider's function
			found=1
			spider_function="run_$j $spider_arg"
			echo -e "\n*** running $j ..."
			eval $spider_function
			break
		fi
	done
	
	# the user provided an invalid spider name
	if [ ! $found -eq 1 ]; then
		echo -e "\n*** error: unknown spider - \"$i\""
	fi
}

# start
main $@
