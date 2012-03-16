#!/bin/sh

if [ $# -lt 2 ]; then
	echo "\n*** usage: `basename $0` [ site ] [ images_dir ]"
	echo "\n*** example: ./`basename $0` airgas airgas_images-030412\n"
	exit 1
fi

site="$1"
dir="$2"
ids_dir="NIE/ECOMMERCESPIDER/_netsol/src/airgas/config"

ids0=$ids_dir/$site"_ids0.txt"
ids1=$ids_dir/$site"_ids1.txt"
ids2=$ids_dir/$site"_ids2.txt"
ids3=$ids_dir/$site"_ids3.txt"
ids4=$ids_dir/$site"_ids4.txt"
ids5=$ids_dir/$site"_ids5.txt"

for i in `ls $dir | cut -f1 -d'.' -`; do
	echo $i
	result0=`grep $i $ids0`
	result1=`grep $i $ids1`
	result2=`grep $i $ids2`
	result3=`grep $i $ids3`
	result4=`grep $i $ids4`
	result5=`grep $i $ids5`
	
	if [ -z "$result0$result1$result2$result3$result4$result5" ]; then
		echo $i >> $site"_bad_images.txt"
	fi
done