@echo off
REM For Windows, use an installed Gradle 8.9 or Android Studio's bundled Gradle installation.
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle 8.9 is required. Use Android Studio or install Gradle first.
  exit /b 1
)
gradle %*
