@echo off
REM Build signed release APK - lan sau chi can chay file nay
REM Key: naq-release.jks (da cau hinh trong app/build.gradle + keystore.properties)
setlocal
set JAVA_HOME=E:\PROGRAM\AndroidStudio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1/3] Build release...
call "%~dp0gradlew.bat" assembleRelease
if errorlevel 1 (
  echo BUILD THAT BAI!
  exit /b 1
)

set APK=%~dp0app\build\outputs\apk\release\app-release.apk
echo.
echo [2/3] Verify chu ky...
call "E:\PROGRAM\AndroidStudioSDK\build-tools\34.0.0\apksigner.bat" verify --print-certs "%APK%"
if errorlevel 1 (
  echo VERIFY THAT BAI!
  exit /b 1
)

echo.
echo [3/3] XONG: %APK%
echo Cai de len may (giu data): adb install -r "%APK%"
echo Neu bao INSTALL_FAILED_UPDATE_INCOMPATIBLE (do ban cu khac key) thi go 1 lan duy nhat:
echo   adb uninstall naq.dns ^&^& adb install "%APK%"
echo Tu lan sau tro di, cu chay script nay roi adb install -r la cai de duoc.
endlocal
