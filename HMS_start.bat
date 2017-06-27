E:
cd "E:\Dropbox\Private\PC-Synchronisation\WorkSpace\HomeManagementSystem"
call HMS_stop.exe
rem javaw -jar "..\a runnable jars container\HMS.jar" standpc > "E:\Dropbox\Private\PC-Synchronisation\WorkSpace\HomeManagementSystem\log.log" 2>&1
javaw -jar "..\a runnable jars container\HMS.jar" standpc 2>&1