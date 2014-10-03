
create table tapjobstable (
  jobid        varchar(128),
  jobstatus    varchar(32),
  starttime    bigint,
  endtime      bigint,
  duration     bigint,
  destruction  bigint,
  lang         varchar(16),
  query        varchar(4096),
  adql         varchar(4096),
  resultFormat varchar(32),
  request      varchar(64),
  maxrec       integer,
  error        varchar(1024),
  uploadparam  varchar(128),
  runid        varchar(64),
  owner        varchar(1024),

  primary key (jobid)
)
;
