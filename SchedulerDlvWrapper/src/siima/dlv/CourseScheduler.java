/* CourseScheduler.java
 * 205-03-23 from HP
 * Configuration in excel courses_real.xlsx 
 */

package siima.dlv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import siima.dlv.utils.ModelParserHelp;
import siima.util.ExcelToStringArray;
import siima.util.FileUtil;
import it.unical.mat.dlv.program.Program;
import it.unical.mat.dlv.program.Rule;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import it.unical.mat.wrapper.ModelBufferedHandler;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;

public class CourseScheduler {

	private ExcelToStringArray ex2s;
	private Map<String, String> configuremap = null;
	private Map<String, String> codeperiodmap = null;
	private List<String> firstperiodevents = null;
	private List<String> doubleperiodevents = null;
	private List<String> secondperiodevents = null;
	private List<String> seconddoubleperiodevents = null;
	private List<String> thirdperiodevents = null;
	private List<String> tripleperiodevents = null;

	public CourseScheduler(String xlsname) {

		this.ex2s = new ExcelToStringArray(xlsname);
		this.configuremap = new HashMap<String, String>();
		this.codeperiodmap = new HashMap<String, String>();
		this.firstperiodevents = new ArrayList<String>();
		this.doubleperiodevents = new ArrayList<String>();
		this.secondperiodevents=new ArrayList<String>();
		this.seconddoubleperiodevents = new ArrayList<String>();
		this.thirdperiodevents=new ArrayList<String>();
		this.tripleperiodevents = new ArrayList<String>();
		
	}
	
	public String[] readPredefinedSchedulesFromExcel(String season, int runnumber, String sheetname, int firstcolind,
			int lastcolind, int firstrowind, int lastrowind) {
		/* ----- READING PREDEFINED (FIXED) COURSE SCHEDULEs FROM EXCEL -------- */		

		String[] fixedscheds;
		String[] periods;
		List<String> predefinedslots = new ArrayList<String>();
		int readrows = lastrowind - firstrowind + 1;
		
		this.ex2s.setSheetind(this.ex2s.getSheetIndex(sheetname)); // ("Configure"));
		this.ex2s.setCellArea(firstcolind, lastcolind, firstrowind, lastrowind); //
		fixedscheds = this.ex2s.toStringArray();

		/* --- Mapping property values --- */
	
		this.ex2s.setCellArea(firstcolind + 1, lastcolind + 1, firstrowind,	lastrowind); //
		periods = this.ex2s.toStringArray();
		
		for (int i = 0; i < readrows; i++) { 
			if ((!"null".equalsIgnoreCase(fixedscheds[i]))
					&& (!"null".equalsIgnoreCase(fixedscheds[i])) && fixedscheds[i]!=null && fixedscheds[i]!=null) {				
				
				if("spring".equalsIgnoreCase(season)){
					if((runnumber==1)&&(("iii".equalsIgnoreCase(periods[i]))||("iii-iv".equalsIgnoreCase(periods[i]))||("iii-v".equalsIgnoreCase(periods[i])))) predefinedslots.add(fixedscheds[i]);
					else if((runnumber==2)&&(("iv".equalsIgnoreCase(periods[i]))||("iii-iv".equalsIgnoreCase(periods[i]))||("iii-v".equalsIgnoreCase(periods[i]))||("iv-v".equalsIgnoreCase(periods[i])))) predefinedslots.add(fixedscheds[i]);
					else if((runnumber==3)&&(("v".equalsIgnoreCase(periods[i]))||("iii-v".equalsIgnoreCase(periods[i]))||("iv-v".equalsIgnoreCase(periods[i])))) predefinedslots.add(fixedscheds[i]);
				} else { //fall
					if((runnumber==1)&&(("i".equalsIgnoreCase(periods[i]))||("i-ii".equalsIgnoreCase(periods[i])))) predefinedslots.add(fixedscheds[i]);
					if((runnumber==2)&&(("ii".equalsIgnoreCase(periods[i]))||("i-ii".equalsIgnoreCase(periods[i])))) predefinedslots.add(fixedscheds[i]);
					else if(runnumber==3) System.out.println("readFixedSchedulesFromExcel:There shoud be No 3th run for fall period");										
				}
				//System.out.println(">>>PREDEFINED SLOTS IN EXCEL:" + i + ":" + fixedscheds[i] + ":" + periods[i]);						
			}
		} //end for
		
		return predefinedslots.toArray(new String[predefinedslots.size()]);
		
	}


	
	
	public void readConfigureDataFromExcel(String sheetname, int firstcolind,
			int lastcolind, int firstrowind, int lastrowind) {
		/*----- READING CONFIGURATION METADATA FROM EXCEL -------- */		

		String[] properties;
		String[] values;
		int readrows = lastrowind - firstrowind + 1;
		
		this.ex2s.setSheetind(this.ex2s.getSheetIndex(sheetname)); // ("Configure"));
		this.ex2s.setCellArea(firstcolind, lastcolind, firstrowind, lastrowind); //
		properties = this.ex2s.toStringArray();

		/* --- Mapping property values --- */
	
		this.ex2s.setCellArea(firstcolind + 1, lastcolind + 1, firstrowind,	lastrowind); //
		values = this.ex2s.toStringArray();
		
		for (int i = 0; i < readrows; i++) { 
			if ((!"null".equalsIgnoreCase(properties[i]))
					&& (!"null".equalsIgnoreCase(properties[i])) && properties[i]!=null && properties[i]!=null) {				
				this.configuremap.put(properties[i], values[i]);			
				System.out.println("CONFIGURE:" + i + ":" + properties[i] + "" + values[i]);						
			}
		} //end for
	}

	

	public String[] readCourseDataFromExcel(String season, String sheetname, int firstcolind,
			int lastcolind, int firstrowind, int lastrowind) {
		/*----- READING INPUT DATA FACTS FROM EXCEL -------- */
		// Map<String, String> cpmap= new HashMap<String, String>();

		String[] allevents;
		List<String> periodevents = new ArrayList<String>();

		int readrows = lastrowind - firstrowind + 1;
		/* --- read course data from excel --- */
		this.ex2s.setSheetind(this.ex2s.getSheetIndex(sheetname)); // ("Courses"));
		// firstcolind, lastcolind, firstrowind, lastrowind
		this.ex2s.setCellArea(firstcolind, lastcolind, firstrowind, lastrowind); //
		allevents = this.ex2s.toStringArray();

		/* --- Mapping course periods --- */
		// TODO:
		String[] code;
		String[] period;
		this.ex2s.setCellArea(firstcolind + 1, lastcolind + 1, firstrowind,
				lastrowind); //
		code = this.ex2s.toStringArray();
		//System.out.println("HHHHHHHHHHHHHHHH" + code.length + "["
		//		+ code[code.length - 1] + "]");
		this.ex2s.setCellArea(firstcolind + 2, lastcolind + 2, firstrowind,
				lastrowind); //
		period = this.ex2s.toStringArray();
		//System.out.println("PPPPPPPPPPPPPPPP" + period.length + "["
		//		+ period[period.length - 1] + "]");
		for (int i = 0; i < readrows; i++) { // code.length
			//System.out.println(">>>>>>>>>>>>>>:" + readrows + "==" + period[i]);
			if ((!"null".equalsIgnoreCase(code[i]))
					&& (!"null".equalsIgnoreCase(period[i])) && code[i]!=null && period[i]!=null) {				
				this.codeperiodmap.put(code[i], period[i]);			
				String whichperiod = period[i].toLowerCase();
				
				if(("fall".equalsIgnoreCase(season))||("autumn".equalsIgnoreCase(season))){
				 switch (whichperiod) {
			        case "i":
			        	periodevents.add(allevents[i]);
			        	this.firstperiodevents.add(allevents[i]);
			           //System.out.println("" + whichperiod + ": = i ");
			            break;
			        case "i-ii":
			        	periodevents.add(allevents[i]);
			        	this.doubleperiodevents.add(allevents[i]);
			        	//System.out.println("" + whichperiod + ":");
			            break;
			        case "ii":
			        	this.secondperiodevents.add(allevents[i]);
			        	//System.out.println("" + whichperiod + ":");
			            break;
			        default:
			        	System.out.println("Invalid period:" + whichperiod);			        
			        	break;
			    } 
				
				} else 	if(("spring".equalsIgnoreCase(season))){
					 switch (whichperiod) {
				        case "iii":
				        	periodevents.add(allevents[i]);
				        	this.firstperiodevents.add(allevents[i]);
				           //System.out.println("" + whichperiod + ": = i ");
				            break;
				        case "iii-iv":
				        	periodevents.add(allevents[i]);
				        	this.doubleperiodevents.add(allevents[i]);
				        	//System.out.println("" + whichperiod + ":");
				            break;
				        case "iv":
				        	this.secondperiodevents.add(allevents[i]);
				        	//System.out.println("" + whichperiod + ":");
				            break;
				        case "iv-v":
				        	this.seconddoubleperiodevents.add(allevents[i]);
				        	//System.out.println("" + whichperiod + ":");
				            break;
				        case "v":
				        	this.thirdperiodevents.add(allevents[i]);
				        	//System.out.println("" + whichperiod + ":");
				            break;
				        case "iii-v":
				        	this.tripleperiodevents.add(allevents[i]);
				        	//System.out.println("" + whichperiod + ":");
				            break;
				        default:
				        	System.out.println("Invalid period:" + whichperiod);			        
				        	break;
				    }
				} else {
					System.out.println("Invalid season:" + season);	
				}
			}
		} //end for

		return periodevents.toArray(new String[periodevents.size()]);
		// return allevents;

	}

	public void writeScheduleToExcel(String[] linesToWrite, String excelfile,
			String sheetname, int rownum, int firstcellnum) {
		/*
		 * http://stackoverflow.com/questions/20694915/writing-on-an-existing-excel
		 * -file-using-apachi-poi
		 * 
		 * FileInputStream file = new FileInputStream(new
		 * File("C:\\Users\\onu\\test.xlsx"));
		 * 
		 * XSSFWorkbook workbook = new XSSFWorkbook(file); XSSFSheet sheet =
		 * workbook.getSheetAt(0); Cell cell = null;
		 * 
		 * //Update the value of cell cell = sheet.getRow(0).getCell(3); // in
		 * excel this is cell D1 cell.setCellValue("onu"); // line 28
		 * 
		 * file.close();
		 * 
		 * FileOutputStream outFile =new FileOutputStream(new
		 * File("C:\\Users\\onu\\test.xlsx")); workbook.write(outFile);
		 * outFile.close();
		 * 
		 * ----- //Update the value of cell cell = sheet.getRow(0).getCell(3);
		 * // in excel this is cell D1 cell.setCellValue("onu"); // line 28
		 * 
		 * if (cell == null) { cell = sheet.getRow(0).createCell(3); } // Then
		 * set the value. cell.setCellValue("onu");
		 * 
		 * cell = sheet.getRow(0).getCell(3,
		 * Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		 * 
		 * 
		 * 
		 * from:
		 * http://stackoverflow.com/questions/20694915/writing-on-an-existing
		 * -excel-file-using-apachi-poi
		 */
		// from Timesheetdemo.java
		Workbook wb;
		try {

			FileInputStream file = new FileInputStream(new File(excelfile)); // ("data/scheduler/testi.xlsx"));
			String[] ssp = excelfile.split("\\.");
			String extype = ssp[ssp.length - 1];
			// System.out.println("SSSSSSSSS" + extype);
			if ("xls".equals(extype))
				wb = new HSSFWorkbook(file);
			else
				wb = new XSSFWorkbook(file);

			Sheet sheet = wb.getSheet(sheetname);
			if (sheet == null) {
				sheet = wb.createSheet(sheetname);
			}
			// title row
			Row titleRow = sheet.createRow(0);
			titleRow.setHeightInPoints(45);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("Weekly Timesheet");
			int maxcells = linesToWrite.length;
			int maxslots = 20;
			// int slotspercol=4; 
			// int rowcount=slotspercol;
			//int firstcellnum = 4;
			int cellcnt = 0;
			// int colcount=1;
			//int rownum = 2;
			// int ci=0;

			while (cellcnt < maxcells) {
				Row row = sheet.createRow(rownum++);
				for (int j = firstcellnum; j < maxslots + firstcellnum; j++) {
					Cell cell = row.createCell(j);
					cellcnt++;
					if (linesToWrite[cellcnt - 1] != null) {
						cell.setCellValue(linesToWrite[cellcnt - 1]);
					} else
						cell.setCellValue("-"); // ("none(" + cellcnt + ")");
				}
			}

			/*
			 * Write the output to a file String file = excelfile + ".xls";
			 * if(wb instanceof XSSFWorkbook) file += "x"; FileOutputStream out;
			 */

			/*
			 * Recalculation of Formulas
			 * http://poi.apache.org/spreadsheet/eval.html
			 */
			wb.setForceFormulaRecalculation(true);

			FileOutputStream outFile = new FileOutputStream(new File(excelfile));
			wb.write(outFile);
			outFile.close();
			/*
			 * out = new FileOutputStream(file); wb.write(out); out.close();
			 */

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Rule> getFixedSchedules(List<Rule> factrules, List<String> eventcodes, List<String> fl, String season, String forrun ){
		// From factrules (not from excel)
		List<Rule> fixedrules= new ArrayList<Rule>();
		
		for (int i = 0; i < eventcodes.size(); i++) {
			System.out
					.println(i + ": EVENT CODE: " + eventcodes.get(i));
			String[] splits = eventcodes.get(i).split("_");
			if (splits.length > 1) {
				String coursecode = splits[0] + "_" + splits[1];
				String period = this.codeperiodmap.get(coursecode);
				System.out.println(i + ": COURSE CODE: " + coursecode
						+ ": PERIOD: " + period);
				String inslotlit = fl.get(i);
				
				if(!"spring".equalsIgnoreCase(season)){// FALL
					
				if ("secondrun".equalsIgnoreCase(forrun) && "i-ii".equalsIgnoreCase(period)) {
					fixedrules.add(factrules.get(i));
					System.out.println(i
							+ ": FIXED FACT FOR NEXT PERIOD: "
							+ inslotlit + ": PERIOD: " + period);
					System.out.println(i
							+ ": FIXED RULE FOR NEXT PERIOD: "
							+ factrules.get(i).toString()
							+ ": PERIOD: " + period);
				}

				} else { // Spring
					
					if ("secondrun".equalsIgnoreCase(forrun) && ("iii-iv".equalsIgnoreCase(period) || "iii-v".equalsIgnoreCase(period))) {
						fixedrules.add(factrules.get(i));
						System.out.println(i
								+ ": FIXED FACT FOR NEXT PERIOD: "
								+ inslotlit + ": PERIOD: " + period);
						System.out.println(i
								+ ": FIXED RULE FOR NEXT PERIOD: "
								+ factrules.get(i).toString()
								+ ": PERIOD: " + period);
					}
					if ("thirdrun".equalsIgnoreCase(forrun) && ("iv-v".equalsIgnoreCase(period) || "iii-v".equalsIgnoreCase(period))) {
						fixedrules.add(factrules.get(i));
						System.out.println(i
								+ ": FIXED FACT FOR NEXT PERIOD: "
								+ inslotlit + ": PERIOD: " + period);
						System.out.println(i
								+ ": FIXED RULE FOR NEXT PERIOD: "
								+ factrules.get(i).toString()
								+ ": PERIOD: " + period);
					}
					
				}
				
			} else
				System.out
						.println(i + ": WHAT IS THIS??: " + splits[0]);
		}
		return fixedrules;
	}
	

	public static void main(String[] args) {

		
		/*
		 * Input data excel
		 */
		String inputdatafile = "data/scheduler/courses_real.xlsx"; // course.xlsx";
		
		String dlvsolver; //="C:/SpecialPrograms/dlv/dlv.mingw.exe";
		String outputdatafile; // = "data/scheduler/schedules_real.xlsx";
		String dlvcodefile; // = "data/scheduler/scheduler_dlvcode"; 	//dlvtest.txt"
		String dlvfactfile; // = "data/scheduler/scheduler_dlvfacts.db"; 	//dlvtest.db"
		
		//Number of models MUST BE ONE (because of prs.parseHandlerBuffer()-method, which collects predicates from all models (TODO?))
		int models = 1; //Number of models to be inferred: MUST BE ONE 
		String str_models;
		FileUtil fileutil = new FileUtil();

		CourseScheduler cs = new CourseScheduler(inputdatafile);

		/*----- READING CONFIGURE METADATA FROM EXCEL -------- */
		cs.readConfigureDataFromExcel("Configure", 0, 0, 10, 30);
		
		dlvsolver=cs.configuremap.get("dlvsolver:");
		dlvcodefile=cs.configuremap.get("dlvcodefile:");
		dlvfactfile=cs.configuremap.get("dlvfactfile:");
		outputdatafile=cs.configuremap.get("outputdatafile:");
		
		/*Number of models MUST BE ONE (because of prs.parseHandlerBuffer()-method, which collects predicates from all models (TODO?))
		str_models=cs.configuremap.get("models:");
		int tmp=0;
		if(str_models!=null){ 
			tmp=Integer.parseInt(str_models);
			if(tmp>0 && tmp<100) models=tmp;
			}
		*/
		
		/*----- READING INPUT DATA FACTS FROM EXCEL -------- */
		String[] coursedata;
		String[] teacherdata;
		String[] majordata;
		String[] predefinedslots;
		String[] auxfactdata;
		/*
		 * --- read course data from excel ---
		 * cs.ex2s.setSheetind(cs.ex2s.getSheetIndex("Courses")); //(0); //
		 * firstcolind, lastcolind, firstrowind, lastrowind
		 * cs.ex2s.setCellArea(0, 0, 9, 49); coldata = cs.ex2s.toStringArray();
		 * // String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		 */
		String season=cs.configuremap.get("season:"); //"spring"; //spring
		String schedulesheet; //"ScheduleFall";
		if("autumn".equalsIgnoreCase(season) || "fall".equalsIgnoreCase(season)) schedulesheet=cs.configuremap.get("fallsheet:"); //"ScheduleFall";
		else schedulesheet=cs.configuremap.get("springsheet:"); //"ScheduleSpring";
		coursedata = cs.readCourseDataFromExcel(season,cs.configuremap.get("coursesheet:"), 0, 0, 4, 100); //"Courses"
		
		// write to .db file 
		String[] firstpers=cs.firstperiodevents.toArray(new String[cs.firstperiodevents.size()]);
		String[] doublepers=cs.doubleperiodevents.toArray(new String[cs.doubleperiodevents.size()]);
		
		
		fileutil.writeLinesToFile(dlvfactfile, firstpers, false);
		fileutil.writeLinesToFile(dlvfactfile, doublepers, true);
		if(("spring".equalsIgnoreCase(season))){
			String[] triplepers=cs.tripleperiodevents.toArray(new String[cs.tripleperiodevents.size()]);
			fileutil.writeLinesToFile(dlvfactfile, triplepers, true);
		}
		
		//fileutil.writeLinesToFile(dlvfactfile, coursedata, false);

		/* --- read teacher data from excel --- */
		cs.ex2s.setSheetind(cs.ex2s.getSheetIndex(cs.configuremap.get("teachersheet:"))); //"Teachers")); // (1)
		// firstcolind, lastcolind, firstrowind, lastrowind
		cs.ex2s.setCellArea(0, 0, 4, 100);
		teacherdata = cs.ex2s.toStringArray();
		// String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		fileutil.writeLinesToFile(dlvfactfile, teacherdata, true);

		/* --- read major data from excel --- */
		cs.ex2s.setSheetind(cs.ex2s.getSheetIndex(cs.configuremap.get("majorsheet:"))); //"Majors")); // (3)
		// firstcolind, lastcolind, firstrowind, lastrowind
		cs.ex2s.setCellArea(0, 0, 2, 100);
		majordata = cs.ex2s.toStringArray();
		// String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		fileutil.writeLinesToFile(dlvfactfile, majordata, true);
		
		/* --- read predefined fixed schedules from excel --- */
		predefinedslots=cs.readPredefinedSchedulesFromExcel(season, 1, cs.configuremap.get("predefinedslotssheet:"), 0, 0, 10, 50); 
		fileutil.writeLinesToFile(dlvfactfile, predefinedslots, true);
		
		/* --- read other predefined facts that are the same for each period (eg. day preferences) from excel --- */
		cs.ex2s.setSheetind(cs.ex2s.getSheetIndex(cs.configuremap.get("auxfactsheet:"))); // e.g. day preferences
		cs.ex2s.setCellArea(0, 0, 10, 59);
		auxfactdata = cs.ex2s.toStringArray();
		fileutil.writeLinesToFile(dlvfactfile, auxfactdata, true);

		/*----- RUNNING DLV SCHEDULER PROGRAM -------- */

		/*
		 * dlv.mingw.exe -silent -filter=inslot -costbound=40,8,_ -n=1
		 * ./work/Course_scheduler_beta1
		 */

		ModelParserHelp prs = new ModelParserHelp();
		List<DLVError> inverrors = new ArrayList<DLVError>();

		try {
			/* I create a new instance of DLVInputProgram */
			DLVInputProgram inputProgram = new DLVInputProgramImpl();

			/* I can add some file to the DLVInputProgram */

			// inputProgram.addFile("data/scheduler/Course_scheduler_beta1_mod");
			inputProgram.addFile(dlvcodefile); //"data/scheduler/dlvtest.txt");
			inputProgram.addFile(dlvfactfile); //"data/scheduler/dlvtest.db");

			// ERRORS:[line 1: syntax error., Aborting due to parser errors.,
			// DLV Error, exitValue=1]
			// String timeperiod =
			// " :~ inslot(C1,S), inslot(E2,S), C1 != E2, crsex(C1,E2). [1:2]";
			//String timelimit = "exer(java).";

			/* VPA */
			Program actionprogram = new Program();
			// actionprogram.add(new Rule(timeperiod));
			// toimii: actionprogram.add(new Rule(timelimit));
			// actionprogram.addAll(takeArules);
			inputProgram.includeProgram(actionprogram);

			/*
			 * I create a new instance of DLVInvocation using the DLVWrapper
			 * class specifying a path for DLV executable
			 */
			DLVInvocation invocation = DLVWrapper.getInstance()
					.createInvocation(dlvsolver); //"C:/SpecialPrograms/dlv/dlv.mingw.exe");

			invocation.setInputProgram(inputProgram);

			invocation.addOption("-silent");
			List<String> filters = new ArrayList();
			filters.add("inslot");
			invocation.setFilter(filters, true);
			invocation.addOption("-costbound=40,8,_"); //"-costbound=40,8,_")
			invocation.setNumberOfModels(models);

			ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(
					invocation);
			
			/* RUNING DLV: In this moment I can start the DLV execution */
			invocation.run();
			/* Scroll all models computed */
			//int hm = 0; // models
			//int ip = 0; // predicates
			//int jl = 0; // literals

			/* If i wont to wait the finish of execution, i can use thi method */
			invocation.waitUntilExecutionFinishes();
			inverrors = invocation.getErrors();
			if (inverrors.size() > 0)
				System.out.println("ERRORS:" + inverrors.toString());

			List<Rule> factrules = prs.parseHandlerBuffer(modelBufferedHandler,
					"inslot", null);
		

			/**** 1. DLV RUN SHOULD HAVE ENDED ****************/
			String state = invocation.getState().toString();
			if (state.equals("FINISHED")) {

				// rowsticks= mari.parseOnRowLiterals("onRow", timelimitvalue);
				List<String> fl = prs.first_literals;
				//String[] sa = fl.toArray(new String[fl.size()]);

				// TODOOOO: järjestys inslot(a1, 20) HUOM overrides
				String pred = "inslot";
				String regex = "(" + pred + ")(\\((\\S*),\\s(\\d{1,2})\\))";
				String[] orderedlits = prs.orderLiteralsByTerm("", 0, fl,
						regex, 4, 3, 20);

				/***** WRITE 1. RUN RESULTS TO SCHEDULE EXCEL **********/
				cs.writeScheduleToExcel(orderedlits,outputdatafile
						, schedulesheet, 2, 4); // testi.xlsx", "data/scheduler/schedules_real.xlsx"

				
				 /* ********* PREPARE NEXT RUN for NEXT PERIOD
				  *  
				 * inslot-literals for next period 
				 * The schedules of the double period courses must be fixed
				 * to be the same on the second period also.
				 */
				
				// write .db file again
				//String[] doublepers=cs.doubleperiodevents.toArray(new String[cs.doubleperiodevents.size()]);
				String[] secondpers=cs.secondperiodevents.toArray(new String[cs.secondperiodevents.size()]);
				fileutil.writeLinesToFile(dlvfactfile, doublepers, false);
				fileutil.writeLinesToFile(dlvfactfile, secondpers, true);
				if(("spring".equalsIgnoreCase(season))){
					String[] triplepers=cs.tripleperiodevents.toArray(new String[cs.tripleperiodevents.size()]);
					fileutil.writeLinesToFile(dlvfactfile, triplepers, true);
				}			
				fileutil.writeLinesToFile(dlvfactfile, teacherdata, true);
				fileutil.writeLinesToFile(dlvfactfile, majordata, true);
				
				/* --- read predefined fixed schedules from excel --- */
				predefinedslots=cs.readPredefinedSchedulesFromExcel(season, 2, cs.configuremap.get("predefinedslotssheet:"), 0, 0, 10, 50); 
				fileutil.writeLinesToFile(dlvfactfile, predefinedslots, true);
				
				/* --- auxilary facts e.g. day preferences --- */
				fileutil.writeLinesToFile(dlvfactfile, auxfactdata, true);
				
				/* Scheduled inslot fact rules to be used also on the next period */
				List<Rule> fixedrules = new ArrayList<Rule>();
				// parseTerms(String pred, String regexp, int matchgroup,
				// List<String> pred_literals)
				List<String> eventcodes = prs.parseTerms("inslot", null, 3, fl);			
				fixedrules = cs.getFixedSchedules(factrules, eventcodes, fl, season, "secondrun" );
				
				/*** NEXT RUN (SECOND) for period II ***/

				invocation.reset();
				// invocation.resetMaxint();
				inputProgram.removeProgram(actionprogram);
				actionprogram = new Program();
				actionprogram.addAll(fixedrules);
				inputProgram.includeProgram(actionprogram);
				invocation.setInputProgram(inputProgram);

				invocation.addOption("-silent");
				invocation.setFilter(filters, true);
				invocation.addOption("-costbound=40,8,_");
				invocation.setNumberOfModels(models);

				invocation.run();

				invocation.waitUntilExecutionFinishes();
				inverrors = invocation.getErrors();
				if (inverrors.size() > 0)
					System.out.println("ERRORS:" + inverrors.toString());

				factrules = prs.parseHandlerBuffer(modelBufferedHandler,
						"inslot", null);
				/* print
				if (factrules.size() > 0) {
					System.out.print("Literal:{"
							+ factrules.get(factrules.size() - 1).toString()
							+ "}\n");
					for (int i = 0; i < factrules.size(); i++) {
						System.out.print(factrules.get(i).toString() + "\n");
					}
				}
				*/

				/**** 2. DLV RUN SHOULD HAVE ENDED ****************/
				state = invocation.getState().toString();
				if (state.equals("FINISHED")) {					
					fl = prs.first_literals;
					//  järjestys inslot(a1, 20) HUOM overrides
					pred = "inslot";
					regex = "(" + pred + ")(\\((\\S*),\\s(\\d{1,2})\\))";
					orderedlits = prs.orderLiteralsByTerm("", 0, fl, regex, 4, 3, 20);

					/***** WRITE 2. RUN RESULTS TO SCHEDULE EXCEL **********/
					cs.writeScheduleToExcel(orderedlits,outputdatafile
							, schedulesheet, 32, 4); //"data/scheduler/schedules_real.xlsx"
					
					
					
					 /* ********* PREPARE NEXT RUN (Third)for NEXT PERIOD (in autumn season)
					  *  
					 * inslot-literals for next period 
					 * The schedules of the double period courses must be fixed
					 * to be the same on the second period also.
					 */
					if(("spring".equalsIgnoreCase(season))){
					// write .db file again
					//String[] doublepers=cs.doubleperiodevents.toArray(new String[cs.doubleperiodevents.size()]);
					String[] thirdpers=cs.thirdperiodevents.toArray(new String[cs.thirdperiodevents.size()]);
					String[] seconddoublepers=cs.seconddoubleperiodevents.toArray(new String[cs.seconddoubleperiodevents.size()]);
					String[] triplepers=cs.tripleperiodevents.toArray(new String[cs.tripleperiodevents.size()]);
					fileutil.writeLinesToFile(dlvfactfile, seconddoublepers, false);
					fileutil.writeLinesToFile(dlvfactfile, thirdpers, true);
					fileutil.writeLinesToFile(dlvfactfile, triplepers, true);
					fileutil.writeLinesToFile(dlvfactfile, teacherdata, true);
					fileutil.writeLinesToFile(dlvfactfile, majordata, true);
					fileutil.writeLinesToFile(dlvfactfile, auxfactdata, true); //  day preferences
					
					/* --- read predefined fixed schedules from excel --- */
					predefinedslots=cs.readPredefinedSchedulesFromExcel(season, 3, cs.configuremap.get("predefinedslotssheet:"), 0, 0, 10, 50); 
					fileutil.writeLinesToFile(dlvfactfile, predefinedslots, true);
					
					/* Scheduled inslot fact rules to be used also on the next period */
					fixedrules = new ArrayList<Rule>();
					// parseTerms(String pred, String regexp, int matchgroup,
					// List<String> pred_literals)
					eventcodes = prs.parseTerms("inslot", null, 3, fl);			
					fixedrules = cs.getFixedSchedules(factrules, eventcodes, fl, season, "thirdrun" );
					
					/*** NEXT RUN (THIRD) for third period ***/

					invocation.reset();
					// invocation.resetMaxint();
					inputProgram.removeProgram(actionprogram);
					actionprogram = new Program();
					actionprogram.addAll(fixedrules);
					inputProgram.includeProgram(actionprogram);
					invocation.setInputProgram(inputProgram);

					invocation.addOption("-silent");
					invocation.setFilter(filters, true);
					invocation.addOption("-costbound=40,8,_");
					invocation.setNumberOfModels(models);

					invocation.run();

					invocation.waitUntilExecutionFinishes();
					inverrors = invocation.getErrors();
					if (inverrors.size() > 0)
						System.out.println("ERRORS:" + inverrors.toString());

					factrules = prs.parseHandlerBuffer(modelBufferedHandler,
							"inslot", null);
					/* print */
					if (factrules.size() > 0) {
						System.out.print(" III RUN: Literal:{"
								+ factrules.get(factrules.size() - 1).toString()
								+ "}\n");
						for (int i = 0; i < factrules.size(); i++) {
							System.out.print(factrules.get(i).toString() + "\n");
						}
					}
					

					/**** 3. DLV RUN for spring season SHOULD HAVE ENDED ****************/
					state = invocation.getState().toString();
					if (state.equals("FINISHED")) {					
						fl = prs.first_literals;
						//  järjestys inslot(a1, 20) HUOM overrides
						pred = "inslot";
						regex = "(" + pred + ")(\\((\\S*),\\s(\\d{1,2})\\))";
						orderedlits = prs.orderLiteralsByTerm("", 0, fl, regex, 4, 3, 20);

						/***** WRITE 2. RUN RESULTS TO SCHEDULE EXCEL **********/
						cs.writeScheduleToExcel(orderedlits,outputdatafile
								, schedulesheet, 62, 4); //"data/scheduler/schedules_real.xlsx"
						
						
						
						
					} // ****** END IF 3. RUN "FINISHED"
					} // end spring season
				} // ****** END IF 2. RUN "FINISHED"
			} // ****** END IF 1. RUN "FINISHED"

		} catch (DLVInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
