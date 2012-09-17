#!/usr/bin/perl

use strict;

### check cmd line args
if ($#ARGV!=1) {
    &abort("Main","Wrong number of arguments; " 
                . "expecting --mysql/--oracle/--monet and file");
} elsif (!($ARGV[0] eq "--mysql"
        || $ARGV[0] eq "--oracle"
        || $ARGV[0] eq "--monet")) {
    &abort("Main","Illegal first argument; expected --mysql/--oracle/--monet");
}


### global variables
my $last_refblank_id;
my $last_refblank;

###############################################################################
############# STEP 0: process cmd line and prepare
my $TYPE_MYSQL=0;
my $TYPE_ORACLE=1;
my $TYPE_MONET=2;

my $db_type;
if ($ARGV[0] eq "--mysql") {
    $db_type=$TYPE_MYSQL;
} elsif ($ARGV[0] eq "--oracle") {
    $db_type=$TYPE_ORACLE;
} elsif ($ARGV[0] eq "--monet") {
    $db_type=$TYPE_MONET;
}

my $infile=$ARGV[1];
my $outfile="./out.sql"; # fixed
&abort("Main","Input file '$infile' could not be opened.")
    if (!open(IN,"<$infile")); 
&abort("Main","Output file '$outfile' could not be opened.")
    if (!open(OUT,">$outfile"));


###############################################################################
########### STEP 1: create tables
my $shortstring;
my $mediumstring;
my $largestring;
my $integer;
if ($db_type==$TYPE_MYSQL) {
    $shortstring="VARCHAR(100)";
    $mediumstring="VARCHAR(200)";
    $largestring="VARCHAR(20000)";
    $integer="INTEGER";
} elsif ($db_type==$TYPE_ORACLE) {
    $shortstring="VARCHAR(100)";
    $mediumstring="VARCHAR(200)";
    $largestring="CLOB";
    $integer="INTEGER";
} elsif ($db_type==$TYPE_MONET) {
    $shortstring="VARCHAR(100)";
    $mediumstring="VARCHAR(500)";
    $largestring="VARCHAR(20000)";
    $integer="INTEGER";
} else {
    &abort("Main","Undefined SQL database type");
}


### here we store delayed editors
# (sometimes editors are written before the corresponding person is defined;
#  in this case, we have to insert them right at the end of processing)
my @delay;

my %venue_types=(
    "journal"     => 0,
    "proceedings" => 1);

my %publication_types=(
    "article"       => 0,
    "inproceedings" => 1,
    "book"          => 2,
    "incollection"  => 3,
    "www"           => 4,
    "phdthesis"     => 5,
    "mastersthesis" => 6);


#######################
###### TABLE DEFINITION
##################################################################
### Doc table (and corresponding multi attribute tables)
my $tbl_doc="Document";
my %doc_attributes=(
    "ID"        => $integer,
    "address"   => $shortstring,
    "booktitle" => $shortstring,
    "isbn"      => $shortstring,
    "issued"    => $integer,
    "mnth"      => $integer,
    "note"      => $mediumstring,
    "nr"        => $integer,
    "publisher" => $shortstring,
    "series"    => $shortstring,
    "stringid"  => $shortstring,
    "title"     => $mediumstring,
    "volume"    => $integer);
my $doc_attributes_pkey="ID";
my %doc_multi_attributes=(
    "homepage" => $mediumstring,
    "seeAlso"  => $mediumstring);
my @doc_fk=();

##################################################################
### Venue type table
my $tbl_venue_type="VenueType";
my %venue_type_attributes=(
    "ID"   => $integer,
    "name" => $shortstring);
my $venue_type_attributes_pkey="ID";

##################################################################
### Venue table (and corresponding multi attribute tables)
my $tbl_venue="Venue";
my %venue_attributes=(
    "ID"            => $integer,
    "fk_document"   => $integer,
    "fk_venue_type" => $integer);
my $venue_attributes_pkey="ID";
my %venue_multi_attributes=();
my %venue_fk1=(
    "DEST"        => $tbl_doc,
    "fk_document" => "ID");
my %venue_fk2=(
    "DEST"          => $tbl_venue_type,
    "fk_venue_type" => "ID");
my @venue_fk=(\%venue_fk1,\%venue_fk2);


##################################################################
### Publication type table
my $tbl_publication_type="PublicationType";
my %publication_type_attributes=(
    "ID"   => $integer,
    "name" => $shortstring);
my $publication_type_attributes_pkey="ID";


##################################################################
### Publication table (and corresponding multi attribute tables)
my $tbl_publication="Publication";
my %publication_attributes=(
    "ID"                  => $integer,
    "chapter"             => $integer,
    "fk_document"         => $integer,
    "fk_publication_type" => $integer,
    "fk_venue"            => $integer,
    "pages"               => $integer);
my $publication_attributes_pkey="ID";
my %publication_multi_attributes=(
    "cdrom" => $mediumstring
);    
my %publication_fk1=(
    "DEST"        => $tbl_doc,
    "fk_document" => "ID");
my %publication_fk2=(
    "DEST"                => $tbl_publication_type,
    "fk_publication_type" => "ID");
my %publication_fk3=(
    "DEST"     => $tbl_venue,
    "fk_venue" => "ID");
my @publication_fk=(\%publication_fk1,\%publication_fk2,
                    \%publication_fk3);


##################################################################
### Peron table
my $tbl_person="Person";
my %person_attributes=(
    "ID"       => $integer,
    "name"     => $shortstring,
    "stringid" => $shortstring);
my $person_attributes_pkey="ID";


##################################################################
### References table
my $tbl_references="Reference";
my %references_attributes=(
    "fk_from" => $integer,
    "fk_to"   => $integer);
my $references_attributes_pkey=0; # external primary key
my %references_fk1=(
    "DEST"    => $tbl_publication,
    "fk_from" => "ID");
my %references_fk2=(
    "DEST"  => $tbl_publication,
    "fk_to" => "ID");
my @references_fk=(\%references_fk1,\%references_fk2);


##################################################################
### Abstract table
my $tbl_abstract="Abstract";
my %abstract_attributes=(
    "fk_publication" => $integer,
    "txt"            => $largestring);
my $abstract_attributes_pkey="fk_publication";
my %abstract_fk1=(
    "DEST"           => $tbl_publication,
    "fk_publication" => "ID");
my @abstract_fk=(\%abstract_fk1);


##################################################################
### Author table
my $tbl_author="Author";
my %author_attributes=(
    "fk_person"      => $integer,
    "fk_publication" => $integer);
my $author_attributes_pkey="fk_person,fk_publication";
my %author_fk1=(
    "DEST"      => $tbl_person,
    "fk_person" => "ID");
my %author_fk2=(
    "DEST"           => $tbl_publication,
    "fk_publication" => "ID");
my @author_fk=(\%author_fk1,\%author_fk2);


##################################################################
### Editor table
my $tbl_editor="Editor";
my %editor_attributes=(
    "fk_document" => $integer,
    "fk_person"   => $integer);
my $editor_attributes_pkey="fk_person,fk_document";
my %editor_fk1=(
    "DEST"      => $tbl_person,
    "fk_person" => "ID");
my %editor_fk2=(
    "DEST"        => $tbl_doc,
    "fk_document" => "ID");
my @editor_fk=(\%editor_fk1,\%editor_fk2);


#####################
###### Table creation
### Document table (and corresponding multi attribute tables)
my $sql_document=
    &createTableSQL($tbl_doc,\%doc_attributes,
                    $doc_attributes_pkey,\@doc_fk);
my $sql_repeat_document=
    &createRepeatTables($tbl_doc,
            "fk_document","$integer",
            \%doc_multi_attributes);

### Publication table (and corresponding multi attribute tables)
my $sql_publication=
    &createTableSQL($tbl_publication,\%publication_attributes,
                    $publication_attributes_pkey,\@publication_fk);
my $sql_repeat_publication=
    &createRepeatTables($tbl_publication,
            "fk_publication","$integer",
            \%publication_multi_attributes);

### Publication type table
my $sql_publication_type=
    &createTableSQL($tbl_publication_type,\%publication_type_attributes,
                    $publication_type_attributes_pkey,);

### Venue table (and corresponding multi attribute tables)
my $sql_venue=
    &createTableSQL($tbl_venue,\%venue_attributes,
                    $venue_attributes_pkey,\@venue_fk);
my $sql_repeat_venue=
    &createRepeatTables($tbl_venue,
                        "fk_venue","$integer",
                        \%venue_multi_attributes);

### Venue type table
my $sql_venue_type=
    &createTableSQL($tbl_venue_type,\%venue_type_attributes,
                    $venue_type_attributes_pkey);

### Peron table
my $sql_person=
    &createTableSQL($tbl_person,\%person_attributes,
                    $person_attributes_pkey);

### References table
my $sql_references=
    &createTableSQL($tbl_references,\%references_attributes,
                    $references_attributes_pkey,\@references_fk);

### Abstract table
my $sql_abstract=
    &createTableSQL($tbl_abstract,\%abstract_attributes,
                    $abstract_attributes_pkey,\@abstract_fk);

### Author table
my $sql_author=
    &createTableSQL($tbl_author,\%author_attributes,
                    $author_attributes_pkey,\@author_fk);

### Editor table
my $sql_editor=
    &createTableSQL($tbl_editor,\%editor_attributes,
                    $editor_attributes_pkey,\@editor_fk);


### Fixed inserts
my $sql_venue_journal="INSERT INTO $tbl_venue_type VALUES "
                    . "(" . $venue_types{"journal"} . ",'Journal');";
my $sql_venue_proceeding="INSERT INTO $tbl_venue_type VALUES "
                    . "(" . $venue_types{"proceedings"} . ",'Proceedings');";
my $sql_publication_article="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"article"} . ",'Article');";
my $sql_publication_inproceeding="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"inproceedings"} . ",'Inproceedings');";
my $sql_publication_book="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"book"} . ",'Book');";
my $sql_publication_incollection="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"incollection"} . ",'Incollection');";
my $sql_publication_www="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"www"} . ",'Www');";
my $sql_publication_phd="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"phdthesis"} . ",'PhDThesis');";
my $sql_publication_masters="INSERT INTO $tbl_publication_type VALUES "
                    . "(" . $publication_types{"mastersthesis"} . ",'MastersThesis');";


### write tables (order matters)
print OUT "START TRANSACTION;\n" if ($db_type==$TYPE_MONET);
print OUT "$sql_document\n";
print OUT "$sql_publication_type\n";
print OUT "$sql_venue_type\n";
print OUT "$sql_venue\n";
print OUT "$sql_publication\n";
print OUT "$sql_person\n";
print OUT "$sql_references\n";
print OUT "$sql_abstract\n";
print OUT "$sql_author\n";
print OUT "$sql_editor\n";
print OUT "$sql_repeat_document\n";
print OUT "$sql_repeat_publication\n";
print OUT "$sql_repeat_venue\n";
print OUT "\n";

### write default inserts
print OUT "$sql_venue_journal\n";
print OUT "$sql_venue_proceeding\n";
print OUT "$sql_publication_article\n";
print OUT "$sql_publication_inproceeding\n";
print OUT "$sql_publication_book\n";
print OUT "$sql_publication_incollection\n";
print OUT "$sql_publication_www\n";
print OUT "$sql_publication_phd\n";
print OUT "$sql_publication_masters\n";
print OUT "\n";


###############################################################################
########### STEP 2: parse input file
### prefixes (for more efficient internal storage)
my %std_prefixes=(
    "journal"        => "http://localhost/publications/journals/",
    "article"        => "http://localhost/publications/articles/",
    "incollection"    => "http://localhost/publications/incolls/",
    "inproceedings"    => "http://localhost/publications/inprocs/",
    "proceedings"    => "http://localhost/publications/procs/",
    "book"            => "http://localhost/publications/books/",
    "www"            => "http://localhost/publications/wwws/",
    "mastersthesis"    => "http://localhost/publications/masters/",
    "phdthesis"        => "http://localhost/publications/phds/",
    "misc"            => "http://localhost/misc/");
&log("Main","Manually added standard prefixes");

### set up environment (for IDs), initially empty
my %publication_env=("ID_CTR" => 0);
my %venue_env=("ID_CTR" => 0);
my %doc_env=("ID_CTR" => 0);
my %blank_env=("ID_CTR" => 0);

my %venue_to_doc;
my %publication_to_doc;


### parse prefix section
my %prefixes=%std_prefixes; # will be extended

&log("Main","Parsing input file '$infile' now");
my $cnt=0;
my $initial=1;
while (my $line=<IN>) {
    chomp($line);
    $line=~s/\.$//g;

    ### process prefix sextion
    if ($initial) {
        if ($line!~/^\@prefix/) {
            $initial=0;
        } else {
            my @prefix=split(/ +/,$line);
            my $short=$prefix[1];
            $short=~s/://;
            my $long=$prefix[2];
            $long=~s/<//; $long=~s/>//;
            &log("Main","Extracted prefix '$short' -> '$long'");
            $prefixes{$short}=$long;
        }
    }

    ### process data section
    if (!$initial) {
        my $result=&parseLine($line,\%prefixes);
        my ($subject,$predicate,$object)=@$result;

        #&log("Main","Processing the following tuple");
        #&log("Main","subject=" . &toRDF($subject),1);
        #&log("Main","predicate=" . &toRDF($predicate),1);
        #&log("Main","object=" . &toRDF($object),1);
        
        # ignore triples with "bench:..." subject
        if ($subject->{"type"} eq "U" && $subject->{"prefix"} eq "bench") {
            &log("Main","Ignoring " . $subject->{"value"} . " typing (not required)");
        } elsif ($subject->{"type"} eq "U" && $subject->{"prefix"} eq "Misc") {
            &log("Main","Ignoring UnknownDocument typing (not required)");
        } else {

            # if subject is fresh URI (never been used before)
            my $id=&lookup($subject,\%publication_env,\%venue_env,
                            \%blank_env,\%doc_env,\%publication_types,
                            \%venue_types);
            if (defined($id)) {
                print OUT &createUpdateSQL($id,$subject,$predicate,$object,
                                            \%publication_env,\%venue_env,
                                            \%blank_env,\%publication_types,
                                            \%venue_types,$cnt,
                                            \%venue_to_doc,
                                            \%publication_to_doc) . "\n";
            } else {
                # ... then this should be an rdf:type declaration
                if ($predicate->{"prefix"} eq "rdf" &&
                    $predicate->{"value"} eq "type" &&
                    !($object->{"prefix"} eq "rdf" &&
                      $object->{"value"} eq "Bag")) {

                      if ($subject->{"prefix"} eq "misc" &&
                        $subject->{"value"} eq "UnknownDocument") {
                        &log("Main","Ignoring UnknownDocument");
                    } else {
                        # we insert this node in the environment
                        my $id=&insert($subject,$object,\%publication_env,
                                        \%venue_env,\%blank_env,\%doc_env,
                                        \%publication_types,\%venue_types,
                                        \%venue_to_doc,\%publication_to_doc);
                        print OUT &createInsertSQL($id,$subject,$predicate,$object,
                                                    \%venue_to_doc,
                                                    \%publication_to_doc) . "\n";
                    }
                } elsif ($predicate->{"prefix"} eq "rdf" &&
                    $predicate->{"value"} eq "type" &&
                    $object->{"prefix"} eq "rdf" &&
                    $object->{"value"} eq "Bag") {
                    # ignore this triple
                } elsif ($predicate->{"prefix"} eq "rdf" &&
                            $predicate->{"value"}=~/^_[1-9][0-9]*$/) {
                    print OUT &createReferenceSQL($subject,$predicate,$object,
                                                    \%publication_types,
                                                    \%publication_env) . "\n";
                } else {
                    &abort("Main","Expected type declaration for fresh URI");
                }

            }
        }
    }

    $cnt++;
    &log("Main","$cnt triples processed") if ($cnt%10000==0);
}

&log("Main","Processing $#delay delayed editors");
for (my $i=0;$i<=$#delay;$i++) {
    if ($delay[$i]=~/^INSERT INTO $tbl_editor VALUES \((.*),(.*)\);$/) {
        my $sql="INSERT INTO $tbl_editor VALUES ("
                . $1 . "," . $blank_env{$2} . ");";
        print OUT "$sql\n";
    } else {
        &abort("Main","Wrong format for delayed editor");
    }
}
sub createExitSQL {

    my $db_type=shift;

    my $sql="";
    if ($db_type==$TYPE_ORACLE) {
        $sql.="exit;";
    } elsif ($db_type==$TYPE_MONET) {
        $sql.="COMMIT;";
    }
    return $sql;
}

print OUT &createAlterTableSQL($tbl_editor,\%editor_fk1);
print OUT &createAlterTableSQL($tbl_editor,\%editor_fk2);
print OUT &createAlterTableSQL($tbl_author,\%author_fk1);
print OUT &createAlterTableSQL($tbl_author,\%author_fk2);
print OUT &createAlterTableSQL($tbl_abstract,\%abstract_fk1);
print OUT &createAlterTableSQL($tbl_references,\%references_fk1);
print OUT &createAlterTableSQL($tbl_references,\%references_fk2);
print OUT &createAlterTableSQL($tbl_publication,\%publication_fk1);
print OUT &createAlterTableSQL($tbl_publication,\%publication_fk2);
print OUT &createAlterTableSQL($tbl_publication,\%publication_fk3);
print OUT &createAlterTableSQL($tbl_venue,\%venue_fk1);
print OUT &createAlterTableSQL($tbl_venue,\%venue_fk2);
my %document_fk=(
    "DEST"        => $tbl_doc,
    "fk_document" => "ID");
print OUT &createAlterTableSQL("Document_homepage",\%document_fk);
print OUT &createAlterTableSQL("Document_seeAlso",\%document_fk);
my %publication_fk=(
	"DEST"           => $tbl_publication,
	"fk_publication" => "ID");
print OUT &createAlterTableSQL("Publication_cdrom",\%publication_fk);

my $exit_sql=&createExitSQL($db_type);
print OUT "$exit_sql";
close(OUT);

###############################################################################
########### Function library
sub createTableSQL {
    my $table_name=shift;
    my $fields=shift;
    my $pkey=shift;
    my $fkeys=shift;

    my $sql="CREATE TABLE $table_name (";

    my $i=0;
    foreach my $key (sort keys %$fields) {
        $sql.="," if ($i++>0);
        $sql.="$key " . $fields->{$key};
    }
    $sql.=",PRIMARY KEY($pkey)" if ($pkey);

#    if (defined($fkeys)) {
#        my @fkey_arr=@$fkeys;
#        for (my $i=0;$i<=$#fkey_arr;$i++) {
#            $sql.="," . &foreignKeySQL($fkey_arr[$i]);
#        }
#    }

    $sql.=");";
    return $sql;
}

sub createAlterTableSQL {
    my $table_name=shift;
    my $fkey=shift;

	return "ALTER TABLE $table_name ADD " , &foreignKeySQL($fkey) . ";\n";
}



sub foreignKeySQL {

    my $fk=shift;

    my $dest="";
    my $from="";
    my $to="";

    my $ctr=0;
    foreach my $key (keys %$fk) {


        if ($key eq "DEST") {
            $dest=$fk->{$key};
        } else {
            if ($ctr++>0) {
                $from.=",";
                $to.=",";
            }
            $from.=$key;
            $to.=$fk->{$key};
        }
    }

    &abort("foreignKeySQL","Foreign Key creation failed!")
        if ($dest eq "" || $from eq "" || $to eq "");

    # build result string
    return "FOREIGN KEY ($from) REFERENCES $dest($to)";        
}


sub createRepeatTables {
    my $base_table=shift;
    my $base_key=shift;
    my $base_key_datatype=shift;
    my $fields=shift;

    my $sql;
    my $ctr=0;
    foreach my $key (sort keys %$fields) {
        $sql.="\n" if ($ctr++>0);
        $sql.="CREATE TABLE $base_table" . "_$key ";
        $sql.="($base_key $base_key_datatype,$key $fields->{$key},";
        $sql.="PRIMARY KEY($base_key,$key));";
    }

    return $sql;
}


sub parseLine {
    my $line=shift;
    my $prefixes=shift;

    my @triple=split(/ +/,$line);
    my %subject=&parseElement($triple[0],$prefixes);
    my %predicate=&parseElement($triple[1],$prefixes);

    my $tail;
    for (my $i=2;$i<=$#triple;$i++) {
        $tail.=" " if ($i>2);
        $tail.=$triple[$i];
    }
    my %object=&parseElement($tail,$prefixes);

    my @result=(\%subject,\%predicate,\%object);
    return \@result;

}

sub parseElement {
    my $element=shift;
    my $prefixes=shift;

    my %elem;

    ################################################## parsing
    # (1) special case: UnknownDocument
    if ($element eq "<http://localhost/misc/UnknownDocument>") {
        $elem{"type"}="U";
        $elem{"prefix"}="misc";
        $elem{"value"}="UnknownDocument";

    # (2) URI of the form <http://.../value> 
    } elsif ($element=~/^<(.*)>$/) {

        my $match=0;
        $elem{"type"}="U";
        foreach my $key (keys %$prefixes) {
            my $cur_prefix=$prefixes->{$key};
            if ($element=~/^<$cur_prefix(.*)>$/) {
                $elem{"value"}=$1;
                $elem{"prefix"}=$key;
                $match++;
            }
        }
        if ($match!=1) {
            &abort("parseElement","'$element' matches $match prefixes");
        }

    # (3) Literal of the form "some text"^^xsd::datatype
    } elsif ($element=~/^"(.*)"\^\^xsd:(.*)$/) {
        $elem{"value"}=$1;
        $elem{"datatype"}=$2;
        $elem{"type"}="L";

    # (4) URI of the form prefix:value or blank node
    } else {
        my @spl=split(/:/,$element);
        if ($spl[0] eq "_") {
            $elem{"type"}="B";
        } else {
            $elem{"prefix"}=$spl[0];
            $elem{"type"}="U";
        }
        $elem{"value"}=$spl[1];
    }

    # some consistency checks
    # (a) type must be defined
    &abort("parseElement","No type defined") if (!defined($elem{"type"}));
    # (b) if prefix defined, it must be listed
    &abort("parseElement","Undefined prefix '" . $elem{"prefix"} . "'")
        if (!($elem{"prefix"} eq "") && !defined($prefixes->{$elem{"prefix"}})); 
    # (c) URI must have prefix and value defined, but no datatype
    if ($elem{"type"} eq "U") {
        &abort("parseElement","URI without prefix/value detected")
            if ($elem{"prefix"} eq "" || $elem{"value"} eq "");
        &abort("parseElement","URI with datatype detected")
            if (defined($elem{"datatype"}));
    }
    # (d) Blank node must not value defined, but neither prefix nor datatype
    if ($elem{"type"} eq "B") {
        &abort("parseElement","Blank node without value detected")
            if ($elem{"value"} eq "");
        &abort("parseElement","Blank node with datatype/prefix detected")
            if (defined($elem{"prefix"}) || defined($elem{"datatype"}));
    }
    # (e) Literal must have datatype defined, but no prefix 
    if ($elem{"type"} eq "L") {
        &abort("parseElement","Literal without data type detected") 
            if ($elem{"datatype"} eq "");            
        &abort("parseElement","Literal with prefix detected") 
            if (defined($elem{"prefix"}));            
    }

    # OK, all fine... so return the element
    return %elem;
}

sub abort {
    my $caller=shift;
    my $message=shift;
    print STDOUT "[$caller] ERROR: $message!\nABORTING!\n";
    exit(1);
}

sub log {
    my $caller=shift;
    my $message=shift;
    my $indent=shift;

    if ($indent) {
        print STDOUT "\t$message.\n";
    } else {
        print STDOUT "[$caller] $message.\n";
    }
}

sub toRDF {
    my $element=shift;

    my $elem;
    if ($element->{"type"} eq "U") {
        $elem=$element->{"prefix"} . ":" . $element->{"value"};
    } elsif ($element->{"type"} eq "B") {
        $elem="_:" . $element->{"value"};
    } elsif ($element->{"type"} eq "L") {
        $elem="\"" . $element->{"value"} . "\"^^xsd:"
                . $element->{"datatype"};
    }    

    return $elem;
}


sub lookup {
    my $elem=shift;
    my $publication_env=shift;
    my $venue_env=shift;
    my $blank_env=shift;
    my $doc_env=shift;
    my $publication_types=shift;
    my $venue_types=shift;

    if ($elem->{"type"} eq "B" ||
        ($elem->{"prefix"} eq "person" && $elem->{"value"} eq "Paul_Erdoes")) {
        return $blank_env->{$elem->{"value"}};
    } elsif ($elem->{"type"} eq "U") {
        if (defined($venue_types->{$elem->{"prefix"}})) {
            return $venue_env->{$elem->{"value"}};
        } elsif (defined($publication_types->{$elem->{"prefix"}})) {
            return $publication_env->{$elem->{"value"}};
        } elsif ($elem->{"prefix"} eq "misc" &&
                    $elem->{"value"} eq "UnknownDocument") {
            &log("lookup","Ignoring UnknownDocument");
            return undef;
        }
    }

    &abort("lookup","Unexpected element " . &toRDF($elem)); #failure
}

sub insert {

    my $subject=shift;
    my $object=shift;
    my $publication_env=shift;
    my $venue_env=shift;
    my $blank_env=shift;
    my $doc_env=shift;
    my $publication_types=shift;
    my $venue_types=shift;
    my $venue_to_doc=shift;
    my $publication_to_doc=shift;

    if (!($object->{"type"} eq "U")) {
        &abort("insert","Unexpected object type " . $object->{"type"});
    }

    if ($subject->{"type"} eq "B" || $subject->{"prefix"} eq "person") {

        # this is a person
        if ($object->{"prefix"} eq "foaf" && $object->{"value"} eq "Person") {
            $blank_env->{$subject->{"value"}}=$blank_env->{"ID_CTR"}++;
            return $blank_env->{$subject->{"value"}};
        } else {
            &abort("insert","Blank node must be a person");
        }
        
    } elsif ($subject->{"type"} eq "U") {

        # consistency check
        if (!($subject->{"prefix"} eq lc($object->{"value"}) &&
                $object->{"prefix"} eq "bench")) {
            &log("insert",&toRDF($subject) . " --- " . &toRDF($object) . "\n");
            &abort("insert","Subject prefix and object value must coincide");
        }

        # lookup fresh id in environment and insert
        if (defined($venue_types->{$subject->{"prefix"}})) {
            $venue_env->{$subject->{"value"}}=$venue_env->{"ID_CTR"}++;
            $doc_env->{$subject->{"value"}}=$doc_env->{"ID_CTR"}++;
            my $venue_id=$venue_env->{$subject->{"value"}};
            my $doc_id=$doc_env->{$subject->{"value"}};

            # add mapping to venue_to_doc hash
            $venue_to_doc->{$venue_id}=$doc_id;

            return $venue_id;
        } elsif (defined($publication_types->{$subject->{"prefix"}})) {
            $publication_env->{$subject->{"value"}}=$publication_env->{"ID_CTR"}++;
            $doc_env->{$subject->{"value"}}=$doc_env->{"ID_CTR"}++;
            my $publication_id=$publication_env->{$subject->{"value"}};
            my $doc_id=$doc_env->{$subject->{"value"}};

            # add mapping to venue_to_doc hash
            $publication_to_doc->{$publication_id}=$doc_id;

            return $publication_id;
        }
    } 

    &abort("insert","Insertion failed");
}

sub createInsertSQL {
    my $id=shift;
    my $subject=shift;
    my $predicate=shift;
    my $object=shift;
    my $venue_to_doc=shift;
    my $publication_to_doc=shift;

    my $sql;
    if ($subject->{"type"} eq "B" ||
        ($subject->{"type"} eq "U" && $subject->{"prefix"} eq "person")) {
        $sql="INSERT INTO $tbl_person VALUES ($id,NULL,'"
            . $subject->{"value"} . "');";
    } elsif (defined($venue_types{$subject->{"prefix"}})) {
        my $docid=$venue_to_doc->{$id};
        $sql.="INSERT INTO $tbl_doc VALUES ($docid,"
            . "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'"
            . $subject->{"value"} . "',NULL,NULL);\n";
        $sql.="INSERT INTO $tbl_venue VALUES ($id,$docid,"
            . $venue_types{$subject->{"prefix"}} . ");";
    } elsif (defined($publication_types{$subject->{"prefix"}})) {
        my $docid=$publication_to_doc->{$id};
        $sql.="INSERT INTO $tbl_doc VALUES ($docid,"
            . "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'"
            . $subject->{"value"} . "',NULL,NULL);\n";
        $sql.="INSERT INTO $tbl_publication VALUES ($id,NULL,$docid,"
            . $publication_types{$subject->{"prefix"}} . ",NULL,NULL);";
    } else {
        &abort("createInsertSQL","Undefined type '" . $subject->{"prefix"} . "'");
    }

    return $sql;
}

sub createUpdateSQL {
    my $id=shift;
    my $subject=shift;
    my $predicate=shift;
    my $object=shift;
    my $publication_env=shift;
    my $venue_env=shift;
    my $blank_env=shift;
    my $publication_types=shift;
    my $venue_types=shift;
    my $line_nr=shift;
    my $venue_to_doc=shift;
    my $publication_to_doc=shift;

    $object->{"value"}=~s/'/-/;

    my $sql;
    if ($subject->{"type"} eq "B" ||
        ($subject->{"type"} eq "U" && $subject->{"prefix"} eq "person")) {

        ### update name table
        if ($predicate->{"type"} eq "U" &&
            $predicate->{"prefix"} eq "foaf" &&
            $predicate->{"value"} eq "name") {
            $sql="UPDATE $tbl_person SET name='" . $object->{"value"} 
                . "' WHERE ID=$id;";
        } else {
            &log("createUpdateSQL","WARNING: duplicate definition of "
                . &toRDF($subject));
            &abort("createUpdateSQL","Unhandled blank predicate");
        }

    } elsif (defined($venue_types{$subject->{"prefix"}})) {

        # get corresponding parent id
        my $docid=$venue_to_doc->{$id};

        if ($predicate->{"type"} eq "U") {

            ###################################### Common fields
            ###################################### (single)
            ### swrc:address
            if ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "address") {
                $sql="UPDATE $tbl_doc SET address='"
                    . $object->{"value"} . "' WHERE ID=$docid;";
                
            ### swrc:note
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "note") {
                $sql="UPDATE $tbl_doc SET note='"
                    . $object->{"value"} . "' WHERE ID=$docid;";
                
            ### swrc:number
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                $predicate->{"value"} eq "number") {
                $sql="UPDATE $tbl_doc SET nr="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### dc:title
            } elsif ($predicate->{"prefix"} eq "dc" && 
                        $predicate->{"value"} eq "title") {
                $sql="UPDATE $tbl_doc SET title='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc: volume
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "volume") {
                $sql="UPDATE $tbl_doc SET volume="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### dcterms:issued
            } elsif ($predicate->{"prefix"} eq "dcterms" && 
                        $predicate->{"value"} eq "issued") {
                $sql="UPDATE $tbl_doc SET issued="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### bench:booktitle
            } elsif ($predicate->{"prefix"} eq "bench" && 
                        $predicate->{"value"} eq "booktitle") {
                $sql="UPDATE $tbl_doc SET booktitle='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc:isbn
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "isbn") {
                $sql="UPDATE $tbl_doc SET isbn='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### dc:publisher
            } elsif ($predicate->{"prefix"} eq "dc" && 
                        $predicate->{"value"} eq "publisher") {
                $sql="UPDATE $tbl_doc SET publisher='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc:series 
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "series") {
                $sql="UPDATE $tbl_doc SET series="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### swrc:month
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "month") {
                $sql="UPDATE $tbl_doc SET mnth="
                    . $object->{"value"} . " WHERE ID=$docid;";


            ###################################### Common fields
            ###################################### (multi)
            ### foaf:homepage
            } elsif ($predicate->{"prefix"} eq "foaf" && 
                        $predicate->{"value"} eq "homepage") {
                $sql="INSERT INTO $tbl_doc" . "_homepage VALUES ("
                    . "$docid,'" . $object->{"value"} . "');";

            ### rdfs:seeAlso
            } elsif ($predicate->{"prefix"} eq "rdfs" && 
                        $predicate->{"value"} eq "seeAlso") {
                $sql="INSERT INTO $tbl_doc" . "_seeAlso VALUES ("
                    . "$docid,'" . $object->{"value"} . "');";


            ###################################### Persons 
            ### swrc:editor
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "editor") {
                if ($object->{"prefix"} eq "person" &&
                        $object->{"value"} eq "Paul_Erdoes") {
                    $sql="INSERT INTO $tbl_editor VALUES ("
                        . $docid . ",0);"; # Paul Erdoes has ID 0
                } elsif (defined($blank_env->{$object->{"value"}})) {
                    $sql="INSERT INTO $tbl_editor VALUES ("
                        . $docid . ","
                        . $blank_env->{$object->{"value"}} . ");";
                } else {
                    push(@delay,"INSERT INTO $tbl_editor VALUES ($docid,"
                                . $object->{"value"} . ");");
                }

            } else {
                &abort("createUpdateSQL","Unhandled venue triple type "
                        . &toRDF($subject) . " - " . &toRDF($predicate)
                        . " - " . &toRDF($object));
            }
        } else {
            &abort("createUpdateSQL","Non-URI predicate detected");
        }

    } elsif (defined($publication_types{$subject->{"prefix"}})) {

        # get corresponding parent id
        my $docid=$publication_to_doc->{$id};

        if ($predicate->{"type"} eq "U") {

            ###################################### Common fields
            ###################################### (single)
            ### swrc:address
            if ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "address") {
                $sql="UPDATE $tbl_doc SET address='"
                    . $object->{"value"} . "' WHERE ID=$docid;";
                
            ### swrc:note
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "note") {
                $sql="UPDATE $tbl_doc SET note='"
                    . $object->{"value"} . "' WHERE ID=$docid;";
                
            ### swrc:number
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                $predicate->{"value"} eq "number") {
                $sql="UPDATE $tbl_doc SET nr="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### dc:title
            } elsif ($predicate->{"prefix"} eq "dc" && 
                        $predicate->{"value"} eq "title") {
                $sql="UPDATE $tbl_doc SET title='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc: volume
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "volume") {
                $sql="UPDATE $tbl_doc SET volume="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### dcterms:issued
            } elsif ($predicate->{"prefix"} eq "dcterms" && 
                        $predicate->{"value"} eq "issued") {
                $sql="UPDATE $tbl_doc SET issued="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### bench:booktitle
            } elsif ($predicate->{"prefix"} eq "bench" && 
                        $predicate->{"value"} eq "booktitle") {
                $sql="UPDATE $tbl_doc SET booktitle='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc:isbn
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "isbn") {
                $sql="UPDATE $tbl_doc SET isbn='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### dc:publisher
            } elsif ($predicate->{"prefix"} eq "dc" && 
                        $predicate->{"value"} eq "publisher") {
                $sql="UPDATE $tbl_doc SET publisher='"
                    . $object->{"value"} . "' WHERE ID=$docid;";

            ### swrc:series 
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "series") {
                $sql="UPDATE $tbl_doc SET series="
                    . $object->{"value"} . " WHERE ID=$docid;";

            ### swrc:month
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "month") {
                $sql="UPDATE $tbl_doc SET mnth="
                    . $object->{"value"} . " WHERE ID=$docid;";


            ###################################### Common fields
            ###################################### (multi)
            ### foaf:homepage
            } elsif ($predicate->{"prefix"} eq "foaf" && 
                        $predicate->{"value"} eq "homepage") {
                $sql="INSERT INTO $tbl_doc" . "_homepage VALUES ("
                    . "$docid,'" . $object->{"value"} . "');";

            ### rdfs:seeAlso
            } elsif ($predicate->{"prefix"} eq "rdfs" && 
                        $predicate->{"value"} eq "seeAlso") {
                $sql="INSERT INTO $tbl_doc" . "_seeAlso VALUES ("
                    . "$docid,'" . $object->{"value"} . "');";

                
            ###################################### Individual fields 
            ###################################### (single)
            ### swrc:pages
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "pages") {
                $sql="UPDATE $tbl_publication SET pages="
                    . $object->{"value"} . " WHERE ID=$id;";
                
            ### swrc:chapter
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "chapter") {
                $sql="UPDATE $tbl_publication SET chapter="
                    . $object->{"value"} . " WHERE ID=$id;";
                
            ### swrc:journal
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "journal") {
                $sql="UPDATE $tbl_publication SET fk_venue="
                    . $venue_env->{$object->{"value"}}
                    . " WHERE ID=$id;";

            ### dcterms:partOf
            } elsif ($predicate->{"prefix"} eq "dcterms" && 
                        $predicate->{"value"} eq "partOf") {
                # this is exactly the same as swrc:journal
                # (but links inproceedings to proceedins
                #  instead of articles to journals)
                $sql="UPDATE $tbl_publication SET fk_venue="
                    . $venue_env->{$object->{"value"}}
                    . " WHERE ID=$id;";

            ###################################### Individual fields 
            ###################################### (single)
            ### bench:cdrom
            } elsif ($predicate->{"prefix"} eq "bench" && 
                        $predicate->{"value"} eq "cdrom") {
                $sql="INSERT INTO $tbl_publication" . "_cdrom VALUES "
                    . "($id,'" . $object->{"value"} . "');";

            ###################################### Persons, 
            ###################################### References, 
            ###################################### Abstract
            ### dc:creator
            } elsif ($predicate->{"prefix"} eq "dc" && 
                        $predicate->{"value"} eq "creator") {
                $sql="INSERT INTO $tbl_author VALUES ("
                    . $blank_env->{$object->{"value"}} . ",$id);";

            ###################################### Persons 
            ### swrc:editor
            } elsif ($predicate->{"prefix"} eq "swrc" && 
                        $predicate->{"value"} eq "editor") {
                if ($object->{"prefix"} eq "person" &&
                        $object->{"value"} eq "Paul_Erdoes") {
                    $sql="INSERT INTO $tbl_editor VALUES ("
                        . $docid . ",0);"; # Paul Erdoes has ID 0
                } elsif (defined($blank_env->{$object->{"value"}})) {
                    $sql="INSERT INTO $tbl_editor VALUES ("
                        . $docid . ","
                        . $blank_env->{$object->{"value"}} . ");";
                } else {
                    push(@delay,"INSERT INTO $tbl_editor VALUES ($docid,"
                                . $object->{"value"} . ");");
                }

            ### dcterms:references
            } elsif ($predicate->{"prefix"} eq "dcterms" && 
                        $predicate->{"value"} eq "references") {

                $last_refblank_id=$publication_env->{$subject->{"value"}};
                $last_refblank=&toRDF($object);

            ### bench:abstract 
            } elsif ($predicate->{"prefix"} eq "bench" && 
                        $predicate->{"value"} eq "abstract") {
                $sql="INSERT INTO $tbl_abstract VALUES ($id,'"
                    . $object->{"value"} . "');";

            } else {
                &abort("createUpdateSQL","Unhandled publication triple type "
                        . &toRDF($subject) . " - " . &toRDF($predicate)
                        . " - " . &toRDF($object));
            }
        } else {
            &abort("createUpdateSQL","Non-URI predicate detected");
        }
    } else {
        &abort("createUpdateSQL","Undefined type '" . $subject->{"prefix"} . "'");
    }
    return $sql;
}    


sub createReferenceSQL {

    my $subject=shift;
    my $predicate=shift;
    my $object=shift;
    my $publication_types=shift;
    my $publication_env=shift;

    my $sql;
    ### consistency check                
    if (&toRDF($subject) eq $last_refblank) {
    
           if ($object->{"value"} eq "UnknownDocument") {
               $sql="INSERT INTO $tbl_references VALUES ("
                   . "$last_refblank_id,NULL);";
        } else {
               $sql="INSERT INTO $tbl_references VALUES ("
                 . "$last_refblank_id,";
               if (defined($publication_types->{$object->{"prefix"}})) {
                   $sql.=$publication_env->{$object->{"value"}};
               } else {
                   &abort("createReferenceSQL","Reference to non-publication-type");
               }
               $sql.=");";
           }
    } else {
        &abort("createReferenceSQL","Inconstistent reference");
    }
    return $sql;
}
