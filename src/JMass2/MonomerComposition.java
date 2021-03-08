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

import JMass2.MonomerTable.MonomerType;

/**
 * The MonomerComposition class defines a list of <b>MonomerType</b> instances and n ordered array containing
 * a stoichiometric coefficients for each <b>MonomerType</b> instance.  This class is effectively used in combination
 * with an instance the <b>MassSearcher</b> class.
 *
 */
public class MonomerComposition {
	
	/**
	 * an instance of MonomerTable, which defines all the MonomerType instances listed in the MonomerComposition
	 */
	MonomerTable mt;
	
	/**
	 * a list of MonomerType instances contained in the MonomerComposition
	 */
	Vector<MonomerType> typeList = new Vector<MonomerType>();
	
	/**
	 * an ordered array of stoichiometric coefficients whose sequence matches that of typeList
	 */
	int[] coefficient = new int[0];
	
	
	/**
	 * constructs an empty MonomerComposition based on the MonomerTable mt.
	 * @param mt an instance of MonomerTable that defines the monomers in the MonomerComposition.
	 */
	MonomerComposition(MonomerTable mt) {
		this.mt = mt;
	} // end MonomerComposition constructor

	
	/**
	 * constructs a MonomerComposition based on the MonomerTable mt, built using a list of MonomerType instances
	 * and a corresponding array of stoichiometric coefficients.
	 * @param mt an instance of MonomerTable that defines the monomers in the MonomerComposition
	 * @param typeArray an ordered array of MonomerType instances to be included in the MonomerComposition
	 * @param coefficient an ordered array of stoichiometric coefficients corresponding to the MonomerType instances
	 * in the MonomerComposition
	 */
	MonomerComposition(MonomerTable mt, MonomerTable.MonomerType[] typeArray, int[] coefficient) {
		this.mt = mt;
		this.typeList = new Vector<MonomerTable.MonomerType>(typeArray.length);
		this.coefficient = new int[coefficient.length];
		for (int i = 0; i < typeArray.length; i++) {
			this.typeList.add(mt.getMonomerType(typeArray[i].abbrev));
			this.coefficient[i] = coefficient[i];
		}
		
	}  // end MonomerComposition constructor
	
	
	
	/**
	 * constructs a MonomerComposition based on the MonomerTable mt, built using the information in a String
	 * alternately listing the id of each component of the new MonomerType instance
	 * and the corresponding stoichiometric coefficient.
	 * @param mt an instance of MonomerTable that defines the monomers in the MonomerComposition
	 * @param monomerCompositionString a String alternately listing the id of each component of the new MonomerType
	 * instance and the corresponding stoichiometric coefficient.  For example, the String
	 * "Pent 3 Hex 4" specifies a MonomerComposition consisting of 3 Pent MomnomerTypes and 4 Hex MonomerTypes.
	 */
	MonomerComposition(MonomerTable mt, String monomerCompositionString) {
		this.mt = mt;
		
		String[] splitString = monomerCompositionString.split(" ");
		
		this.coefficient = new int[splitString.length/2];
		this.typeList = new Vector<MonomerTable.MonomerType>(splitString.length/2);
		
		for (int i = 0; i < coefficient.length; i++) {
			this.typeList.add(mt.getMonomerType(splitString[2*i]));
			this.coefficient[i] = Integer.parseInt(splitString[2*i+1]);
		}			

	}  // end MonomerComposition constructor

	
	/**
	 * adds a specified number of specified monomers to the MonomerComposition
	 * @param monomer an instance of the MonomerType to be added
	 * @param numberAdded the number of monomers to be added
	 */
	public void addResidues(MonomerType monomer, int numberAdded) {
		if (!typeList.contains(monomer)) {  // typeList does NOT contain ms
			typeList.add(monomer);
			// resize coefficient array
			int[] tempCoefficient = new int[this.coefficient.length+1];
			for (int i = 0; i < this.coefficient.length; i++) tempCoefficient[i] = this.coefficient[i];
			tempCoefficient[this.coefficient.length] = numberAdded;
			this.coefficient = tempCoefficient;
			return;
		}
		
		// typeList DOES contain ms
		int i = typeList.indexOf(monomer);
		coefficient[i] += numberAdded;
		return;
	} // end addResidues method
	
	
	
	/**
	 * creates a copy of this MonomerComposition.  This MonomerComposition is serialized as a 
	 * String and used to generate a new instance of MonomerComposition.  This creates entirely new 
	 * copies of the typeList and coefficient array of this MonomerComposition, which are used to generate the new instance
	 * of MonomerComposition.  These objects can be modified without affecting this MonomerComposition.
	 * This differs from the <b>copyFull</b> method in that here, only Monomers in this MonomerComposition 
	 * with stoichiometric coefficients greater than zero are copied.  This is useful when the stoichometric
	 * coefficients of the copy are to be manipulated, but the identities of the constituent Monomers
	 * are to remain static.
	 * @return an independent copy of this MonomerComposition instance
	 */
	public MonomerComposition copyBrief() {
		
		String compositionString = this.getBriefMonomerCompositionString();
		
		MonomerComposition mcCopy = new MonomerComposition(this.mt, compositionString);

		return mcCopy;
	} // end method copy

	
	
	/**
	 * creates a copy of this MonomerComposition.  This MonomerComposition is serialized as a 
	 * String and used to generate a new instance of MonomerComposition.  This creates entirely new 
	 * copies of the typeList and coefficient array of this MonomerComposition, which are used to generate the new instance
	 * of MonomerComposition.  These objects can be modified without affecting this MonomerComposition.
	 * This differs from the <b>copy</b> method in that all Monomers in this MonomerComposition are
	 * included, even if their stoichiometric coefficients are zero.  This is useful when the stoichometric
	 * coefficients of the copy are to be manipulated, but the identities of the constituent Monomers
	 * are to remain static.
	 * @return an independent copy of this MonomerComposition instance
	 */
	public MonomerComposition copyFull() {
		
		String compositionString = this.getFullMonomerCompositionString();
		
		MonomerComposition mcCopy = new MonomerComposition(this.mt, compositionString);

		return mcCopy;
	} // end method copy

      
	/**
	 * generates a String representation of this MonomerComposition.  This string can be used to generate a new
	 * independent copy of the MonomerComposition that can be manipulated. As the string includes all of the MonomerTypes in
	 * this MonomerComposition, even if their stoichiometric coefficients are zero, the copy can be used
	 * to generate a template in which all relevant MonomerTypes are maintained for subsequent manipulation.
	 * @return a human-readable String describing this MonomerComposition, including MonomerTypes whose stoichiometric 
	 * coefficients are zero
	 */
    public String getFullMonomerCompositionString() {
    	
    	String compositionString = new String();

        for (int i = 0; i < this.coefficient.length; i++) {  // for each monomerType
        	compositionString = compositionString.concat(" " + this.typeList.get(i).abbrev + " " + this.coefficient[i]);
        }
        return compositionString.trim();
    } // end method - getFullMonomerCompositionString

    
    
	/**
	 * generates a String serialization of this MonomerComposition.  This string can be used to generate a new
	 * independent copy of the MonomerComposition that can be manipulated. However, the string includes only the MonomerTypes in
	 * this MonomerComposition whose stoichiometric coefficients are greater than zero.  Therefore, the copy may not be appropriate
	 * as a template in which all relevant MonomerTypes are maintained for subsequent manipulation.
	 * @return a concise human-readable String describing this MonomerComposition
	 */
    public String getBriefMonomerCompositionString() {
    	
    	String compositionString = new String();

        for (int i = 0; i < this.coefficient.length; i++) {  // for each monomerType
        	if (this.coefficient[i] > 0)
        		compositionString = compositionString.concat(" " + this.typeList.get(i).abbrev + " " + this.coefficient[i]);
        }
        return compositionString.trim();
    } // end method - getMonomerCompositionString

	
}
