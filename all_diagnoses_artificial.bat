@echo off
setlocal
:: =====================================
:: Notes on * ExperimentsInfo.java * - main class responsible for running  * Experiment 2 *  for both datasets.
:: =====================================
:: The desired functionality can be changed in the code, by setting the following parameters:
:: LOG file:
::    logFile = new File("experiments\\logFiles\\experiment2_artificial.log");
:: OUTPUT file:
::    csvFile = "results\\superSets_artificial.csv```
:: SPREADSHEETS to be analyzed:
::    files.addAll(Directory.getFiles("Benchmarks\\ArtifSpreadsheets\\SEEDED\\ExcelFiles",".xlsx"));
:: INPUT file containing the results of * Experiments 1*:
::    rfile = new FileInputStream( new File("experiments\\experiment1_results_artificial.xlsx"));```
:: =====================================

:: Set the path to your Java executable
set JAVA_PATH="C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe"

:: Set the path to your JAR file
set JAR_PATH="C:\Users\inica\Documents\GitHub\SFLspreadsheets\superSets_artificial.jar"

:: Run the JAR file 
%JAVA_PATH% -jar %JAR_PATH% 

endlocal
pause