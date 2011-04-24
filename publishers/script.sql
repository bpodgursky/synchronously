drop table daily_readings;

CREATE TABLE daily_readings (time DATETIME, reading FLOAT);

DROP FUNCTION IF EXISTS lib_mysqludf_sys_info;
DROP FUNCTION IF EXISTS sys_get;
DROP FUNCTION IF EXISTS sys_set;
DROP FUNCTION IF EXISTS sys_exec;
DROP FUNCTION IF EXISTS sys_eval;

CREATE FUNCTION lib_mysqludf_sys_info RETURNS string SONAME 'lib_mysqludf_sys.so';
CREATE FUNCTION sys_get RETURNS string SONAME 'lib_mysqludf_sys.so';
CREATE FUNCTION sys_set RETURNS int SONAME 'lib_mysqludf_sys.so';
CREATE FUNCTION sys_exec RETURNS int SONAME 'lib_mysqludf_sys.so';
CREATE FUNCTION sys_eval RETURNS string SONAME 'lib_mysqludf_sys.so';

drop procedure if exists udfwrapper_sp;

DELIMITER $$
CREATE PROCEDURE udfwrapper_sp(IN data text)
BEGIN
select sys_eval(concat('echo ', data, ' >> alog.txt')) into @output;
END$$

delimiter |
CREATE TRIGGER push_update AFTER INSERT ON daily_readings
FOR EACH ROW BEGIN
call udfwrapper_sp(concat(NEW.time, ' ', NEW.reading));
END;
|

delimiter ;
