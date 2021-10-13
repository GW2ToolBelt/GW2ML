@echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

for /F "tokens=*" %%A in (%1) do %%A

:end
@rem End local scope for the variables with windows NT shell
if not ERRORLEVEL 1 goto mainEnd

:fail
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal