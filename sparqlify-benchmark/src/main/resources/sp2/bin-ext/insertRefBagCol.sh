#/bin/bash
awk -f insertRefBagCol.awk out.sql > out_with_bag.sql
