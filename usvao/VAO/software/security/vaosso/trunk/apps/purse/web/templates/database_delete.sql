-- Clean up before running tests
delete from user_roles_table;
delete from user_table;
delete from status_table;
delete from role_table;
drop sequence status_id_seq;
drop sequence role_id_seq;
create sequence status_id_seq;
create sequence role_id_seq;