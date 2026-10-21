@echo off
REM ============================================================================
REM SkillBarter Build Script for Windows
REM Compiles Java source files and packages the WAR without Maven or Gradle.
REM ============================================================================

echo [1/3] Setting up directories...
if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"
if not exist "WebContent\WEB-INF\lib" mkdir "WebContent\WEB-INF\lib"

echo [2/3] Compiling Java classes...
if exist "lib\postgresql-42.7.3.jar" (
    copy /Y "lib\postgresql-42.7.3.jar" "WebContent\WEB-INF\lib\" >nul
)

javac -encoding UTF-8 -cp "lib\*;WebContent\WEB-INF\lib\*" -d WebContent\WEB-INF\classes src\model\*.java src\interfaces\*.java src\util\*.java src\dao\*.java src\service\*.java src\servlet\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

echo [3/3] Creating SkillBarter.war package...
cd WebContent
jar -cvf ..\SkillBarter.war * >nul
cd ..

echo ============================================================================
echo [SUCCESS] Build completed successfully!
echo Generated package: SkillBarter.war
echo To deploy, copy SkillBarter.war into your Apache Tomcat 'webapps' directory.
echo ============================================================================
