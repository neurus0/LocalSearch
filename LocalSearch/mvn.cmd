@echo off
setlocal
if "%JAVA_HOME%"=="" (
    for /f "delims=" %%i in ('where java 2^>nul') do (
        set "JAVA_EXE=%%i"
        goto found_java
    )
)
:found_java
if not "%JAVA_EXE%"=="" if "%JAVA_HOME%"=="" (
    for %%d in ("%JAVA_EXE%\..\..") do set "JAVA_HOME=%%~fd"
)
"%~dp0mvnw.cmd" %*
endlocal
