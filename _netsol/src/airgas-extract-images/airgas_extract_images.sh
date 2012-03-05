#!/bin/bash

MOZILLA_CACHE_VIEW="C:/Apps/MozillaCacheView.exe"

site="$1"
data_folder="$2"
images_folder="images"
images_moved_folder="$images_folder/_moved"

function usage {
	echo "usage"
}

if [ $# -lt 1 ]; then
	usage
	exit 1
fi

echo -e "*** airgas_extract_images.sh - $site ***\n"

if [ ! -d $images_folder ]; then
	mkdir $images_folder
	mkdir $images_folder/_moved
fi

for i in 0 1 2 3 4 5 6; do
	site_image_cache=$site"_image_cache"
	folder="$data_folder\\$site_image_cache$i\\Cache"
	
	echo -e "\n*** extracting images from cache$i"
	$MOZILLA_CACHE_VIEW -folder $folder /copycache "https://emarket.airgas.com" "image/jpeg" /CopyFilesFolder $images_folder /UseWebSiteDirStructure 1
	done

echo -e "\n*** associating and renaming images with product-ids"
for i in 0 1 2 3 4 5 6; do
	file=$data_folder\\$site"_image_dict"$i.csv

	for j in `cat $file`; do
		id=`echo $j | cut -f1 -d',' -`
		url=`echo $j | cut -f2 -d',' -`
 		url_p1=`echo -e $url | cut -f4 -d'/' -`
		url_p2=`echo -e $url | cut -f5 -d'/' -`
		url_p3=`echo -e $url | cut -f6 -d'/' - | tr -d '\r'`

		if [[ ${#url_p1} -gt 1 && ${#url_p2} -gt 1 && ${#url_p3} -gt 1 ]]; then
			i=$images_folder/emarket.airgas.com/$url_p1/$url_p2/$url_p3
			cp $i $images_moved_folder/$id.jpg
		else
			echo $id >> $images_moved_folder/_ids_without_image.txt
		fi
	done

done

mv $images_moved_folder ./_moved
rm -r $images_folder
mv ./_moved $images_folder

echo -e "\n*** done!"