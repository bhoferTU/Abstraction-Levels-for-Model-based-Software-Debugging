# Model Evaluation - Supplemental material 

This repository provides the evaluation data of the experiments 
described in *"Choosing Abstraction Levels for Model-based Software Debugging: 
A Theoretical and Empirical Analysis for Spreadsheet Programs"* written by Birgit Hofer, Dietmar Jannach, Iulia Nica, Patrick Rodler, and Franz Wotawa.

## Contents

* [About the paper](#user-content-about-the-paper)
* [Benchmark data](#user-content-benchmark-data)
* [Rerun the experiments](#user-content-rerun-the-experiments)
* [Results](#user-content-results)
  * [Raw results data](#user-content-raw-results-data)
  * [Minion files](#user-content-minion-files)
* [Recompile the code](#user-content-recompile-the-code)
* [Run your own experiments](#user-content-run-your-own-experiments)
* [Acknowledgement](#user-content-acknowledgement)


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
+ a set of [artificially created spreadsheets](Benchmarks/ArtifSpreadsheets): The structure of these spreadsheets is described in the paper.
+ the [Integer spreadsheet corpus](Benchmarks/INTEGER): Details about this corpus can be found on the website [spreadsheets.ist.tugraz.at](https://spreadsheets.ist.tugraz.at/index.php/corpora-for-benchmarking/integer-corpus/) and in the  paper ["The Right Choice Matters! SMT Solving Substantially Improves Model-Based Debugging of Spreadsheets"](https://ieeexplore.ieee.org/document/6605919?arnumber=6605919)

Each faulty spreadsheet comes with a .properties file listing the correct and erroneous output cells.
A detailed description of the file format can be found [here](https://spreadsheets.ist.tugraz.at/index.php/corpora-for-benchmarking/format/).


## Rerun the experiments

**Note:** We ran the experiments on *Windows*. If you want to rerun the experiments on another OS you might have to modify the code that calls the Minion solver and recompile the code.

Before running the experiments, you have to
* [download](https://constraintmodelling.org/sdm_downloads/minion-1-8-windows-version/) the executable of the Minion constraint solver and place it in the project's main folder and
* adjust the paths in the batch files.

We conducted two experiments:
* Experiment 1 computes 10 times all minimal diagnoses up to cardinality 3.
  * [Artificial spreadsheets](min_diagnoses_artificial_10RUNS.bat)
  * [Integer corpus](min_diagnoses_integer_10RUNS.bat)
* Experiment 2 computes all diagnoses (i.e. minimal diagnoses and their supersets) up to cardinality 3.
  * [Artificial spreadsheets](all_diagnoses_artificial.bat)
  * [Integer corpus](all_diagnoses_integer.bat)

    



## Results


### Raw results data 

The raw results data of our main experiments  can be found in the ```.cvs``` files in the  [results](results) folder:
* Experiment 1:
  * [Artificial spreadsheets RUN1](results/min_diagnoses_artificial_RUN1.csv), ..., [Artificial spreadsheets RUN10](results/min_diagnoses_artificial_RUN10.csv)
  * [Integer corpus RUN1](results/min_diagnoses_integer_RUN1.csv), ..., [Integer corpus RUN10](results/min_diagnoses_integer_RUN10.csv)
* Experiment 2: 
  * [Artificial spreadsheets](results/all_diagnoses_artificial.csv)
  * [Integer corpus](results/all_diagnoses_integer.csv)

  

Each ```.cvs``` file contains the following columns:

1. ```Properties file```	- indicates the examined spreadsheet 

2. ```VBM diagnosis size=1```, ```VBM diagnosis amount (size=1)```, ```VBM diagnosis size=2```, ```VBM diagnosis amount (size=2)```,  ```VBM diagnosis size=3```, ```VBM diagnosis amount (size=3)``` - list the set of *all the minimal diagnoses (Experiments 1) OR all diagnoses (Experiments 2)* of cardinality one/two/three, respectively, and the number of them for the value-based model 
 
3. ```DBM diagnosis size=1```, ```DBM diagnosis amount (size=1)```, ```DBM diagnosis size=2```, ```DBM diagnosis amount (size=2)```, ```DBM diagnosis size=3, DBM diagnosis amount (size=3)```- list the set of *all the minimal diagnoses (Experiments 1) OR all diagnoses (Experiments 2)* of cardinality one/two/three, respectively, and the number of the diagnoses for the functional dependency model	

4. ```CBM diagnosis size=1```, ```CBM diagnosis amount (size=1)```, ```CBM diagnosis size=2```, ```CBM diagnosis amount (size=2)```, ```CBM diagnosis size=3, CBM diagnosis amount (size=3)``` - list the set of *all the minimal diagnoses (Experiments 1) OR all diagnoses (Experiments 2)* of cardinality one/two/three, respectively, and the number of them for the qualitative deviation model

5. ```diag VBM```, ```diag DBM```, ```diag CBM``` - list *the total number of minimal diagnoses (Experiments 1) OR all diagnoses (Experiments 2)* up to cardinality three for the value-based model, the functional dependency model, and the qualitative deviation model	

6. ```All VBM containd in DBM```, ```All VBM containd in CBM``` - TRUE/FALSE, answer the question if *all the minimal diagnoses(Experiments 1) OR all diagnoses (Experiments 2)* generated for VBM are among the minimal diagnoses obtained for DBM and CBM, respectively

7. ```True fault in VBM```, ```True fault in DBM```, ```True fault in CBM``` - indicates if the real fault is actually found for each model  

8. ```VBM time [ms] (diagnosis size=1)```, ```VBM time [ms] (diagnosis size=2)```, ```VBM time [ms] (diagnosis size=3)```, ```DBM time [ms] (diagnosis size=1)```, ```DBM time [ms] (diagnosis size=2)```, ```DBM time [ms] (diagnosis size=3)```, ```CBM time [ms] (diagnosis size=1)```, ```CBM time [ms] (diagnosis size=2)```, ```CBM time [ms] (diagnosis size=3)``` - state the Minion's runtime in milliseconds or ```timeout``` if the Minion's solving process exceeded a time limit of 20 minutes	

9. ```Incorrect Output cells``` - states the number of output cells which have a wrong value	

10. ```Correct Output cells``` - states the number of  output cells whose value is correct

11. ```Faulty cells``` - states the number of faulty cells




### Minion files

The spreadsheets are automatically translated into Minion constraints.
We have added the generated [Minion files](results/minionFiles) to this repository.
There are three files for each spreadsheet: a value-based model (ending with ```_value.minion```), a functional dependency model (```_dependency.minion```), and a qualitative deviation model (```_comparision.minion```).    
The [Minion constraint solver](https://constraintmodelling.org/minion/) can be used to solve the constraints.
In order to obtain the diagnoses of a certain cardinality the ```#SIZE OF SOLUTION``` section in the individual Minion files has to be modified:
+ The number in the constraints ```watchsumgeq(ab,3)``` and ```watchsumleq(ab,3)``` has to be set to the desired diagnosis size. By default, they are set to size 3 in all files.
+ In case you are interested in single fault diagnoses, you have to remove all constraints of the form ```element(ab,n,0)``` where ```n``` is a number. These constraints were added in order to prevent the solver from reporting supersets of the single fault diagnoses when computing the double and triple fault diagnoses.
+ In case you are interested in double fault diagnoses, you have to remove all constraints of the form ```watched-or({element(ab,i,0),element(ab,j,0)})``` where ```i``` and ```j``` are  numbers. These constraints were added in order to prevent the solver from reporting supersets of the double fault diagnoses when computing the triple fault diagnoses.
+ By default, only the minimal diagnoses are reported. If you are interested in all diagnoses, you have to remove all ```element(ab,n,0)``` and ```watched-or({element(ab,i,0),element(ab,j,0)})``` statements.

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



## Run your own experiments
* [Download](https://constraintmodelling.org/sdm_downloads/minion-1-8-windows-version/) the executable of the Minion constraint solver and place it in the project's main folder.
* Adjust the experiment settings in the [config](config) file. The parameters are explained in the file.
* Run the [jar file](MBDebugger.jar): ````java -jar MBDebugger.jar config````




## Acknowledgement

This work is funded by the Austrian Science Fund (FWF) project Interactive Spreadsheet Debugging under contract number P 32445.
