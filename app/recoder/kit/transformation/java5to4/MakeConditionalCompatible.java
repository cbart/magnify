/*
 * Created on 17.03.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ClassType;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeParameter;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.operator.Conditional;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.TypeReference;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.service.SourceInfo;

/**
 * Deals with uses of the conditional(c-like trinary) operator which create intersection types.
 * @author Tobias Gutzmann
 *
 */
public class MakeConditionalCompatible extends TwoPassTransformation {
	private static class Item {
		Conditional c;
		Type t, st1, st2;
		Type p;
		TypeReference tr;
		Item(Conditional c, Type t, TypeReference tr, Type p, Type st1, Type st2) {
			if (t == null)
				throw new NullPointerException();
			this.c = c;
			this.t = t;
			this.tr = tr;
			this.p = p;
			this.st1 = st1;
			this.st2 = st2;
		}
	}
	
	private NonTerminalProgramElement root;
	private List<CompilationUnit> cul;
	private List<Item> list;
	private List<Conditional> visited;
	
	/**
	 * @param sc
	 */
	public MakeConditionalCompatible(CrossReferenceServiceConfiguration sc, NonTerminalProgramElement root) {
		super(sc);
		this.root = root;
	}
	
	public MakeConditionalCompatible(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}

	private Type nestedConditionals(Conditional c, int deepth) {
		boolean inner = true;
		Type p = null;
		Type t = getSourceInfo().getType(c);
		Expression e1 = c.getExpressionAt(1);
		Expression e2 = c.getExpressionAt(2);
		Type t1 = getSourceInfo().getType(c.getExpressionAt(1)); 
		Type t2 = getSourceInfo().getType(c.getExpressionAt(2));
//		System.out.println("deepth: " + deepth + " t1: " + t1.getFullName() + " t2: " + t2.getFullName());
		if (e1 instanceof Conditional) {
			p = nestedConditionals((Conditional)e1, deepth + 1);
			inner = false;
		}
		if (e1 instanceof ParenthesizedExpression) {
			ParenthesizedExpression pe = (ParenthesizedExpression)e1;
			for (int i = 0; i < pe.getChildCount(); i++) {
				if (pe.getChildAt(i) instanceof Conditional) {
					p = nestedConditionals((Conditional)pe.getChildAt(i), deepth + 1);
					inner = false;
				}
			}
		}
		if (e2 instanceof Conditional) {
//			System.out.println("deepth: " + deepth + " e2 instanceof Conditional");
			p = nestedConditionals((Conditional)e2, deepth + 1);
			inner = false;
		}
		if (e2 instanceof ParenthesizedExpression) {
			ParenthesizedExpression pe = (ParenthesizedExpression)e2;
			for (int i = 0; i < pe.getChildCount(); i++) {
				if (pe.getChildAt(i) instanceof Conditional) {
					p = nestedConditionals((Conditional)pe.getChildAt(i), deepth + 1);
					inner = false;
				}
			}
		}
//		System.out.println("deepth: " + deepth);
//		System.out.println("deepth: " + deepth + " " + t1.getFullName() + " " + t2.getFullName());
		if (inner) p = t1;
		if (t instanceof IntersectionType || (t1 != t2 && 
				!(t1 instanceof PrimitiveType && t2 instanceof PrimitiveType) &&
				!(t1 == getNameInfo().getNullType()) && !(t2 == getNameInfo().getNullType()) &&
				!getSourceInfo().isWidening(t1, t2) && !getSourceInfo().isWidening(t2, t1)
		)) {
//			System.out.println("deepth: " + deepth + " To transform: " + t1.getFullName() + " " + t2.getFullName());
			NonTerminalProgramElement parent = c.getASTParent();
			Type target;
			target = Util.getRequiredContextType(getSourceInfo(), c); 
			if (target == null) {
				// (hopefully) special case: "" + (expr ? "" : somethingNotString). Fake to
				// String for now. We catch that in the transformation!
				// TODO check if it really is the special case.
				target = getNameInfo().getJavaLangString();
//				System.out.println(c.getASTParent().getASTParent().getASTParent().toSource());
//				throw new RuntimeException("MakeConditionalCompatible: Don't know how to handle parent type " + parent.getClass().getName());
			}
			// some special cases: one of the operands is a type variable...
			// TODO clean this code up...
			if (t1 instanceof TypeParameter) {
				if (((TypeParameter)t1).getBoundCount() == 0 && target == getNameInfo().getJavaLangObject()) {
					// skip
				} else if (getClassTypeFromTypeParameter((TypeParameter)t1, 0) == target) {
					// skip
				} else {
					TypeReference tr = TypeKit.createTypeReference(getSourceInfo(), target, c);
					list.add(new Item(c,target,tr,p, 
							getSourceInfo().getType(c.getExpressionAt(1)),
							getSourceInfo().getType(c.getExpressionAt(2))));
				}
			} else if (t2 instanceof TypeParameter) {
				if (((TypeParameter)t2).getBoundCount() == 0 && target == getNameInfo().getJavaLangObject()) {
					// skip
				} else if (getClassTypeFromTypeParameter((TypeParameter)t2, 0) == target) {
					// skip
				} else {
					TypeReference tr = TypeKit.createTypeReference(getSourceInfo(), target, c);
					list.add(new Item(c,target,tr,p,
							getSourceInfo().getType(c.getExpressionAt(1)),
							getSourceInfo().getType(c.getExpressionAt(2))));
				}
			} else {
				TypeReference tr = TypeKit.createTypeReference(getSourceInfo(), target, c);
				list.add(new Item(c,target,tr,p,
						getSourceInfo().getType(c.getExpressionAt(1)),
						getSourceInfo().getType(c.getExpressionAt(2))));

			}
		}
		visited.add(c);
		return p;
	}

	private ClassType getClassTypeFromTypeParameter(TypeParameter tp, int i) {
		ClassType t;
		t = getNameInfo().getClassType(tp.getBoundName(i));
		return t;
	}
	
	@Override
	public ProblemReport analyze() {
		list = new ArrayList<Item>();
		visited = new ArrayList<Conditional>();
		setProblemReport(NO_PROBLEM);
		TreeWalker tw;
		if (cul != null) {
			for (CompilationUnit cu : cul) {
				root = cu;
				tw = new TreeWalker(root);
				while (tw.next()) {
					ProgramElement pe = tw.getProgramElement();
					if (pe instanceof Conditional) {
						Conditional c = (Conditional)pe;
						if (!visited.contains(c)) {
							nestedConditionals(c, 0);
						}
					}
				}
			}
		}
		else {
			tw = new TreeWalker(root);
			while (tw.next()) {
				ProgramElement pe = tw.getProgramElement();
				if (pe instanceof Conditional) {
					Conditional c = (Conditional)pe;
					if (!visited.contains(c)) {
						nestedConditionals(c, 0);
					}
				}
			}
		}
		if (list.isEmpty())
			return IDENTITY;
		return NO_PROBLEM;
	}

	@Override
	public void transform() {
		super.transform();
		ProgramFactory f = getProgramFactory();
		System.out.println("Conditionals to be transformed: " + list.size());
		
		for (Item i : list) {
			Expression e1 = i.c.getExpressionAt(1);
			Expression e2 = i.c.getExpressionAt(2);
			Type t1 = i.st1;
			Type t2 = i.st2;
			
			boolean toString = i.t == getNameInfo().getJavaLangString();
			if (toString) {
				if (t1 != getNameInfo().getJavaLangString()) {
					replace(e1, f.createMethodReference((ReferencePrefix)e1.deepClone(), 
								f.createIdentifier("toString")));
				}
				if (t2 != getNameInfo().getJavaLangString()) {
					replace(e2, f.createMethodReference((ReferencePrefix)e2.deepClone(), 
							f.createIdentifier("toString")));
				}
			} else {
				if (t1 != i.t)
					replace(e1, f.createTypeCast(e1.deepClone(), i.tr.deepClone()));
				if (t2 != i.t)
					replace(e2, f.createTypeCast(e2.deepClone(), i.tr)); // no deepClone required
			}
		}
	}
}
