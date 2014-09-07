-- -----------
-- 
-- initialization of the database contents
-- 
-- -----------

insert into preference_type (name, description) values (
  'shareUsername', 'allow sharing of users the login name'
);
insert into preference_type (name, description) values (
  'shareName', 'allow sharing of the users full name'
);
insert into preference_type (name, description) values (
  'shareEmail', 'allow sharing of users email address'
);
insert into preference_type (name, description) values (
  'shareInstitution', 
  'allow sharing of the name of the institution the user is affiliated with'
);
insert into preference_type (name, description) values (
  'sharePhone', 'allow sharing of the users phone number'
);
insert into preference_type (name, description) values (
  'shareCountry', 'allow sharing of the users home country'
);
insert into preference_type (name, description) values (
  'shareCredentials', 'allow sharing of temporary X.509 credentials'
);
insert into preference_type (name, description) values (
  'alwaysConfirm', 'always ask for confirmation before sharing information'
);
