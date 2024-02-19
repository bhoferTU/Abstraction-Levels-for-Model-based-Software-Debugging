@echo off
setlocal

REM Set the path to your Java executable
set JAVA_PATH="C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe"

REM Set the path to your JAR file
set JAR_PATH="C:\Users\inica\Documents\GitHub\SFLspreadsheets\MBDebugger.jar"

REM Set the parameter you want to pass to the JAR file
set CONFIG_FILE1=min_diagnoses_integer_RUN1.config
set CONFIG_FILE2=min_diagnoses_integer_RUN2.config
set CONFIG_FILE3=min_diagnoses_integer_RUN3.config
set CONFIG_FILE4=min_diagnoses_integer_RUN4.config
set CONFIG_FILE5=min_diagnoses_integer_RUN5.config
set CONFIG_FILE6=min_diagnoses_integer_RUN6.config
set CONFIG_FILE7=min_diagnoses_integer_RUN7.config
set CONFIG_FILE8=min_diagnoses_integer_RUN8.config
set CONFIG_FILE9=min_diagnoses_integer_RUN9.config
set CONFIG_FILE10=min_diagnoses_integer_RUN10.config

REM Run the JAR files with the specified configuration file
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE1%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE2%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE3%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE4%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE5%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE6%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE7%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE8%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE9%
%JAVA_PATH% -jar %JAR_PATH% %CONFIG_FILE10%

endlocal
pause