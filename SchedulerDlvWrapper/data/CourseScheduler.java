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

public class CourseScheduler {

	private ExcelToStringArray ex2s;
	private Map<String, String> codeperiodmap = null;
	private List<String> doubleperiodevents = null;

	public CourseScheduler(String xlsname, String dlvsourcefile) {

		this.ex2s = new ExcelToStringArray(xlsname);
		this.codeperiodmap = new HashMap<String, String>();
		this.doubleperiodevents = new ArrayList<String>();
	}

	public String[] readInputDataFromExcel(String sheetname, int firstcolind,
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
		System.out.println("HHHHHHHHHHHHHHHH" + code.length + "["
				+ code[code.length - 1] + "]");
		this.ex2s.setCellArea(firstcolind + 2, lastcolind + 2, firstrowind,
				lastrowind); //
		period = this.ex2s.toStringArray();
		System.out.println("PPPPPPPPPPPPPPPP" + period.length + "["
				+ period[period.length - 1] + "]");
		for (int i = 0; i < readrows; i++) { // code.length
			System.out.println(">>>>>>>>>>>>>>:" + readrows + "==" + period[i]);
			if ((!"null".equalsIgnoreCase(code[i]))
					&& (!"null".equalsIgnoreCase(period[i]))) {
				this.codeperiodmap.put(code[i], period[i]);

				if (("i-ii".equalsIgnoreCase(period[i]))
						|| ("i".equalsIgnoreCase(period[i]))) {
					periodevents.add(allevents[i]);
					if ("i-ii".equalsIgnoreCase(period[i])) {
						this.doubleperiodevents.add(allevents[i]);
					}
				}
			}
		}

		return periodevents.toArray(new String[periodevents.size()]);
		// return allevents;

	}

	public void writeScheduleToExcel(String[] linesToWrite, String excelfile,
			String sheetname) {
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
			// int slotspercol=4; //TODO
			// int rowcount=slotspercol;
			int firstcellnum = 4;
			int cellcnt = 0;
			// int colcount=1;
			int rownum = 2;
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

	public static void main(String[] args) {

		/*
		 * Input excel to output dlvsourcefile
		 */
		String xlsname = "data/scheduler/courses_real.xlsx"; // course.xlsx";
		String dlvcodefile = "data/scheduler/dlvtest.txt";
		String dlvfactfile = "data/scheduler/dlvtest.db";
		FileUtil fileutil = new FileUtil();

		CourseScheduler cs = new CourseScheduler(xlsname, dlvcodefile);

		/*----- READING INPUT DATA FACTS FROM EXCEL -------- */
		String[] coldata;
		/*
		 * --- read course data from excel ---
		 * cs.ex2s.setSheetind(cs.ex2s.getSheetIndex("Courses")); //(0); //
		 * firstcolind, lastcolind, firstrowind, lastrowind
		 * cs.ex2s.setCellArea(0, 0, 9, 49); coldata = cs.ex2s.toStringArray();
		 * // String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		 */
		coldata = cs.readInputDataFromExcel("Courses", 0, 0, 9, 49);

		fileutil.writeLinesToFile(dlvfactfile, coldata, false);

		/* --- read teacher data from excel --- */
		cs.ex2s.setSheetind(cs.ex2s.getSheetIndex("Teachers")); // (1)
		// firstcolind, lastcolind, firstrowind, lastrowind
		cs.ex2s.setCellArea(0, 0, 9, 49);
		coldata = cs.ex2s.toStringArray();
		// String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		fileutil.writeLinesToFile(dlvfactfile, coldata, true);

		/* --- read major data from excel --- */
		cs.ex2s.setSheetind(cs.ex2s.getSheetIndex("Majors")); // (3)
		// firstcolind, lastcolind, firstrowind, lastrowind
		cs.ex2s.setCellArea(0, 0, 9, 49);
		coldata = cs.ex2s.toStringArray();
		// String[] coldata=cs.ex2s.toFullRowStringArray(3,false);
		fileutil.writeLinesToFile(dlvfactfile, coldata, true);

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
			inputProgram.addFile("data/scheduler/dlvtest.txt");
			inputProgram.addFile("data/scheduler/dlvtest.db");

			// ERRORS:[line 1: syntax error., Aborting due to parser errors.,
			// DLV Error, exitValue=1]
			// String timeperiod =
			// " :~ inslot(C1,S), inslot(E2,S), C1 != E2, crsex(C1,E2). [1:2]";
			String timelimit = "exer(java).";

			/* VPA */
			Program actionprogram = new Program();
			// actionprogram.add(new Rule(timeperiod));
			actionprogram.add(new Rule(timelimit));
			// actionprogram.addAll(takeArules);
			inputProgram.includeProgram(actionprogram);

			/*
			 * I create a new instance of DLVInvocation using the DLVWrapper
			 * class specifying a path for DLV executable
			 */
			DLVInvocation invocation = DLVWrapper.getInstance()
					.createInvocation("C:/SpecialPrograms/dlv/dlv.mingw.exe");

			invocation.setInputProgram(inputProgram);

			invocation.addOption("-silent");
			List<String> filters = new ArrayList();
			filters.add("inslot");
			invocation.setFilter(filters, true);
			invocation.addOption("-costbound=40,8,_");
			invocation.setNumberOfModels(1);

			ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(
					invocation);

			/* RUNING DLV: In this moment I can start the DLV execution */
			invocation.run();
			/* Scroll all models computed */
			int hm = 0; // models
			int ip = 0; // predicates
			int jl = 0; // literals

			/* If i wont to wait the finish of execution, i can use thi method */
			invocation.waitUntilExecutionFinishes();
			inverrors = invocation.getErrors();
			if (inverrors.size() > 0)
				System.out.println("ERRORS:" + inverrors.toString());

			List<Rule> factrules = prs.parseHandlerBuffer(modelBufferedHandler,
					"inslot", null);
			if (factrules.size() > 0) {
				System.out.print("Literal:{"
						+ factrules.get(factrules.size() - 1).toString()
						+ "}\n");
				for (int i = 0; i < factrules.size(); i++) {
					System.out.print(factrules.get(i).toString() + "\n");
				}
			}

			/**** 1. DLV RUN SHOULD HAVE ENDED ****************/
			String state = invocation.getState().toString();
			if (state.equals("FINISHED")) {

				// rowsticks= mari.parseOnRowLiterals("onRow", timelimitvalue);
				List<String> fl = prs.first_literals;
				String[] sa = fl.toArray(new String[fl.size()]);

				// TODOOOO: järjestys inslot(a1, 20) HUOM overrides
				String pred = "inslot";
				String regex = "(" + pred + ")(\\((\\S*),\\s(\\d{1,2})\\))";
				String[] orderedlits = prs.orderLiteralsByTerm("", 0, fl,
						regex, 4, 3, 20);

				/***** WRITE 1. RUN RESULTS TO SCHEDULE EXCEL **********/
				cs.writeScheduleToExcel(orderedlits,
						"data/scheduler/schedules_real.xlsx", "Schedule1"); // testi.xlsx",

				/*
				 *  *********TODO: inslot-literals for next period *********
				 * Kopioitu Marienbad toinen ajokierros
				 */
				List<Rule> fixedrules = new ArrayList<Rule>();
				// parseTerms(String pred, String regexp, int matchgroup,
				// List<String> pred_literals)
				List<String> eventcodes = prs.parseTerms("inslot", null, 3, fl);
				for (int i = 0; i < eventcodes.size(); i++) {
					System.out
							.println(i + ": EVENT CODE: " + eventcodes.get(i));
					String[] splits = eventcodes.get(i).split("_");
					if (splits.length > 1) {
						String coursecode = splits[0] + "_" + splits[1];
						String period = cs.codeperiodmap.get(coursecode);
						System.out.println(i + ": COURSE CODE: " + coursecode
								+ ": PERIOD: " + period);
						String inslotlit = fl.get(i);
						if ("i-ii".equalsIgnoreCase(period)) {
							fixedrules.add(factrules.get(i));
							System.out.println(i
									+ ": FIXED FACT FOR NEXT PERIOD: "
									+ inslotlit + ": PERIOD: " + period);
							System.out.println(i
									+ ": FIXED RULE FOR NEXT PERIOD: "
									+ factrules.get(i).toString()
									+ ": PERIOD: " + period);
						}

					} else
						System.out
								.println(i + ": WHAT IS THIS??: " + splits[0]);
				}

				/*** NEXT RUN (SECOND) for period II ***/

				invocation.reset();
				// invocation.resetMaxint();
				inputProgram.removeProgram(actionprogram);
				actionprogram = new Program();
				actionprogram.addAll(fixedrules);
				inputProgram.includeProgram(actionprogram);
				invocation.setInputProgram(inputProgram);

				invocation.addOption("-silent");
				// List<String> filters = new ArrayList();
				// filters.add("inslot");
				invocation.setFilter(filters, true);
				invocation.addOption("-costbound=40,8,_");
				invocation.setNumberOfModels(1);

				invocation.run();

				invocation.waitUntilExecutionFinishes();
				inverrors = invocation.getErrors();
				if (inverrors.size() > 0)
					System.out.println("ERRORS:" + inverrors.toString());

				factrules = prs.parseHandlerBuffer(modelBufferedHandler,
						"inslot", null);
				if (factrules.size() > 0) {
					System.out.print("Literal:{"
							+ factrules.get(factrules.size() - 1).toString()
							+ "}\n");
					for (int i = 0; i < factrules.size(); i++) {
						System.out.print(factrules.get(i).toString() + "\n");
					}
				}

				/**** 2. DLV RUN SHOULD HAVE ENDED ****************/
				state = invocation.getState().toString();
				if (state.equals("FINISHED")) {

					
					fl = prs.first_literals;
					sa = fl.toArray(new String[fl.size()]);

					// TODOOOO: järjestys inslot(a1, 20) HUOM overrides
					pred = "inslot";
					regex = "(" + pred + ")(\\((\\S*),\\s(\\d{1,2})\\))";
					orderedlits = prs.orderLiteralsByTerm("", 0, fl, regex, 4,
							3, 20);

					/***** WRITE 2. RUN RESULTS TO SCHEDULE EXCEL **********/
					cs.writeScheduleToExcel(orderedlits,
							"data/scheduler/schedules_real.xlsx", "Schedule2"); // testi.xlsx",
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
