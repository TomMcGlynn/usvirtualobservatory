-- -----------
-- 
-- initialization of the database contents
-- 
-- -----------

insert into preference_type (name, description) values (
  'share_username', 'allow sharing of users the login name'
);
insert into preference_type (name, description) values (
  'share_name', 'allow sharing of the users full name'
);
insert into preference_type (name, description) values (
  'share_email', 'allow sharing of users email address'
);
insert into preference_type (name, description) values (
  'share_institution', 
  'allow sharing of the name of the institution the user is affiliated with'
);
insert into preference_type (name, description) values (
  'share_phone', 'allow sharing of the users phone number'
);
insert into preference_type (name, description) values (
  'share_country', 'allow sharing of the users home country'
);
insert into preference_type (name, description) values (
  'share_credentials', 'allow sharing of temporary X.509 credentials'
);
insert into preference_type (name, description) values (
  'always_confirm', 'always ask for confirmation before sharing information'
);

insert into portal (name, description, url, curator_id, status) values (
  'default',
   'A non-existent portal for identifying default privacy settings for unrecognized portals', 
   'xxxx', 3, 2);
insert into portal (name, description, url, curator_id, status) values (
  'recognized',
   'A non-existent portal for identifying default privacy settings for recognized portals', 
   'xxxx', 3, 2);

-- add system and other disallowed users
select (@sys:=user_id) from user_table where user_name='system';

-- unrecognized default preferences
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 1, "true"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 2, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 3, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 4, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 5, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 6, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 7, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 1, 8, "true"
);

-- recognized default preferences
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 1, "true"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 2, "true"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 3, "true"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 4, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 5, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 6, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 7, "false"
);
insert into user_preference (
   user_table_id, portal_id, preference_type_id, value
) values (
   @sys, 2, 8, "true"
);
