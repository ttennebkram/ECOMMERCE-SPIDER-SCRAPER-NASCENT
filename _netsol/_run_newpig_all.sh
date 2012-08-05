#!/bin/bash

top_dir="C:\\_netsol"

echo -e "\nstarted - `date`"
echo -e "steps: newpig, newpig-indexer\n"

echo -e "*** running newpig ...\n"
newpig_datadir=`./_run_netsol.sh newpig | tail -n 1 | cut -f2 -d'=' `
newpig_datadir_absolute="\"$top_dir\\$newpig_datadir\""
echo -e "*** done with newpig\n\n"

echo -e "*** running newpig-indexer ...\n"
newpig_indexer_datadir=`./_run_netsol.sh newpig-indexer $newpig_datadir_absolute | tail -n 1 | cut -f2 -d'=' -`
echo -e "*** done with newpig-indexer\n\n"

echo -e "ended - `date`"