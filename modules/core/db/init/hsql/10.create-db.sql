-- begin SEC_USER
alter table SEC_USER add column TOTP_SECRET varchar(255) ^
alter table SEC_USER add column TOTP_VALIDATION_CODE integer ^
alter table SEC_USER add column DTYPE varchar(100) ^
update SEC_USER set DTYPE = 'demo$ExtUser' where DTYPE is null ^
-- end SEC_USER
