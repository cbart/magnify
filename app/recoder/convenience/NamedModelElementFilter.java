// This file is part of the RECODER library and protected by the LGPL.

package recoder.convenience;

import recoder.ModelElement;
import recoder.NamedModelElement;

/**
 * Filter predicate for named model elements. Can search for certain names and
 * types (optionally).
 * 
 * @author AL
 */
public class NamedModelElementFilter implements ModelElementFilter {

    private Class<? extends NamedModelElement> type;

    private String name;

    /**
     * Create a filter that accepts any named model element with the given name.
     * 
     * @param name
     *            the name that is accepted.
     */
    public NamedModelElementFilter(String name) {
        this.type = NamedModelElement.class;
        this.name = name;
    }

    /**
     * Create a filter that accepts any named model element with the given name
     * and type.
     * 
     * @param type
     *            the most general type to accept.
     * @param name
     *            the name that is accepted.
     * @exception IllegalArgumentException
     *                if the type is not a subtype of
     *                {@link recoder.NamedModelElement}.
     */
    public NamedModelElementFilter(Class<? extends NamedModelElement> type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Accepts or denies a given model element.
     * 
     * @param e
     *            the model element to value.
     * @return true iff the given element is accepted by the filter.
     */
    public boolean accept(ModelElement e) {
        return type.isInstance(e) && name.equals(((NamedModelElement) e).getName());
    }
}