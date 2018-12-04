# time-log-reader

Little helper scripts to log start-up and shutdown times of your windows machine

## Log Shutdown and Startup Times 

### Scripts
- The start-log.bat will append date and time to a defined log file
- The end-log.bat will append date and time to a defined log file 

### Installation
Run gpedit.msc to open the Group Policy Editor,
Navigate to Computer Configuration | Windows Settings | Scripts Startup. and add start-log.bat
Navigate to Computer Configuration | Windows Settings | Scripts Shutdown. and add end-log.bat


## Log-reader
In order to get a overview of a limited amount of log entries run the 
log-reader.bat.



