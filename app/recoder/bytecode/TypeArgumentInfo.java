/*
 * Created on 27.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.bytecode;

import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeParameter;
import recoder.service.DefaultImplicitElementInfo;

/**
 * 
 * @author Tobias Gutzmann
 *
 */
public class TypeArgumentInfo implements TypeArgument {
	final WildcardMode wildcardMode;
	private final String typeName;
	final List<TypeArgumentInfo> typeArgs;
	private final boolean isTypeVariable;
	private ClassType typeVariable;
	ClassTypeContainer parent;
	/**
	 * 
	 */
	public TypeArgumentInfo(WildcardMode wildcardMode, String typeName, List<TypeArgumentInfo> typeArgs, ClassTypeContainer parent, boolean isTypeVariable) {
		super();
		if ((typeName == null && wildcardMode != WildcardMode.Any) || wildcardMode == null || parent == null
				|| typeName != null && wildcardMode == WildcardMode.Any) 
			throw new IllegalArgumentException();
		this.wildcardMode = wildcardMode;
		if (typeName != null)
			this.typeName = typeName.intern();
		else
			this.typeName = null;
		this.typeArgs = typeArgs;
		this.isTypeVariable = isTypeVariable;
		this.parent = parent;
	}
	public WildcardMode getWildcardMode() {
		return wildcardMode;
	}
	public String getTypeName() {
		return typeName;
	}
	public List<TypeArgumentInfo> getTypeArguments() {
		return typeArgs;
	}
	
	public ClassType getTypeParameter() {
		if (!isTypeVariable)
			return null;
		if (typeVariable == null) {
			ClassTypeContainer ctc = getContainer();
			String taName = typeName;
			int dim = 0;
			while (taName.endsWith("[]")) {
				taName = taName.substring(0, taName.length()-2);
				dim++;
			}
			while (true) {
				if (ctc instanceof MethodInfo) {
					MethodInfo mi = (MethodInfo)ctc;
					if (mi.getTypeParameters() != null) {
						for (int i = 0; i < mi.getTypeParameters().size(); i++) {
							if (mi.getTypeParameters().get(i).getName().equals(taName)) {
								ClassType res = mi.getTypeParameters().get(i);
								while (dim-- > 0)
									res = res.createArrayType();
								typeVariable = res;
								return res;
							}
						}
					}
				}
				else if (ctc instanceof ClassFile) {
					ClassFile cf = (ClassFile)ctc;
					if (cf.getTypeParameters() != null) {
						for (int i= 0; i < cf.getTypeParameters().size(); i++) {
							if (cf.getTypeParameters().get(i).getName().equals(taName)) {
								ClassType res = cf.getTypeParameters().get(i);
								while (dim-- > 0)
									res = res.createArrayType();
								typeVariable = res;
								return res;
							}
						}
					}
				} else {
					// ???
					throw new RuntimeException();
				}
				ctc = ctc.getContainer();
			}
		}
		return typeVariable;
	}
	
	public boolean isTypeVariable() {
		return isTypeVariable;
	}
	
	public ClassTypeContainer getContainer() {
		return parent;
	}
	
	public ClassFile getContainingClassFile() {
		if (parent instanceof ClassFile)
			return (ClassFile)parent;
		else return (ClassFile)((MethodInfo)parent).getContainingClassType();
	}
	public MethodInfo getContainingMethodInfo() {
		if (parent instanceof MethodInfo)
			return (MethodInfo)parent;
		return null;
	}
	
	public boolean semanticalEquality(TypeArgument ta) {
		// TODO clean up the call below... 
		return TypeArgument.EqualsImpl.equals(this, ta, (DefaultImplicitElementInfo)parent.getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
	}

	public int semanticalHashCode() {
		return TypeArgument.EqualsImpl.semanticalHashCode(this);
	}
		
	public TypeParameter getTargetedTypeParameter() {
		// TODO ???
		throw new RuntimeException("TODO");
	}

    public String getFullSignature() {
    	return TypeArgument.DescriptionImpl.getFullDescription(this);
    }

}
