# Integer corpus

```.properties``` files point to their respective EXCEL_SHEET with a relative path, separated by \\. 



## File Structure


- INTEGER\

  - configuration_files
    - fromAFW
      - ```.properties``` --> ../../spreadsheets/fromAFW/SEEDED/*.xls(x)
    - others
    
  - spreadsheets
    - fromAFW
      - original
      - SEEDED
    - others
      
      
## Remarks


- [configuration_files/others/](configuration_files/others/):
  If_example_Fault{1,2,4}.properties - files are empty, deleted

- [configuration_files/fromAFW/](configuration_files/fromAFW/) FW_fibonacci_3Faults_Fault1.properties:
    The faulty cell 0!C!12 does not contain a formula and has been therefore removed from the experiments.

