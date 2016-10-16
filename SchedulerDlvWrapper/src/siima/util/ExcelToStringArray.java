/* ExcelToStringArray.java
 * 2013-11-01 VERSIO 3
 * (2013-02-22 VERSIO 2)
 * Transforming Excel file column to String array
 *  
 *  (code orig from: project ExcelJavaPOtest)
 */
package siima.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


//wanha import org.apache.poi.hssf.record.formula.functions.Column;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

public class ExcelToStringArray {

	//private String[]resultarray;
	private Workbook wb;
	private int sheetind=0;
	//??private Sheet sheet;
	private Column sourcecol;
	private Row sourcerow;
	private int firstcolind = 0;
	private int lastcolind = 0;
	//private int sourcerownum = 0;
	private int firstrowind = 0; // index starts from 0
	private int lastrowind = 0; // index starts from 0
	private int rowcount=0;
	
	//NEW
	//HANKALAA HALLITTAVAA NUO DESIMAALIT, 
	//JOITA EI INTEGEREISSÄ HALUAISI NÄKYÄ -> (true)
	//jos panee (TRUE) tällöin myös floatit pyöristyy kokonaisluvuiksi ??
	private boolean roundNumericalCells=false;
	//NEW formula=false -> value of the formula is read, not the formula
	private boolean formula=false; 
	
	public boolean isFormula() {
		return formula;
	}

	public void setFormula(boolean formula) {
		this.formula = formula;
	}

	public int getSheetIndex(String sheetname){
		
		return this.wb.getSheetIndex(sheetname);
	}
	
	public void setParameters(int sheetind, int firstcolind, int lastcolind, int firstrowind, int lastrowind,  boolean roundNumericalCells){
		/* NOTE: if parameter value is negative, old value is retained.
		 * Default boolean roundNumericalCells=false: Also integers (1) 
		 * will be presented with decimal point (1.0)
		 * 
		 */
		
		if(sheetind>=0){
			this.sheetind=sheetind;
		} else { //retain old value
			//this.sheetind=0;
		}			
		if((firstrowind>=0)){
			this.firstrowind=firstrowind;
		} else {
			////retain old value
			//this.firstrowind=0;	
		}		
		
		if((lastrowind>=0)){
			this.lastrowind=lastrowind;
		} else {
			////retain old value
			//this.lastrowind=0;		
		}
		
		if(this.lastrowind<this.firstrowind)this.lastrowind=this.firstrowind;
		
		
		if(firstcolind>=0){
			this.firstcolind=firstcolind;
		} else {//retain old value
			//this.firstcolind=0;
		}
		if(lastcolind>=0){
			this.lastcolind=lastcolind;
		} else {//retain old value
			//this.lastcolind=0;
		}
		
		if(this.lastcolind<this.firstcolind)this.lastcolind=this.firstcolind;
		
		
		this.roundNumericalCells=roundNumericalCells;
		/*
		this.sheetind=sheetind;
		this.firstcolind=firstcolind;
		this.lastcolind=lastcolind;
		this.firstrowind=firstrowind;
		this.lastrowind= lastrowind;
		*/
		
	}
	
	public String getSheetName(int sheetindex){
		int nums=wb.getNumberOfSheets();
		if(sheetindex<nums){
			Sheet st = wb.getSheetAt(sheetindex);
			if(st!=null)
				return (String)st.getSheetName();
		} 
		
		return null;
	}
	
	
	public String getSheetName(){
		
		return wb.getSheetAt(this.sheetind).getSheetName();
	}
	
	
	public int getLastRowNumInSheet(){
		int last=0;
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		last=sheet1.getLastRowNum();
		return last;
	}
	
	public int getLastCellNumInRow(int rowi){
		int last=0;
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		Row r=sheet1.getRow(rowi);
		last=r.getLastCellNum();
		return last;
	}

	
	public String getCellValue(int coli, int rowi){
		String value="";
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		Row r=sheet1.getRow(rowi);
		if(r!=null){
		Cell cell2=r.getCell(coli);
		if (cell2 != null) {			
			switch (cell2.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				value=cell2.getRichStringCellValue().getString();				
				break;
			case Cell.CELL_TYPE_NUMERIC:
				
				if(roundNumericalCells)
					value=Long.toString(Math.round(cell2.getNumericCellValue()));
				else value=Double.toString(cell2.getNumericCellValue());
	
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				value=Boolean.toString(cell2.getBooleanCellValue()) ;
				break;
			case Cell.CELL_TYPE_FORMULA:				
				//--- NEW
				if(this.formula) value=cell2.getCellFormula();
				else value=cell2.getRichStringCellValue().getString();
				//---
				break;
			default:
				System.out.println("UNKNOWN TYPE");
			}			
			
		} else {
			System.out.println("EMPTY CELL");
		}
		} else {
			System.out.println("EMPTY ROW");
		}	
		return value;
	}
	
	
	public List<String> toStringList(boolean also_nulls) {
		//NEW
		List<String> colList = new ArrayList<String>();
		String[] colarray=toStringArray();
		for(int i=0; i<colarray.length; i++){
			if((colarray[i]!=null)||(also_nulls))
				colList.add(colarray[i]);
		}				
		return colList;
	}
	
	
	public String[] toStringArray() {
		/* 
		 * Reads only the first column defined by firstcolind
		 * NOTE: Skips over empty rows, if all columns of that row 
		 * are really empty (not even border lines etc.)
		 * NOTE Does not Skips over empty, if some column of that row 
		 * contains something e.g border lines:
		 * SO: It is a good way to get row included is to but | border on that row!
		 * HOWEVER: Empty rows at the end of the array will leave the null-Strings into the array.
		 **/
		
		int i=0;
		String[] colarray = new String[rowcount];
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		for (Row row2 : sheet1) {

			if ((row2.getRowNum() >= firstrowind) && (row2.getRowNum() <= lastrowind)) {
				
				Cell cell2 = row2.getCell(firstcolind);
				if (cell2 != null) {

					CellReference cellRef = new CellReference(row2.getRowNum(),
							cell2.getColumnIndex());
	
					switch (cell2.getCellType()) {
					case Cell.CELL_TYPE_STRING:

						colarray[i]=cell2.getRichStringCellValue().getString();
						
						break;

					case Cell.CELL_TYPE_NUMERIC:
						// if(DateUtil.isCellDateFormatted(cell2)) { Tämä ei
						// toimi?
						// System.out.println(cell2.getDateCellValue());
						// } else {
						//System.out.println(cell2.getNumericCellValue());
						// }
						//TODO: OK?
						if(roundNumericalCells)
							colarray[i]=Long.toString(Math.round(cell2.getNumericCellValue()));
						else colarray[i]=Double.toString(cell2.getNumericCellValue());
						break;

					case Cell.CELL_TYPE_BOOLEAN:
						colarray[i]=Boolean.toString(cell2.getBooleanCellValue()) ;
						break;
					case Cell.CELL_TYPE_FORMULA:
						//--- NEW
						if(this.formula) colarray[i]=cell2.getCellFormula();
						else colarray[i]=cell2.getRichStringCellValue().getString();
						//---
						break;
					default:
						System.out.println();
					}
					
					
				} else {
					//TODO:Want to skip over empty lines?
					//colarray[i]="NIL";
				}
				
				i++;
				
			}
		}
		return colarray;
	}

	public String[] toFullRowStringArray(int colcount, boolean mysql_insert_line_marking) {
		/* 
		 * See Also: toFullRowStringArray(int colcount, String separator, String empty_mark, String prefix, String suffix)
		 * defaultmarking
		 * mysql_insert_line_marking.
		 */
		
		String[] colarray=null;
		
		String prefix = "(";
		String suffix = "),";
		String separator =",";
		String empty_mark="NIL";
		String lastrow_suffix=";\n";	
		
		if(mysql_insert_line_marking)
			colarray=toFullRowStringArray( colcount,  separator,  empty_mark,  prefix, suffix, lastrow_suffix);
		else
			colarray=toFullRowStringArray(colcount,  ",",  "",  "", "", "");
		return colarray;
	}
	
	
	
	public String[] toFullRowStringArray(int colcount, String separator, String empty_mark, String prefix, String suffix, String lastrow_suffix) {
		/* NOTE: starting from this.firstcolind.
		 * See Also: toFullRowStringArray(int colcount, boolean defaultmarking) 
		 * TOIMII
		 * PAITSI, jos haettavalla rivillä kaikki solut (myös hakusarakkeiden ulkopuolella)
		 * ovat tyhjiä, koko rivi jää väliin (eli ei myöskään noita täyttö null:jä).
		 * RIVI EI KUITENKAAN JÄÄ VÄLIIN, jos jollain hakualueen ulkopuolisella sarakkeella 
		 * on jotain (vaikka vain solun rajausviiva)?
		 * TÄMÄ ON OTETTAVA HUOMIOON taulukkoa purettaessa, 
		 * eli että se ei ole välttämättä "täynnä".
		 */
		//String prefix = "(";
		//String suffix = "),";
		//String separator =",";
		//String empty_mark="NIL";
		// String lastrow_suffix=";\n";
		boolean issfx=true;
		boolean islastsfx=true;
		if((suffix==null)||(suffix.isEmpty()))issfx=false;		
		if((lastrow_suffix==null)||(lastrow_suffix.isEmpty()))islastsfx=false;
		
		
		
		String[] colarray = new String[0];
		
		int i=0;
		//int colcount=3; //new
		if((this.rowcount>0)&&(colcount>0)){
		colarray = new String[rowcount];
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		for (Row row2 : sheet1) {

			if ((row2.getRowNum() >= firstrowind) && (row2.getRowNum() <= lastrowind)) {
				colarray[i]=prefix;
				
				for(int colind=firstcolind; colind<firstcolind+colcount; colind++){
					if(colind>firstcolind)colarray[i]+=separator;
					
				Cell cell2 = row2.getCell(colind);
				if (cell2 != null) {

					//CellReference cellRef = new CellReference(row2.getRowNum(),
					//		cell2.getColumnIndex());
					
	
					switch (cell2.getCellType()) {
					case Cell.CELL_TYPE_STRING:

						colarray[i]=colarray[i] + cell2.getRichStringCellValue().getString();
						//System.out.println(i + ") CELL_TYPE_STRING: " + cell2.getStringCellValue());
						
						break;

					case Cell.CELL_TYPE_NUMERIC:
						// if(DateUtil.isCellDateFormatted(cell2)) { Tämä ei
						// toimi?
						// System.out.println(cell2.getDateCellValue());
						// } else {
						//System.out.println(cell2.getNumericCellValue());
						// }
						/* HANKALAA HALLITTAVAA NUO DESIMAALIT, JOITA EI INTEGEREISSÄ HALUAISI NÄKYÄ */
						if(roundNumericalCells)
							colarray[i]=Long.toString(Math.round(cell2.getNumericCellValue()));
						else colarray[i]=Double.toString(cell2.getNumericCellValue());
						//System.out.println(i + ") CELL_TYPE_NUMERIC: " + Long.toString(Math.round(cell2.getNumericCellValue())));
						
						break;

					case Cell.CELL_TYPE_BOOLEAN:
						colarray[i]=colarray[i] + Boolean.toString(cell2.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						//--- NEW
						if(this.formula) colarray[i]=colarray[i] + cell2.getCellFormula();
						else colarray[i]=colarray[i] + cell2.getRichStringCellValue().getString();
						//---
						//colarray[i]=colarray[i] + cell2.getCellFormula();
						break;
					default:
						System.out.println("UNKNOWN CELL TYPE");
					}
					
					
				} else {
					colarray[i]=colarray[i] + empty_mark;
				}
				
			} // column for
				colarray[i]+=suffix;
				i++;
				
			}
		} //end row loop
		
		if((issfx)&&(islastsfx)) //replace suffix by last_suffix
			colarray[i-1]=colarray[i-1].substring(0,colarray[i-1].length()-1 ) + lastrow_suffix;
		else if(islastsfx) colarray[i-1]=colarray[i-1] + lastrow_suffix; //add last_suffix
		} // end if
		System.out.println("METHOD: toFullRowStringArray: ROWS total: " + i);
		return colarray;
	}
	

	public void toStringPrint() {
		Sheet sheet1 = wb.getSheetAt(this.sheetind);
		for (Row row2 : sheet1) {

			if ((row2.getRowNum() >= firstrowind) && (row2.getRowNum() <= lastrowind)) {
				Cell cell2 = row2.getCell(firstcolind);
				if (cell2 != null) {

					// CellReference cellRef = new
					// CellReference(row2.getRowNum(), cell2.getCellNum());
					CellReference cellRef = new CellReference(row2.getRowNum(),
							cell2.getColumnIndex());
					System.out.print(cellRef.formatAsString());
					System.out.print(" - ");

					switch (cell2.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						System.out.println(cell2.getRichStringCellValue()
								.getString());
						break;

					case Cell.CELL_TYPE_NUMERIC:
						// if(DateUtil.isCellDateFormatted(cell2)) { Tämä ei
						// toimi?
						// System.out.println(cell2.getDateCellValue());
						// } else {
						System.out.println(cell2.getNumericCellValue());
						// }
						break;

					case Cell.CELL_TYPE_BOOLEAN:
						System.out.println(cell2.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						//--- NEW
						if(this.formula) System.out.println(cell2.getCellFormula());
						else System.out.println(cell2.getRichStringCellValue().getString());
						//---
						//System.out.println(cell2.getCellFormula());
						break;
					default:
						System.out.println("UNKNOWN TYPE");
					}
				} else {
					//empty cell
					System.out.println("NIL");
				}
			}
		}

	}

	/* ************
	 * CONSTRUCTORS
	 * 
	 */
	
	public ExcelToStringArray(String xlsname) {
		// index and num starts from 0
		FileInputStream inp;
				
		try {
			inp = new FileInputStream(xlsname);
			wb = WorkbookFactory.create(inp);
						
				this.sheetind=0;
				this.firstrowind=0;
				this.lastrowind=0;				
				this.firstcolind=0;			
				this.lastcolind=0;
			
			this.rowcount=lastrowind-firstrowind+1;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	public ExcelToStringArray(String xlsname, int sheetind) {
		// index and num starts from 0
		FileInputStream inp;
		
		
		try {
			inp = new FileInputStream(xlsname);

			wb = WorkbookFactory.create(inp);
			
			if(sheetind>=0){
				this.sheetind=sheetind;
			} else {
				this.sheetind=0;
			}

			//??sheet = wb.getSheetAt(this.sheetind);
					
				this.firstrowind=0;
				this.lastrowind=0;				
				this.firstcolind=0;			
				this.lastcolind=0;
			
			this.rowcount=lastrowind-firstrowind+1;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public ExcelToStringArray(String xlsname, int sheetind, 
			int firstcolnum, int lastcolnum, int firstrownum, int lastrownum, boolean roundNumericalCells) {
		// index and num starts from 0
		FileInputStream inp;
		
		
		try {
			inp = new FileInputStream(xlsname);

			wb = WorkbookFactory.create(inp);
			
			if(sheetind>=0){
				this.sheetind=sheetind;
			} else {
				this.sheetind=0;
			}			
			if((firstrownum>=0)&&(lastrownum>=firstrownum)){
				this.firstrowind=firstrownum;
				this.lastrowind=lastrownum;
			} else {
				this.firstrowind=0;
				this.lastrowind=0;		
			}
			if(firstcolnum>=0){
				this.firstcolind=firstcolnum;
			} else {
				this.firstcolind=0;
			}
			if(lastcolnum>=0){
				this.lastcolind=lastcolnum;
			} else {
				this.lastcolind=0;
			}
			
			this.roundNumericalCells=roundNumericalCells;
			rowcount=lastrowind-firstrowind+1;
			//resultarray = new String[lastrowind-firstrowind+1];
			//??sheet = wb.getSheetAt(this.sheetind);
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCellArea(int firstcolind,int lastcolind,int firstrowind,int lastrowind){
		setFirstcolind(firstcolind);
		setLastcolind(lastcolind);
		setFirstrowind(firstrowind);
		setLastrowind(lastrowind);
	}
	
	public void setColumnInterval(int firstcolind,int lastcolind){
		setFirstcolind(firstcolind);
		setLastcolind(lastcolind);
	}
	public void setRowInterval(int firstrowind,int lastrowind){
	
		setFirstrowind(firstrowind);
		setLastrowind(lastrowind);
	}
	
	
	
	public int getFirstcolind() {
		return firstcolind;
	}

	public void setFirstcolind(int firstcolind) {
		this.firstcolind = firstcolind;
	}

	public int getLastcolind() {
		return lastcolind;
	}

	public void setLastcolind(int lastcolind) {
		this.lastcolind = lastcolind;
	}

	

	public int getFirstrowind() {
		return firstrowind;
	}

	public void setFirstrowind(int firstrowind) {
		this.firstrowind = firstrowind;
		this.rowcount=this.lastrowind-this.firstrowind+1;
	}

	public int getLastrowind() {
		return lastrowind;
	}

	public void setLastrowind(int lastrowind) {
		this.lastrowind = lastrowind;
		this.rowcount=this.lastrowind-this.firstrowind+1;
	}

	public int getSheetind() {
		return sheetind;
	}

	public void setSheetind(int sheetind) {
		this.sheetind = sheetind;
	}

	public boolean isRoundNumericalCells() {
		return roundNumericalCells;
	}

	public void setRoundNumericalCells(boolean roundNumericalCells) {
		this.roundNumericalCells = roundNumericalCells;
	}

}
