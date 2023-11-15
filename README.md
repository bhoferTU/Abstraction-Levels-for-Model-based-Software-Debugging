# Model Evaluation - Supplemental material 

This repository provides the evaluation data of the experiments 
described in *"Choosing Abstraction Levels for Model-based Software Debugging: 
A Theoretical and Empirical Analysis for Spreadsheet Programs"* written by Birgit Hofer, Dietmar Jannach, Iulia Nica, Patrick Rodler, and Franz Wotawa.

## About the paper

Model-based diagnosis is a generally applicable, principled approach to the systematic debugging of a wide range of system types such as circuits, knowledge bases, physical devices, or software.
Based on a formal description of the system,
model-based diagnosis allows to precisely and deterministically reason about potential faults responsible for an observed system misbehavior.
In the case of software, such a formal system description can often even be extracted from the buggy program in a fully automatic way.
Since logical reasoning is a core building block of the diagnosis process, the performance of model-based debuggers
is largely affected by the reasoning efficiency.
The latter in turn depends on the complexity and expressivity of the system description.
As highly detailed system descriptions capturing the exact semantics of the system are often beyond the reach of state-of-the-art reasoning techniques, researchers have proposed to reduce the level of detail by introducing more abstract ways to describe the system.

In this work, we thoroughly analyze different system modeling techniques with a focus on fault localization in spreadsheets, one of the most popular end-user programming paradigms used by millions in a wide range of areas.
Specifically, we present three constraint model types to characterize a spreadsheet at various levels of abstraction, show how they can be extracted from a buggy spreadsheet automatically, and provide theoretical and empirical investigations of the implications of the abstraction level on the computed diagnostic solutions as well as on the computation performance.
The main conclusions are that (i) for the model types, there is a trade-off between the conciseness of the generated set of
fault candidates and the computational efficiency, (ii) the exact model is often inefficient and thus impractical, and (iii) a newly proposed model based on qualitative reasoning allows to compute the same solutions as the exact model in
up to more than half of the cases while
exhibiting an orders-of-magnitude faster computation time.

Due to their capability of restricting the solution space in a proven sound way, the examined model-based techniques, rather than being used as standalone approaches,
are expected to unfold their full potential in combination with iterative sequential diagnosis techniques, or with
indeterministic, but more performant statistical debugging techniques.


## Benchmark data

We used two datasets in our experiments:
+ a set of [artificially created spreadsheets](Benchmarks/ArtifSpreadsheets/): The structure of these spreadsheets is described in the paper.
+ the [Integer spreadsheet corpus](Benchmarks/INTEGER/): Details about this corpus can be found on the website [spreadsheets.ist.tugraz.at](https://spreadsheets.ist.tugraz.at/index.php/corpora-for-benchmarking/integer-corpus/) and in the  paper ["The Right Choice Matters! SMT Solving Substantially Improves Model-Based Debugging of Spreadsheets"](https://ieeexplore.ieee.org/document/6605919?arnumber=6605919)

Each faulty spreadsheet comes with a .properties file listing the correct and erroneous output cells.
A detailed description of the file format can be found [here](https://spreadsheets.ist.tugraz.at/index.php/corpora-for-benchmarking/format/).

### Minion files

The spreadsheets are automatically translated into Minion constraints.
We have added the generated [Minion files](results/minionFiles/) to this repository.
There are three files for each spreadsheet: a value-based model (ending with ```_value.minion```), a functional dependency model (```_dependency.minion```), and a qualitative deviation model (```_comparision.minion```).    
The [Minion contraint solver](https://constraintmodelling.org/minion/) can be used to solve the contraints.
In order to obtain the diagnoses of a certain cardinality the ```#SIZE OF SOLUTION``` section in the individual Minion files has to be modified:
+ The number in the constraints ```watchsumgeq(ab,3)``` and ```watchsumleq(ab,3)``` has to be set to the desired diagnosis size. By default they are set to size 3 in all files.
+ In case you are interested in single fault diagnoses, you have to remove all constraints of the form ```element(ab,n,0)``` where ```n``` is a number. These constraints were added in order to prevent the solver from reporting supersets of the single fault diagnoses when computing the double and triple fault diagnoses.
+ In case you are interested in double fault diagnoses, you have to remove all constraints of the form ```watched-or({element(ab,i,0),element(ab,j,0)})``` where ```i``` and ```j``` are  numbers. These constraints were added in order to prevent the solver from reporting supersets of the double fault diagnoses when computing the triple fault diagnoses.
+ By default, only the minimal diagnoses are reported. If you are interested in all diagnoses, you have to remove all ```element(ab,n,0)``` and ```watched-or({element(ab,i,0),element(ab,j,0)})``` statements.


## Rerun the experiments

**Note:** We ran the experiments on Windows. If you want to rerun the experiments on another OS you might have to modify the code that calls the Minion solver and recompile the code.


#### Settings Experiment 1
* [Download](https://constraintmodelling.org/sdm_downloads/minion-1-8-windows-version/) the executable of the Minion constraint solver and place it in the project's main folder.
* Adjust the experiment settings in the [config](config) file. The parameters are explained in the file. If you want to rerun the experiments, please copy the settings from below.
  * NUM_RUNS = 10
  * MAX_DIAGNOSIS_SIZE = 3
  * TIMEOUT_MINUTES = 20
  * ALL_DIAGNOSES = false
  * PATH_TO_PROPERTIES_FILES = Benchmarks\\ArtifSpreadsheets\\SEEDED\\PropertiesFiles
* Run the [jar file](ModelBasedDebuggerForSpreadsheets.jar): 
````java -jar ModelBasedDebuggerForSpreadsheets.jar````



Afterwards rerun the experiments with
* PATH_TO_PROPERTIES_FILES = Benchmarks\\INTEGER\\configuration_files\\fromAFW

#### Experiments 2 (Further analysis)

Based on the raw results obtained from *Experiments 1*, we further analysed the three models wrt. their diagnosis accuracy. Details on the examined aspects can be found in the paper (subsection *6.3. Evaluation Results*). 

The [ExperimentsInfo](src/at/tugraz/ist/debugging/spreadsheets/datastructures/cells/ExperimentsInfo.java) class is the main class responsible for running the experimental analysis for both datasets. 
The desired functionality can be changed in the code:
+ you can choose the file to put the new analysis results in:
```String csvFile = "NewAnalysisIC.csv";```
+ you have to specify the spreadsheets to be analyzed:
```files.addAll(Directory.getFiles("Benchmarks\\INTEGER\\spreadsheets\\fromAFW\\SEEDED",".xlsx"));```
+ you have to specify the Excel file containing the results of *Experiments 1*:
```FileInputStream rfile = new FileInputStream( new File("Experiments1ResultsIC.xlsx"));```

One can easily rerun also these experiments for the [artificially created spreadsheets](Benchmarks/ArtifSpreadsheets) by executing [Experiments2AS.jar](Experiments2AS.jar). For the [Integer spreadsheet corpus](Benchmarks/INTEGER), the experiments can be reproduced by running [Experiments2IC.jar](Experiments2IC.jar). A detailed description of the experimental results can be found in *Further analysis data*.

## Recompile the code

*Required Libraries:*

* ```commons-collections4-4.1.jar```
* ```commons-lang3-3.3.2.jar``` 
* ```javassist-3.18.0-GA.jar```
* ```ooxml-lib.jar```
* ```poi-3.15.jar```
* ```slf4j-1.7.6.jar```
* ```jung2-2_0_1.zip.jar```
* ```choco-solver-2.1.5.jar```
* ```choco-solver-cpviz-2.1.5.jar```
* ```com.microsoft.z3.jar```
* ```commons-io-1.4.jar```
* ```guava-16.0.1.jar```
* ```junit-4.10jar```
* ```reflections-0.9.9-RC1.jar```
* [lib/hittingSets.jar](lib/hittingSets.jar)
* [lib/IstUtils.jar](lib/IstUtils.jar)


## Raw results data 

The raw results data of our experiments can be found in the [results](results/) folder.
The generated raw results for the Integer spreadsheet corpus can be found in [RawDataIntegerCorpus.csv](results/RawDataIntegerCorpus.csv);
those of the arificial spreadsheets can be found in [RawDataArtifSpreadsheets.csv](results/RawDataArtifSpreadsheets.csv).
From the per dataset generated data, we extracted  the following information (depicted as header in the file):

1. ```Properties file```	- indicates the examined spreadsheet 

2. ```VBM diagnosis size=1```, ```VBM diagnosis amount (size=1)```, ```VBM diagnosis size=2```, ```VBM diagnosis amount (size=2)```,  ```VBM diagnosis size=3```, ```VBM diagnosis amount (size=3)``` - list the set of all the minimal diagnoses of cardinality one/two/three, respectively, and the number of them for the value-based model 
 
3. ```DBM diagnosis size=1```, ```DBM diagnosis amount (size=1)```, ```DBM diagnosis size=2```, ```DBM diagnosis amount (size=2)```, ```DBM diagnosis size=3, DBM diagnosis amount (size=3)```- list the set of all the minimal diagnoses of cardinality one/two/three, respectively, and the number of the diagnoses for the functional dependency model	

4. ```CBM diagnosis size=1```, ```CBM diagnosis amount (size=1)```, ```CBM diagnosis size=2```, ```CBM diagnosis amount (size=2)```, ```CBM diagnosis size=3, CBM diagnosis amount (size=3)``` - list the set of all the minimal diagnoses of cardinality one/two/three, respectively, and the number of them for the qualitative deviation model

5. ```diag VBM```, ```diag DBM```, ```diag CBM``` - list the total number of minimal diagnoses up to cardinality three for the value-based model, the functional dependency model, and the qualitative deviation model	

6. ```All VBM containd in DBM```, ```All VBM containd in CBM``` - TRUE/FALSE, answer the question if all the minimal diagnoses generated for VBM are among the minimal diagnoses obtained for DBM and CBM, respectively

7. ```True fault in VBM```, ```True fault in DBM```, ```True fault in CBM``` - indicates if the real fault is actually found for each model  

8. ```VBM time [ms] (diagnosis size=1)```, ```VBM time [ms] (diagnosis size=2)```, ```VBM time [ms] (diagnosis size=3)```, ```DBM time [ms] (diagnosis size=1)```, ```DBM time [ms] (diagnosis size=2)```, ```DBM time [ms] (diagnosis size=3)```, ```CBM time [ms] (diagnosis size=1)```, ```CBM time [ms] (diagnosis size=2)```, ```CBM time [ms] (diagnosis size=3)``` - state the Minion's runtime in mili-seconds or ```timeout``` if the Minion's solving process exceeded a time limit of 20 minutes	

9. ```Incorrect Output cells``` - states the number of output cells which have a wrong value	

10. ```Correct Output cells``` - states the number of  output cells whose value is correct

11. ```Faulty cells``` - states the number faulty cells


## Further analysis data 

Based on the raw results presented in the previous section, we generated two further csv-files with the following structure.
The analysis results for the Integer spreadsheet corpus can be found in [NewAnalysisIC.csv](results/NewAnalysisIC.csv);
those of the arificial spreadsheets can be found in [NewAnalysisAS.csv](results/NewAnalysisAS.csv).



1. ```File``` - indicates the examined spreadsheet 

2. ```Formulas```, ```Input Cells```, ```Output Cells``` - state the number of formulas, input cells and output cells in the examined spreadsheets

3. ```AMOUNT of all VBMdiags (size1)```, ```AMOUNT of all QDMdiags (size1)```, ```AMOUNT of all FDMdiags (size1)``` - list the number of single-fault diagnoses for the value-based model, the qualitative deviation model, and the functional dependency model, respectively (information extracted from the ```raw results data```)

4. ```AMOUNT of all (minimal and not minimal) VBMdiags (size2)``` - states the number of all double-fault diagnoses for the VBM (i.e., the union of the set of all the minimal double-fault diagnoses and the supersets of lower cardinality diagnoses) 

5. ```AMOUNT of all minimal QDMdiags (size2)```, ```AMOUNT of all minimal QDMdiags in all VBMdiags (size2)``` -	state the number of all minimal double-fault diagnoses for the QDM and how many of these diagnoses are among the double-fault diagnoses for the VBM  

6. ```AMOUNT of all minimal FDMdiags (size2)```, ```AMOUNT of all minimal FDMdiags in all VBMdiags (size2)``` -	state the number of all minimal double-fault diagnoses for the FDM and how many of these diagnoses are among the double-fault diagnoses for the VBM  

7. ```AMOUNT of all (minimal and not minimal) VBMdiags (size3)``` - states the number of all triple-fault diagnoses for the VBM (i.e., the union of the set of all the minimal triple-fault diagnoses and the supersets of lower cardinality diagnoses) 

8. ```AMOUNT of all minimal QDMdiags (size3)```	, ```AMOUNT of all minimal QDMdiags in all VBMdiags (size3)``` -  state the number of all minimal triple-fault diagnoses for the QDM and how many of these diagnoses are among the triple-fault diagnoses for the VBM  

9. ```AMOUNT of all minimal FDMdiags (size3)```, ```AMOUNT of all minimal FDMdiags in all VBMdiags (size3)``` - state the number of all minimal triple-fault diagnoses for the FDM and how many of these diagnoses are among the triple-fault diagnoses for the VBM  

10. ```QDM: AMOUNT of all 2-supersets of 1-fault diagnosis```, ```QDM: 2-supersets NOT in VBMdiags2```	- state, for the QDM, the number of all cardinality 2 supersets of single-fault diagnoses and how many of these supersets are NOT among the double-fault diagnoses for the VBM  

11. ```FDM: AMOUNT of all 2-supersets of 1-fault diagnosis```, ```FDM: 2-supersets NOT in VBMdiags2``` - state, for the FDM, the number of all cardinality 2 supersets of single-fault diagnoses and how many of these supersets are NOT among the double-fault diagnoses for the VBM 

12. ```QDM: AMOUNT of all 3-supersets of 2-fault diagnosis```, ```QDM: 3-supersets NOT in VBM diags3``` - state, for the QDM, the number of all cardinality 3 supersets of double-fault diagnoses and how many of these supersets are NOT among the triple-fault diagnoses for the VBM 

13. ```FDM: AMOUNT of all 3-supersets of 2-fault diagnosis```, ```FDM: 3-supersets NOT in VBM diags3``` - state, for the FDM, the number of all cardinality 3 supersets of double-fault diagnoses and how many of these supersets are NOT among the triple-fault diagnoses for the VBM 


## Acknowledgement

This work is funded by the Austrian Science Fund (FWF) project Interactive Spreadsheet Debugging under contract number P 32445.
