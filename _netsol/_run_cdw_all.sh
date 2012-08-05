#!/bin/bash

top_dir="C:\\_netsol"

echo -e "\nstarted - `date`"
echo -e "steps: cdw, cdw-indexer\n"

echo -e "*** running cdw ...\n"
cdw_datadir=`./_run_netsol.sh cdw | tail -n 1 | cut -f2 -d'=' -`
cdw_datadir_absolute="\"$top_dir\\$cdw_datadir\""
echo -e "*** done with cdw\n\n"

echo -e "*** running cdw-indexer ...\n"
cdw_indexer_datadir=`./_run_netsol.sh cdw-indexer $cdw_datadir_absolute | tail -n 1 | cut -f2 -d'=' -`
echo -e "*** done with cdw-indexer\n\n"

echo -e "ended - `date`"