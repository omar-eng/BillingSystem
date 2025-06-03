@echo off
cd /d %~dp0
echo Running Billing System...
java -jar Billing\target\Billing-1.0-SNAPSHOT-jar-with-dependencies.jar
pause
