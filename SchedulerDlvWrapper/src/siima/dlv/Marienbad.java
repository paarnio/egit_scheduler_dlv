/* Marienbad.java
 * TOIMII
 * Interactive game: asks player A (user) 
 * the row number and number of sticks she takes from that row.
 * (To quit: answer 'q' or 'stop' to this question)
 * 
 * TODO: show graphically the sticks on the table after each round.
 */

package siima.dlv;

import it.unical.mat.dlv.program.Literal;
import it.unical.mat.dlv.program.Program;
import it.unical.mat.dlv.program.Rule;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelBufferedHandler;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;
import it.unical.mat.wrapper.Predicate;
import it.unical.mat.wrapper.PredicateHandler;
import it.unical.mat.wrapper.PredicateResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Marienbad {
	
	public List<String> onrow_literals =  new ArrayList<String>();
	
	public int[] parseOnRowLiterals(String onrowpred, int matchtime){
		/*
		 * Parses specific terms of literals on a list af literals
		 * Returns number of sticks in rows (Marienbad)
		 */
		int[] rowsticks = new int[4]; 
		
		for(String lit : this.onrow_literals){
			//System.out.println("-------Matching [" + lit + "]--------------");
			Matcher m = Pattern.compile("(" + onrowpred + ")(\\((\\d{1,2}),\\s(\\d),\\s(\\d)\\))").matcher(lit);
			while(m.find()){
				String ts=m.group(3);
				int itime=Integer.parseInt(ts);
				/*System.out.print("Time[" + m.group(3) + "]");
				System.out.print("-Row[" + m.group(4) + "]");
				System.out.print("-Sticks[" + m.group(5) + "]\n");
				*/
				if(itime==matchtime){
					int irow=Integer.parseInt(m.group(4));
					int isticks=Integer.parseInt(m.group(5));
					rowsticks[irow-1]=isticks;
				}
			}
		}
		return rowsticks;
	}
	
	public List<Rule> parseHandlerBuffer(ModelBufferedHandler modelBufferedHandler, String parsepred, String onrowpred){
		List<Rule> parsepredrules = new ArrayList<Rule>();
		List<String> onrow_lits = new ArrayList<String>();
		
		/* Scroll all models computed */
		int h = 0; // models
		int i = 0; // predicates
		int j = 0; // literals
		while (modelBufferedHandler.hasMoreModels()) {
			Model model = modelBufferedHandler.nextModel();
			Predicate firstPred = model.firstPredicate(); // VPA: tämä täytyy
															// olla, jotta
			// seuraava looppi (while(model.hasMorePredicates())) lähtee
			// juoksemaan
			i++;
			//System.out.println("----------------firstPred:" + firstPred.toString());
			h++;
			while (firstPred.hasMoreLiterals()) {
				Literal literal = firstPred.nextLiteral();
				j++;
				//System.out.println(j + ": Literal**: " + literal.toString());
				String[] literalpreds=literal.toString().split("\\(");
				//System.out.println("pred:" + literalpreds[0]);
				if(parsepred.equals(literalpreds[0])){
					parsepredrules.add(new Rule(literal.toString() + "."));
				} else if(onrowpred.equals(literalpreds[0])) {
					onrow_lits.add(literal.toString());
				}
			}
			while (model.hasMorePredicates()) {
				Predicate predicate = model.nextPredicate();
				i++;
				while (predicate.hasMoreLiterals()) {
					Literal literal = predicate.nextLiteral();
					j++;
					//System.out.println(j + ": Literal**: " + literal.toString());
					String[] literalpreds=literal.toString().split("\\(");
					//System.out.println("pred:" + literalpreds[0]);
					if(parsepred.equals(literalpreds[0])){
						parsepredrules.add(new Rule(literal.toString() + "."));
					} else if(onrowpred.equals(literalpreds[0])) {
						onrow_lits.add(literal.toString());
					}
				}
			}
		}
		this.onrow_literals.clear();
		this.onrow_literals.addAll(onrow_lits);
		return parsepredrules;
	}
	
	
	public void printRowsOnTable(int[] rowsticks){
		int snr=0;
		
		for(int ir=0; ir<4; ir++){
			//System.out.println("ONTABLE====" +ir+ ": sticks:" + rowsticks[ir]);
			
			if(ir==0) snr=1;
			if(ir==1) snr=3;
			if(ir==2) snr=5;
			if(ir==3) snr=7;
			System.out.print("\nR" + (ir+1) + ":");
			for(int is=0; is<10-ir*2; is++){
				System.out.print(" ");
			}
			
			for(int is=0; is<rowsticks[ir]; is++){
				System.out.print("I ");
			}
			
			for(int is=0; is<snr-rowsticks[ir]; is++){
				System.out.print("- ");
			}
			
		}
		System.out.print("\n");
	}
	
	
	public static void main(String[] args) throws DLVInvocationException,
			IOException {
		
		Marienbad mari = new Marienbad();
		List<DLVError> inverrors = new ArrayList<DLVError>();
		// USER INPUT
		List<Rule> takeArules = new ArrayList<Rule>();
		List<Rule> takeMrules;
		int[] rowsticks = {1,3,5,7};
		/* play round 1 */
		int round = 1;
		int timelimitvalue=1+round*2;
		int playerAtime=1+(round-1)*2; // player A takes sticks at odd times
		Scanner user_input = new Scanner( System.in );
		//System.out.print("Enter timelimitvalue (e.g. 8):");
		//timelimitvalue = Integer.parseInt(user_input.next( ));
		String takeA_row_sticks;
		
		System.out.println("\n******* Last Year in Marienbad ******\n");
		
		mari.printRowsOnTable(rowsticks);
		
		System.out.println("\n--FIRST ROUND ------------------------");
		
		System.out.print("ROUND " + round + ":PLAYER A: enter row and sticks (row,sticks e.g. 4,1 ) for time(" + playerAtime + "):");
		takeA_row_sticks = user_input.next( );
		String takeA="takeA(" + playerAtime + "," + takeA_row_sticks + ").";
		takeArules.add(new Rule(takeA));
		
		/* I create a new instance of DLVInputProgram */
		DLVInputProgram inputProgram = new DLVInputProgramImpl();

		/* I can add some file to the DLVInputProgram */
		
		inputProgram.addFile("data/game/Marienbad8rules"); 
		inputProgram.addFile("data/game/Marienbad8facts.db"); 
		
		String timeperiod = "time(1.." + timelimitvalue + ").";
		String timelimit="timelimit(" + timelimitvalue + ").";
		
		
		/* VPA */
		Program actionprogram=new Program();
		actionprogram.add(new Rule(timeperiod));
		actionprogram.add(new Rule(timelimit));
		actionprogram.addAll(takeArules);//add(takeArules.get(0));//add(new Rule("uusiohjelma(2) .")); // addAll(takeArules); 
		inputProgram.includeProgram(actionprogram);
		
		/*
		 * I create a new instance of DLVInvocation using the DLVWrapper class
		 * specifying a path for DLV executable
		 */
		DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(
				"C:/SpecialPrograms/dlv/dlv.mingw.exe");
		// "C:/SpecialPrograms/dlv_db/dldb_win32.exe"); //dlvdb:ssä ei toimi
		// named constants #const

		/* VPA ADDIN: */
		invocation.setInputProgram(inputProgram);


		 /* Includes only instances of the predicate specified in the filter parameter in output. This method 

			deletes each option that contains the string filter= or -pfilter=, and adds a new option with the new 

			filter */
			List<String> filters=new ArrayList(); //List filters=new ArrayList();
			filters.add( "takeA" );
			filters.add( "takeM" );
			filters.add( "onRow" ); 
			invocation.setFilter(filters, true);
		 
		 
		/*
		 * Sets the max number of models computed in the DLV execution. If n is
		 * 0, all models are computed.
		 * 
		 * This method deletes each option that contains the string "-n=" and
		 * adds a new option "-n='n'.
		 */
		invocation.setNumberOfModels(1); // 10

		


		/*
		 * Another mode to scroll the computed models is to use the
		 * ModelBufferedHandler that is a concrete
		 * 
		 * observer that work like Enumeration. NOTE: the ModelBufferedHandler
		 * subscribe itself to the
		 * 
		 * DLVInvocation
		 */
		ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(
				invocation);

		/* RUNING DLV: In this moment I can start the DLV execution */
		invocation.run();
		/* Scroll all models computed */
		int h = 0; // models
		int i = 0; // predicates
		int j = 0; // literals
		
	
		/* If i wont to wait the finish of execution, i can use thi method */
		invocation.waitUntilExecutionFinishes();
		inverrors=invocation.getErrors();
		if(inverrors.size()>0) System.out.println("ERRORS:" + inverrors.toString());
		
		takeMrules= mari.parseHandlerBuffer(modelBufferedHandler, "takeM", "onRow");		
		if(takeMrules.size()>0) System.out.print("LAST TAKE BY PLAYER M (row,sticks):{" + takeMrules.get(takeMrules.size()-1).toString().substring(9,14) + "}\n");
		rowsticks= mari.parseOnRowLiterals("onRow", timelimitvalue);
		mari.printRowsOnTable(rowsticks);
		
		/* -------NEW RUN--WITH NEW SETTINGS------------- */
		boolean newround=true;
		
		while(newround){
		System.out.println("\n--NEW ROUND ------------------------");
		
		
		String state=invocation.getState().toString();
		//System.out.println("State: " + state);
		//invocation.stopExecution(); //stopExecution feature not implemented
		//invocation.killDlv(); //DLV is not Running
		if(state.equals("FINISHED")){
			invocation.reset();
			//invocation.resetMaxint();
			inputProgram.removeProgram(actionprogram);
			actionprogram=new Program();
			/* play next round  */
			round++;
			timelimitvalue=1+round*2;
			playerAtime=1+(round-1)*2; // player A takes sticks at odd times
			
			timeperiod = "time(1.." + timelimitvalue + ").";
			timelimit="timelimit(" + timelimitvalue + ").";
			actionprogram.add(new Rule(timeperiod));
			actionprogram.add(new Rule(timelimit));
			System.out.print("ROUND " + round + ":PLAYER A: enter row and sticks (row,sticks e.g. 4,1 ) for time(" + playerAtime + "):");
			takeA_row_sticks = user_input.next( );
			if(("q".equals(takeA_row_sticks))||("stop".equals(takeA_row_sticks))){ newround=false; break;}
			takeA="takeA(" + playerAtime + "," + takeA_row_sticks + ").";
			takeArules.add(new Rule(takeA));
			
			actionprogram.addAll(takeArules);
			actionprogram.addAll(takeMrules);
			inputProgram.includeProgram(actionprogram);	
		
		
		invocation.setInputProgram(inputProgram);
		//invocation.setMaxint(11);
		invocation.setNumberOfModels(1);
		invocation.setFilter(filters, true);
		invocation.run();
		
		invocation.waitUntilExecutionFinishes();
		inverrors=invocation.getErrors();
		if(inverrors.size()>0) System.out.println("ERRORS:" + inverrors.toString());
		
		takeMrules= mari.parseHandlerBuffer(modelBufferedHandler, "takeM", "onRow");		
		if(takeMrules.size()>0) System.out.print("LAST TAKE BY PLAYER M (row,sticks):{" + takeMrules.get(takeMrules.size()-1).toString().substring(9,14) + "}\n");
		rowsticks= mari.parseOnRowLiterals("onRow", timelimitvalue);
		mari.printRowsOnTable(rowsticks);
		
	
		}
		} System.out.print("COMMAND WAS: STOP " + takeA_row_sticks);
	}

}
