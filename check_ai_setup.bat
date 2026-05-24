@echo off
echo Checking configured Python command...
for /f "tokens=1,* delims==" %%A in ('findstr /b "python.command=" config.properties') do set PYTHON_CMD=%%B
if "%PYTHON_CMD%"=="" set PYTHON_CMD=python
echo Using: %PYTHON_CMD%
%PYTHON_CMD% AIModel\check_ai_setup.py
