/** @author  William S. York.
 *  Copyright (c) 2009 - 2013, William S. York and the University of Georgia.
 *****  MIT style license  *****
 *  Permission is hereby granted, free of charge, to any person obtaining a copy 
 *  of this software and associated documentation files (the "Software"), to deal 
 *  in the Software without restriction, including without limitation the rights 
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 *  copies of the Software, and to permit persons to whom the Software is 
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in 
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 *  IN THE SOFTWARE.
 *  @version 1.1
 *  @date    Sun Apr 14, 2013
 */
package JMass2;

import java.util.Vector;

/**
 * The ElementComposition class defines an ordered list of TableOfIsotopes.Element instances
 * and a corresponding array of stoichiometric coefficients
 * to fully describe the elemental composition of a molecule.
 * The Element instances must first be defined as Elements in an instance of 
 * the TableOfIsotopes class.
 */
public class ElementComposition {
	
	/**
	 * a TableOfIsotopes, which defines the elements and pseudoelements that comprise the ElementComposition
	 * and the masses and fractional abundances of the isotopes of those elements and pseudoelements.
	 */
	TableOfIsotopes toi;
	
	/**
	 * an ordered list of Elements that comprise the ElementComposition.
	 */
	Vector<TableOfIsotopes.Element> eList = new Vector<TableOfIsotopes.Element>();
	
	/**
	 * an ordered list of stoichiometric coefficients that, along with the eList, defines the elemental
	 * composition corresponding to the ElementComposition.
	 */
	int[] coefficient = new int[0];
	
	
	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition
	 * are not specified when this constructor is invoked.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 */
	ElementComposition(TableOfIsotopes toi) {
		//  generates an empty ElementComposition based on toi
		this.toi = toi;
	}  // end constructor

	
	
	
	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition
	 * are specified by an ordered list of elements (and pseudoelements) and a similarly ordered
	 * array of stoichiometric coefficients.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param eList an ordered list of elements and pseudoelements included in the ElementComposition
	 * @param coefficient an array of stoichiometric coefficients corresponding to the elements and
	 * pseudoelements in the same order as specified in the eList
	 */
	ElementComposition(TableOfIsotopes toi, Vector<TableOfIsotopes.Element> eList, int[] coefficient) {
		this.toi = toi;
		this.eList = eList;
		this.coefficient = coefficient;
	} // end constructor
	
	
	

	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition are defined 
	 * using the fullStoichiometry array, whose elements specify stoichiometric coefficients that map 
	 * to elements of the same rank in the TableOfIsotope's eList.  
	 * Use of this constructor thus requires explicit knowledge of the order
	 * of Elements in the TableOfIsotope's eList, which can be obtained using the TableOfIsotope's
	 * <b>getElementSymbols</b> method.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param fullStoichiometry an array of integers that specify the stoichiometric coefficients
	 * that map to elements of the same rank in the TableOfIsotope's eList.  
	 */
	ElementComposition(TableOfIsotopes toi, int[] fullStoichiometry) {
		this.toi = toi;
		set(toi, fullStoichiometry);		
	} // end constructor
	
	

	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition are defined 
	 * using the elementCompString, which can either list a set of integers or a set of symbols and numbers
	 * separated by spaces.  
	 * <p>When elementCompString contains only integers, it is parsed to generate
	 * a full stoichiometry array, whose elements specify stoichiometric coefficients that map 
	 * to elements of the same rank in the TableOfIsotope's eList.  
	 * Use of this constructor with a list of integers thus requires explicit knowledge of the order
	 * of Elements in the TableOfIsotope's eList, which can be obtained using the TableOfIsotope's
	 * <b>getElementSymbols</b> method.</p>
	 * <p>When elementCompString contains both symbols and integers, it is parsed to to generate an ordered eList 
	 * (list of Elements), which are specified by the symbols, and a corresponding ordered array of 
	 * stoichiometric coefficients, which are specified by the integers.</p>
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param elementCompString a String containing either integers separated by spaces 
	 * (for example, "0 0 0 0 12 0 0 0 0 6 0 6") or element symbols and coefficients separated by spaces
	 * (for example, "H 12 C 6 O 6").
	 */	
	ElementComposition(TableOfIsotopes toi, String elementCompString) {
		// this version of the method takes a SINGLE String (elementCompString) to specify elemental composition
		// elementCompString can either list a set of integers or a set of symbols and numbers
		this.toi = toi;
		set(toi, elementCompString);		
	} // end constructor
	
	
	

	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition are defined 
	 * using stoichiometryString, which is an array of Strings, each of which specify
	 * an element (or pseudoelement) and the corresponding stoichiometric coefficient, separated by a 
	 * space.
	 * Use of this constructor does <b>not</b> require any knowledge of the order
	 * of Elements in the TableOfIsotope's eList.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param stoichiometryString an array of Strings, each containing a symbol and the corresponding
	 * stoichiometric coefficient separated by a space.  For example, {"H 12", "C 6", "O 6"}.
	 */	
	ElementComposition(TableOfIsotopes toi, String[] stoichiometryString) {
		this.toi = toi;
		set(toi, stoichiometryString);		
	} // end constructor

	
	
	
	/**
	 * constructs a new instance of ElementComposition, specifying the TableOfIsotopes used to 
	 * define the components of the ElementComposition.  The components of the ElementComposition are defined 
	 * using two arguments: (1) <b>elementSymbol</b>, which is an array of Strings, each of which specify
	 * an element (or pseudoelement); (2) <b>coefficient</b> which is an array of integers, which specify
	 * the corresponding stoichiometric coefficients.
	 * Use of this constructor does <b>not</b> require any knowledge of the order
	 * of Elements in the TableOfIsotope's eList.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param elementSymbol an array of Strings, each containing a symbol.  For example, {"H", "C", "O"}.
	 * @param coefficient an array of integers that specify the stoichiometric coefficients corresponding
	 * in order to the array elementSymbol.  For example, {12, 6, 6}.
	 */	
	ElementComposition(TableOfIsotopes toi, String[] elementSymbol, int[] coefficient) {
		this.toi = toi;
		set(toi, elementSymbol, coefficient);		
	} // end constructor

	
	/**
	 * sets the eList and coefficient fields of the ElementComposition using 
	 * the fullStoichiometry array, whose elements specify stoichiometric coefficients that map 
	 * to elements of the same rank in the TableOfIsotope's eList.  
	 * Use of this method thus requires explicit knowledge of the order
	 * of Elements in the TableOfIsotope's eList, which can be obtained using the TableOfIsotope's
	 * <b>getElementSymbols</b> method.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param fullStoichiometry an array of integers that specify the stoichiometric coefficients
	 * that map to elements of the same rank in the TableOfIsotope's eList.  
	 */
	public void set(TableOfIsotopes toi, int[] fullStoichiometry) {
		// first, count non-zero elements in fullStoichiometry
		int count = 0;
		for (int j = 0; j < fullStoichiometry.length; j++) { 
			// k must map to index of corresponding element in toi.eList
			if (fullStoichiometry[j] > 0) count++;
		}
		
		this.coefficient = new int[count];
		this.eList = new Vector<TableOfIsotopes.Element>();
		int i = 0;  // index for elements in this molecule
		for (int j = 0; j < fullStoichiometry.length; j++) { 
			// j must map to index of corresponding element in toi.eList
			if (fullStoichiometry[j] > 0) {  // the number of atoms of this element [k] is greater than zero
				// retrieve the symbol of the kth Element in toi.eList
				this.coefficient[i] = fullStoichiometry[j];
				this.eList.add(toi.eList.get(j));
				i++;
			}
		}		
	}  // end set method
	
	/**
	 * 
	 * sets the eList and coefficient fields of the ElementComposition using 
	 * the elementCompString, which can either list a set of integers or a set of symbols and numbers
	 * separated by spaces.  
	 * <p>When elementCompString contains only integers, it is parsed to generate
	 * a full stoichiometry array, whose elements specify stoichiometric coefficients that map 
	 * to elements of the same rank in the TableOfIsotope's eList.  
	 * Use of this method with a list of integers thus requires explicit knowledge of the order
	 * of Elements in the TableOfIsotope's eList, which can be obtained using the TableOfIsotope's
	 * <b>getElementSymbols</b> method.</p>
	 * <p>When elementCompString contains both symbols and integers, it is parsed to to generate an ordered eList 
	 * (list of Elements), which are specified by the symbols, and a corresponding ordered array of 
	 * stoichiometric coefficients, which are specified by the integers.</p>
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param elementCompString a String containing either integers separated by spaces 
	 * (for example, "0 0 0 0 12 0 0 0 0 6 0 6") or element symbols and coefficients separated by spaces
	 * (for example, "H 12 C 6 O 6").
	 */
	public void set(TableOfIsotopes toi, String elementCompString) {
		// this version of the method takes a SINGLE String (elementCompString) to specify elemental composition
		// elementCompString can either list a set of integers or a set of symbols and numbers

		String[] eSymbols = toi.getElementSymbols();
		// eSymbols is a complete list of the element symbols in the TableOfIsotopes toi
		
		boolean noSymbols = true;
		
		String[] splitString = elementCompString.split(" ");
		
		// test whether elementCompString contains both symbols and integers or just integers
		for (int i = 0; i < eSymbols.length; i++) 
			if (elementCompString.indexOf(eSymbols[i]) > -1) 
				// elementCompString contains element symbols
				noSymbols = false;
		
		if (noSymbols) {
			try {
				//  the String elementCompString just contains a series of integers, 
				//    each mapped to an Element in toi.eList

				//  Convert "elementCompString" into two arrays (String[] elementSymbol and int[] fullStoichiometry)
				int[] fullStoichiometry = new int[splitString.length];
				for (int j = 0; j < splitString.length; j++) { 
					// j is index for tokens in the String, must map to index of corresponding element in toi.eList
					fullStoichiometry[j] = Integer.parseInt(splitString[j]);
				}
				set(toi, fullStoichiometry);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else {	// the elementComString contains both element symbols and numbers 
			try {
				this.coefficient = new int[splitString.length/2];
				this.eList = new Vector<TableOfIsotopes.Element>(splitString.length/2);
				int i = 0;
				while (i < coefficient.length) {
					this.eList.add(toi.getElement(splitString[2*i]));
					this.coefficient[i] = Integer.parseInt(splitString[2*i+1]);
					i++;
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

		}
	}  // end set method
	
	
	
	/**
	 * sets the eList and coefficient fields of the ElementComposition using  
	 * stoichiometryString, which is an array of Strings, each of which specify
	 * an element (or pseudoelement) and the corresponding stoichiometric coefficient, separated by a 
	 * space.
	 * Use of this method does <b>not</b> require any knowledge of the order
	 * of Elements in the TableOfIsotope's eList.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param stoichiometryString an array of Strings, each containing a symbol and the corresponding
	 * stoichiometric coefficient separated by a space.  For example, {"H 12", "C 6", "O 6"}.
	 */
	public void set(TableOfIsotopes toi, String[] stoichiometryString)  {
		  
		  this.eList = new Vector<TableOfIsotopes.Element>(stoichiometryString.length);
		  this.coefficient = new int[stoichiometryString.length];
		  for (int i = 0; i < stoichiometryString.length; i++) {
			  String[] elementStoichiometry = stoichiometryString[i].split(" ");
			  this.eList.add(toi.getElement(elementStoichiometry[0]));
			  this.coefficient[i] = Integer.parseInt(elementStoichiometry[1]);
		  }
	}  // end set method
	
	
	
	/**
	 * sets the eList and coefficient fields of the ElementComposition using 
	 * two arguments: (1) <b>elementSymbol</b>, which is an array of Strings, each of which specify
	 * an element (or pseudoelement); (2) <b>coefficient</b> which is an array of integers, which specify
	 * the corresponding stoichiometric coefficients.
	 * Use of this method does <b>not</b> require any knowledge of the order
	 * of Elements in the TableOfIsotope's eList.
	 * @param toi the TableOfIsotopes used to define the components of the ElementComposition
	 * @param elementSymbol an array of Strings, each containing a symbol.  For example, {"H", "C", "O"}.
	 * @param coefficient an array of integers that specify the stoichiometric coefficients corresponding
	 * in order to the array elementSymbol.  For example, {12, 6, 6}.
	 */
	public void set(TableOfIsotopes toi, String[] elementSymbol, int[] coefficient) {
		// this version of the method takes an ARRAY of String[] to specify elements 
		//    and an ARRAY of int[] to specify coefficient
		this.eList = new Vector<TableOfIsotopes.Element>(elementSymbol.length);
		// the local eList contains only those elements that are in the molecule
		for (int i = 0; i < elementSymbol.length; i++) {
			this.eList.add(toi.getElement(elementSymbol[i]));
		} 
		this.coefficient = coefficient;
	}  // end set method
	
	
	
	/**
	 * adds atoms in another ElementComposition instance to this ElementComposition instance.  
	 * @param ec another ElementComposition instance whose atoms are to be added. 
	 * The number of atoms of each element in ec is added
	 * to this instance of ElementComposition.  For example,
	 * <xmp>originalEC.addAtoms(ec);</xmp>
	 * adds all of the atoms of ec to originalEC.
	 */
	public void addCompositionAtoms(ElementComposition ec) {
		 
		int[] tempCoefficient = new int[100];
		Vector<TableOfIsotopes.Element> tempEList = new Vector<TableOfIsotopes.Element>();
		
		// add elements from ec and their coefficients to temp variables
		for (int i = 0; i < ec.coefficient.length; i++) {
			
			int match = this.eList.indexOf(ec.eList.get(i));
			if (match > -1) {
				// this.eList already includes ec.EList.get(i)
				//  so add both ec.coefficient and this.coefficient to tempCoefficient for ec.EList.get(i)
				tempCoefficient[i] = this.coefficient[match] + ec.coefficient[i];
			} else {
				// this.eList does not yet include ec.EList.get(i)
				//   so add coefficient from ec to tempCoefficient for that element
				tempCoefficient[i] = ec.coefficient[i];
			}
			// add the element from ec.eList to tempEList
			tempEList.add(ec.eList.get(i));
			// System.out.println(i + " @@ " + tempEList.size());
		}
		
		// set the variable 'count' to the number of elements already in tempCoefficient
		int count = ec.coefficient.length;
		System.out.println("adding elements: size of this.eList is " + this.eList.size() + " size of added list is " + count);
		//  add the elements that are in this.eList but not yet in tempEList because they are not in ec
		for (int j = 0; j < this.eList.size(); j++) {
			int match = tempEList.indexOf(this.eList.get(j));
			if (match == -1) {
				// this.EList.get(j) is not in tempEList, so add it
				tempEList.add(this.eList.get(j));
				//  set tempCoefficient for this element and increment count
				tempCoefficient[count++] = this.coefficient[j];
				// System.out.println(count + " ## " + tempEList.size());
			}
		}
		// System.out.println(count + " $$ " + tempEList.size());
		
		this.coefficient = new int[count];
		for (int i = 0; i < count; i++) this.coefficient[i] = tempCoefficient[i];
		this.eList = tempEList;
	}  // end addCompositionAtoms method

	
	
	/**
	 * adds a specified number of atoms specified by an id to this ElementComposition instance.  
	 * @param id the (String) identifier of the atoms to be added (e.g., "C")
	 * @param n the number of atoms identified by id to be added
	 * @return the final number of atoms identified by id in this ElementComposition instance
	 */
	public int addAtoms(String id, int n) {

		TableOfIsotopes.Element theElement = toi.getElement(id);
		int s = 0;
			
		int match = this.eList.indexOf(theElement);
		if (match > -1) {
			// this.eList already includes theElement
			//  so add both n and this.coefficient to tempCoefficient for ec.EList.get(i) 
			this.coefficient[match] += n;
			s = this.coefficient[match];
		} else {
			// this.eList does not yet include theElement
			//  so add theElement to the list and set its coefficient
			this.eList.add(theElement);
			int[] tempCoefficient = new int[this.coefficient.length + 1];
			for (int i = 0; i < this.coefficient.length; i++) {
				tempCoefficient[i] = this.coefficient[i];
			}
			tempCoefficient[this.coefficient.length] = n;
			this.coefficient = tempCoefficient;
			s = n;
		}
		
		return s;
	}  // end addAtoms method

	
	
	/**
	 * removes all atoms contained in another instance of ElementComposition from this ElementComposition instance.  
	 * @param ec another ElementComposition instance whose atoms are to be removed. 
	 * The number of atoms of each element in ec is removed
	 * from the calling instance of ElementComposition.  For example,
	 * <xmp>originalEC.removeAtoms(ec);</xmp>
	 * removes all of the atoms of ec from originalEC.  The resulting number
	 * of atoms of any element is NEVER set to a negative number.  Atoms that are in 
	 * ec but not in originalEC are ignored. 
	 * To make a composition with some negative coefficients, use addCompositionAtoms
	 */
	public void removeCompositionAtoms(ElementComposition ec) {
		for (int i = 0; i < ec.coefficient.length; i++) {
			int match = this.eList.indexOf(ec.eList.get(i));
			if (match > -1) {
				// remove atoms but do not set coefficient[match] to less than zero
				this.coefficient[match] = Math.max(0, this.coefficient[match] - ec.coefficient[i]);
				// @@@
				// this.coefficient[match] = this.coefficient[match] - ec.coefficient[i];
			} 
		}
	}  // end removeCompositionAtoms method
	
	
	
	/**
	 * removes a specified number of atoms specified by an id from this ElementComposition instance.  
	 * The resulting number of atoms of any element is never set to a negative number. 
	 * @param id the (String) identifier of the atoms to be removed (e.g., "C")
	 * @param n the number of atoms identified by id to be removed
	 * @return the number of atoms associated with id that would remain after atom removal.
	 * If this number is less than zero, the number of atoms of that element is set to zero.
	 */
	public int removeAtoms(String id, int n) {
		
		TableOfIsotopes.Element theElement = toi.getElement(id);
		int match = this.eList.indexOf(theElement);
		int r = 0;
		if (match > -1) {
			r = this.coefficient[match] - n;
			this.coefficient[match] = Math.max(0, r);
		}
		
		return r;
	}  // end removeAtoms method
	  
	
	/**
	 * fetches a String serializing this <b>ElementComposition</b>.
	 * @return a string in the form "C 6 H 12 O 6"
	 */
	public String getElementalCompositionString() {
		String elementalCompositionString = new String(" ");
		for (int i = 0; i < this.coefficient.length; i++) {
			elementalCompositionString = elementalCompositionString.concat(" " + this.eList.get(i).getSymbol() + " ");
			elementalCompositionString = elementalCompositionString.concat(String.valueOf(this.coefficient[i]));			
		}
		elementalCompositionString = elementalCompositionString.trim();
		return elementalCompositionString;
	}  // end method getElementalCompositionString
	
	
	/**
	 * generates a new ElementComposition instance that has the same composition as this
	 * instance of ElementComposition, but using a specified TableOfIsotopes to define
	 * the new ElementComposition.  For example, 
	 * <xmp>ElementComposition ec2 = ec.copy(toi2)</xmp>
	 * creates a new ElementComposition called ec2 that has the same composition
	 * as ec.  However, ec2 is based explicitly on Elements defined in toi2, which
	 * may be a different TableOfIsotopes than that used to define ec. The method
	 * <b>getElementalCompositionString</b> is used to serialize this ElementComposition so that it can be reconstructed
	 * using a different TableOfIsotopes.
	 * @param toi the TableOfIsotopes used to define the components of the new ElementComposition
	 * @return the new ElementComposition
	 */
	public ElementComposition copy(TableOfIsotopes toi) {
		ElementComposition newEC = new ElementComposition(toi, getElementalCompositionString());
		return newEC;
	}  // end method copy

	
	/**
	 * generates a new ElementComposition instance that has the same composition as this
	 * instance of ElementComposition, using the same TableOfIsotopes to define
	 * the new ElementComposition.  For example, 
	 * <xmp>ElementComposition ec2 = ec.copy()</xmp>
	 * creates a new ElementComposition called ec2 that has the same composition
	 * as ec.  The new instance ec2 is based on Elements defined in the TableOfIsotopes that was
	 * originally used to define the elements of ec.
	 * @return the new ElementComposition
	 */	
	public ElementComposition copy() {
		ElementComposition newEC = new ElementComposition(this.toi, getElementalCompositionString());
		return newEC;
	}  // end method copy
	
	
	
	/**
	 * fetches the monoisotopic mass that corresponds to the ElementComposition
	 * @return the monoisotopic mass
	 */
	public double getMonoisotopicMass() {
		double miMass = 0;
		for (int i = 0; i < this.coefficient.length; i++) {
			//@@@ error introduced here when scars included - negative coefficients for elements
			System.out.println("i is " + i + "   length of coefficient array is " + coefficient.length + "   size of eList is " +  eList.size() + "  mass is " + miMass);
			miMass += eList.get(i).getMIMass() * coefficient[i];
		}
		return miMass;
	}  // end method getMonoisotopicMass
	
	
	
	/**
	 * fetches the average (chemical) mass that corresponds to the ElementComposition
	 * @return the average (chemical) mass
	 */
	public double getAverageMass() {
		double avgMass = 0;
		for (int i = 0; i < this.coefficient.length; i++) {
			avgMass += eList.get(i).getAvgMass() * coefficient[i];
		}
		return avgMass;
	}  // end method getAverageMass

}
