#!/bin/bash

r2rmlBasePath='org/w3c/r2rml_tests/'

smlFileList='/tmp/smlFileList.txt'
r2rmlFileList='/tmp/r2rmlFileList.txt'
r2rmlNormFileList='/tmp/r2rmlNormFileList.txt'

echo '' > "$smlFileList"
echo '' > "$r2rmlFileList"
echo '' > "$r2rmlNormFileList"

#find . | grep sparqlify.*txt | sort -u > /tmp/files.txt



tableFile='table.html'

echo '' > "$tableFile"

echo '<html>' >> "$tableFile"
echo '<body>' >> "$tableFile"

echo '<table class="table table-striped">' >> "$tableFile"
echo '<tr><th>Id</th><th>SML-loc</th><th>R2RML-loc</th><th>R2RML-norm-loc</th><th>SML</th><th>R2RML</th><th>R2RML-norm</th></tr>' >> "$tableFile"

for smlFileName in `find . | grep 'sparqlify.*txt' | sort -u`; do
  tmp=`echo "$smlFileName" | sed -r 's|(.*)/([^/]+)/sparqlify(.*)txt$|\2/r2rml\3ttl|g'`

  mapId=`echo "$smlFileName" | sed -r 's|(.*)/([^/]+)/sparqlify(.*)txt$|\2|g'`
  mapSubId=`echo "$smlFileName" | sed -r 's|(.*)/([^/]+)/sparqlify(.*)\\.txt$|\3|g'`

  r2rmlFileName="$r2rmlBasePath""$tmp"

  if [ ! -f "$r2rmlFileName" ]; then
#    echo "Skipping: $r2rmlFileName"
    continue
  fi

  r2rmlNormFileName="/tmp/r2rml/$tmp"
  mkdir -p `dirname "$r2rmlNormFileName"`

  rapper -i turtle -o turtle "$r2rmlFileName" > "$r2rmlNormFileName"
  
#  echo "Process: $r2rmlFileName"

  echo "$smlFileName" >> "$smlFileList"
  echo "$r2rmlFileName" >> "$r2rmlFileList"
  echo "$r2rmlNormFileName" >> "$r2rmlNormFileList"

#  smlCloc=`cloc --force-lang='php' --quiet --csv "$smlFileName" | awk -F "," '{print $5}'`
#  r2rmlCloc=`cloc --force-lang='php' --quiet --csv "$r2rmlFileName"`
#  r2rmlNormCloc=`cloc --force-lang='php' --quiet --csv "$r2rmlNormFileName"`


  smlCloc=`cloc --force-lang='php' --quiet --xml "$smlFileName" | tail -n+2 | xmllint --xpath 'string(//total/@code)' -`
  r2rmlCloc=`cloc --force-lang='php' --quiet --xml "$r2rmlFileName" | tail -n+2 | xmllint --xpath 'string(//total/@code)' -`
  r2rmlNormCloc=`cloc --force-lang='php' --quiet --xml "$r2rmlNormFileName" | tail -n+2 | xmllint --xpath 'string(//total/@code)' -`

  smlHtml=`cat "$smlFileName" | recode utf8..html`
  r2rmlHtml=`cat "$r2rmlFileName" | recode utf8..html`
  r2rmlNormHtml=`cat "$r2rmlNormFileName" | recode utf8..html`

  echo "<tr><td>$mapId-$mapSubId</td><td>$smlCloc</td><td>$r2rmlCloc</td><td>$r2rmlNormCloc</td><td><pre>$smlHtml</pre></td><td><pre>$r2rmlHtml</pre></td><td><pre>$r2rmlNormHtml</pre></td></tr>" >> "$tableFile"


done

echo '</table>' >> "$tableFile"
echo '</body>' >> "$tableFile"
echo '</html>' >> "$tableFile"
echo '' >> "$tableFile"



echo "SML Results"
cloc --force-lang='php' --list-file="$smlFileList" --skip-uniqueness

echo ""

echo "Raw R2RML Results"
cloc --force-lang='php' --list-file="$r2rmlFileList" --skip-uniqueness

echo "Norm R2RML Results"
cloc --force-lang='php' --list-file="$r2rmlNormFileList" --skip-uniqueness

