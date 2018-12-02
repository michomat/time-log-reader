@echo off

set log="D:/test/log.txt"
set hour=%time:~0,2%
set min=%time:~3,2%

echo "End: %date% %hour%:%min%" >> "%log%"