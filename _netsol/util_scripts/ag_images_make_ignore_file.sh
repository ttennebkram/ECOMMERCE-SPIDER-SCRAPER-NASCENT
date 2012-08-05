#!/bin/sh

if [ $# -lt 1 ]; then
	echo "\n*** usage: `basename $0` [ images_dir ]\n"
	echo "*** example: ./`basename $0` airgas_images-030912\n"
	exit 1
fi

dir="$1"
file="`echo $dir | cut -f1 -d'_' -`_images_ignore_ids.csv"
for i in `ls $dir`; do
	echo $i | rev | cut -f2- -d'.' - | rev | tr '_' '/' >> $file
done
