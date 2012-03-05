#!/bin/bash

top_dir="C:\\ECOMMERCESPIDER\\_netsol"

site="$1"

echo -e "\nstarted - `date`"
echo -e "steps: $site, $site-complete, $site-images, $site-extract-images\n"

echo -e "*** running $site ...\n"
airgas_datadir=`./_run_netsol.sh $site | tail -n 1 | cut -f2 -d'=' `
airgas_datadir_absolute="\"$top_dir\\$airgas_datadir\""
echo -e "*** done with $site\n\n"

echo -e "*** running $site-complete ...\n"
airgas_complete_datadir=`./_run_netsol.sh $site-complete $airgas_datadir_absolute | tail -n 1 | cut -f2 -d'=' -`
echo -e "*** done with $site-complete\n\n"

echo -e "*** running $site-images ...\n"
airgas_images_datadir=`./_run_netsol.sh $site-images $airgas_datadir_absolute | tail -n 1 | cut -f2 -d'=' -`
airgas_images_datadir_absolute="\"$top_dir\\$airgas_images_datadir\""
echo -e "*** done with $site-images\n\n"

echo -e "*** running $site-extract-images ...\n"
airgas_extract_images_datadir=`./_run_netsol.sh $site-extract-images $airgas_images_datadir_absolute | tail -n 1 | cut -f2 -d'=' -`
echo -e "*** done with $site-extract-images\n\n"

echo -e "ended - `date`"