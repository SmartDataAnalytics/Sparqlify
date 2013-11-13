#!/bin/sh

debFile="$1"

if [ -z "$debFile" ]; then
        echo "No deb file specified."
        exit 1
fi

debFileBase=`basename "$debFile"`

targetFile="/tmp/$debFileBase"

#rsync -av "$debFilePath" cstadler:/tmp
#scp "$debFile" cstadler:/"$targetFile"

#ssh cstadler "reprepro -Vb /home/cstadler/Workspace/System/var/www/cstadler.aksw.org/repos/apt includedeb precise \"$targetFile\" && rm \"$targetFile\""

echo "$targetFile -> $targetFile"

