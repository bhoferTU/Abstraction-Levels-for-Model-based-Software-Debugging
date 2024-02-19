@echo off
setlocal

REM Set the path to your Java executable
set JAVA_PATH="C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe"

REM Set the path to your JAR file
set JAR_PATH="C:\Users\inica\Documents\GitHub\SFLspreadsheets\MBDebugger.jar"

REM Set the parameter you want to pass to the JAR file
set CONFIG_FILE=all_diagnoses_integer.config

REM Run the JAR file with the specified configuration file
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE%

endlocal
pause