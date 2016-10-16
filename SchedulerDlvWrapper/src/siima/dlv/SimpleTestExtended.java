package siima.dlv;

import it.unical.mat.dlv.program.Literal;
import it.unical.mat.dlv.program.Program;
import it.unical.mat.dlv.program.Rule;
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

public class SimpleTestExtended {
	public static void main(String[] args) throws DLVInvocationException,
	IOException {
// My test with selected commands from the other class

/* I create a new instance of DLVInputProgram */
DLVInputProgram inputProgram = new DLVInputProgramImpl();

/* I can add some file to the DLVInputProgram */

//inputProgram.addFile("data/game/Marienbad7rules"); 
//inputProgram.addFile("data/game/Marienbad7facts.db"); 

inputProgram.addFile("data/game/numconst.txt"); 

inputProgram.addText("huuhaa(1) .");

Program program=new Program();
program.add(new Rule("uusiohjelma(2) ."));
inputProgram.includeProgram(program);

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



/* function remove automatically any existing max-int '-N' option. */
 invocation.setMaxint(50); //VPA: Marienbad6 vaatii max-int=11

/*
 * Sets the max number of models computed in the DLV execution. If n is
 * 0, all models are computed.
 * 
 * This method deletes each option that contains the string "-n=" and
 * adds a new option "-n='n'.
 */
invocation.setNumberOfModels(1); // 10

/*
 * I create a new observer that receive a notification for the models
 * computed and store them in a
 * 
 * list
 */
final List models = new ArrayList();
ModelHandler modelHandler = new ModelHandler() {
	final public void handleResult(DLVInvocation obsd, ModelResult res) {
		models.add(res);
		System.out.println("MODEL RESULT---" + res.toString());
	}
};
/* Subscribe the handler from the DLVInvocation */
invocation.subscribe(modelHandler);

/*
 * Observer creation FactHandler factHandler=new FactHandler(){ public
 * void handleResult(DLVInvocation obsd, FactResult res) {
 * System.out.println("FACT====" + res.toString()); } };
 * 
 * // Observer registration invocation.subscribe(factHandler);
 */

/* Observer creation */
PredicateHandler predicateHandler = new PredicateHandler() {
	public void handleResult(DLVInvocation obsd, PredicateResult res) {
		System.out.println("MODELID:" + res.getModelID()
				+ " PREDICATE====" + res.toString());
	}
};

// Observer registration
invocation.subscribe(predicateHandler);

/* In this moment I can start the DLV execution */
// invocation.run();
/* Scroll all models computed */

/* SKIP some code --------- */

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

while (modelBufferedHandler.hasMoreModels()) {
	Model model = modelBufferedHandler.nextModel();
	Predicate firstPred = model.firstPredicate(); // VPA: tämä täytyy
													// olla, jotta
	// seuraava looppi (while(model.hasMorePredicates())) lähtee
	// juoksemaan
	i++;
	System.out.println("----------------firstPred:"
			+ firstPred.toString());
	h++;
	while (firstPred.hasMoreLiterals()) {
		Literal literal = firstPred.nextLiteral();
		j++;
		System.out.println(j + ": Literal**: " + literal.toString());
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
	}
}

/* If i wont to wait the finish of execution, i can use thi method */
invocation.waitUntilExecutionFinishes();
/*
 * At the term of execution, I can control the errors created by DLV
 * invocation
 */
// List =invocation.getErrors();

// VPA hij=1:0:0:
System.out.println("MODELS:PREDICATES:LITERALS=(" + h + ":" + i + ":"
		+ j + ")");

System.out.println("has model=" + invocation.haveModel());

/* -------NEW RUN--------------- */
System.out.println("------------NEW RUN ------------------------");

inputProgram.removeProgram(program);
String state=invocation.getState().toString();
System.out.println("State: " + state);
//invocation.stopExecution(); //stopExecution feature not implemented
//invocation.killDlv(); //DLV is not Running
if(state.equals("FINISHED")){
//invocation.resetMaxint();
invocation.reset();
invocation.setInputProgram(inputProgram);
invocation.setMaxint(66);
invocation.setNumberOfModels(2);
invocation.run();

invocation.waitUntilExecutionFinishes();

}
}

}
