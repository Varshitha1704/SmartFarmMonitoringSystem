@echo off
javac *.java
if errorlevel 1 exit /b 1
java Login
