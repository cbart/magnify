/**
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias
 *
 */
public class ErasedMethod implements Method {
	private final Method genericMethod;
	private final ImplicitElementInfo service;
	
	/**
	 * 
	 */
	public ErasedMethod(Method genericMethod, ImplicitElementInfo service) {
		this.service = service;
		this.genericMethod = genericMethod;
		assert !(genericMethod instanceof ErasedMethod 
				|| genericMethod instanceof ParameterizedMethod);
	}
	
	public Method getGenericMethod() {
		return genericMethod;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getExceptions()
	 */
	public List<ClassType> getExceptions() {
		return service.getExceptions(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getReturnType()
	 */
	public Type getReturnType() {
		return service.getReturnType(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getSignature()
	 */
	public List<Type> getSignature() {
		return service.getSignature(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getTypeParameters()
	 */
	public List<? extends TypeParameter> getTypeParameters() {
		// Retain. May be used for type inference. See bug 2046005
		return genericMethod.getTypeParameters();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isAbstract()
	 */
	public boolean isAbstract() {
		return genericMethod.isAbstract();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isNative()
	 */
	public boolean isNative() {
		return genericMethod.isNative();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isSynchronized()
	 */
	public boolean isSynchronized() {
		return genericMethod.isSynchronized();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isVarArgMethod()
	 */
	public boolean isVarArgMethod() {
		return genericMethod.isVarArgMethod();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getAnnotations()
	 */
	public List<? extends AnnotationUse> getAnnotations() {
		return getGenericMethod().getAnnotations(); // ??
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	public ClassType getContainingClassType() {
		return genericMethod.getContainingClassType().getErasedType();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isFinal()
	 */
	public boolean isFinal() {
		return genericMethod.isFinal();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPrivate()
	 */
	public boolean isPrivate() {
		return genericMethod.isPrivate();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isProtected()
	 */
	public boolean isProtected() {
		return genericMethod.isProtected();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPublic()
	 */
	public boolean isPublic() {
		return genericMethod.isPublic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStatic()
	 */
	public boolean isStatic() {
		return genericMethod.isStatic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStrictFp()
	 */
	public boolean isStrictFp() {
		return genericMethod.isStrictFp();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return genericMethod.getFullName();
	}
	
    public String getBinaryName() {
		return genericMethod.getBinaryName();
	}


	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ImplicitElementInfo getProgramModelInfo() {
		return service;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new RuntimeException();
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return genericMethod.getName();
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getContainer()
	 */
	public ClassTypeContainer getContainer() {
		return genericMethod.getContainer(); // ??
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getPackage()
	 */
	public Package getPackage() {
		return genericMethod.getPackage();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getTypes()
	 */
	public List<? extends ClassType> getTypes() {
		return service.getTypes(this); 
	}

	@Override
	public String toString() {
		return genericMethod.toString() + "%ERASED%";
	}
	
	@Override
	public Method getGenericMember() {
		return genericMethod;
	}

}
