delete from logs;
delete from sensors_data;
delete from sms_inbox;
delete from sms_outbox;
update settings set `value`='' where `name`='NotificationsEmail';

