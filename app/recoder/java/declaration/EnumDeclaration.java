/*
 * Created on 15.08.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.java.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import recoder.ModelException;
import recoder.java.Identifier;
import recoder.java.ProgramElement;
import recoder.java.SourceVisitor;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Final;
import recoder.list.generic.ASTList;
import recoder.service.IllegalModifierException;

/**
 * @author Tobias Gutzmann
 *
 */
public class EnumDeclaration extends TypeDeclaration {
	/**
	 * serialization id
	 */
	private static final long serialVersionUID = -6436741776435910109L;
	
	private Implements implementing;
	
	/**
	 * 
	 */
	public EnumDeclaration() {
		super();
	}

	/**
	 * @param declSpecs
	 * @param name
	 * @param extended
	 * @param implemented
	 * @param members
	 */
	public EnumDeclaration(ASTList<DeclarationSpecifier> declSpecs, Identifier name, 
			Implements implementing, ASTList<MemberDeclaration> members) {
		super(declSpecs, name);
		setMembers(members);
		this.implementing = implementing;
		makeParentRoleValid();
	}

	/**
	 * @param proto
	 */
	public EnumDeclaration(EnumDeclaration proto) {
		super(proto);
		this.members = proto.members.deepClone();
		if (proto.implementing != null)
			this.implementing = proto.implementing.deepClone();
		makeParentRoleValid();
	}
	
    public boolean isInterface() {
        return false;
    }
    
    public boolean isOrdinaryInterface() {
        return false;
    }

    public boolean isAnnotationType() {
        return false;
    }

    public boolean isEnumType() {
        return true;
    }

    public boolean isOrdinaryClass() {
        return false;
    }
    
    public void makeParentRoleValid() {
    	super.makeParentRoleValid();
    	if (implementing != null) {
    		implementing.setParent(this);
    	}
    }
    
    public int getChildPositionCode(ProgramElement child) {
        // role 0 (IDX): declaration specifier
        // role 1: identifier
        // role 2: implements
        // role 3 (IDX): members
    	if (declarationSpecifiers != null) {
    		int idx = declarationSpecifiers.indexOf(child);
    		if (idx != -1)
    			return (idx << 4) | 0;
    	}
    	if (child == name)
    		return 1;
    	if (child == implementing)
    		return 2;
    	if (members != null) {
    		int idx = members.indexOf(child);
    		if (idx != -1)
    			return (idx << 4) | 3;
    	}
    	return -1;
    }

	public int getChildCount() {
		int res = 0;
		if (declarationSpecifiers != null) res += declarationSpecifiers.size();
		if (name != null) res++;
		if (implementing != null) res++;
		if (members != null) res += members.size();
		return res;
	}

	public ProgramElement getChildAt(int index) {
		if (declarationSpecifiers != null) {
			if (index < declarationSpecifiers.size())
				return declarationSpecifiers.get(index);
			index -= declarationSpecifiers.size();
		}
		if (name != null) {
			if (index == 0)
				return name;
			index--;
		}
		if (implementing != null) {
			if (index == 0)
				return implementing;
			index--;
		}
		return members.get(index);
	}

	public boolean replaceChild(ProgramElement p, ProgramElement q) {
		if (p == null) throw new NullPointerException();
		if (name == p) {
			name = (Identifier)q;
			if (name != null)
				name.setParent(this);
			return true;
		}
		if (implementing == p) {
			implementing = (Implements)q;
			if (implementing != null)
				implementing.setParent(this);
			return true;
		}
		if (declarationSpecifiers != null) {
			int idx = declarationSpecifiers.indexOf(p);
			if (idx != -1) {
				if (q != null) {
					DeclarationSpecifier ds = (DeclarationSpecifier)q;
					declarationSpecifiers.set(idx, ds);
					ds.setParent(this);
				} else {
					declarationSpecifiers.remove(idx);
				}
				return true;
			}
		}
		if (members != null) {
			int idx = members.indexOf(p);
			if (idx != -1) {
				if (q != null) {
					MemberDeclaration md = (MemberDeclaration)q;
					members.set(idx, md);
					md.setMemberParent(this);
				} else {
					members.remove(idx);
				}
				return true;
			}
		}
		return false;
	}

	public void accept(SourceVisitor v) {
		v.visitEnumDeclaration(this);
	}

	public EnumDeclaration deepClone() {
		return new EnumDeclaration(this);
	}
	
	public void setImplementedTypes(Implements implementing) {
		this.implementing = implementing;
	}
	
	public Implements getImplementedTypes() {
		return implementing;
	}
	
	@Override
	public boolean isFinal() {
		boolean res = true;
		for (int i = 0; i < members.size(); i++) {
			MemberDeclaration m = members.get(i);
			if (m instanceof EnumConstantDeclaration) {
				if (((EnumConstantDeclaration)m).getEnumConstantSpecification().getConstructorReference().getClassDeclaration() != null) {
					res = false;
					break;
				}
			}
		}
		return res;
	}
	
	@Override
	public boolean isAbstract() {
		// forbidden by language specification, so return false
		return false;
	}
	
	@Override
	public boolean isStatic() {
		// nested enum types are implicitly static (JLS)
		return getASTParent() instanceof TypeDeclaration || super.isStatic();
	}
	
	@Override
	public void validate() throws ModelException {
		if (containsModifier(Abstract.class)) throw new IllegalModifierException("Illegal abstract modifier in EnumDeclaration " + getFullName());
		if (containsModifier(Final.class)) throw new IllegalModifierException("Illegal final modifier in EnumDeclaration " + getFullName());
		// TODO this appears wrong, check again:
		//if (getASTParent() instanceof TypeDeclaration && !((TypeDeclaration)getASTParent()).isStatic())
		//	throw new ModelException("enum " + getFullName() + " may not be member type of a (non-static) inner class");
		// TODO: local ? => error
		
	}

	@Override
	public ASTList<TypeParameterDeclaration> getTypeParameters() {
		return null;
	}

    /**
     * returns an unmodifiable list containing the enum constants. 
     * never returns <code>null</code>.
     * @return the enum constants
     */
    public List<EnumConstantDeclaration> getConstants() {
        if (members == null)
            return Collections.emptyList();
        ArrayList<EnumConstantDeclaration> result = new ArrayList<EnumConstantDeclaration>();
        for (int i = 0; i < members.size(); i++) {
            MemberDeclaration m = members.get(i);
            if (m instanceof EnumConstantDeclaration)
                result.add((EnumConstantDeclaration)m);
        }
        result.trimToSize();
        return result;
    }
    
    /**
     * returns an unmodifiable list of all members excluding the constants
     * Never returns <code>null</code>.
     * @return a list of the members excluding constants
     */
    public List<MemberDeclaration> getNonConstantMembers() {
        if (members == null)
            return Collections.emptyList();
        ArrayList<MemberDeclaration> result = new ArrayList<MemberDeclaration>();
        for (int i = 0; i < members.size(); i++) {
            MemberDeclaration m = members.get(i);
            if (!(m instanceof EnumConstantDeclaration))
                result.add(m);
        }
        result.trimToSize();
        return result;
    }
    
	public boolean isInner() {
		if (isStatic()) 
			return false;
		if (!(getContainingClassType() instanceof ClassDeclaration))
			return false;
		if (getName() == null)
			return false;
		return true;
	}
}
