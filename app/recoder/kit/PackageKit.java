// This file is part of the RECODER library and protected by the LGPL

package recoder.kit;

import java.util.ArrayList;
import java.util.List;

import recoder.ProgramFactory;
import recoder.abstraction.ClassType;
import recoder.abstraction.Package;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.PackageReference;

/**
 * this class implements basic functions for package handling.
 * 
 * @author Uwe Assmann
 * @author Andreas Ludwig
 * @author Rainer Neumann
 */
public class PackageKit {

    private PackageKit() {
    	super();
    }

    /**
     * Creates a new package reference derived by the name of the given package.
     * 
     * @param f
     *            the program factory to be used.
     * @param p
     *            the package which shall be referenced.
     */
    public static PackageReference createPackageReference(ProgramFactory f, Package p) {
        PackageReference result = null;
        String name = p.getFullName();
        /* Fix by T.Gutzmann */
        if (name.equals(""))
            return null; // null is admissible as prefix
        int i, j = -1;
        do {
            i = j + 1;
            j = name.indexOf(".", i);
            String token = (j > i) ? name.substring(i, j) : name.substring(i);
            result = f.createPackageReference(result, f.createIdentifier(token));
        } while (j > i);
        return result;
    }

    /**
     * Query that collects all types in a package that are not available as
     * sources.
     * 
     * @param pkg
     *            the package to check for non-source types.
     * @return a list of class types of the given package that are no
     *         {@link recoder.java.declaration.TypeDeclaration}s.
     */
    public static List<ClassType> getNonSourcePackageTypes(Package pkg) {
        List<ClassType> result = new ArrayList<ClassType>();
        List<? extends ClassType> classes = pkg.getTypes();
        for (int i = classes.size() - 1; i >= 0; i -= 1) {
            ClassType ct = classes.get(i);
            if (!(ct instanceof TypeDeclaration)) {
                result.add(ct);
            }
        }
        return result;
    }
}