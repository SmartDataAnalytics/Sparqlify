

$0 == "CREATE TABLE Reference (fk_from INTEGER,fk_to INTEGER);" {print "CREATE TABLE Reference (fk_from INTEGER,fk_to INTEGER, refno INTEGER);"}

index($0,"INSERT INTO Reference VALUES ")==1 {
        insertval = $5
	gsub("[(|)|,|;]", " ", insertval)
	split(insertval,splits)
	srcdocid  = splits[1]
	targetdocid  = splits[2]

	if(lastsrcdoc!=srcdocid){
		inc=0;
		lastsrcdoc = srcdocid;
	}else{
		inc++;
	}
	
	print $1 " "  $2  " " $3 " " $4 " (" srcdocid "," targetdocid "," inc ");"
	}

index($0,"INSERT INTO Reference VALUES ")!=1 && $0 != "CREATE TABLE Reference (fk_from INTEGER,fk_to INTEGER);" {print $0}
