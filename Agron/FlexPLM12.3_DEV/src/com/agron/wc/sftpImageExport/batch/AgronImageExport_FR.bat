@ECHO OFF
rem 
rem
cmd.exe /C

rem 
rem
cd ..

set WT_HOME=D:\PTC\Windchill_11.1\Windchill
set LOADER_HOME=D:\PTC\Windchill_11.1\Windchill\codebase
 
REM ######Date format - MM-dd-yyyy:hh:mm:ss a
REM set STARTDATE=06-26-2020:11:35:56 AM
REM set STARTDATE=07-25-2019:11:35:56 AM
REM set STARTDATE=06-26-2020:03:35:56 AM
REM set STARTDATE=08-31-2017:03:35:56 AM
REM ENDDATE=08-31-2020:01:14:23 PM

set CLASSPATH=%JAVA_HOME%;%LOADER_HOME%;D:\PTC\Windchill_11.1\Windchill\codebase\com\agron\jar\jsch-0.1.55.jar;D:\PTC\Windchill_11.1\Windchill\codebase\com\agron\jar\ojdbc7.jar;%WT_HOME%\lib\log4j.jar;%WT_HOME%\lib\sqljdbc4.jar;%WT_HOME%\codebase;%WT_HOME%\codebase\WEB-INF\lib\*;%WT_HOME%\lib\*;
 
echo %CLASSPATH%
java -Xms512m -Xmx1024m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:${DIR}/logs/GC.logs com.agron.wc.sftpImageExport.AgronSKUImageExport FR "%STARTDATE%" "%ENDDATE%"

echo Call successfully!!
pause

:exit
exit	