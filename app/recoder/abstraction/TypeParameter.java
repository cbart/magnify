/*
 * Created on 25.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.ArrayList;
import java.util.List;

import recoder.service.ProgramModelInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public interface TypeParameter extends ClassType {
	public String getParameterName();
	
	/**
	 * Guaranteed to be >= 1. Implementations of 
	 * this class make sure that at least 
	 * java.lang.Object is declared.
	 * @return The number of bounds.
	 */
	public int getBoundCount();
	/**
	 *
	 * @param boundidx
	 * @return the fully qualified name 
	 */
	public String getBoundName(int boundidx);
	/**
	 * @return The type arguments to the bound at position <code>i</code>.
     * possibly <code>null</code>.
	 */
	public List<? extends TypeArgument> getBoundTypeArguments(int boundidx);
	
	/**
	 * ONLY FOR INTERNAL USE !!!
	 * TODO better documentation
	 * @param other
	 * @return
	 */
	public boolean inheritanceEqual(TypeParameter other);
	
	public static class DescrImp {
		public static String getFullSignature(TypeParameter tp) {			
			String res = tp.getParameterName();
			String del = " extends ";
			for (int i = 0; i < tp.getBoundCount(); i++) {
				res += del;
				res += tp.getBoundName(i);
				List<? extends TypeArgument> tas = tp.getBoundTypeArguments(i);
				if (tas != null && tas.size() > 0) {
					res += "<";
					String delim2 = "";
					for (TypeArgument ta : tas) {
						res += delim2;
						res += ta.getFullSignature();
						delim2 = ",";
					}
					res += ">";
				}
				del = ",";
			}
			return res;
		}
	}
	
	/**
	 * TODO 0.90 could be removed !?
	 * Helper class that implements the equals() method for implementing classes.
	 * @see recoder.java.declaration.TypeParameterDeclaration
	 * @see recoder.bytecode.TypeParameterInfo
	 */
	public static class EqualsImplementor {
		private static boolean checkSameBounds(TypeParameter tp1, TypeParameter tp2) {
			// they MIGHT be compatible, if they have the same bounds.
			int tp1s = tp1.getBoundCount();
			int tp2s = tp2.getBoundCount();
			if (tp2s-tp1s < -1 || tp1s-tp2s > 1)
				return false;
			if (tp1s != tp2s)
				return false;
			List<String> l1 = new ArrayList<String>();
			List<String> l2 = new ArrayList<String>();
			for (int i = 0; i < tp1.getBoundCount(); i++) {
				l1.add(tp1.getBoundName(i));
			}
			for (int i = 0; i < tp2.getBoundCount(); i++) {
				l2.add(tp2.getBoundName(i));
			}
			// in case one comes from bytecode and one from source code
			// and the bounds of the source code tp are all of type 
			// interfaces...
			// TODO TypeParameterDeclaration should report this properly!
			if (!l1.contains("java.lang.Object"))
				l1.add("java.lang.Object");
			if (!l2.contains("java.lang.Object"))
				l2.add("java.lang.Object");
			if (l1.size() != l2.size())
				return false;
			if (!l1.containsAll(l2))
				return false;
			return true;
			// TODO check bound type args ??!!
	    }
		
		public static boolean matchButNotEqual(TypeParameter t1, TypeParameter t2) {
			if (t1 == t2)
				return true;
			ClassTypeContainer ctc1 = t1.getContainer();
			ClassTypeContainer ctc2 = t2.getContainer();
			if (ctc1 instanceof Method && ctc2 instanceof Method) {
				return checkSameBounds(t1, t2);
			} 
			return false;
		}
		
	}
}
