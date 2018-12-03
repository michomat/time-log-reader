@echo off

set limit=14

java -jar  de.mik.timelog-0.0.1-SNAPSHOT-jar-with-dependencies.jar -log "C:/time/log.txt" -limit %limit%

pause
