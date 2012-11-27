// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;

import java.util.List;

/**
 * A program model element representing methods.
 */
public interface Method extends Member, ClassTypeContainer {

    /**
     * Returns the signature of this method or constructor.
     * 
     * @return the signature of this method.
     */
	List<Type> getSignature();

    /**
     * Returns the exceptions of this method or constructor.
     * 
     * @return the exceptions of this method.
     */
	List<ClassType> getExceptions();

    /**
     * Returns the return type of this method.
     * 
     * @return the return type of this method.
     */
    Type getReturnType();

    /**
     * Checks if this member is abstract. A constructor will report <CODE>false
     * </CODE>.
     * 
     * @return <CODE>true</CODE> if this member is abstract, <CODE>false
     *         </CODE> otherwise.
     * @see recoder.abstraction.Constructor
     */
    boolean isAbstract();

    /**
     * Checks if this method is native. A constructor will report <CODE>false
     * </CODE>.
     * 
     * @return <CODE>true</CODE> if this method is native, <CODE>false
     *         </CODE> otherwise.
     * @see recoder.abstraction.Constructor
     */
    boolean isNative();

    /**
     * Checks if this method is synchronized. A constructor will report <CODE>
     * false</CODE>.
     * 
     * @return <CODE>true</CODE> if this method is synchronized, <CODE>false
     *         </CODE> otherwise.
     * @see recoder.abstraction.Constructor
     */
    boolean isSynchronized();
    
    /**
     * Checks if this method takes a variable number of arguments.
     * 
     * @since 0.80
     * @return <CODE>true</CODE> if this methods takes a variable number of arguments,
     * 		   <code>false</code> otherwise.
     */
    boolean isVarArgMethod();

    /**
     * 
     * @since 0.80
     * @return
     */
	public List<? extends TypeParameter> getTypeParameters();

}