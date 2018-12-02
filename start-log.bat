@echo off

set log="D:/test/log.txt"
set hour=%time:~0,2%
set min=%time:~3,2%

echo "Start: %date% %hour%:%min%" >> "%log%"