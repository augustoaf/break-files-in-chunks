@echo off
REM Package the project using Maven
cd %~dp0
mvn clean package
pause