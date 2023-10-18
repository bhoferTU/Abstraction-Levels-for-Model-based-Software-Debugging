package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.tugraz.ist.debugging.spreadsheets.evaluation.Result;

import at.tugraz.ist.util.fileManipulation.Directory;

/**
 * 
 * @author inica 2022
 *
 */
public class ExperimentsInfo {
	public static BufferedWriter writer = null;
	public static boolean fileExists = false;
	public static int intersectionSize=0;
	
	public static void main(String[] args) {
		
		try {
			File logFile = new File("NewTest.log");
			if (!logFile.exists())
				logFile.createNewFile();
			PrintStream log = new PrintStream(logFile);
						System.setOut(log);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// *1) the file to put the new analysis results in
	    //String csvFile = "NewAnalysisIC.csv";
	    String csvFile = "NewAnalysisAS.csv";
		File f = new File(csvFile);

		if (f.exists()) {
			fileExists = true;
		}

		try {
			writer = new BufferedWriter(new FileWriter(csvFile, true));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Result result = new Result();
		try {
		List<String> files = new ArrayList<String>();
		
		// *2) get the set of spreadsheets to be analyzed
		//files.addAll(Directory.getFiles("Benchmarks\\INTEGER\\spreadsheets\\fromAFW\\SEEDED",".xlsx"));
		files.addAll(Directory.getFiles("Benchmarks\\ArtifSpreadsheets\\SEEDED\\ExcelFiles",".xlsx"));
		
		
			/** NEW 2022: 
			 * 
			 (1) Unter den computed k (2,3)-cardinality diagnoses fuer QDM und FDM, wieviele diagnoses sind unter den k-fault diagnoses fuer VBM?
			 
			**/

			try {
		   
				// *3) get the Excel file containing the results of Experiments1  
				//FileInputStream rfile = new FileInputStream( new File("Experiments1ResultsIC.xlsx"));
		        FileInputStream rfile = new FileInputStream( new File("Experiments1ResultsAS.xlsx"));
				
		        // Create Workbook instance holding reference to // .xlsx file
				XSSFWorkbook workbook = new XSSFWorkbook(rfile);
				// Get first/desired sheet from the workbook
				XSSFSheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				
				String vbmDiag1=new String(); // col 1
				String vbmDiag2=new String(); // col 3
				String qdmDiag2=new String(); // col 15
				String intVbmQdm2; 
				
				String fdmDiag2=new String(); // col 9
				String intVbmFdm2=new String(); 
				
				String vbmDiag3=new String(); // col 5
				String qdmDiag3=new String(); // col 17
				String intVbmQdm3=new String(); 
				
				String fdmDiag3=new String(); // col 11
				String intVbmFdm3=new String(); 
				
				
				String qdmDiag1=new String(); // col 13
				String fdmDiag1=new String(); // col 7
				
				String all2SuperSetsQDM1=new String();
				String all2SuperSetsFDM1=new String();
				
				String all3SuperSetsQDM2=new String();
				String all3SuperSetsFDM2=new String();
				
				String formulaCellsString=new String();
				
				boolean first=true;
				
				while (rowIterator.hasNext()) {
				  
				  Row row = rowIterator.next();
				  if(first) {
					  row=rowIterator.next();
					  first=false;
				  }
				  String filefromResults=row.getCell(0).toString();
				  String temp=filefromResults.substring(filefromResults.lastIndexOf(' ')+1)+".xlsx";
				  
				  for (String file : files) {
					    
					    if(file.endsWith(temp)) {
					     System.out.println("file result: "+ filefromResults + "   file: "+file);
						 CellContainer cells = CellContainer.create(file);
						 result.addData("File",temp);
						 result.addData("Formulas", cells.getFormulaCoords().size());
						 result.addData("Input Cells", cells.getInputCoords().size());
						 result.addData("Output Cells", cells.getOutputCoords().size());
						 
						 //* get the formula cells from the current spreadsheet
						 formulaCellsString=cells.getFormulaCoords().toString(); // it looks like  [0!D!3,..,..]
						 //*
						
						 break;
					    }
					}
				  /** size 2: 
				   * 
				   * (I) QDMdiags=VBMdiags 
				   * 
				   * (II) FDMdiags=VBMdiags   
				   * 
				   * 
				   * 
				   * */
				  //*   (I) QDMdiags=VBMdiags --- get the Strings for vbmDiag2 and qdmDiag2
				  
				  vbmDiag2=row.getCell(3).toString();
				  //*  all k-fault diagnoses for VBM = SuperSet[SET_diags(k-1)] + SET_diags(k) 
                  vbmDiag1=row.getCell(1).toString();
				  
				  // 2-fault diagnoses for VBM SET
                  boolean notok1=vbmDiag1.contains("-")||vbmDiag1.contains("timeout");
                  boolean notok2=vbmDiag2.contains("-")||vbmDiag2.contains("timeout");
                  SuperSet sSetVBM1 = null;
                  SuperSet setVBM2 = null;
                  if(notok1||notok2) {
                		  if(notok1 && notok2) {
                				  result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size2)", vbmDiag2);
                				  
                		  }
                		  else {
                			  if(notok1)
                			     setVBM2=new SuperSet(vbmDiag2);
                			  else
                				  if(notok2) {
                					  sSetVBM1=new SuperSet(vbmDiag1, formulaCellsString);
                					  setVBM2=sSetVBM1;
                				  }
                			  result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size2)", setVBM2.lenght );
                			 
                		  }
                  }
                  else 
                  if (!notok1 && !notok2 ){
                	  sSetVBM1=new SuperSet(vbmDiag1, formulaCellsString);
                	  setVBM2=new SuperSet(sSetVBM1,vbmDiag2, 2);
                	  result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size2)", setVBM2.lenght );
                  }
				  
				// ** col 16 get the amount of QDMdiags (size2) from Results
				  result.addData("AMOUNT of all minimal QDMdiags (size2)", row.getCell(16).toString() );
				  qdmDiag2=row.getCell(15).toString();
				  
				  
				  SuperSet setQDM2= null;
				  boolean notok3=qdmDiag2.contains("-")||qdmDiag2.contains("timeout");
				  if(notok3)
					  result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size2)", "- (no 2-fault minimal QDMdiags) "+qdmDiag2);
				  else
					  if(notok1 && notok2)
						  result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size2)", "- (no 2-fault VBMdiags) "+vbmDiag2);
					  else 
					  {
					      setQDM2=new SuperSet(qdmDiag2); // only the MINIMAL 2-fault diagnoses in setQDM2
				          int inter=setVBM2.compare(setQDM2);
				          result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size2)", inter );
				      }
				  
				  
				  
				//* (II) FDMdiags=VBMdiags --- get the String for fdmDiag2
				  fdmDiag2=row.getCell(9).toString();
				// ** col 10 get the amount of FDMdiags (size2) from Results
				  result.addData("AMOUNT of all minimal FDMdiags (size2)", row.getCell(10).toString() );
				  
				  SuperSet setFDM2= null;
				  notok3=fdmDiag2.contains("-")||fdmDiag2.contains("timeout");
				  if(notok3)
					  result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size2)", "- (no 2-fault minimal FDMdiags) "+fdmDiag2);
				  else
					  if(notok1 && notok2)
						  result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size2)", "- (no 2-fault VBMdiags) "+vbmDiag2);
					  else 
					  {
					      setFDM2=new SuperSet(fdmDiag2);
				          int inter=setVBM2.compare(setFDM2);
				          result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size2)", inter );
				          }
				  
				  
				
				 
				  /** END size 2
				   * **/
				  
				  
				  /** size 3: 
				   * 
				   * (I) QDMdiags=VBMdiags
				   * 
				   * (II) FDMdiags=VBMdiags   
				   * 
				   * 
				   * */
				  
				
				  //(I) QDMdiags=VBMdiags --- get the Strings for vbmDiag3 and qdmDiag3
						 
						  vbmDiag3=row.getCell(5).toString();
						  // 3-fault diagnoses for VBM SET
		                  boolean notokVBM3=vbmDiag3.contains("-")||vbmDiag3.contains("timeout");
		                  SuperSet sSetVBM2 = null;
		                  SuperSet setVBM3 = null;
		                  if(notok1 && notok2 && notokVBM3){
		                	  result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size3)", vbmDiag3);
		                	 
		                  }
		                  else 
		                  {
		                	  if (!(notok1 && notok2) && !notokVBM3){
			                	  sSetVBM2=new SuperSet(setVBM2, formulaCellsString);
			                	  setVBM3=new SuperSet(sSetVBM2,vbmDiag3, 3);
			                	  result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size3)", setVBM3.lenght );
			                  }	  
		                	   else 
		                	   {
		                	      if(notok1 && notok2)
		                		         setVBM3=new SuperSet(vbmDiag3);
		                	        else
		                		          if(notokVBM3) {
		                			           sSetVBM2=new SuperSet(setVBM2, formulaCellsString);
                					           setVBM3=sSetVBM2;
		                		           }	
		                	       result.addData("AMOUNT of all (minimal and not minimal) VBMdiags (size3)", setVBM3.lenght );
                			         
		                	   }
		                  }  
		                  
		                	  
						  
		                  qdmDiag3=row.getCell(17).toString();
						  result.addData("AMOUNT of all minimal QDMdiags (size3)", row.getCell(18).toString() );
						  
						  SuperSet setQDM3= null;
						  notok3=qdmDiag3.contains("-")||qdmDiag3.contains("timeout");
						  if(notok3)
							  result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size3)", "- (no 3-fault minimal QDMdiags) "+qdmDiag3);
						  else
							  if(notok1 && notok2 && notokVBM3)
								  result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size3)", "- (no 3-fault VBMdiags) "+vbmDiag3);
							  else 
							  {
							      setQDM3=new SuperSet(qdmDiag3);
						          int inter=setVBM3.compare(setQDM3);
						          result.addData("AMOUNT of all minimal QDMdiags in all VBMdiags (size3)", inter );
						      }
						  
						  
						  
				 
				//* (II) FDMdiags=VBMdiags --- get the String for fdmDiag2
				  fdmDiag3=row.getCell(11).toString();
				  result.addData("AMOUNT of all minimal FDMdiags (size3)", row.getCell(12).toString() );
				  
				  SuperSet setFDM3= null;
				  notok3=fdmDiag3.contains("-")||fdmDiag3.contains("timeout");
				  if(notok3)
					  result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size3)", "- (no 3-fault minimal FDMdiags) "+fdmDiag3);
				  else
					  if(notok1 && notok2 && notokVBM3)
						  result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size3)", "- (no 3-fault VBMdiags) "+vbmDiag3);
					  else 
					  {
					      setFDM3=new SuperSet(fdmDiag3);
				          int inter=setVBM3.compare(setFDM3);
				          result.addData("AMOUNT of all minimal FDMdiags in all VBMdiags (size3)", inter );
				      }
				  
				  
				  
				  
				  
				 
				  /** END size 3
				   * **/
				  
				  
				/**  (2) a) Wieviele k-supersets von (k-1)-fault diagnoses fuer QDM und FDM gibt es fuer jedes k? 
				 *       b) Und wieviele davon sind nicht unter den k-fault diagnoses fuer VBM?     
				  */
				  
				  // a) for k=2 
				
				  // QDM - create the 2-SuperSET of the 1-fault diagnoses for QDM --> from the qdmDiag1 and formulaCellsString
				  qdmDiag1=row.getCell(13).toString();
				  boolean notOkQDM1=qdmDiag1.contains("-")||qdmDiag1.contains("timeout");
				  SuperSet sSetQDM1=null;
				  if(notOkQDM1) {
					  result.addData("QDM: AMOUNT of all 2-supersets of 1-fault diagnosis", qdmDiag1);
					  result.addData("QDM: 2-supersets NOT in VBMdiags2", qdmDiag1);
				  }
				  else {
					  sSetQDM1=new SuperSet(qdmDiag1, formulaCellsString); // no minimal 2-fault diagnoses in sSetQDM1
					  result.addData("QDM: AMOUNT of all 2-supersets of 1-fault diagnosis", sSetQDM1.lenght);
					  if(setVBM2!=null) {
						  int INvbm2=sSetQDM1.compare(setVBM2);
						  int NOTinVBM2=sSetQDM1.lenght-INvbm2;
						  result.addData("QDM: 2-supersets NOT in VBMdiags2", NOTinVBM2+ " ("+INvbm2+ " diagnoses from "+sSetQDM1.lenght+ " are in VBMdiags2)");
					  }
					  else
						  result.addData("QDM: 2-supersets NOT in VBMdiags2", "- (no 2-fault VBMdiags)");
				  }
					  
				  // FDM - create the 2-SuperSET of the 1-fault diagnoses for FDM --> from the fdmDiag1 and formulaCellsString
				  fdmDiag1=row.getCell(7).toString();
				  boolean notOkFDM1=fdmDiag1.contains("-")||fdmDiag1.contains("timeout");
				  SuperSet sSetFDM1=null;
				  if(notOkFDM1) {
					  result.addData("FDM: AMOUNT of all 2-supersets of 1-fault diagnosis", fdmDiag1);
					  result.addData("FDM: 2-supersets NOT in VBMdiags2", fdmDiag1);
				  }
				  else {
					  sSetFDM1=new SuperSet(fdmDiag1, formulaCellsString);
					  result.addData("FDM: AMOUNT of all 2-supersets of 1-fault diagnosis", sSetFDM1.lenght);
					  if(setVBM2!=null) {
						  int INvbm2=sSetFDM1.compare(setVBM2);
						  int NOTinVBM2=sSetFDM1.lenght-INvbm2;
						  result.addData("FDM: 2-supersets NOT in VBMdiags2", NOTinVBM2+ " ("+INvbm2+ " diagnoses from "+sSetFDM1.lenght+ " are in VBMdiags2)");
					  }
					  else
						  result.addData("FDM: 2-supersets NOT in VBMdiags2", "- (no 2-fault VBMdiags)");
				  }
				  
				  
				  
				  // a) for k=3 
				  // QDM - create the 3-SuperSET of the 2-fault diagnoses for QDM --> from the [sSetQDM1 U qdmDiag2]=sSetQDM2 and formulaCellsString
				  
				  //* create [sSetQDM1 U qdmDiag2]--> sSetQDM2 COMBINE formulaCells-->sSetQDM3
				  SuperSet sSetQDM2=null;
				  SuperSet sSetQDM3=null;
				  boolean notOkQDM2=qdmDiag2.contains("-")||qdmDiag2.contains("timeout");
				  if(sSetQDM1==null) { 
					  if(notOkQDM2)
					  {
						  result.addData("QDM: AMOUNT of all 3-supersets of 2-fault diagnosis", qdmDiag2);
						  result.addData("QDM: 3-supersets NOT in VBM diags3", qdmDiag2); 
					  }
					  else // if we have 2-fault diagnoses
					  {
						  sSetQDM2=new SuperSet(qdmDiag2);
						  sSetQDM3=new SuperSet(sSetQDM2,formulaCellsString);
						  result.addData("QDM: AMOUNT of all 3-supersets of 2-fault diagnosis", sSetQDM3.lenght);						 
					  }
				  }
				  else { // we have 2-supersets of smaller cardinality diagnoses
					  if(!notOkQDM2) {
					       sSetQDM2=new SuperSet(sSetQDM1,qdmDiag2,2);
					       sSetQDM3=new SuperSet(sSetQDM2,formulaCellsString);
					  }
					  else
						  sSetQDM3=new SuperSet(sSetQDM1,formulaCellsString);
					  result.addData("QDM: AMOUNT of all 3-supersets of 2-fault diagnosis", sSetQDM3.lenght);
					 
				  }
				  
				 
				  
				//* END  create [sSetQDM1 U qdmDiag2]--> sSetQDM2 COMBINE formulaCells-->sSetQDM3
				  
				  //* compare with VBM
				
					   if((setVBM3!=null)&&(sSetQDM3!=null)) {
						  int INvbm3=sSetQDM3.compare(setVBM3);
						  int NOTinVBM3=sSetQDM3.lenght-INvbm3;
						  result.addData("QDM: 3-supersets NOT in VBM diags3", NOTinVBM3+ " ("+INvbm3+ " diagnoses from "+sSetQDM3.lenght+ " are in VBMdiags3)");
						  
						  
					   }
					   else
						  result.addData("QDM: 3-supersets NOT in VBM diags3", "- (no 3-fault VBMdiags)");
				  
				
				  // FDM - create the 3-SuperSET of the 2-fault diagnoses for FDM --> from the [sSetFDM1 U fdmDiag2]=sSetFDM2 and formulaCellsString
				  

				  //* create [sSetQDM1 U qdmDiag2]--> sSetQDM2 COMBINE formulaCells-->sSetQDM3
				  SuperSet sSetFDM2=null;
				  SuperSet sSetFDM3=null;
				  boolean notOkFDM2=fdmDiag2.contains("-")||fdmDiag2.contains("timeout");
				  if(sSetFDM1==null) { 
					  if(notOkFDM2)
					  {
						  result.addData("FDM: AMOUNT of all 3-supersets of 2-fault diagnosis", fdmDiag2);
						  result.addData("FDM: 3-supersets NOT in VBM diags3", fdmDiag2); 
					  }
					  else // if we have 2-fault diagnoses
					  {
						  sSetFDM2=new SuperSet(fdmDiag2);
						  sSetFDM3=new SuperSet(sSetFDM2,formulaCellsString);
						  result.addData("FDM: AMOUNT of all 3-supersets of 2-fault diagnosis", sSetFDM3.lenght);
					  }
				  }
				  else { // we have 2-supersets of smaller cardinality diagnoses
					  if(!notOkFDM2) {
						  sSetFDM2=new SuperSet(sSetFDM1,fdmDiag2,2);
						  sSetFDM3=new SuperSet(sSetFDM2,formulaCellsString);
					  }
					  else
						  sSetFDM3=new SuperSet(sSetFDM1,formulaCellsString);
					  
					  result.addData("FDM: AMOUNT of all 3-supersets of 2-fault diagnosis", sSetFDM3.lenght);
				  }
				  
				 
				  
				//* END  create [sSetQDM1 U qdmDiag2]--> sSetQDM2 COMBINE formulaCells-->sSetQDM3
				  
				  //* compare with VBM
				
					   if((setVBM3!=null)&&(sSetFDM3!=null)) {
						  int INvbm3=sSetFDM3.compare(setVBM3);
						  int NOTinVBM3=sSetFDM3.lenght-INvbm3;
						  result.addData("FDM: 3-supersets NOT in VBM diags3", NOTinVBM3+ " ("+INvbm3+ " diagnoses from "+sSetFDM3.lenght+ " are in VBMdiags3)");
					   }
					   else
						  result.addData("FDM: 3-supersets NOT in VBM diags3", "- (no 3-fault VBMdiags)");
				  
				 
				  
				  writeToFile(result);
				  
				 
				  
				}
				rfile.close();
				}
			catch (Exception e) {
				e.printStackTrace();
				}
			
	   } catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		
	}
	
	private static void writeToFile(Result result) {
		try {
			if (!fileExists) {
				writer.write(Result.getColumnHeader());
				fileExists = true;
			}
			writer.write(result.toString());
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
