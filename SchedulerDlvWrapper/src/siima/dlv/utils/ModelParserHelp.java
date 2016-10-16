/*
 * some helper methods orig from Marienbad
 * 
 */

package siima.dlv.utils;

import it.unical.mat.dlv.program.Literal;
import it.unical.mat.dlv.program.Rule;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelBufferedHandler;
import it.unical.mat.wrapper.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelParserHelp {
	public List<String> first_literals =  new ArrayList<String>();
	public List<String> second_literals =  new ArrayList<String>();
	public List<String> getFirst_literals() {
		return first_literals;
	}

	public List<String> getSecond_literals() {
		return second_literals;
	}

	
	public String[] orderLiteralsByTerm(String pred, int matchtime, List<String> pred_literals, String regex, int ordergroupind, int selectgroupind, int maxordertermvalue){
		/* TODO: KESKEN: JOS monella literalilla on sama orderterm arvo, ne kirjoittuvat p‰‰llek‰in orderedlits array:hin
		 * Parses specific term of literals on a list of literals
		 * 
		 */
		String[] orderedlits = new String[4*maxordertermvalue];
		int[] slotcounts = new int[maxordertermvalue]; 
		
		for(String lit : pred_literals){
			//System.out.println("-------Matching [" + lit + "]--------------");
			//Matcher m = Pattern.compile("(" + pred + ")(\\((\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2})\\))").matcher(lit);
			//System.out.print("LITERAL[" + lit + "]");
			//String huu = "inslot(a3, 18)";
			Matcher m = Pattern.compile(regex).matcher(lit); //lit
			while(m.find()){
				String orderterm=m.group(ordergroupind);
				int orderind=Integer.parseInt(orderterm);
				String sel=m.group(selectgroupind);
				/*System.out.print("group0[" + m.group(0) + "]");
				System.out.print("group1[" + m.group(1) + "]");
				System.out.print("group2[" + m.group(2) + "]\n");
				System.out.print("group3[" + m.group(3) + "]\n");
				System.out.print("group4[" + m.group(4) + "]\n");
				
				if(itime==matchtime){
					int irow=Integer.parseInt(m.group(4));
					int isticks=Integer.parseInt(m.group(5));
					rowsticks[irow-1]=isticks;
				}
				*/
				int cnt=slotcounts[orderind-1];
				//orderedlits[(orderind-1)+cnt*maxordertermvalue]=lit;
				orderedlits[(orderind-1)+cnt*maxordertermvalue]=sel; // vain yksi termi
				slotcounts[orderind-1]=++cnt;
			}
		}
		return orderedlits;
	}
	
	public List<String> parseTerms(String pred, String regexp, int matchgroup, List<String> pred_literals){
		/* 
		 * Parses specific term of literals on a list of literals
		 */
		List<String> terms = new ArrayList<String>(); 
		
		for(String lit : pred_literals){
			//System.out.println("-------Matching [" + lit + "]--------------");
			//Matcher m = Pattern.compile("(" + pred + ")(\\((\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2})\\))").matcher(lit);
			Matcher m = Pattern.compile("(" + pred + ")(\\((\\w*),\\s(\\d{1,2})\\))").matcher(lit);
			while(m.find()){
				String ts=m.group(3);
				//int itime=Integer.parseInt(ts);
				/*System.out.print("Time[" + m.group(3) + "]");
				System.out.print("-Row[" + m.group(4) + "]");
				System.out.print("-Sticks[" + m.group(5) + "]\n");
				*/
				if(true){
					terms.add(ts);
				}
			}
		}
		return terms;
	}
	
	
	public int[] parseOnRowLiterals(String pred, int matchtime, List<String> pred_literals){
		/* TODO: sovita t‰m‰ tarpeen mukaan
		 * Parses specific term of literals on a list of literals
		 * Returns number of sticks in rows (in Marienbad)
		 */
		int[] rowsticks = new int[4]; 
		
		for(String lit : pred_literals){
			//System.out.println("-------Matching [" + lit + "]--------------");
			Matcher m = Pattern.compile("(" + pred + ")(\\((\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2})\\))").matcher(lit);
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
	
	public List<Rule> parseHandlerBuffer(ModelBufferedHandler modelBufferedHandler, String firstpred, String secondpred){
		/*
		 * Finds predicate literals from the model matching firstpred and secondpred.
		 * and put them on the class member lists: first_literals, second_literals.
		 * (secondpred can be null)
		 * Returns a list of literal facts (as rules) for the firstpred.
		 */
		
		List<Rule> parsepredrules = new ArrayList<Rule>();
		List<String> first_lits = new ArrayList<String>();
		List<String> second_lits = new ArrayList<String>();
		
		/* Scroll all models computed */
		int h = 0; // models
		int i = 0; // predicates
		int j = 0; // literals
		while (modelBufferedHandler.hasMoreModels()) {
			Model model = modelBufferedHandler.nextModel();
			Predicate firstPred = model.firstPredicate(); // VPA: t‰m‰ t‰ytyy
															// olla, jotta
			// seuraava looppi (while(model.hasMorePredicates())) l‰htee
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
				if(firstpred.equals(literalpreds[0])){
					parsepredrules.add(new Rule(literal.toString() + "."));
					first_lits.add(literal.toString());
				} else if((secondpred != null) && (secondpred.equals(literalpreds[0]))) {
					second_lits.add(literal.toString());
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
					if(firstpred.equals(literalpreds[0])){
						parsepredrules.add(new Rule(literal.toString() + "."));
						first_lits.add(literal.toString());
					} else if((secondpred != null) && (secondpred.equals(literalpreds[0]))) {
						second_lits.add(literal.toString());
					}
				}
			}
		}
		this.first_literals.clear();
		this.first_literals.addAll(first_lits);
		this.second_literals.clear();
		this.second_literals.addAll(second_lits);
		return parsepredrules;
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
