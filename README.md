# time-log-reader

Little helper scripts to log start-up and shutdown times of your windows machine

## Time Log
The start-log.bat will append date and time to a defined log file
The end-log.bat will append date and time to a defined log file 


## Create Winows Group Policies
Run gpedit.msc to open the Group Policy Editor,
Navigate to Computer Configuration | Windows Settings | Scripts Startup. and add start-log.bat
Navigate to Computer Configuration | Windows Settings | Scripts Shutdown. and add end-log.bat


## Log-reader
If start and shutdown scripts have been configured each start and shutdown will log a timestamp.
With the log-reader.bat you can view the log



