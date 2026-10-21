# ==============================================================================
# SkillBarter Build Script for PowerShell
# Compiles Java classes and packages the WAR without Maven or Gradle.
# ==============================================================================

Write-Host "[1/3] Setting up directories..." -ForegroundColor Cyan
if (!(Test-Path -Path "WebContent\WEB-INF\classes")) {
    New-Item -ItemType Directory -Path "WebContent\WEB-INF\classes" | Out-Null
}
if (!(Test-Path -Path "WebContent\WEB-INF\lib")) {
    New-Item -ItemType Directory -Path "WebContent\WEB-INF\lib" | Out-Null
}

Write-Host "[2/3] Compiling Java classes..." -ForegroundColor Cyan
if (Test-Path -Path "lib\postgresql-42.7.3.jar") {
    Copy-Item "lib\postgresql-42.7.3.jar" "WebContent\WEB-INF\lib\postgresql-42.7.3.jar" -Force
}

$compileCmd = 'javac -encoding UTF-8 -cp "lib/*;WebContent/WEB-INF/lib/*" -d WebContent/WEB-INF/classes src/model/*.java src/interfaces/*.java src/util/*.java src/dao/*.java src/service/*.java src/servlet/*.java'
Invoke-Expression $compileCmd

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[3/3] Creating SkillBarter.war package..." -ForegroundColor Cyan
Push-Location WebContent
jar -cvf ..\SkillBarter.war * | Out-Null
Pop-Location

Write-Host "============================================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Build completed successfully!" -ForegroundColor Green
Write-Host "Generated package: SkillBarter.war" -ForegroundColor Green
Write-Host "Deploy by copying SkillBarter.war to your Tomcat webapps/ directory." -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Green
