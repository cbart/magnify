// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;

import java.util.List;

/**
 * A program model element representing class types.
 *
 * @author AL
 * @author RN
 */
public interface ClassType extends Type, Member, ClassTypeContainer {

    /**
     * Checks if this class type denotes an interface (i.e. ordinary interface or annotation type).
     *
     * @return <CODE>true</CODE> if this object represents an interface,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isInterface();

    /**
     * Checks if this class type denotes an ordinary (i.e. not annotation type) interface.
     *
     * @return <CODE>true</CODE> if this object represents an ordinary interface,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isOrdinaryInterface();

    /**
     * Checks if this class type denotes an annotation type
     * @return <CODE>true</CODE> if this object represents an annotation type,
     *         <CODE>false</CODE> otherwise
     */
    boolean isAnnotationType();

    /**
     * Checks if this class type denotes an enum type
     * @return <CODE>true</CODE> if this object represents an enum type,
     *         <CODE>false</CODE> otherwise
     */
    boolean isEnumType();

    /**
     * Checks if this class type denotes an ordinary (i.e. not an enum) class.
     * @return <CODE>true</CODE> if this object represents an ordinary class type,
     *         <CODE>false</CODE> otherwise
     */
    boolean isOrdinaryClass();

    /**
     * Checks if this member is abstract. An interface will report <CODE>true
     * </CODE>.
     *
     * @return <CODE>true</CODE> if this member is abstract, <CODE>false
     *         </CODE> otherwise.
     * @see #isInterface()
     */
    boolean isAbstract();

    /**
     * Checks if this type is an inner class, as described in JLS, 3rd edition, 8.1.3
     * @return
     */
    boolean isInner();

    /**
     * Returns the list of locally declared supertypes of this class type.
     *
     * @return the list of locally defined supertypes of this type.
     */
    List<ClassType> getSupertypes();

    /**
     * Returns the list of all supertypes of this class type, in topological
     * order, including the class type itself as first element. The order allows
     * to resolve member overloading or overloading.
     *
     * @return the list of all supertypes of this type in topological order.
     */
    List<ClassType> getAllSupertypes();

    /**
     * Returns the fields locally defined within this class type.
     *
     * @return the list of field members of this type.
     */
    List<? extends Field> getFields();

    /**
     * Returns all visible fields that are defined in this class type or any of
     * its supertypes. The fields are in topological order with respect to the
     * inheritance hierarchy.
     *
     * @return the list of visible field members of this type and its
     *         supertypes.
     */
    List<Field> getAllFields();

    /**
     * Returns the methods locally defined within this class type.
     *
     * @return the list of methods of this type.
     */
    List<Method> getMethods();

    /**
     * Returns all visible methods that are defined in this class type or any of
     * its supertypes. The methods are in topological order with respect to the
     * inheritance hierarchy.
     *
     * @return the list of visible methods of this type and its supertypes.
     */
    List<Method> getAllMethods();

    /**
     * Returns the constructors locally defined within this class type.
     *
     * @return the list of constructors of this type.
     */
    List<? extends Constructor> getConstructors();

    /**
     * Returns all class types that are inner types of this class type,
     * including visible inherited types.
     *
     * @return a list of class types that are members of this type or any of its
     *         supertypes.
     * @see #getAllSupertypes
     */
    List<ClassType> getAllTypes();

    /**
     * Returns the type parameters of this class type.
     * @return the list of type parameters of this class type.
     */
	public List<? extends TypeParameter> getTypeParameters();

	/**
	 * Returns the type erasue of the given type. May be the type itself,
	 * if the type is "unerasable".
	 * TODO Might be moved to ProgramModelInfo, in case that types such as
	 * java.lang.Object, who equal their erased type, shouldn't create an erased type after all.
	 * @return
	 *
	 */
	public ClassType getErasedType();

	/**
	 * Returns the base type of the given class type, e.g., an erased type returns
	 * its unerased type, a parameterized type it's generic base type, a
	 * type parameter the type of its first bound. May trigger a model update.
	 * @return
	 */
	public ClassType getBaseClassType();
}