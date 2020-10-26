REM Made by sspacelynx@github.com

@echo off
setlocal EnableDelayedExpansion

cls
title Dolphin MMJR Debug Tool
set msgTitle="Dolphin MMJR Debug Tool"
echo  *******************************************
echo ***                                       ***
echo ***       Dolphin MMJR Debug Tool         ***
echo ***        ---[ version 1.1 ]---          ***
echo ***                                       ***
echo  *******************************************
echo.

if exist "adb.exe" (
	REM continue
) else (
	goto adb_not_found
)

:adb_found
    for /f "tokens=2" %%i in ('adb devices') do (
      if "%%i"=="unauthorized" goto allow_usb_debug
    )

if %ERRORLEVEL% == 1 goto phone_not_found
if %ERRORLEVEL% == 0 goto start_log

:allow_usb_debug
    echo Wsh.Echo MsgBox("Press OK on the dialog on your phone and click retry." + vbCrLf + "If the popup doesn't show up, click Cancel and run me again.",vbRetryCancel,%msgTitle%) >%TEMP%\tmp.vbs
    REM Clever way to get vbscript result based on which button was pressed
    for /f %%i in ('cscript %TEMP%\tmp.vbs //nologo //e:vbscript') do (set "return=%%i")
    del /q %TEMP%\tmp.vbs

if %return%==4 goto adb_found
if %return%==2 exit

:phone_not_found
    echo Wsh.Echo MsgBox("Your phone was not found" + vbCrLf + "1. Make sure USB Debugging is enabled under Developer settings;" + vbCrLf + "2. Unplug and replug the USB cable;" + vbCrLf + "3. Click Retry;" + vbCrLf + "4. If it still fails, please make sure your phone's drivers are installed.",vbRetryCancel,%msgTitle%) >%TEMP%\tmp.vbs
    for /f %%i in ('cscript %TEMP%\tmp.vbs //nologo //e:vbscript') do (set "return=%%i")
    del /q %TEMP%\tmp.vbs

if %return%==4 goto adb_found
if %return%==2 exit

:adb_not_found
    echo Wsh.Echo MsgBox("Adb executable not found." + vbCrLf + "Are you sure to have extracted all the files from the .zip?",vbOKOnly,%msgTitle%) >%TEMP%\tmp.vbs
    for /f %%i in ('cscript %TEMP%\tmp.vbs //nologo //e:vbscript') do (set "return=%%i")
    del /q %TEMP%\tmp.vbs
exit

:start_log
    adb logcat --clear
    echo Opening Dolphin MMJR
    adb shell monkey -p org.mm.jr.debug -c android.intent.category.LAUNCHER 1 >nul
    echo Do the necessary testing, then pressy any button to save the log file and exit...
    pause >nul
    adb logcat *:E -b default --regex org.dolphinemu -d >debug.log
exit
