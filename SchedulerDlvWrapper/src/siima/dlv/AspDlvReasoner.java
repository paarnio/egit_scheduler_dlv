/* AspDlvReasoner.java
 * 2017-04-27 TOIMII
 * 
 * See http://www.dlvsystem.com/html/DLV_User_Manual.html
 * 
 * 
 */
package siima.dlv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unical.mat.dlv.program.Atom;
import it.unical.mat.dlv.program.Conjunction;
import it.unical.mat.dlv.program.Literal;
import it.unical.mat.dlv.program.NormalAtom;
import it.unical.mat.dlv.program.Program;
import it.unical.mat.dlv.program.Query;
import it.unical.mat.dlv.program.Rule;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import it.unical.mat.wrapper.FactHandler;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelBufferedHandler;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;
import it.unical.mat.wrapper.Predicate;
import it.unical.mat.wrapper.PredicateHandler;
import it.unical.mat.wrapper.PredicateHandlerWithName;

import it.unical.mat.wrapper.PredicateResult;
import siima.util.FileUtil2;
import it.unical.mat.wrapper.FactResult;


public class AspDlvReasoner {
	/*
	 * HUOM: HP:  "C:/SpecialPrograms/dlv/dlv.mingw.exe"
	 * HUOM: WPC: "C:/Special_Programs/dlv/dlv.mingw.exe"
	 */
	
	public static String dlv_solver_path = "C:/SpecialPrograms/dlv/dlv.mingw.exe";
	
	
	public StringBuffer parseAspModel(List<Model> models){
		StringBuffer buffer = new StringBuffer();
		boolean hasModel = false;		
		/* Scroll all models computed */
		int h = 0; // models
		int i = 0; // predicates
		int j = 0; // literals
		
		if((models!=null)&&(!models.isEmpty())){
			hasModel = true;
			for(Model model : models){
				h++;
				buffer.append("<model num='" + h + "'>\n");
				Predicate firstPred = model.firstPredicate(); 
				i++;
				//System.out.println("--firstPred:" + firstPred.toString());				
				while (firstPred.hasMoreLiterals()) {
					Literal literal = firstPred.nextLiteral();
					j++;
					buffer.append("<" + h + ":" + i + ":" + j + ">");
					buffer.append("<literal predicate='" + literal.getName() + "' atom='"
							+ literal.getAtom() + "'>" + literal.toString() + "</literal>\n");
					System.out.println(j + ": Literal*: " + literal.toString());
				}
				while (model.hasMorePredicates()) {
					Predicate predicate = model.nextPredicate();
					i++;
					while (predicate.hasMoreLiterals()) {
						Literal literal = predicate.nextLiteral();
						j++;
						buffer.append("<" + h + ":" + i + ":" + j + ">");
						buffer.append("<literal predicate='" + literal.getName() + "' atom='"
								+ literal.getAtom() + "'>" + literal.toString() + "</literal>\n");
						System.out
								.println(j + ": Literal**: " + literal.toString());
					}
				}
				buffer.append("</model>\n");
				
			}
			
			
		}
		
		if(hasModel) System.out.println("=AspDlvReasoner: MODELS:PREDICATES:LITERALS=(" + h + ":" + i + ":" + j + ")");
		return buffer;
	}
	
	
	
	public List<Model> invokeDlvReasoning(String rules_dlv_file, String facts_db_file, int numOfModels ) throws DLVInvocationException,
	IOException { //TOIMII: huom tiedostossa po. rivi #maxint = ?. TAI asettaa se invocation.setMaxint(20);
				boolean hasModel = false;

				/* I create a new instance of DLVInputProgram */
				DLVInputProgram inputProgram = new DLVInputProgramImpl();
				
				if(rules_dlv_file!=null) inputProgram.addFile(rules_dlv_file); //TOIMII:kun lisäsin #maxint = 2 .
				if(facts_db_file!=null) inputProgram.addFile(facts_db_file);
				
							
				DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(dlv_solver_path);
						//"C:/Special_Programs/dlv/dlv.mingw.exe"); WorkPC
						//"C:/SpecialPrograms/dlv_db/dldb_win32.exe"); //in HP

				/* VPA ADDIN:  */
				invocation.setInputProgram(inputProgram);
				invocation.setNumberOfModels(numOfModels);
				invocation.setMaxint(20);
				// Filters TOIMII
				List<String> filters=new ArrayList(); 
				filters.add("subbranch"); 
				filters.add("mainbranch");
				//invocation.setFilter(filters, true);
				// OPTIONS TOIMII
				invocation.addOption("-nofacts"); // TOIMII
				
				// TESTING QUERY
				//USING "hasint(leg1,B)" -> ERROR: Non-ground queries are only supported with brave and cautious reasoning.
				// NormalAtom(boolean trueNegated,String name);
				Atom atom = new NormalAtom(false,"lego(leg1)"); //NormalAtom(true,"lego(leg1)") -> HAS MODEL: false
				invocation.setQuery(new Query(new Conjunction(new Literal(true, atom))));
				
				/* MODE 1: TOIMII
				 * I create a new observer that receive a notification for the models
				 * computed and store them in a list
				 
				final List<ModelResult> modresults = new ArrayList<ModelResult>();
				ModelHandler modelHandler = new ModelHandler() {
					final public void handleResult(DLVInvocation obsd, ModelResult res) {
						modresults.add(res);
						System.out.println("=AspDlvReasoner: MODEL RESULTS SAVED:\n" + res.toString());
					}
				};			
				invocation.subscribe(modelHandler);
				*/

				/* MODE 2: TOIMII
				 * Observer creation
				 * 
				 *
				PredicateHandler predicateHandler = new PredicateHandler() {
					public void handleResult(DLVInvocation obsd, PredicateResult res) {
						System.out.println("MODELID:" + res.getModelID()
								+ " PREDICATE====" + res.toString());
					}
				};
				// Observer registration
				invocation.subscribe(predicateHandler);
				*/

				/* MODE 3: TOIMII
				 * Another mode to scroll the computed models is to use the
				 * ModelBufferedHandler that is a concrete
				 * observer that work like Enumeration. NOTE: the ModelBufferedHandler
				 * subscribe itself to the
				 */
				ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(
						invocation);
				
				/*--- RUNING DLV: In this moment I can start the DLV execution ---*/
				invocation.run();
				
				/* Scroll all models computed */
				int h = 0; // models
				int i = 0; // predicates
				int j = 0; // literals

				List<Model> models = new ArrayList<Model>();
				while (modelBufferedHandler.hasMoreModels()) {
					Model model = modelBufferedHandler.nextModel();
					//Saving models to a list
					models.add(model);
					/* parsing predicates and literals for printing
					Predicate firstPred = model.firstPredicate(); 
					i++;
					System.out.println("----------------firstPred:"
							+ firstPred.toString());
					h++;
					while (firstPred.hasMoreLiterals()) {
						Literal literal = firstPred.nextLiteral();
						j++;
						System.out.println(j + ": Literal*: " + literal.toString());
					}
					while (model.hasMorePredicates()) {
						Predicate predicate = model.nextPredicate();
						i++;
						while (predicate.hasMoreLiterals()) {
							Literal literal = predicate.nextLiteral();
							j++;
							System.out
									.println(j + ": Literal**: " + literal.toString());
						}
					}*/
				}

				/* If i wont to wait the finish of execution, i can use thi method */
				invocation.waitUntilExecutionFinishes();
				/*
				 * At the term of execution, I can control the errors created by DLV
				 * invocation
				 */
				List<DLVError> errors = invocation.getErrors();//line 0: can't open input??
				if((errors!=null)&&(!errors.isEmpty())) {
					System.out.println("????ERRORS Exists");
					for (DLVError err : errors){					
					System.out.println("ERROR: " + err.getText());
					}				
				}
				
				hasModel = invocation.haveModel();
				System.out.println("HAS MODEL: " + invocation.haveModel());
				if(hasModel) System.out.println("=AspDlvReasoner: MODELS:PREDICATES:LITERALS=(" + h + ":" + i + ":" + j + ")");
				return models;		
	}
	
	public void writeAspModelToFile(StringBuffer modelbuf, String filepath){
				
		FileUtil2.writeTextFile(modelbuf.toString(), filepath);
		
	}


	public static void main(String[] args) { // throws DLVInvocationException, IOException {
		
		String rules_dlv_file = "data/legotower/legotower_rules_mod4.dlv";
		String facts_db_file = "data/legotower/legotower_facts_mod4.db";
		String filepath = "data/legotower/asp_testresults.txt";

		AspDlvReasoner asp = new AspDlvReasoner();
		try {
			int numOfModels=2;
			List<Model> models = asp.invokeDlvReasoning(rules_dlv_file,facts_db_file, numOfModels);
			StringBuffer modelbuf = asp.parseAspModel(models);
			asp.writeAspModelToFile(modelbuf, filepath);
			
		} catch (DLVInvocationException e) {		
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}

}
