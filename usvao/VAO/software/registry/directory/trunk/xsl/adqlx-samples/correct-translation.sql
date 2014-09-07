-- File: adqlx-samples/allsiaservices.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN dbo.capability ON (resource.pkey = capability.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (standard_id = 'ivo://ivoa.net/std/SIA'))


-- File: adqlx-samples/big.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value capability_maxRecords 
                     FROM res_detail 
                     WHERE detail_utype='vor:capability.maxRecords') AS rd0
               ON (resource.pkey = rd0.rkey)
         INNER JOIN (SELECT rkey, role_name publisher_name, 
                                  role_ivoid publisher_ivoid 
                     FROM res_role 
                     WHERE base_utype='vor:resource.curation.publisher') AS rl0
               ON (resource.pkey = rl0.rkey)
         INNER JOIN dbo.capability ON (resource.pkey = capability.rkey)
         INNER JOIN dbo.subject ON (resource.pkey = subject.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (standard_id = 'ivo://ivoa.net/std/ConeSearch' AND rl0.publisher_name LIKE '%CDS%' AND CAST(rd0.capability_maxRecords AS int) > 1000 AND ((subject LIKE '%lenticular%') OR (res_description LIKE '%lenticular%')) AND ((waveband LIKE '%optical%') OR (waveband LIKE '%radio%'))))


-- File: adqlx-samples/detailequals.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value resource_managingOrg_ivoid 
                     FROM res_detail 
                     WHERE detail_utype='vor:resource.managingOrg.ivoid') AS rd0
               ON (resource.pkey = rd0.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (rd0.resource_managingOrg_ivoid = 'stsci'))


-- File: adqlx-samples/detaillike.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value resource_managingOrg_ivoid 
                     FROM res_detail 
                     WHERE detail_utype='vor:resource.managingOrg.ivoid') AS rd0
               ON (resource.pkey = rd0.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (rd0.resource_managingOrg_ivoid LIKE '%stsci%'))


-- File: adqlx-samples/error1_badColName.xml:
-- ERROR: resource metadatum name not supported: capability/goober


-- File: adqlx-samples/error2_emptyColName.xml:
-- ERROR: Missing resource metadatum name (@xpathName)


-- File: adqlx-samples/error3_missingColName.xml:
-- ERROR: Unspecified column name (missing @xpathName?)


-- File: adqlx-samples/findcaptypelike.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN dbo.Capability ON (resource.pkey = Capability.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (cap_type LIKE '%Search%'))


-- File: adqlx-samples/findrestypeeq.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         WHERE dbo.resource.rstat=1 AND 
               (res_type = 'Registry'))


-- File: adqlx-samples/findrestype.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         WHERE dbo.resource.rstat=1 AND 
               (res_type LIKE 'Registry'))


-- File: adqlx-samples/findsrchreg.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN dbo.Capability ON (resource.pkey = Capability.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (cap_type = 'Search'))


-- File: adqlx-samples/idequals.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         WHERE dbo.resource.rstat=1 AND 
               (resource.ivoid = 'ivo://ivoa.net/rofr'))


-- File: adqlx-samples/join.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN dbo.subject ON (resource.pkey = subject.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (res_title LIKE '%lenticular%' AND subject = 'galaxies'))


-- File: adqlx-samples/multidetails2.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value resource_managingOrg_ivoid 
                     FROM res_detail 
                     WHERE detail_utype='vor:resource.managingOrg.ivoid') AS rd1
               ON (resource.pkey = rd1.rkey)
         WHERE dbo.resource.rstat=1 AND 
               ((rd1.resource_managingOrg_ivoid LIKE '%stsci%') OR (rd1.resource_managingOrg_ivoid LIKE '%nasa%')))


-- File: adqlx-samples/multidetails.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value resource_managingOrg_ivoid 
                     FROM res_detail 
                     WHERE detail_utype='vor:resource.managingOrg.ivoid') AS rd0
               ON (resource.pkey = rd0.rkey)
         INNER JOIN (SELECT rkey, detail_value capability_optionalProtocol 
                     FROM res_detail 
                     WHERE detail_utype='vor:capability.optionalProtocol') AS rd1
               ON (resource.pkey = rd1.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (rd0.resource_managingOrg_ivoid LIKE '%stsci%' AND rd1.capability_optionalProtocol = 'xquery'))


-- File: adqlx-samples/multiroles.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, role_name publisher_name, 
                                  role_ivoid publisher_ivoid 
                     FROM res_role 
                     WHERE base_utype='vor:resource.curation.publisher') AS rl0
               ON (resource.pkey = rl0.rkey)
         INNER JOIN (SELECT rkey, role_name creator_name, 
                                  role_ivoid creator_ivoid, logo creator_logo 
                     FROM res_role 
                     WHERE base_utype='vor:resource.curation.creator') AS rl1
               ON (resource.pkey = rl1.rkey)
         INNER JOIN (SELECT rkey, role_name contact_name, 
                                  role_ivoid contact_ivoid, email contact_email 
                     FROM res_role 
                     WHERE base_utype='vor:resource.curation.contact') AS rl2
               ON (resource.pkey = rl2.rkey)
         WHERE dbo.resource.rstat=1 AND 
               ((rl0.publisher_ivoid LIKE '%nasa%' AND rl1.creator_logo LIKE 'http%') OR (rl2.contact_email LIKE '%nasa%')))


-- File: adqlx-samples/numericdetail.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value capability_maxRecords 
                     FROM res_detail 
                     WHERE detail_utype='vor:capability.maxRecords') AS rd0
               ON (resource.pkey = rd0.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (CAST(rd0.capability_maxRecords AS int) > 1000))


-- File: adqlx-samples/siasabout.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN dbo.capability ON (resource.pkey = capability.rkey)
         INNER JOIN dbo.subject ON (resource.pkey = subject.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (standard_id = 'ivo://ivoa.net/std/SIA' AND ((subject LIKE '%lenticular%') OR (res_description LIKE '%lenticular%')) AND ((waveband LIKE '%optical%') OR (waveband LIKE '%radio%'))))


-- File: adqlx-samples/titlelike.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         WHERE dbo.resource.rstat=1 AND 
               (res_title LIKE '%nearby%'))


-- File: adqlx-samples/withdetail.xml:
SELECT xml FROM resource WHERE pkey in 
  (SELECT DISTINCT resource.pkey 
         FROM dbo.resource 
         INNER JOIN (SELECT rkey, detail_value resource_managingOrg_ivoid 
                     FROM res_detail 
                     WHERE detail_utype='vor:resource.managingOrg.ivoid') AS rd0
               ON (resource.pkey = rd0.rkey)
         WHERE dbo.resource.rstat=1 AND 
               (res_title LIKE '%galaxy%' AND rd0.resource_managingOrg_ivoid = 'nasa.gov'))


