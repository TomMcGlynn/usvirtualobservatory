-- Useful MySQL queries for Purse

-- list the last 30 users
select user_id, status_id, user_name, email, creation_time from user_table order by user_id desc limit 30;