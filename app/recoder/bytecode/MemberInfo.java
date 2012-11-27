// This file is part of the RECODER library and protected by the LGPL.

package recoder.bytecode;

import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.Member;

public abstract class MemberInfo extends ByteCodeElement implements Member {

    protected ClassFile parent;

    List<AnnotationUseInfo> annotations;
    
    boolean isTypeVariable;

    public MemberInfo(int accessFlags, String name, ClassFile parent, boolean isTypeVariable) {
        super(accessFlags, name);
        setParent(parent);
        this.isTypeVariable = isTypeVariable;
    }
    
    public boolean isTypeVariable() {
    	return isTypeVariable;
    }
    
    void setAnnotations(List<AnnotationUseInfo> annotations) {
        this.annotations = annotations;
    }

    public void setParent(ClassFile parent) {
        this.parent = parent;
    }

    public ClassFile getParent() {
        return parent;
    }

    public ClassType getContainingClassType() {
        return service.getContainingClassType(this);
    }
    
    /**
     * @return a list of annotations
     */
    public List<AnnotationUseInfo> getAnnotations() {
        return annotations;
    }
}

