/**
 * 
 */
package recoder.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelException;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeParameter;
import recoder.convenience.Format;
import recoder.convenience.Formats;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.Import;
import recoder.java.JavaProgramElement;
import recoder.java.PackageSpecification;
import recoder.java.ProgramElement;
import recoder.java.SourceVisitor;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.Modifier;
import recoder.java.declaration.VariableSpecification;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Final;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.Static;
import recoder.java.declaration.modifier.Volatile;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.statement.Case;
import recoder.java.statement.EnhancedFor;
import recoder.java.statement.If;
import recoder.java.statement.Switch;
import recoder.kit.UnitKit;


/**
 * Currently, this class is a "stand alone" checker, not incorporated into
 * the other services, and must be called from "the outside". Future versions
 * of this class may work differently, i.e., be integrated into the model
 * update process.
 * This class is WORK IN PROGRESS and doesn't at all cover all the semantics
 * checks from the Java Language Specification yet! 
 * @since 0.92 
 * TODO: may need to be reworked to be better integrated into services. 
 * @author Tobias Gutzmann
 * 
 */
public class SemanticsChecker {
	private final CrossReferenceServiceConfiguration crsc;
	private final SourceInfo si;
	private final NameInfo ni;
	private boolean java5allowed, java7allowed;
	private ErrorHandler errorHandler;
	private final SemanticsCheckerVisitor checker = new SemanticsCheckerVisitor();
	
	/**
	 * 
	 */
	public SemanticsChecker(CrossReferenceServiceConfiguration crsc) {
		this.crsc = crsc;
		si = crsc.getSourceInfo();
		ni = crsc.getNameInfo();
	}
	
	public void checkAllCompilationUnits() throws ModelException {
		//if (true) return;
		for (CompilationUnit cu : crsc.getSourceFileRepository().getCompilationUnits()) {
			check(cu);
		}
	}
	
	public void check(CompilationUnit cu) throws ModelException {
		// TODO does not support setting compatibility to different versions yet!!
		java5allowed = crsc.getProjectSettings().java5Allowed();
		java7allowed = crsc.getProjectSettings().java7Allowed();

		errorHandler = crsc.getProjectSettings().getErrorHandler();
		
		// check each single element in the tree:
		TreeWalker tw = new TreeWalker(cu);
		while (tw.next()) {
			tw.getProgramElement().accept(checker);
		}
	}

	/**
	 * The SourceVisitor carrying the actual semantics
	 * @author Tobias Gutzmann
	 */
	private class SemanticsCheckerVisitor extends SourceVisitor {
		@Override
		public void visitIf(If x) {
	    	Type exprType = si.getType(x.getExpression()); 
	    	if (!(exprType == ni.getBooleanType() || 
	    			(java5allowed && exprType == ni.getJavaLangBoolean()))) {
	    		errorHandler.reportError(
	    				new TypingException(x.getExpression()));
	    	}
		}
		
		@Override
		public void visitEnhancedFor(EnhancedFor ef) {
			Type lhsType = 
				si.getType(((LocalVariableDeclaration)ef.getInitializers().get(0)).getTypeReference());
			Type rhsType =
				si.getType(ef.getGuard());
			if (rhsType instanceof ArrayType) {
				if (!si.isWidening(((ArrayType) rhsType).getBaseType(), lhsType))
					errorHandler.reportError(new TypingException("EnhancedFor: types are not compatible", ef.getGuard()));
				return;
			} else {
				// Is it an Iterable type on the right?
				List<ClassType> allSupers = si.getAllSupertypes((ClassType)rhsType);
				// find the supertype "Iterable"
				List<ClassType> iterables = new ArrayList<ClassType>();
				for(ClassType ct: allSupers) {
					if (ct.getFullName().equals("java.lang.Iterable")) {
						iterables.add(ct);
					}
				}
				if (iterables.size() == 0) {
					errorHandler.reportError(new TypingException("EnhancedFor: guard is not Iterable", ef.getGuard()));
				} else {
					if (lhsType == ni.getJavaLangObject())
						return; // ok. the for loop has java.lang.Object as lhs, which is always possible to assign
					if (lhsType instanceof PrimitiveType)
						lhsType = si.getBoxedType((PrimitiveType)lhsType);
					boolean foundProperType = false;
					for (ClassType ct: iterables) {
						if (ct instanceof ParameterizedType) {
							ct = ((DefaultSourceInfo)si).getBaseType(((ParameterizedType)ct).getTypeArgs().get(0));
						} // TODO else ???
						if (si.isWidening(ct, lhsType)) {
							foundProperType = true;
							break;
						}
					}
					if (!foundProperType) {
						errorHandler.reportError(
								new TypingException("lhs does not match rhs - " + lhsType.getFullSignature() + " : " + rhsType.getFullSignature(), ef.getGuard()));
					}
				}
			}
		}
		
		
		@Override
		public void visitCopyAssignment(CopyAssignment ca) {
			Type lhs = si.getType(ca.getChildAt(0));
	    	Type rhs = si.getType(ca.getChildAt(1));
			if (lhs instanceof PrimitiveType && !(rhs instanceof PrimitiveType))
				rhs = si.getUnboxedType((ClassType)rhs);
			else if (rhs instanceof PrimitiveType && !(lhs instanceof PrimitiveType))
				rhs = si.getBoxedType((PrimitiveType)rhs);
			else if (rhs instanceof CapturedTypeArgument)
				rhs = ((DefaultSourceInfo)si).getBaseType(((CapturedTypeArgument)rhs).getTypeArgument());
	    	if (!si.isWidening(
	    			rhs, lhs)) {
	    		if (rhs instanceof PrimitiveType &&
	    			si.getServiceConfiguration().getConstantEvaluator().isCompileTimeConstant((Expression)ca.getChildAt(1))) {
	    			// TODO needs extra check...
	    			// byte b = 5; <- valid code, while
	    			// void foo(byte b) { foo(5);} <- isn't
	    			return;
	    		}
	    		// TODO better error report...
	    		errorHandler.reportError(new ModelException("Invalid assignment"));
	    	}
		}
		
		@Override
		public void visitClassDeclaration(ClassDeclaration cd){
			if (cd.isAbstract()&& cd.isFinal()){
				errorHandler.reportError( new IllegalModifierException("Illegal class declaration " + cd.getFullName() ));
			}
		}
		
		@Override
		public void visitFieldReference(FieldReference fr) {
			Field f = si.getField(fr);	
			if (fr.getReferencePrefix()== null){	
				if(occursStaticContext(fr)){
					if (!f.isStatic()){
						errorHandler.reportError(new IllegalNonStaticAccessException("Non-static field used in static context"));
					}
				}
			}
			else if (fr.getReferencePrefix() instanceof TypeReference){	
				if(occursStaticContext(fr)){
					if (!f.isStatic()){
							errorHandler.reportError(new IllegalNonStaticAccessException("Non-static field used in static context"));
					}
				}
			}
		}			
			
		 // helper method for visitFieldReference
	    private final boolean occursStaticContext(FieldReference fr) {
	    	ProgramElement pe = fr;
	        while (pe != null) {
	        	//  if field reference is defined in another .java file ,
	        	//  the field reference would be a program element of compilation unit,
	        	//  use tree walker to check that compilation unit, find the accurate place of field Reference
	        	//  in the .java file.
	        	if(pe instanceof CompilationUnit){
	        		TreeWalker tw = new TreeWalker(pe);
	        		while (tw.next()) {	   
	        			// check if the reference in import
	        			if(tw.getProgramElement().getClass().getName()=="recoder.java.Import"){
	        				Import imp = (Import) tw.getProgramElement();
	        				return imp.isStaticImport();	        	
	        			}	      
	        			// check if the reference is in the class initializer
	        			if (tw.getProgramElement() instanceof ClassInitializer){	        				
	     	                return ((ClassInitializer)tw.getProgramElement()).isStatic();
	     	            }
	        			// check if the reference is in the method declaration
	     	            if (tw.getProgramElement() instanceof MethodDeclaration){	     	            	
	     	                return ((MethodDeclaration)tw.getProgramElement()).isStatic();
	     	            }
	     	            //check if the reference is in the field declaration
	     	            if (tw.getProgramElement() instanceof FieldDeclaration){	     	            	
	     	            	return ((FieldDeclaration)tw.getProgramElement()).isStatic();
	     	            }	
	     	            // check if the reference is in the annotation declaration
	     	            if(tw.getProgramElement()instanceof AnnotationDeclaration){
	     	            	return false; // field defined in annotation always return false
	     	            }
	        		}
	        	}
	            if (pe instanceof ClassInitializer)
	                return ((ClassInitializer)pe).isStatic();
	            if (pe instanceof MethodDeclaration)
	                return ((MethodDeclaration)pe).isStatic();
	            if (pe instanceof FieldDeclaration)
	            	return ((FieldDeclaration)pe).isStatic();
	            if (pe instanceof PackageSpecification)
	            	return true;
	            pe = pe.getASTParent();
	        }	        
	        // this *hopefully* only happens if parent links aren't set properly	 
	        System.out.println(UnitKit.getCompilationUnit(fr) + " " + fr.getStartPosition());
	        throw new RuntimeException("cannot determine if FieldReference " +
	        		Format.toString(pe) + " occurs in static context; check parent links!");
	    }
	    
		@Override
		public void visitFieldDeclaration(FieldDeclaration fd) {
			int count=0;
			int both=0;
			List<DeclarationSpecifier> fieldDSlist;
			if (fd.getDeclarationSpecifiers()!=null){
				fieldDSlist = fd.getDeclarationSpecifiers();
				if (fieldDSlist.size()>1){			
					for (Iterator<DeclarationSpecifier> it = fieldDSlist.iterator();it.hasNext();){
						DeclarationSpecifier ds = it.next();					
						if (ds instanceof Public){
							count++;
						}	
						if (ds instanceof Protected){
							count++;
						}	
						if (ds instanceof Private){
							count++;
						}
						if(ds instanceof Final){
							both++;	
						}
						if(ds instanceof Volatile){
							both++;
						}				
					}
					if(count>1){
						errorHandler.reportError(new IllegalModifierException("Illegal field declaration"));
					}
					if (both>1){
						errorHandler.reportError(new IllegalModifierException("Illegal field declaration"));
					}
				}
			}
	    }
		
		@Override
		public void visitMethodDeclaration(MethodDeclaration md) {
			if (md.getASTParent() instanceof InterfaceDeclaration){
				int isAbstract = 0;
				int isPublic =0;				
				List<Modifier> mlist;
				if (md.getModifiers()!=null){
					mlist = md.getModifiers();
					for(Modifier modifier: mlist){			
						if (modifier instanceof Abstract){						
							isAbstract ++;
						}
						if (modifier instanceof Public){						
							isPublic ++;
						}					
					}							
					if (mlist.size()!= 0){					
						if (isAbstract == 0){						
							if (isPublic == 0){
								errorHandler.reportError(new IllegalModifierException(" method can only define  abstract && public in interface " + 
					    	        	"method : " + Format.toString(md) + Format.toString(md.getASTParent()) + " method define wrong!")); 
							}	
							if (isPublic >1){						
								errorHandler.reportError(new IllegalModifierException(" method can only define  abstract && public in interface " + 
					    	        	"method : " + Format.toString(md) + Format.toString(md.getASTParent()) + " method define wrong!")); 
							}						
						}	
						if (isAbstract == 1){
								if ((isPublic == 0)&&(mlist.size()>1)){						
									errorHandler.reportError(new IllegalModifierException(" method can only define  abstract && public in interface " + 
						    	        	"method : " + Format.toString(md) + Format.toString(md.getASTParent()) + " method define wrong!")); 
								}	
								if (isPublic >1){						
									errorHandler.reportError(new IllegalModifierException(" method can only define  abstract && public in interface " + 
						    	        	"method : " + Format.toString(md) + Format.toString(md.getASTParent()) + " method define wrong!")); 
								}	
						}		
						if (isAbstract>1){
							errorHandler.reportError(new IllegalModifierException(" method can only define  abstract && public in interface " + 
				    	        	"method : " + Format.toString(md) + Format.toString(md.getASTParent()) + " method define wrong!")); 										
						}
					}				
				}
			}
			if (md.getASTParent() instanceof ClassDeclaration){
				if (md.isAbstract()){
					ClassDeclaration cd = (ClassDeclaration) md.getASTParent();			
					if (!cd.isAbstract()){
						errorHandler.reportError(new IllegalModifierException("abstract method can only define in an abstract class " + 
		    	        		"method : " + Format.toString(md) + "class : "+Format.toString(cd) + " abstract method define wrong!")); 
					}
					else {				
						if (md.getModifiers()!=null){
							int isProtected = 0;
							int isPublic = 0;
							List<Modifier> mlist = md.getModifiers();
														
							for(Modifier modifier: mlist){
								if (modifier instanceof Protected){								
									isProtected ++;
								}
								if (modifier instanceof Public){							
									isPublic ++;
								}
							}
							
							if ((isProtected > 1)||(isPublic > 1)){
								errorHandler.reportError(new IllegalModifierException("abstract method can only define protected or public in an abstract class " + 
					    	        	"method : "  + Format.toString(md) + "class : "+Format.toString(cd) + " abstract method define wrong!")); 
							}
							if ((isProtected == 0)&&(isPublic == 0)&& (mlist.size()>1)){
								errorHandler.reportError(new IllegalModifierException(("abstract method can only define protected or public in an abstract class " + 
					    	        	"method : "  + Format.toString(md) + "class : "+Format.toString(cd) + " abstract method define wrong!"))); 
							}
							if ((isProtected == 1)&&(isPublic == 1)){
								errorHandler.reportError(new IllegalModifierException(("abstract method can only define protected or public in an abstract class " + 
					    	        	"method : "  + Format.toString(md) + "class : "+Format.toString(cd) + " abstract method define wrong!"))); 
							}							
						}
					}
				}
				else{
					if (md.getModifiers()!=null){
						ClassDeclaration cd = (ClassDeclaration) md.getASTParent();	
						int one = 0;
						List<Modifier> mlist = md.getModifiers();
						for(Modifier modifier: mlist){
							if (modifier instanceof Protected){								
								one ++;
							}
							if (modifier instanceof Private){								
								one ++;
							}
							if (modifier instanceof Public){								
								one ++;
							}
						}
						if ((one>1)&&(mlist.size()>1)){
							errorHandler.reportError(new IllegalModifierException(("can only define one access modifer " + 
			    	        		"method : "  + Format.toString(md) + "class : "+Format.toString(cd) + " method access modifier define wrong!"))); 
						}
					}
				}
			}
		}
		
		@Override
		public void visitMethodReference(MethodReference mr) {
			Method m = si.getMethod(mr);
			String msg;
			if ((msg = isAppropriate(m, mr)) != null) {
				errorHandler.reportError(
						new UnresolvedReferenceException(
                        		Format.toString(
                                "Inappropriate method access: " + msg + " at " + Formats.ELEMENT_LONG, mr), mr));

			}
		}
		
		/** 
		 * helper method for visitMethodReference
		 * UNTESTED AND INCOMPLETE !!!
		 */
	    private final String isAppropriate(Method m, MethodReference mr) {	        

	    	if (mr.getReferencePrefix() == null) {
	        	if (!m.isStatic()){
	        		if (occursInStaticContext(mr))
	        			return "method invocation to non-static method occurs in static context (a)"; 
		        	else if (mr.getExpressionContainer()!= null){
		        		if (mr.getExpressionContainer().getASTParent() instanceof LocalVariableDeclaration){	
		        			ProgramModelElement pme =ni.getUnknownElement();
		        			ProgramModelInfo pmi = pme.getProgramModelInfo();		        			
		        			LocalVariableDeclaration ld = 
		        				(LocalVariableDeclaration) mr.getExpressionContainer().getASTParent();   
		        			List<VariableSpecification> varlist = ld.getVariableSpecifications();
		        			for(VariableSpecification var :varlist){		        				
		        				if (!(pmi.isWidening(m.getReturnType(),var.getType()))){
				        			return "return type is wrong!check the reference method";
		        				}
		        			}
		        		}
		        	}
	        	}
	        	else if (mr.getExpressionContainer()!= null){
	        		if (mr.getExpressionContainer().getASTParent() instanceof LocalVariableDeclaration){	
	        			ProgramModelElement pme =ni.getUnknownElement();
	        			ProgramModelInfo pmi = pme.getProgramModelInfo();	        			
	        			LocalVariableDeclaration ld = 
	        				(LocalVariableDeclaration) mr.getExpressionContainer().getASTParent();   
	        			List<VariableSpecification> varlist = ld.getVariableSpecifications();
	        			for(VariableSpecification var :varlist){	        				
	        				if (!(pmi.isWidening(m.getReturnType(),var.getType()))){			        		
					        	return "return type is wrong!check the reference method";
	        				}
	        			}	   
	        		}
	        	}	        	
	        	return null;    
	        }
	        if (mr.getReferencePrefix() instanceof TypeReference && !m.isStatic()) {
	        	// static access to a nun-static member
	        	return "Static access to a non-static member";
	        }
	        if (mr.getTypeReferenceCount() == 1) {
	           if (m.isStatic()){
	            	if (si.getMethodDeclaration(m)!=null){
		            	MethodDeclaration md = si.getMethodDeclaration(m);
		            	if ( md.getDeclarationSpecifiers()!= null && md.getDeclarationSpecifiers().size()!=0){
			            	List<DeclarationSpecifier> dslist = md.getDeclarationSpecifiers();
			            	int size = dslist.size();
			            	for(DeclarationSpecifier ds: dslist){			            		
			            		if (ds instanceof Static){
			            			size = size -1;
			            		}
			            	}
			            	if (size == dslist.size()){
			            		return "cannot access super method because it is static";
			            	}
		            	}
	            	}
	            }	           
	        	return null;
	        }	
	        if (mr.getReferencePrefix() instanceof SuperReference) {
	            SuperReference sr = (SuperReference)mr.getReferencePrefix();           
	            if (m.isAbstract()) {                        	
	            	List<Method> methodlist =  si.getMethods(mr);
	            	for(Method method: methodlist){     
	            		MethodDeclaration md = si.getMethodDeclaration(method);
	            		List<DeclarationSpecifier> dslist = md.getDeclarationSpecifiers();
	            		for(DeclarationSpecifier ds: dslist){
	            			if (ds instanceof Abstract){
		            			return "cannot access super method because it is abstract";
	            			}
	            		}           		
	            	}
	            }
	            if (occursInStaticContext(mr)) return "method invocation to non-static method occurs in static context (c)";
	            if (sr.getReferencePrefix() != null && (sr.getReferencePrefix() instanceof TypeReference)) {
	                //this can only be ThisReference
	            	
	                /* 
	                 * "Let C be the class denoted by ClassName. If the invocation
	                 *  is not directly enclosed by C or an inner class of C, then
	                 *  a compile-time error occurs"
	                 */
	            }
	            return null;
	        }
	        
	        if (mr.getReferencePrefix() instanceof ThisReference){
	        	if (occursInStaticContext(mr)){
	        		 return "method invocation to non-static method occurs in static context (d)";
	        	}
	        }
	        if (mr.getReferenceSuffix() != null && m.getReturnType() == null)
	            return "void method must not have a reference suffix";
	        return null;
	    }
	    
	    // helper method for visitMethodReference
	    private final boolean occursInStaticContext(MethodReference mr) {
	    	ProgramElement pe = mr;
	        while (pe != null) {
	            if (pe instanceof ClassInitializer)
	                return ((ClassInitializer)pe).isStatic();
	            if (pe instanceof MethodDeclaration)
	                return ((MethodDeclaration)pe).isStatic();
	            if (pe instanceof FieldDeclaration)
	            	return ((FieldDeclaration)pe).isStatic();
	            pe = pe.getASTParent();
	        }
	        // this *hopefully* only happens if parent links aren't set properly
	        throw new RuntimeException("cannot determine if MethodReference " + 
	        		Format.toString(pe) + " occurs in static context; check parent links!");
	    }
	    
	    @Override
	    public void visitTypeReference(TypeReference tr) {
	    	checkRawTypeOk(tr);
	    }
	    
	      
	    private void checkRawTypeOk(TypeReference tr) {
	    	if (tr.getReferencePrefix() != null && tr.getReferencePrefix() instanceof TypeReference){
	    		ClassType trType = (ClassType)si.getType(tr);
	    		// ok if the type itself is not a parameterized type, or an outer type that has no type parameters
	    		if (!(trType instanceof ParameterizedType) && 
	    				(trType.getTypeParameters() == null || trType.getTypeParameters().size() == 0))
	    			return;
    	    	TypeReference rp = (TypeReference) tr.getReferencePrefix();    
    	    	ClassType rpType = (ClassType)si.getType(rp);
	    		//check if inner is non-raw
	    	    if (tr.getTypeArguments()!= null && tr.getTypeArguments().size() != 0  ){
	    	    	// then the outer must not be raw, or the member type must not be inner 
	    	    	if (trType.isInner() && rpType instanceof ErasedType) {
	    	    		errorHandler.reportError(
	    	    				new TypingException(
	    	    						"inner type has type arguments, but the outer does not.",null));
	    	    	}
	    	    } else {
	    	    	// if inner type is raw - outer type must not be parameterized then either
	    	    	List<? extends TypeParameter> tps = ((ParameterizedType)trType).getGenericType().getTypeParameters();
	    	    	if(rpType instanceof ParameterizedType && tps != null && tps.size() > 0){
	    	    		errorHandler.reportError(
    	    					new TypingException(
    	    							tr.getName().toString() + " occurs in outer type declaration!",null));
	    	    	}
	    	    	
	    	    }
	    	       	  
	    	}   	
	    }
	    
	    @Override
	    public void visitSwitch(Switch x) {
	    	Type exprType = si.getType(x.getExpression());
	    	if (exprType == ni.getJavaLangString()) {
	    		if (!java7allowed)
	    			errorHandler.reportError(new TypingException("String in Switch allowed only since Java 7.", x.getExpression()));
	    		return;
	    	}
	    	if (java5allowed && exprType instanceof ClassType && !((ClassType)exprType).isEnumType())
	    		exprType = si.getUnboxedType((ClassType)exprType); 
	    	if (si.isWidening(exprType, ni.getIntType()))
	    		return; // ok - convertible to int.
	    	if (java5allowed && si.isWidening(exprType, ni.getJavaLangEnum()))
	    		return; // ok - enum.
	    	errorHandler.reportError(new TypingException("Invalid type in Switch-expression: " + exprType.getFullName(), x.getExpression()));
	    }
	    
	    @Override
	    public void visitCase(Case x) {
	    	Type constType = si.getType(x.getExpression());
	    	
	    	if (!crsc.getConstantEvaluator().isCompileTimeConstant(x.getExpression())) {
	    		// TODO better exception.
	    		errorHandler.reportError(new SyntaxException((JavaProgramElement)x.getExpression(), "is not a compile-time constant"));
	    		return;
	    	}
	    	// only checks if constant matches type; other checks (enum? string?) are performed for switch-expression only.
	    	Type switchExprType = si.getType(x.getParent().getExpression());
	    	if (constType instanceof PrimitiveType) {
	    		if (switchExprType instanceof ClassType)
	    			switchExprType = si.getUnboxedType((ClassType)switchExprType);
	    		if (switchExprType == null) {
	    			errorHandler.reportError(new TypingException("Mismatching case expression (1)", x.getExpression()));
	    			return;
	    		}
	    		// need to check: Is the constant in bound?
	    		if (!fitsIntoForSwitch(x.getExpression(), (PrimitiveType)switchExprType)) {
	    			fitsIntoForSwitch(x.getExpression(), (PrimitiveType)switchExprType); // debug.
	    			System.out.println(x.getExpression().toSource());
	    			System.out.println("->" + switchExprType.getFullName());
	    			System.out.println(UnitKit.getCompilationUnit(x).getPrimaryTypeDeclaration().getFullName());
	    			errorHandler.reportError(new TypingException("Mismatching case expression (2)", x.getExpression()));
	    			return;
	    		}
	    		return; // ok.
	    	}
	    	// must be exact match.
	    	if (constType != switchExprType)  {
	    		errorHandler.reportError(new TypingException("Mismatching case expression (3)", x.getExpression()));
	    		return;
	    	}
	    	// further, reference must *not* be qualified.
	    	if (((ClassType)constType).isEnumType() && ((FieldReference)x.getExpression()).getReferencePrefix() != null)
	    		errorHandler.reportError(new SyntaxException((JavaProgramElement)x.getExpression(), "enum constants in case must not be prefixed."));
	    }

		private boolean fitsIntoForSwitch(Expression expression, PrimitiveType switchExprType) {
			ConstantEvaluator.EvaluationResult er = new ConstantEvaluator.EvaluationResult();
			if (!crsc.getConstantEvaluator().isCompileTimeConstant(expression, er))
				throw new IllegalArgumentException("not a compile time constant: " + expression.toSource()); // ???
			int max, min, actual;
			if (switchExprType == ni.getByteType()) {
				max = Byte.MAX_VALUE;
				min = Byte.MIN_VALUE;
			} else if (switchExprType == ni.getCharType()) {
				max = Character.MAX_VALUE;
				min = Character.MIN_VALUE;
			} else if (switchExprType == ni.getIntType()) {
				max = Integer.MAX_VALUE;
				min = Integer.MIN_VALUE;
			} else if (switchExprType == ni.getShortType()) {
				max = Short.MAX_VALUE;
				min = Short.MIN_VALUE;
			} else throw new IllegalArgumentException(switchExprType.getFullName());
			switch (er.getTypeCode()) {
			case ConstantEvaluator.BYTE_TYPE:  actual = er.getByte();  break;
			case ConstantEvaluator.SHORT_TYPE: actual = er.getShort(); break; 
			case ConstantEvaluator.CHAR_TYPE:  actual = er.getChar();  break;
			case ConstantEvaluator.INT_TYPE:   actual = er.getInt();   break;
			case ConstantEvaluator.LONG_TYPE:  return false; // not allowed in this context.
			default: throw new IllegalArgumentException("???");
			}
			return actual >= min && actual <= max; 
		}
	}
}
