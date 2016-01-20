//package burlap.oomdp.logicalexpressions;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import burlap.behavior.affordances.AffordanceDelegate;
//import burlap.oomdp.core.Domain;
//import burlap.oomdp.core.GroundedProp;
//import burlap.oomdp.core.PropositionalFunction;
//
//public class LogicalExpressionParser {
//	
//	private String[] tokens;
//	
//	public LogicalExpressionParser(String expression) {
//		this.tokens = expression.split(" ");
//	}
//	
//	public LogicalExpression parse(Domain d) {
//		
//		List<String> stack = new ArrayList<String>();
//		List<LogicalExpression> children = new ArrayList<LogicalExpression>();
//		List<LogicalExpression> leStack = new ArrayList<LogicalExpression>();
//		
//		LogicalExpression root = new PFAtom();
//		
//		for (int i = 0; i < this.tokens.length; i++) {
//			LogicalExpression localRoot = new PFAtom();
//			String token = this.tokens[i];
//			stack.add(token);
//			
//			// At end of a logical expression unit, combine
//			if (token == ")" || i == tokens.length - 1) {
//				
//				// Determine if disjunction, conjunction, negation, or pfatom
//				if (stack.contains("^")) {
//					localRoot = new Conjunction();
//				}
//				else if (stack.contains("v")) {
//					localRoot = new Disjunction();
//				}
//				else {
//					localRoot = new PFAtom();
//				}
//				
//				// Iterate through stack to fill "children" list of PFAtoms
//				String topOfStack = stack.remove(stack.size() - 1);
//				while (stack.size() >= 0) {
//					System.out.println("topOfStack = " + topOfStack);
//					// If it's a negated PFAtom
//					if(topOfStack.charAt(0) == '!') {
//						topOfStack = topOfStack.substring(1);
//						PropositionalFunction tempPF = d.getPropFunction(topOfStack);
//						String[] freeParams = AffordanceDelegate.makeFreeVarListFromObjectClasses(tempPF.getParameterClasses());
//						GroundedProp tempGP = new GroundedProp(tempPF, freeParams);
//						PFAtom pfAtom = new PFAtom(tempGP);
//						LogicalExpression negation = new Negation(pfAtom);
//						children.add(negation);
//					}
//					// If it's a PFAtom
//					else if(topOfStack.matches("^[^v][a-zA-Z]+")) {
//						PropositionalFunction tempPF = d.getPropFunction(topOfStack);
//						String[] freeParams = AffordanceDelegate.makeFreeVarListFromObjectClasses(tempPF.getParameterClasses());
//						GroundedProp tempGP = new GroundedProp(tempPF, freeParams);
//						PFAtom pfAtom = new PFAtom(tempGP);
//						children.add(pfAtom);
//					}
//					
//					if(stack.size() == 0) {
//						break;
//					}
//					topOfStack = stack.remove(stack.size() - 1);
//				}
//				if(localRoot.getClass() != PFAtom.class) {
//					localRoot.childExpressions.addAll(children);
//				}
//				else {
//					// If its a PFAtom, there will only be one child.
//					localRoot = children.get(0);
//				}
//				leStack.add(localRoot);
//			}
//		}
//		
//		return leStack.get(0);
//	}
//	
//}
