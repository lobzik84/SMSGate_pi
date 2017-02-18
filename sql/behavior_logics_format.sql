
SELECT 
p.description as parameter_name, 
c.name as condititon_name,
case when(a.condition_state=1) then 'перешёл в 1' when (a.condition_state=0)  then 'перешёл в 0' else 'всегда' end as transfer,
a.module as module,
a.event_name as event_name,
a.notification_text as text,
a.severity as severity,
case when (a.box_mode='ARMED') then 'охрана' when (a.box_mode='IDLE') then 'контроль' else 'всегда' end as mode
 FROM conditions c
left join parameters p on p.id = c.parameter_id
inner join actions a on a.condition_id = c.id 
where a.box_mode ='IDLE'
order by (CASE WHEN p.id IS NULL then 1 ELSE 0 END), p.id, c.alias, (case when a.condition_state=1 then 0  when a.condition_state=0 then 1 else 2 end)