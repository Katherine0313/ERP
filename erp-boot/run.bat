@echo off
chcp 65001 >nul
cd /d "%~dp0"

if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java"
) else (
    set "JAVA=java"
)

if exist "mvnw.cmd" (
    set "MAVEN=.\mvnw.cmd"
) else (
    set "MAVEN=mvn"
)

set "JVM_OPTS=--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

echo ========================================
echo   ERP-Boot Starting
echo   Maven: %MAVEN%
echo   JVM:  %JVM_OPTS%
echo   Port: 8082
echo ========================================
echo.

%MAVEN% spring-boot:run "-Dspring-boot.run.jvmArguments=%JVM_OPTS%"
pause
