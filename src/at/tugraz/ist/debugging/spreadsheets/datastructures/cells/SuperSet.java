package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.util.HashSet;
import java.util.Set;

public class SuperSet {
public Set<Set<String>> allksupersets; 
public int lenght;

public SuperSet() {}

/** constructor to be used just if you want to transform a String depicting a set of diagnoses into a SET structure (there is in fact NO SUPER SET) 
 * */
public SuperSet(String diags) {
	String[] diags1 = diags.split("\\(");
		lenght=0;
		allksupersets = new HashSet<>(10000);
		
		String temp=new String();
		for (int i = 1; i <diags1.length; i++) {
			//the exact diagnosis
			temp=diags1[i].substring(0, diags1[i].indexOf(")"));
			Set<String> ksuperset=new HashSet();
			
			String[] cellD = temp.split(",");
			for(int k = 0; k <cellD.length; k++)
				ksuperset.add(cellD[k]);
			
			allksupersets.add(ksuperset);
		}
		
		lenght=allksupersets.size();
}

/**
 * constructor to be used for computing all k-fault diagnoses (UNION of the minimal SET and the non-minimal SET) for VBM and also for QDM and FDM
 * */
public SuperSet(SuperSet sSet, String diags, int card) {
	
	    SuperSet setDiags=new SuperSet(diags);
		SuperSet newSet=sSet;
		newSet.allksupersets.addAll(setDiags.allksupersets);
		newSet.lenght=newSet.allksupersets.size();
		this.allksupersets=newSet.allksupersets;
		this.lenght=newSet.lenght;
	
	
}

/**
 * constructor to be used when K>2, i.e., when we already have a computed SET= SUPER_SET + Computed_Diags for (k-1) 
 * */
public SuperSet(SuperSet set1, String allFormulas) {
	// First, transform the allFormulas cells, so that we have a common name schema for the elements of the supersets
		//1. replace the prefix "0!" and eliminate the []
		allFormulas=allFormulas.replaceAll("0!", "Sheet1!");
		allFormulas=allFormulas.substring(1, allFormulas.length()-1);
		//2. eliminate the "!" after column in every cell
		String[] cells = allFormulas.split(", ");
		String substr1,substr2;
		for (int i = 0; i <cells.length; i++) {
			substr1=cells[i].substring(0, cells[i].lastIndexOf("!"));
			substr2=cells[i].substring(cells[i].lastIndexOf("!")+1, cells[i].length());
			cells[i]=substr1.concat(substr2);
		}
		// END 2.

		// *combine the SET s1 with the cells
		lenght=0;
		allksupersets = new HashSet<>(10000);
		
		String kMinusSet=new String();
		for (Set<String> element1 : set1.allksupersets) {
			//the exact name of the cell in the diagnosis
			kMinusSet=element1.toString().substring(1, element1.toString().length()-1);
			String[] cellD = kMinusSet.split(", ");
			for (int j = 0; j <cells.length; j++) {
				if(kMinusSet.indexOf(cells[j])==-1) {
					Set<String> ksuperset=new HashSet<String>();
					for(int k = 0; k <cellD.length; k++) {
						ksuperset.add(cellD[k]);
					}
					ksuperset.add(cells[j]);
					allksupersets.add(ksuperset);
				}
			}
			
		}
		
		lenght=allksupersets.size();
		
		
		
		
}
/**
 * constructor used only for computing SUPERSets of cardinality 2 (K=2)!   IN (String) diags we have diagnoses of cardinality 1!
*/
public SuperSet(String diags, String allFormulas) {
	
	//** diags is a String with the form: (Sheet1!D3),(Sheet1!I11),..
	//** allFormulas is a String with the form:  [0!D!3, 0!I!11,..] !!! we have a space after ','
	
	// First, transform the allFormulas cells, so that we have a common name schema for the elements of the supersets
	//1. replace the prefix "0!" and eliminate the []
	allFormulas=allFormulas.replaceAll("0!", "Sheet1!");
	allFormulas=allFormulas.substring(1, allFormulas.length()-1);
	//2. eliminate the "!" after column in every cell
	String[] cells = allFormulas.split(", ");
	String substr1,substr2;
	for (int i = 0; i <cells.length; i++) {
		substr1=cells[i].substring(0, cells[i].lastIndexOf("!"));
		substr2=cells[i].substring(cells[i].lastIndexOf("!")+1, cells[i].length());
		cells[i]=substr1.concat(substr2);
	}
	// END 2.
	
	// *combine the diagnoses with the cells
	String[] diags1 = diags.split("\\(");
	lenght=0;
	allksupersets = new HashSet<>(10000);
	
	String temp=new String();
	for (int i = 1; i <diags1.length; i++) {
		//the exact name of the cell in the diagnosis
		temp=diags1[i].substring(0, diags1[i].indexOf(")"));
		
		for (int j = 0; j <cells.length; j++) {
			Set<String> ksuperset=new HashSet();
			
			ksuperset.add(temp);
			if(!temp.contains(cells[j])) {
				ksuperset.add(cells[j]);
				allksupersets.add(ksuperset);
				
			}
		}
		
	}
	
	lenght=allksupersets.size();
	
	
}
public static void main(String[] args) {
	
	// just for testing purposes
	SuperSet s1=new SuperSet("(Sheet1!D5,Sheet1!F6),(Sheet1!D3,Sheet1!F3)");
	SuperSet s2=new SuperSet("(Sheet1!D5,Sheet1!F6,Sheet1!D4),(Sheet1!D3,Sheet1!F3,Sheet1!I7),(Sheet1!D5,Sheet1!F6,Sheet1!D3)");
	
	SuperSet s3=new SuperSet("(Sheet1!D5),(Sheet1!D3)", "[0!D!3, 0!D!4, 0!D!5, 0!F!3, 0!F!6, 0!I!7, 0!I!11]");
	System.out.println(" s3: "+ s3.allksupersets.toString()+ "no super sets: "+ s3.allksupersets.size());
	System.out.println(" s1: "+ s1.allksupersets.toString()+ "no super sets: "+ s1.allksupersets.size());
	System.out.println(" IN: "+s3.compare(s1));
}

public int compare(SuperSet ss2) {
	// TODO Auto-generated method stub
	int out=0;
	for (final Set<String> element : this.allksupersets) {
		if(ss2.allksupersets.contains(element))
		{
			out++;
		}
		else {
			String diags1=element.toString();
			diags1=diags1.substring(1,diags1.lastIndexOf("]"));
			String[] cells = diags1.split(", ");
			
			for (final Set<String> element2 : ss2.allksupersets) {
			   String diags2=element2.toString();
			   boolean ok=true;
			   for (int j = 0; j <cells.length; j++)
				   if(!diags2.contains(cells[j]))
				   {
					   ok=false;
					   break;
				   }
			   if(ok) 
			   {
				   out++;
			   }
			}
		}
	}
	return out;
	
}
}
