/*
 * Created on 01.04.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedMethod;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.ParameterizedMethod;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.abstraction.TypeParameter;
import recoder.abstraction.Variable;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.bytecode.ClassFile;
import recoder.bytecode.MethodInfo;
import recoder.bytecode.TypeParameterInfo;
import recoder.convenience.TreeWalker;
import recoder.java.Comment;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.StatementContainer;
import recoder.java.declaration.InheritanceSpecification;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.expression.Assignment;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.expression.operator.TypeCast;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferenceSuffix;
import recoder.java.reference.TypeReference;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Return;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.UnitKit;
import recoder.kit.transformation.java5to4.Util.IntroduceCast;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.SourceInfo;

@Deprecated
public class ResolveGenerics extends TwoPassTransformation {
	
	private static long time = 0;
	private static int rec = 0;
	
	public static void tic() {
		if (rec++ == 0)
			time -= System.currentTimeMillis();
	}
	
	public static void toc() {
		if (--rec == 0)
			time += System.currentTimeMillis();
		if (rec < 0)
			throw new Error();
	}
	
	private void resolveTypeParameters(
			List<TypeParameterDeclaration> typeParams
	) {
		// NOTE: no performance issues here
		// deal with type parameter uses in own Type Declaration first
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		for (int i = 0, s = typeParams.size(); i < s; i++) {
			TypeParameterDeclaration tpd = typeParams.get(i);
			TypeReference repl;
			ClassType resolvedType;
			if (tpd.getBounds() == null || tpd.getBounds().size() == 0) {
				resolvedType = ci.getServiceConfiguration().getNameInfo().getJavaLangObject();
				repl = TypeKit.createTypeReference(ci, resolvedType, tpd); // in rare cases where another type named "Object" is used (e.g. Corba applications)
			} else {
				resolvedType = (ClassType)ci.getType(tpd.getBounds().get(0));
				repl = makeReplacement(tpd);
			}
			Type rt = tpd;
			do {
				List<TypeReference> tprl = ci.getReferences(rt);
				for (int j = 0, t = tprl.size(); j < t; j++) {
					TypeReference tr = tprl.get(j);
					if (!(tr.getASTParent() instanceof TypeArgumentDeclaration))
						typeParamReferences.add(new TypeParamRefReplacement(tr, repl.deepClone()));
					else stuffToBeRemoved.add(tr.getASTParent());
				}
				rt = ci.getServiceConfiguration().getNameInfo().getArrayType(rt);
//				if (rt != null) System.out.println(rt.getFullName());
//				dim++;
				repl.setDimensions(repl.getDimensions()+1);
			}  while (rt != null); 
		}
	}
	
	private TypeReference makeReplacement(TypeParameterDeclaration tpd) {
		// NOTE: no performance issues here
		ProgramFactory f = getProgramFactory();
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		TypeReference repl;
		TypeReference tr;
		List<Method> meth = null;
		List<MemberReference> methRefs = null;
		repl = tpd.getBounds().get(0).deepClone();
		if (tpd.getBoundCount() > 1) {
			StringBuffer text = new StringBuffer("/*");
			for (int x = 1; x < tpd.getBoundCount(); x++) {
				tr = tpd.getBounds().get(x);
				ClassType ct = (ClassType)ci.getType(tr);
				if (tpd.getASTParent() instanceof MethodDeclaration)
					meth = Collections.singletonList(((Method)tpd.getASTParent())); 
				else
					meth = ct instanceof ParameterizedType ? ((ParameterizedType)ct).getAllMethods() : ci.getMethods(ci.getTypeDeclaration(ct));
				for (Method m : meth) {
					methRefs = ci.getReferences(m);
					for (MemberReference mr : methRefs) {
						if (UnitKit.getCompilationUnit(tpd) == UnitKit.getCompilationUnit(mr)) {
//							System.out.println(((MethodReference)mr).getName());
							if (((MethodReference)mr).getReferencePrefix() instanceof Expression) {
								casts.add(new IntroduceCast((Expression)((MethodReference)mr).getReferencePrefix(), tr));
							}
						}
					}
				}
				text.append(" & ");
				text.append(tpd.getBoundName(x));
			}
			text.append(" */");
			repl.setComments(new ASTArrayList<Comment>(f.createComment(text.toString(), false)));
		}
		return repl;
	}

	private static class TypeParamRefReplacement {
		TypeReference typeParamRef;
		TypeReference replacement;
		TypeParamRefReplacement(TypeReference from, TypeReference to) {
			this.typeParamRef = from;
			this.replacement = to;
			replacement.setTypeArguments(null);
		}
	}
	
	private List<CompilationUnit> cul;
	private List<ProgramElement> stuffToBeRemoved;
	private List<IntroduceCast> casts;
	private List<TypeParamRefReplacement> typeParamReferences;
	private List<TypeParamRefReplacement> paramReplacements;
	/**
	 * @param sc
	 */
	public ResolveGenerics(CrossReferenceServiceConfiguration sc, CompilationUnit cu) {
		super(sc);
		cul = new ArrayList<CompilationUnit>();
		cul.add(cu);
	}
	
	public ResolveGenerics(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}
	
	private boolean isLeftHandSide(ProgramElement pe) {
		ProgramElement parent = pe.getASTParent(), tmp = null;
		boolean res = false;
		while (parent != null) {
			if (parent instanceof Assignment && ((Assignment)parent).getExpressionAt(0).equals(pe) && ((Assignment)parent).getArity() == 2)
				return true;
			tmp = parent;
			parent = parent.getASTParent();
			pe = tmp;
		}
		return res;
	}
	
	private void resolveGenericMethod(MethodDeclaration md) {
		// NOTE: no performance issues here
		List<TypeParameterDeclaration> typeParams = md.getTypeParameters();
		if (typeParams == null || typeParams.size() == 0)
			return;
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		resolveTypeParameters(typeParams);
		
		// now deal with type references using type arguments (no need to deal with raw types)
		List<MemberReference> mrl = ci.getReferences(md);
		for (int i = 0, s = mrl.size(); i < s; i++) {
			if (!(mrl.get(i) instanceof MethodReference)) continue;
			// TODO need to deal with SuperConstructorReference !!!!!!!
			MethodReference mr = (MethodReference)mrl.get(i);
			List<TypeArgumentDeclaration> typeArgs = mr.getTypeArguments();
			if (typeArgs == null || typeArgs.size() == 0) {
				Type t = Util.getRequiredContextType(getSourceInfo(), mr);
				if (t instanceof PrimitiveType) // hot fix: this allows ResolveBoxing to do its job properly later!!
					t = ci.getBoxedType((PrimitiveType)t);
				if (t == ci.getReturnType(md) || t == null)
					continue;
				int dim = 0;
				while (t instanceof ArrayType) {
					dim++;
					t = ((ArrayType)t).getBaseType();
				}
				if (!(t instanceof TypeParameter)) {
					TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), t);
					ntr.setDimensions(dim);
					IntroduceCast ic = new IntroduceCast(mr, ntr);
					casts.add(ic);

				}
			} else {
				stuffToBeRemoved.addAll(typeArgs);
			}
		}
		// remove type parameters
		stuffToBeRemoved.addAll(typeParams);
	}
	
	private void resolveMethodReturnType(Method md) {
		// NOTE: this method has performance issues (as of pre-0.93)
		Type returnType = md.getReturnType();
		
		if (!(returnType instanceof ParameterizedType) && !(returnType instanceof TypeParameter) && !(returnType instanceof ArrayType) && !(md instanceof ParameterizedMethod)) {
			return ;
		}
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		
		List<MemberReference> mrl = ci.getReferences(md);
		for (int j = 0, t = mrl.size(); j < t; j++) {
			if (!(mrl.get(j) instanceof MethodReference)) continue; // TODO this may happen with AnnotationPropertyReference. Why?
			MethodReference mr = (MethodReference)mrl.get(j);

			// TODO (Why) do we need this ?
			returnType = md.getReturnType(); // NOTE: this is one performance bottle-neck(!!)

			NonTerminalProgramElement parent = mr.getASTParent();
			while (parent instanceof ParenthesizedExpression)
				parent = parent.getASTParent();
			
			if (parent instanceof TypeCast || parent instanceof StatementContainer)
				continue; // not required to do anything.
		
			// TODO fix all the following code, looks WAY to complicated...
//			while (parent instanceof ParenthesizedExpression || parent instanceof TypeCast) {
//				parent = parent.getASTParent();
//			}
			Type ty = null;
			boolean argument = false;
			boolean firstTime = false;
			if (parent instanceof MethodReference || returnType instanceof TypeParameter || md instanceof ParameterizedMethod) {
				if ( (returnType instanceof TypeParameter || md instanceof ParameterizedMethod) || (((MethodReference)parent).getArguments() != null && ((MethodReference)parent).getArguments().contains(mr))) {
					ty = ci.getMethod(mr).getReturnType(); // may indeed differ from the method's declared return type(!)
					
					if (ty instanceof TypeParameter) {
						Type ttt = Util.getRequiredContextType(ci, mr);
						if (ttt instanceof CapturedTypeArgument) {
							casts.add(new IntroduceCast(mr, resolveCapturedTypeArgument(ttt)));
						} else if (ttt != null && !(ttt instanceof TypeParameter))
							casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(ci, ttt, parent)));
					}
					if (ty != null && !(ty instanceof TypeParameter) && !(ty instanceof ArrayType)) {
//						if (returnType instanceof TypeParameter) System.out.println(vr.getName() + " " + ty.getFullName() + " " + ty.getClass());
						int dim = 0;
						while (ty instanceof ArrayType) {
							dim++;
							ty = ((ArrayType)ty).getBaseType();
						}
						if (ty instanceof CapturedTypeArgument) {
							assert dim == 0;
							// then dim must be 0, so no extra taking care of dim here.
							casts.add(new IntroduceCast(mr, resolveCapturedTypeArgument(ty)));
						}
						else {
							TypeReference ntr = TypeKit.createTypeReference(ci, ty, parent);
							ntr.setDimensions(dim);
							casts.add(new IntroduceCast(mr, ntr));
						}
						while (dim-- > 0) ty = ty.getArrayType();
					}
					else if (parent instanceof Assignment) {
						ty = ci.getType(((Assignment)parent).getExpressionAt(0));
						int dim = 0;
						while (ty instanceof ArrayType) {
							dim++;
							ty = ((ArrayType)ty).getBaseType();
						}
						if (!(ty instanceof TypeParameter)) {
							if (ty instanceof CapturedTypeArgument) {
								assert dim == 0;
								casts.add(new IntroduceCast(mr, resolveCapturedTypeArgument(ty)));
							}
							else {
								TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), ty.getFullName());
								ntr.setDimensions(dim);
								casts.add(new IntroduceCast(mr, ntr));
							}
						}
						if (ty instanceof TypeParameter && ((TypeParameter)ty).getBoundCount() == 0) {
							TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), "java.lang.Object");
							ntr.setDimensions(dim);
							casts.add(new IntroduceCast(mr, ntr));
						}
						if (ty instanceof TypeParameter && ((TypeParameter)ty).getBoundCount() > 0) {
							TypeReference tmp =  TypeKit.createTypeReference(getProgramFactory(), ((TypeParameter)ty).getBoundName(0));
							if (md instanceof MethodDeclaration) tmp.setParent((MethodDeclaration)md);
							else tmp.setParent(mr);
							TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), ci.getType(tmp).getFullName());
							ntr.setDimensions(dim);
							casts.add(new IntroduceCast(mr, ntr));
						}
						while (dim-- > 0) ty = ty.getArrayType();
					}
					else if (parent instanceof Return) {
						NonTerminalProgramElement ntpe = parent;
						while (!(ntpe instanceof MethodDeclaration)) ntpe = ntpe.getASTParent();
						ty = ((MethodDeclaration)ntpe).getReturnType();
						int dim = 0;
						while (ty instanceof ArrayType) {
							dim++;
							ty = ((ArrayType)ty).getBaseType();
						}
						if (!(ty instanceof TypeParameter)) {
							if (ty instanceof CapturedTypeArgument) {
								assert dim == 0;
								TypeReference trr = resolveCapturedTypeArgument(ty);
								casts.add(new IntroduceCast(mr, trr));
							} else {
								casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(getProgramFactory(), ty.getFullName(), dim)));
							}
						}
						if (ty instanceof TypeParameter && ((TypeParameter)ty).getBoundCount() == 0) {
							casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(getProgramFactory(), "java.lang.Object", dim)));
						}
						if (ty instanceof TypeParameter && ((TypeParameter)ty).getBoundCount() > 0) {
							// TODO WHY sometimes search from MethodDeclaration, sometimes from MethodReference context??
							Type tt = null;
							if (md instanceof MethodDeclaration) 
								tt = ci.getType(((TypeParameter)ty).getBoundName(0), (MethodDeclaration)md);
							if (tt == null)
								tt = ci.getType(((TypeParameter)ty).getBoundName(0), mr);
							
							casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(
									getProgramFactory(), 
									tt,
									dim)));
						}
						while (dim-- > 0) ty = ty.getArrayType();
					}
					firstTime = true;
				}
			}
			// NOTE: The code below is not the fastest, but neither is it the slowest.
			while (parent instanceof MethodReference) {
				Expression toCast = null;
				// Argument
				if (((MethodReference)parent).getArguments() != null && ((MethodReference)parent).getArguments().contains(mr) && !firstTime) {
					ty = ci.getType(mr);
					toCast = mr;
					argument = true;
					firstTime = false;
				}
				// ReferenceSuffix
				else {
//					ty = ci.getContainingClassType(ci.getMethod((MethodReference)parent));
					ty = ci.getType(parent);
					toCast = (MethodReference)parent;
				}
				if (!(ty instanceof ClassType))
					break;
				int dim = 0;
				while (ty instanceof ArrayType) {
					ty = ((ArrayType)ty).getBaseType();
					dim++;
				}
				if (!(ty instanceof TypeParameter)) {
					if (ty instanceof CapturedTypeArgument) {
						assert dim == 0;
						casts.add(new IntroduceCast(toCast, resolveCapturedTypeArgument(ty)));
					} else {
						TypeReference ntr = TypeKit.createTypeReference(ci, ty, parent);
						ntr.setDimensions(dim);
						casts.add(new IntroduceCast(toCast, ntr));
					}
				}
				if (ty instanceof TypeParameter) {
					if ((ty instanceof TypeParameterDeclaration && ((TypeParameterDeclaration)ty).getBoundCount() == 1) || (ty instanceof TypeParameterInfo && ((TypeParameterInfo)ty).getBoundCount() == 1)) {
						if (ty instanceof CapturedTypeArgument) {
							assert dim == 0;
							casts.add(new IntroduceCast(toCast, resolveCapturedTypeArgument(ty)));
						}
						else {
							if (ty instanceof TypeParameterDeclaration) {
								TypeReference ntr = TypeKit.createTypeReference(ci, ci.getType(((TypeParameterDeclaration)ty).getBounds().get(0)), mr);
								ntr.setDimensions(dim);
								casts.add(new IntroduceCast(toCast, ntr));
							}
							else {
								TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), ((TypeParameterInfo)ty).getBoundName(0));
								ntr.setDimensions(dim);
								casts.add(new IntroduceCast(toCast, ntr));
							}
						}
					}
					else if (((TypeParameter)ty).getBoundCount() == 0) {
						TypeReference ntr = TypeKit.createTypeReference(getProgramFactory(), "java.lang.Object");
						ntr.setDimensions(dim);
						casts.add(new IntroduceCast(toCast, ntr));
					}
					else if (((TypeParameter)ty).getBoundCount() > 0) {
						System.out.println("More than one bound " + ty.getName() + " " + mr.toSource());
					}
				}
//				if(ty instanceof TypeParameter && returnType instanceof ParameterizedType) {
//					ParameterizedType pt = (ParameterizedType)returnType;
//					System.out.println(toCast.toSource() + " " + ty.getFullName() + " " + returnType.getFullName() + " " + md.getFullName());
//					if (pt.getTypeArgs().get(0) instanceof TypeArgumentDeclaration) {
//						Type tmp = ci.getType((TypeArgumentDeclaration)pt.getTypeArgs().get(0));
////						System.out.println("ResolveMethodReturnType: analyze -> " + ((MethodReference)parent).getName() + " " + tmp.getFullName());
//						if (tmp instanceof CapturedTypeArgument) {
//							casts.add(new IntroduceCast(toCast, resolveCapturedTypeArgument(tmp)));
//						}
//						else casts.add(new IntroduceCast(toCast, TypeKit.createTypeReference(ci, tmp, parent)));
//					}
//				}
				if (argument) break;
				mr = (MethodReference)parent;
				// TODO (Why) do we need this?
				returnType = ci.getMethod(mr).getReturnType();
				parent = parent.getASTParent();
				while (parent instanceof ParenthesizedExpression || parent instanceof TypeCast) {
					parent = parent.getASTParent();
				}
			} 
		}
	}
	
	private void resolveSingleGenericType(TypeDeclaration td) {
		List<TypeParameterDeclaration> typeParams = td.getTypeParameters();
		if (typeParams == null || typeParams.size() == 0)
			return ;
		
//		System.out.println("ResolveSingleGenericType: analyze()");
//		List<TypeParamRefReplacement> typeParamReferences = new ArrayList<TypeParamRefReplacement>();

		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();

		resolveTypeParameters(typeParams);
		
		// now deal with type references using type arguments (no need to deal with raw types)
		List<TypeReference> trl = ci.getReferences(td);
		for (int i = 0, s = trl.size(); i < s; i++) {
			TypeReference tr = trl.get(i);
			if (tr.getASTParent() instanceof VariableDeclaration) {
				if (UnitKit.getCompilationUnit(td).equals(UnitKit.getCompilationUnit(tr.getASTParent())))
					resolveSingleVariableDeclaration((VariableDeclaration)tr.getASTParent());
			}
			if (tr.getASTParent() instanceof MethodDeclaration) {
				if (UnitKit.getCompilationUnit(td).equals(UnitKit.getCompilationUnit(tr.getASTParent())))
					resolveMethodReturnType((MethodDeclaration)tr.getASTParent());
			}
			List<TypeArgumentDeclaration> typeArgs = tr.getTypeArguments();
			if (typeArgs == null || typeArgs.size() == 0)
				continue;
			stuffToBeRemoved.addAll(typeArgs);
		}
		// remove type parameters
		stuffToBeRemoved.addAll(typeParams);
	}
	
	private void resolveSingleVariableDeclaration(VariableDeclaration vd) {
		TypeReference tr = vd.getTypeReference();
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		Type varType = ci.getType(tr);
		if ((tr.getTypeArguments() == null || tr.getTypeArguments().size() == 0) && !(varType instanceof TypeParameter)) {
			return ;
		}
//		System.out.println("ResolveSingleVariableDeclaration: analyze()");

//		try {
//		if (tr.getASTParent().getASTParent().getASTParent().getASTParent().getASTParent().toString().equals("<MethodDeclaration> min(1)"))
//			System.out.println();
//		} catch (NullPointerException e) { }
		
		if (tr.getTypeArguments() != null && tr.getTypeArguments().size() > 0) stuffToBeRemoved.addAll(tr.getTypeArguments());
		
		for (int i = 0, s = vd.getVariables().size(); i < s; i++) {
			VariableSpecification vs = vd.getVariables().get(i);
//			if (vs.getName().equals("l")) System.out.println("l");
			List<? extends VariableReference> vrl = ci.getReferences(vs);
			for (int j = 0, t = vrl.size(); j < t; j++) {
				VariableReference vr = vrl.get(j);
				if (isLeftHandSide(vr)) continue;
				
				if (varType instanceof TypeParameter && ((TypeParameter)varType).getBoundCount() > 1 && 
						!(Util.getRequiredContextType(ci, vr) instanceof TypeParameter) &&
						Util.getRequiredContextType(ci, vr).getFullName() != ((TypeParameter)varType).getBoundName(0)) {
					Type nt = Util.getRequiredContextType(ci, vr); 
					if (nt instanceof PrimitiveType) // hot fix: this allows ResolveBoxing to do its job properly later!!
						nt = ci.getBoxedType((PrimitiveType)nt);

					casts.add(new IntroduceCast(vr, TypeKit.createTypeReference(ci, nt, vr)));
//					continue;
				}
				
				ReferenceSuffix parent = vr.getReferenceSuffix();
				Type tmp = ci.getType(vr);
				if (varType instanceof TypeParameter) {
					if (tmp != null && !(tmp instanceof TypeParameter) && !(tmp instanceof ArrayType)) {
//						System.out.println(vs.getFullName() + " " + tmp.getFullName() + " " + tmp.getClass());
						if (tmp instanceof CapturedTypeArgument) {
							casts.add(new IntroduceCast(vr, resolveCapturedTypeArgument(tmp)));
						}
						else casts.add(new IntroduceCast(vr, TypeKit.createTypeReference(ci, tmp, tr.getASTParent())));
					}
				}

				while (parent instanceof MethodReference || parent instanceof FieldReference) {
					Type ty = ci.getType(parent);

					if (!(ty instanceof ClassType)) {
						break;
					}
					
					int dim = 0;
					while (ty instanceof ArrayType) {
						dim++;
						ty = ((ArrayType)ty).getBaseType();
					}
					
					if (!(ty instanceof TypeParameter)) {
//						System.out.println(((MethodReference)parent).getName() + " " + vs.getFullName() + " " + ty.getFullName());
						if (ty instanceof CapturedTypeArgument) {
							assert dim == 0;
							casts.add(new IntroduceCast((Expression)parent, resolveCapturedTypeArgument(ty)));
						} else {
							TypeReference ntr = TypeKit.createTypeReference(ci, ty, parent);
							ntr.setDimensions(dim);
							casts.add(new IntroduceCast((Expression)parent, ntr));
						}
					}
					//TODO look at this
					else if (ty instanceof TypeParameter) {
						TypeParameterDeclaration tpd = (TypeParameterDeclaration)ci.getTypeDeclaration((ClassType)ty);
						if (tpd.getBoundCount() == 1) {
							if (ty instanceof CapturedTypeArgument) {
								assert dim == 0;
								casts.add(new IntroduceCast((Expression)parent, resolveCapturedTypeArgument(ty)));
							} else {
								TypeReference ntr = TypeKit.createTypeReference(ci, ci.getType(tpd.getBounds().get(0)), parent);
								ntr.setDimensions(dim);
								casts.add(new IntroduceCast((Expression)parent, ntr));
							}
						}
						else if (tpd.getBoundCount() == 0) {
//							casts.add(new IntroduceCast((Expression)parent, TypeKit.createTypeReference(getProgramFactory(), "Object")));
						}
						else if (tpd.getBoundCount() > 0) {
							System.out.println("???"); // ???
//							System.out.println(ty.getName() + " " + parent.toSource());
						}
					}
					parent = parent instanceof MethodReference ? ((MethodReference)parent).getReferenceSuffix() : ((FieldReference)parent).getReferenceSuffix();
//					System.out.println(parent instanceof MethodReference);
				} 
			}
		}
	}
	
	private TypeReference resolveCapturedTypeArgument(Type ty) {
		CapturedTypeArgument capTypeArg = (CapturedTypeArgument)ty;
		TypeReference tRef = null;
		Type tmp = null;
		ProgramFactory f = getProgramFactory();
		try {
			if (capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.None && capTypeArg.getTypeArgument() instanceof TypeArgumentDeclaration) {
				tRef = ((TypeArgumentDeclaration)capTypeArg.getTypeArgument()).getTypeReference();
			}
			else if (capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.Extends && capTypeArg.getTypeArgument() instanceof TypeArgumentDeclaration) {
				tRef = ((TypeArgumentDeclaration)capTypeArg.getTypeArgument()).getTypeReference();
			}
			//TODO what to do if Any
			else { //if (capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.Super || capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.Any) {
				return TypeKit.createTypeReference(getProgramFactory(), "java.lang.Object");
			}
			tmp = getSourceInfo().getType(tRef);
//			if (tmp.getFullName().equals("edu.umd.cs.findbugs.gui2.HashList.E")) {
//				System.out.println();
//			}
			if (!(tmp instanceof TypeParameter)) {
				tRef = f.parseTypeReference(tmp.getFullName());
			}
			else {
				TypeParameter tp = (TypeParameter)tmp;
				if (tp.getBoundCount() > 0 && tp instanceof TypeParameterDeclaration) {
					for (int i = 0; i < tp.getBoundCount(); i++) {
						ASTList<TypeArgumentDeclaration> tadList = ((TypeParameterDeclaration)tp).getBoundTypeArguments(i);
						//TODO what if more than one bound
						if (tadList != null && tadList.size() > 0) {
							if (tadList.get(0).getWildcardMode() == WildcardMode.Extends) {
								tRef = ((TypeParameterDeclaration)tp).getBounds().get(0).deepClone();
							}
							else {
								tRef = TypeKit.createTypeReference(f, "java.lang.Object");
							}
						}
					}
				}
				else {
					tRef = TypeKit.createTypeReference(f, "java.lang.Object");
				}
			}
		}
		catch(ParserException e) {
			System.err.println("Parsing Exception");
		}
		return tRef;
	}

	@Override
	public ProblemReport analyze() {
		stuffToBeRemoved = new ArrayList<ProgramElement>(100000);
		casts = new ArrayList<IntroduceCast>(50000);
		typeParamReferences = new ArrayList<TypeParamRefReplacement>(50000);
		paramReplacements = new ArrayList<TypeParamRefReplacement>(50000);
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		TreeWalker tw;
		int done = 0;
		long start = System.currentTimeMillis();
		int elems = 0;
		int oldCasts = 0;
		int oldRemoved = 0;
		for (CompilationUnit cu : cul) {
			if (done++ % 500 == 1) { 
				long end = System.currentTimeMillis();
				System.out.println("so far done: " + (done-1) + " - " + (end - start) + " ms -- " + elems + " PEs");
				System.out.println("\t" + (casts.size()-oldCasts) + " " + (stuffToBeRemoved.size()-oldRemoved));
				System.out.println("\t" + time);
				start = end;
				elems = 0;
				oldCasts = casts.size();
				oldRemoved = stuffToBeRemoved.size(); 
			}
			tw = new TreeWalker(cu);
			while (tw.next()) {
				elems++;
				ProgramElement pe = tw.getProgramElement();
				NonTerminalProgramElement parent = pe.getASTParent();
				/*if (pe instanceof EnhancedFor) {
					System.err.println("Resolve Enhanced For Loops first!");
					return new TransformationNotApplicable(new EnhancedFor2For(getServiceConfiguration(),cul));
				}
				else*/ if (pe instanceof TypeDeclaration && !(pe instanceof TypeParameterDeclaration)) {
					resolveSingleGenericType((TypeDeclaration)pe);
				} else if (pe instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration)pe;
					resolveGenericMethod(md);
					resolveMethodReturnType(md);
					TypeReference tr = md.getTypeReference();
					if (tr != null && tr.getTypeArguments() != null)
						stuffToBeRemoved.addAll(tr.getTypeArguments());
				} else if (pe instanceof TypeReference) {
					TypeReference tr = (TypeReference)pe;
					if (parent instanceof MethodDeclaration) {
						MethodDeclaration md = (MethodDeclaration)parent;
						if (md.getTypeReference() != tr) continue; // argument, not return type
					} else if (parent instanceof VariableDeclaration) { 
						Type t = getSourceInfo().getType(tr);
						if (t instanceof TypeDeclaration && !(t instanceof TypeParameterDeclaration)) {
							CompilationUnit tcu = UnitKit.getCompilationUnit((TypeDeclaration)t);
							if (tcu == cu) {
								continue;
							}
						}
						VariableDeclaration vd = (VariableDeclaration)parent;
						resolveSingleVariableDeclaration(vd);
					} else if (parent instanceof InheritanceSpecification) {
						InheritanceSpecification is = (InheritanceSpecification)parent;
						ClassType supType = null;
						supType = (ClassType)ci.getType(tr);
						if (supType == null) {
							continue;
						}
						Type t = getSourceInfo().getType(tr);
						if (t instanceof TypeParameterDeclaration)
							continue; // will be taken care of by ResolveSingleGenericType
						// need to introduce type cast in every (inherited) method which
						// is not defined incurrent CU and has generic return type
						// TODO fields !!
						List<? extends Method> ml = supType.getAllMethods();
						if (tr.getTypeArguments() != null) {
							for (int i = 0; i < ml.size(); i++) {
								Method m = ml.get(i);
								if (m instanceof ParameterizedMethod || ((m instanceof MethodInfo || (m instanceof MethodDeclaration && UnitKit.getCompilationUnit((MethodDeclaration)m) != cu))
										&& m.getReturnType() instanceof TypeParameter)) {
									resolveMethodReturnType(m);
								}
							}
						}
						if (supType instanceof ParameterizedType) {
							TypeDeclaration tDecl = (TypeDeclaration)is.getASTParent();
							resolveParameterDeclaration(tDecl, supType);
						}
						if (tr.getTypeArguments() != null) stuffToBeRemoved.addAll(tr.getTypeArguments());
						continue;
					} else if (parent instanceof MethodReference && parent.getASTParent() instanceof MethodReference) {
						// reference to static member
						Method m = getSourceInfo().getMethod((MethodReference)parent.getASTParent());
						if (m instanceof MethodInfo || (m instanceof MethodDeclaration && UnitKit.getCompilationUnit((MethodDeclaration)m) != cu)) {
							resolveMethodReturnType(m);
						} else continue;
					}
				} else if (pe instanceof New) {
					New n = (New)pe;
					if (n.getTypeReference().getTypeArguments() != null)
						stuffToBeRemoved.addAll(n.getTypeReference().getTypeArguments());
					if (n.getClassDeclaration() != null) {
						resolveParameterDeclaration(n.getClassDeclaration(), (ClassType)ci.getType(n));
					}
				} else if (pe instanceof NewArray) {
					NewArray n = (NewArray)pe;
					if (n.getTypeReference().getTypeArguments() != null)
						stuffToBeRemoved.addAll(n.getTypeReference().getTypeArguments());
				} else if (pe instanceof TypeCast) {
					TypeCast tc = (TypeCast)pe;
					if (tc.getTypeReference().getTypeArguments() != null)
						stuffToBeRemoved.addAll(tc.getTypeReference().getTypeArguments());
				} else if (pe instanceof Import) {
					// TODO 0.93 ???
					ClassType ct = null;
					for (int i = 0; i < ((Import)pe).getTypeReferenceCount(); i++) {
						ct = (ClassType)ci.getType(((Import)pe).getTypeReferenceAt(i));
						if (ct instanceof ClassFile) {
							for (Method m : ct.getMethods()) {
								if (m.getReturnType() instanceof TypeParameter) {
									resolveMethodReturnType(m);
								}
							}
						}
					}
				} else if (pe instanceof MethodReference) {
					MethodReference mr = (MethodReference)pe;
					
					if (mr.getTypeArguments() != null && mr.getTypeArguments().size() > 0) {
						for (TypeArgumentDeclaration ta : mr.getTypeArguments()) {
							stuffToBeRemoved.add(ta);
						}
					}
					Method meth = ci.getMethod(mr);
					if ((meth instanceof ParameterizedMethod || meth instanceof  ResolvedGenericMethod)
							&& !(mr.getASTParent() instanceof StatementContainer)
							&& !(mr.getASTParent() instanceof TypeCast)
					) {
						// all but the first condition are to avoid unnecessary TypeCasts.
						resolveMethodReturnType(meth);
					}
				} else if (pe instanceof FieldReference) {
					FieldReference fr = (FieldReference)pe;
					Type ft = ci.getType(fr);
					if (ft instanceof TypeParameter) {
						Type req = Util.getRequiredContextType(ci, fr);

						if (req instanceof CapturedTypeArgument) {
							casts.add(new IntroduceCast(fr, resolveCapturedTypeArgument(req)));
						}
						else if (req != null && !(req instanceof TypeParameter)) {
							casts.add(new IntroduceCast(fr, 
									TypeKit.createTypeReference(getProgramFactory(), req)));
									
						}
					}
				}
			}
		}
		if (casts.size() == 0 && stuffToBeRemoved.size() == 0)
			return setProblemReport(IDENTITY);
		return super.analyze();
	}
	
	private boolean doubleMethod(TypeDeclaration td, Method m) {
		SourceInfo ci = getSourceInfo();
		boolean res = false;
		for (Method m2 : td.getAllMethods()) {
			if (!m.equals(m2) && m.getName().equals(m2.getName()) && m.getSignature().size() == m2.getSignature().size() && !m2.isAbstract()) {
				for (int i = 0; i < m.getSignature().size(); i++) {
					if (m.getSignature().get(i) instanceof ClassType && m2.getSignature().get(i) instanceof ClassType &&
							ci.isSubtype((ClassType)m.getSignature().get(i), (ClassType)m2.getSignature().get(i))) {
						res = true;
						break;
					}
				}
			}
		}
		return res;
	}
	
	private void resolveParameterDeclaration(TypeDeclaration tDecl, ClassType supType) {
		List<? extends Method> ml = supType.getAllMethods();
		CrossReferenceSourceInfo ci = getCrossReferenceSourceInfo();
		for (int mDec = 0; mDec <  tDecl.getMethods().size(); mDec++) {
			Method mDecl = tDecl.getMethods().get(mDec);
			for (int i = ml.size() - 1; i >= 0; i--) {
				Method m = ml.get(i);

				if (mDecl instanceof MethodDeclaration && mDecl.getName().equals(m.getName()) && mDecl.getSignature().size() == m.getSignature().size()) { //) && sameSig) {
					if (doubleMethod(tDecl, mDecl)) break;
					ClassType containingClassType = null;
					List<Method> mList = MethodKit.getAllRedefinedMethods(mDecl);
					if (mList != null && mList.size() != 0) m = mList.get(0);
					if (m instanceof ParameterizedMethod) containingClassType = ((ParameterizedMethod)m).getGenericMethod().getContainingClassType();
					else if (m instanceof ErasedMethod) containingClassType = ((ErasedMethod)m).getGenericMethod().getContainingClassType();
					else containingClassType = m.getContainingClassType();
					if (containingClassType.getTypeParameters() == null || containingClassType.getTypeParameters().size() == 0) {
						break;
					}
					for (int k = 0; k < m.getSignature().size(); k++) {
						if ((m instanceof ParameterizedMethod  && ((ParameterizedMethod)m).getGenericMethod().getSignature().get(k) instanceof TypeParameter) ||
								(m instanceof ErasedMethod  && ((ErasedMethod)m).getGenericMethod().getSignature().get(k) instanceof TypeParameter)
								|| m.getSignature().get(k) instanceof TypeParameter) {
//											System.out.println(m.getSignature().get(k).getFullName());
//							if (pType.getTypeArgs() != null) {
								int indexTypeArg = - 1;
								for (int tp = 0; tp < containingClassType.getTypeParameters().size(); tp++) {
//									System.out.println(containingClassType.getTypeParameters().get(tp).getName());
									if (m instanceof ParameterizedMethod && containingClassType.getTypeParameters().get(tp).getName().equals(((ParameterizedMethod)m).getGenericMethod().getSignature().get(k).getName())) {
										indexTypeArg = tp;
										break;
									}
									else if (m instanceof ErasedMethod && containingClassType.getTypeParameters().get(tp).getName().equals(((ErasedMethod)m).getGenericMethod().getSignature().get(k).getName())) {
										indexTypeArg = tp;
										break;
									}
									else if (containingClassType.getTypeParameters().get(tp).getName().equals(m.getSignature().get(k).getName())) {
										indexTypeArg = tp;
										break;
									}
								}
								if (indexTypeArg == -1) continue;
//								System.out.println(((ParameterizedType)supType).getTypeArgs().get(indexTypeArg).getTypeName() + " " + pType.getGenericType().getTypeParameters().get(indexTypeArg).getBoundName(0));
								TypeReference typeArg = null;
								TypeReference param = ((MethodDeclaration)mDecl).getParameterDeclarationAt(k).getTypeReference();
								if (ci.getType(param) instanceof PrimitiveType) continue;
								if (containingClassType.getTypeParameters().get(indexTypeArg).getBoundCount() == 0 && param.getDimensions() == 0)		
									typeArg = TypeKit.createTypeReference(getProgramFactory(), "Object");
								else if (containingClassType.getTypeParameters().get(indexTypeArg).getBoundCount() == 0 && param.getDimensions() > 0) {
									typeArg = TypeKit.createTypeReference(getProgramFactory(), getNameInfo().createArrayType(getNameInfo().getJavaLangObject(), param.getDimensions()).getName());
								}
								else {
									typeArg = TypeKit.createTypeReference(getProgramFactory(), containingClassType.getTypeParameters().get(indexTypeArg).getBoundName(0));
								}
								typeArg = typeArg.deepClone();
								typeArg.setParent(((MethodDeclaration)mDecl).getParameterDeclarationAt(k).getTypeReference().getParent());
//								System.out.println(mDecl.getName() + " " + typeArg.getName() + " " + tDecl.getFullName() + " " + (param.getASTParent().getChildPositionCode(param) != -1));
								try {
									typeArg = getProgramFactory().parseTypeReference(ci.getType(typeArg).getFullName());
								}
								catch (ParserException e) {
									System.err.println("Parsing Exception");
								}
								paramReplacements.add(new TypeParamRefReplacement(param, typeArg));
//								System.out.println(mDecl.getName() + " " + typeArg.getReferencePrefix().toSource() + " " + typeArg.getName() + " " + tDecl.getFullName() + " " + (param.getASTParent().getChildPositionCode(param) != -1));
								List<VariableSpecification> varList = ((MethodDeclaration)mDecl).getParameterDeclarationAt(k).getVariables();
								for(Variable v : varList) {
									List<VariableReference> vrList = ci.getReferences(v);
									for(VariableReference vr : vrList) {
										if (isLeftHandSide(vr)) continue;
										Type ty = ci.getType(param);
										if (!(ty instanceof TypeParameter) && !(ty instanceof PrimitiveType)) {
//											System.out.println(((MethodReference)parent).getName() + " " + vs.getFullName() + " " + ty.getFullName());
											if (ty instanceof CapturedTypeArgument) {
												casts.add(new IntroduceCast(vr, resolveCapturedTypeArgument(ty)));
											}
											else casts.add(new IntroduceCast(vr, TypeKit.createTypeReference(ci, ty, vr)));
										}
										//TODO look at this
										else if (ty instanceof TypeParameter) {
											TypeParameterDeclaration tpd = (TypeParameterDeclaration)ci.getTypeDeclaration((ClassType)ty);
											if (tpd.getBoundCount() > 0) {
												if (ty instanceof CapturedTypeArgument) {
													casts.add(new IntroduceCast(vr, resolveCapturedTypeArgument(ty)));
												}
												else casts.add(new IntroduceCast(vr, TypeKit.createTypeReference(ci, ci.getType(tpd.getBounds().get(0)), vr)));
											}
											else if (tpd.getBoundCount() == 0) {
												casts.add(new IntroduceCast(vr, TypeKit.createTypeReference(getProgramFactory(), "Object")));
											}
										}
									}
								}
//							}
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void transform() {
		System.out.println("casts: " + casts.size() + " paramReplacements: " + paramReplacements.size() + " typeParamReferences: " + typeParamReferences.size() + " stuffToBeRemoved " + stuffToBeRemoved.size());
//		System.out.println("ResolveGenerics: transform()");
		super.transform();
//		System.out.println("Transforming Compilation Unit " + cu.getName());
		ProgramFactory f = getProgramFactory();
//		sortCasts(casts);
		Util.sortCasts(casts);
//		removeAmbiguousCasts(casts);
//		for (int i = parts.size()-1; i >= 0; i--) {
//			parts.get(i).transform();
//		}
//		for (TwoPassTransformation tp : trParts) {
//			tp.transform();
//		}
		for (TypeParamRefReplacement t : paramReplacements) {
			MiscKit.unindent(t.replacement);
			if (t.typeParamRef.getASTParent().getChildPositionCode(t.typeParamRef) != -1)
				replace(t.typeParamRef, t.replacement);
			else {
				// TODO investigate again: why/how does this happen?
				System.out.println("-1 " + t.typeParamRef.getName() + " " + t.replacement.getName());
			}
		}
		for (TypeParamRefReplacement t : typeParamReferences) {
			MiscKit.unindent(t.replacement);
			if (t.typeParamRef.getASTParent().getChildPositionCode(t.typeParamRef) != -1)
				replace(t.typeParamRef, t.replacement);
		}
		for (ProgramElement pe : stuffToBeRemoved) {
			if (pe.getASTParent().getIndexOfChild(pe) != -1)
				detach(pe);
		}
		for (IntroduceCast c : casts) {
			MiscKit.unindent(c.toBeCasted);
			// TODO StatementContainer / TypeCast should be caugt MUCH, MUCH earlier!
			NonTerminalProgramElement ee = c.toBeCasted.getASTParent();
			while (ee instanceof ParenthesizedExpression)
				ee = ee.getASTParent();
			if (ee instanceof StatementContainer || ee instanceof TypeCast)
				continue;
			if (c.toBeCasted.getASTParent().getIndexOfChild(c.toBeCasted) != -1)
//				&&	!(c.toBeCasted.getASTParent() instanceof StatementContainer)
//				&&  !(c.toBeCasted.getASTParent() instanceof TypeCast))
					replace(c.toBeCasted, f.createParenthesizedExpression(f.createTypeCast(c.toBeCasted.deepClone(), c.castedType)));
		}
	}
}
