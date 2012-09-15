--########### publications
create or replace view v_pub_article as
select 

d.*, p.pages as pages,  d_hp.homepage as homepage, d_sa.seealso as seealso , p_cd.cdrom as cdrom, ab.txt as abstract, ven_doc.stringid as venuedocstringid

from  publication p join document d  on p.fk_document = d.id 

left join venue ven on p.fk_venue = ven.id left join document ven_doc on ven_doc.id =   ven.fk_document

left join publication_cdrom p_cd on p_cd.fk_publication = p.id left join document_homepage d_hp on d_hp.fk_document=  d.id left join document_seealso d_sa on d.id = d_sa.fk_document left join abstract ab on p.id = ab.fk_publication 

where p.fk_publication_type = 0;


create or replace view v_pub_inproc as
select 

d.*, p.pages as pages,  d_hp.homepage as homepage, d_sa.seealso as seealso , p_cd.cdrom as cdrom, ab.txt as abstract, ven_doc.stringid as venuedocstringid

from  publication p join document d  on p.fk_document = d.id 

left join venue ven on p.fk_venue = ven.id left join document ven_doc on ven_doc.id =   ven.fk_document

left join publication_cdrom p_cd on p_cd.fk_publication = p.id left join document_homepage d_hp on d_hp.fk_document=  d.id left join document_seealso d_sa on d.id = d_sa.fk_document left join abstract ab on p.id = ab.fk_publication 

where p.fk_publication_type = 1;


create or replace view v_pub_incoll as
select 

d.*, p.pages as pages,  d_hp.homepage as homepage, d_sa.seealso as seealso , p_cd.cdrom as cdrom, ab.txt as abstract

from  publication p join document d  on p.fk_document = d.id 

left join publication_cdrom p_cd on p_cd.fk_publication = p.id left join document_homepage d_hp on d_hp.fk_document=  d.id left join document_seealso d_sa on d.id = d_sa.fk_document left join abstract ab on p.id = ab.fk_publication 

where p.fk_publication_type = 3;




--######### venues
create or replace view v_venue_proceedings as
select 

d.*, d_hp.homepage

from venue v join document d on v.fk_document = d.id

left join document_homepage d_hp on d_hp.fk_document=  d.id left join document_seealso d_sa on d.id = d_sa.fk_document 
where v.fk_venue_type = 1 ;

create or replace view v_venue_journal as
select 

d.*, d_hp.homepage

from venue v join document d on v.fk_document = d.id

left join document_homepage d_hp on d_hp.fk_document=  d.id left join document_seealso d_sa on d.id = d_sa.fk_document 
where v.fk_venue_type = 0 ;


--######### editors
create or replace view v_editor as

select 

CASE  v.fk_venue_type WHEN   0 then 'journals'
 	WHEN     1 then 'proceedings'
 	ELSE 'other'
 			END as resourcepart,

d.*, vt.name as venuetypename, pers.stringid as personid

from venue v join venuetype vt on v.fk_venue_type = vt.id join document d on v.fk_document = d.id

join editor e on e.fk_document = d.id join person pers on e.fk_person = pers.id;

--######### authors

create or replace view v_author as
select 

CASE  fk_publication_type WHEN   0 then 'articles'
 	WHEN     1 then 'inprocs'
 	WHEN   3 then 'incolls'
 	ELSE 'other'
 			END as resourcepart,

d.stringid, pers.stringid as personid

from  publication p join document d  on p.fk_document = d.id
join author a on p.id = a.fk_publication join person pers on a.fk_person = pers.id;


--######persons
create or replace view v_person as 
select * from person;




--######### references
create or replace view v_reference as
select
CASE  p.fk_publication_type WHEN   0 then 'articles'
 	WHEN     1 then 'inprocs'
 	WHEN   3 then 'incolls'
 	ELSE 'other'
 			END as from_resourcepart,


CASE WHEN  p2.fk_publication_type =  0 then 'articles'
 	WHEN  p2.fk_publication_type =   1 then 'inprocs'
 	WHEN  p2.fk_publication_type = 3 then 'incolls'
 	WHEN  p2.fk_publication_type is null then 'misc'
 	ELSE 'other'
 			END as to_resourcepart,


d.stringid as from_stringid, 


CASE WHEN d2.stringid IS NOT NULL THEN d2.stringid
     ELSE 'UnknownDocument'
     END 
 as to_stringid,

ref.refno

from  publication p join document d  on p.fk_document = d.id  join publicationtype pt on pt.id = p.fk_publication_type 
join reference ref on ref.fk_from = p.id 
left join publication p2 on p2.id = ref.fk_to left join document d2  on p2.fk_document = d2.id  left join publicationtype pt2 on pt2.id = p2.fk_publication_type 


