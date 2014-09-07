-- List user preferences (skipping user & portal defaults)
select pref.id pref_id, user_name, user_id, value, type.name "pref type", portal.name "portal name", url
  from user_preference pref, user_table user, preference_type type, portal
  where pref.user_table_id=user.user_id and pref.preference_type_id=type.id and pref.portal_id=portal.id
  order by user_name, url, pref_id;

-- List of portal default preferences
select pref.id, portal.id, type.name "pref type", value, portal.name "portal name", url
  from user_preference pref, preference_type type, portal
  where pref.preference_type_id=type.id and pref.portal_id=portal.id and pref.user_table_id is null
  order by url;

-- List current valid session tokens
select id, user_id, user_name, expire_time, user_session.token, host_address
   from user_session left join user_table on user_session.user_table_id=user_table.user_id
   where expire_time > now();
