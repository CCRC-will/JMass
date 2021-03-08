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


import java.text.DecimalFormat;
import java.io.IOException;
// import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;
// import java.net.URL;
// import java.net.URLDecoder;
import org.w3c.dom.Document;

import java.util.Vector;


/**
 * The TableOfIsotopes class implements a data repository for chemical elements, their isotopes,
 *  isotope masses and isotope abundances.
 * <P>
 * TableOfIsotopes supports the concept of a "pseudoelement", which corresponds to an 
 * element with non-natural isotopic abundances.  A pseudoelement embodies the same isotopes as the corresponding natural
 * abundance element, but in different proportions.  This facilitates the calculation of isotopolog
 * distributions for a molecule in which some of the atoms have been istopically enriched.
 * Such molecules are defined by specifying their composition (elements and pseudoelements).  
 * For example, 98%-enriched [U-<sup>13</sup>C<sub>6</sub>]-glucose can be modeled by defining the pseudoelement "*C",
 * which is composed of 98% <sup>13</sup>C and 2% <sup>12</sup>C.
 * (By convention, pseudoelements are specified using the elemental symbol of the real element
 * upon which the pseudoelement is modeled, with an asterisk to the left as in "*C".
 * It is important to note that the pseudoelement *C is <b>not</b> the same as the isotope <sup>13</sup>C.
 * 
 */
public class TableOfIsotopes {

	/**
	 * an instance of the java Document class that contains information about the elements and pseudoelements, 
	 * including their names, abbreviations, and the masses and abundances of their isotopic forms.
	 */
	static Document configuration;
	

	/**
	 * an array of instances of the inner class Element
	 */
	Vector<Element> eList;
	
	// generator for TableOfIsotopes
	/**
	 * constructs a new instance of TableOfIsotopes.  This is composed of a list of instances of the 
	 * inner class <b>Element</b>, which includes information regarding the element name and symbol,
	 * along with the precise masses and abundances of the isotopes of the element.
	 * @param dataFileName a string specifying the name of a properly formatted XML file that contains all
	 * of the information required to build the Table of Isotopes.  
	 */
	public TableOfIsotopes (String dataFileName) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);

		//factory.setValidating(true);   
		//factory.setNamespaceAware(true);

		try {

			DocumentBuilder builder = factory.newDocumentBuilder();
			configuration = builder.parse(new File(dataFileName));

			org.w3c.dom.NodeList elements = configuration.getElementsByTagName("chemical_element");

			eList = new Vector<TableOfIsotopes.Element>(elements.getLength());


			// System.out.println("Maximum number of isotopes is "+ maxIso);
			double[] a;
			int b;
			double[] m;
			String[] isotope;
			String eName;
			String symbol;
			int atNo;

			for (int i = 0; i < elements.getLength(); i++) { // for each element in XML file
				org.w3c.dom.NodeList isotopes = elements.item(i).getChildNodes();
				// System.out.println(isos);
				// System.out.println("Number of isotopes in element " + i + " is " + isos.getLength());

				isotope = new String[isotopes.getLength()];
				m = new double[isotopes.getLength()];
				a = new double[isotopes.getLength()];
				// System.out.println("\nchemical element:");
				org.w3c.dom.NamedNodeMap elemAtts = elements.item(i).getAttributes();
				atNo =  Integer.parseInt(elemAtts.getNamedItem("atomic_number").getNodeValue());
				eName = elemAtts.getNamedItem("name").getNodeValue();
				symbol= elemAtts.getNamedItem("id").getNodeValue();
				b = Integer.parseInt(elemAtts.getNamedItem("abundant_isotopes").getNodeValue());
				boolean isPseudo = Boolean.parseBoolean(elemAtts.getNamedItem("pseudo").getNodeValue());
				
				for (int j = 0; j < isotopes.getLength(); j++) { // for each isotope
					org.w3c.dom.NamedNodeMap isoAtts = isotopes.item(j).getAttributes();
					isotope[j] = isoAtts.getNamedItem("symbol").getNodeValue();
					m[j] = Double.parseDouble(isoAtts.getNamedItem("mass").getNodeValue());
					a[j] = Double.parseDouble(isoAtts.getNamedItem("abundance").getNodeValue());
				}
				
				// CREATE AN NEW INSTANCE OF Element ...........................
				eList.add(new Element(a, b, m, isotope, eName, symbol, atNo, isPseudo));
			}

		} catch (SAXParseException spe) {
			// Error generated by the parser
			System.out.println("\n** Parsing error" + ", line " +
					spe.getLineNumber() + ", uri " + spe.getSystemId());
			System.out.println("   " + spe.getMessage());

			// Use the contained exception, if any
			Exception x = spe;

			if (spe.getException() != null) {
				x = spe.getException();
			}

			x.printStackTrace();
		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;

			if (sxe.getException() != null) {
				x = sxe.getException();
			}

			x.printStackTrace();
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}

	} // generator for TableOfIsotopes


	/**
	 * adds an instance of Element to the Element list of the Table of Isotopes.
	 * This may be most useful for defining new pseudoelements.  It is used internally
	 * when the TableOfIsotpes is built using information from the configuration file.
	 * @param a an array of isotope abundances, sorted from most abundant to least abundant.  
	 * For pseudoelements, this may not correspond to the natural abundance order.
	 * @param b the number of significantly populated isotopes of the element
	 * @param m an array of isotope masses, in the same order as the abundance array
	 * @param isotope an array of isotope names (e.g., 13C) in the same order as the abundance array
	 * @param eName the name of the element (e.g., "carbon")
	 * @param symbol the symbol of the element or pseudoelement (e.g., "C" or "*C")
	 * @param atNo the atomic number of the element or pseudoelement (e.g., 6 for "C" or "*C")
	 */
	public void addElement(double[] a, int b, double[] m, String[] isotope, String eName, String symbol, int atNo, boolean isPseudo, String html) {
		eList.add(new Element(a, b, m, isotope, eName, symbol, atNo, isPseudo));
	}

	/**
	 * fetch an ordered array of Strings holding the names of the elements (e.g., "carbon") in the TableOfIsotope's list of Elements
	 * @return the array of element names sorted from most abundant to least abundant.
	 */
	
	public String[] getElementNames() {
		String[] eNames = new String[eList.size()];
		int i = 0;
		for(Element anElement : eList) { 
			eNames[i++] = anElement.eName;
		}
		return eNames;
	}
	
	/**
	 * fetch an ordered array of Strings holding the symbols for the elements (e.g., "C") in the TableOfIsotope's list of Elements
	 * @return the array of element names sorted from most abundant to least abundant.
	 */
	public String[] getElementSymbols() {
		String[] eSymbols = new String[eList.size()];
		int i = 0;
		for(Element anElement : eList) { 
			eSymbols[i++] = anElement.symbol;
		}
		return eSymbols;
	}

	/**
	 * formats a String, returning a String of length len, padded with spaces.  This is used internally to 
	 * generate pretty Tables.
	 * @param inStr
	 * @param len
	 * @param justify specifies the justification: when (justify == 0), the string is left justified by adding
	 * white space to the end; otherwise, it is right justified by adding white space to the beginning
	 * @return the padded String
	 */
	private String strPad(String inStr, int len, int justify) {
		String outStr = new String("");
		if (justify == 0) {
			outStr = outStr.concat(inStr);
			for (int k = inStr.length(); k < len; k++) {
				outStr = outStr.concat(" ");
			}
			return outStr;
		}
		for (int k = 0; k < len - inStr.length(); k++) {
			outStr = outStr.concat(" ");
		}
		outStr = outStr.concat(inStr);
		return outStr;
	}  // end of method strPad


	/**
	 * generates a String containing the TableOfIsotopes data in a pretty format.  
	 * This may be useful for checking
	 * the content and integrity of the configuration (XML) file.
	 * @return a String representation of the TableOfIsotopes in ascii format
	 */
	public String prettyTable() {

		DecimalFormat f = new DecimalFormat("0.000 ");
		//DecimalFormat format2 = new DecimalFormat("0.000 ");

		StringBuffer tableBuffer = new StringBuffer("Table of the Isotopes\n\n");
		tableBuffer.append("Index AtNo  Element      ID ");
		for (int j = 0; j < 3; j++) {
			tableBuffer.append("  |   Iso    Mass    Abund");
		}
		tableBuffer.append("  | b\n\n");

		// iterate through eList
		int i = 0;
		for(Element anElement : eList) { // print the table of isotopes
			tableBuffer.append(new String(strPad(String.valueOf(i), 2, 1)) + "    ");
			tableBuffer.append(new String(strPad(String.valueOf(anElement.atNo), 2, 1)) + "     ");
			tableBuffer.append(new String(strPad(anElement.eName, 12, 0)));
			tableBuffer.append(new String(strPad(anElement.symbol, 4, 0)) + " | ");
			for (int j = 0; j < 3; j++) {
				tableBuffer.append(new String(strPad(anElement.isotope[j], 5, 1)));
				String massOut = f.format(anElement.m[j]);
				String abundOut = f.format(anElement.a[j]);
				tableBuffer.append(new String(strPad(massOut, 10, 1)) + new String(strPad(abundOut, 8, 1)));
				tableBuffer.append(" | ");
			}
			tableBuffer.append(anElement.b + "\n");
			i++;
		}
		return new String(tableBuffer);
		
	} // end of method prettyTable

	
	/**
	 * Generates a String containing the TableOfIsotopes data in a tab-separated format.  
	 * This String can be used
	 * to save the Table data to process it by other methods (e.g., in a spread sheet).
	 * @return a String representation of the TableOfIsotopes in tab-separated format
	 */
	public String exportTable() {

		DecimalFormat f = new DecimalFormat("0.000000000 ");
		StringBuffer tableBuffer = new StringBuffer("Atomic-Number\tElement\tID\tb");
		for (int j = 0; j < 3; j++) {
			tableBuffer.append("\tIsotope\tMass\tAbundance");
		}

		for(Element anElement : eList) { 
			// print the table of isotopes
			tableBuffer.append("\n" + anElement.atNo);
			tableBuffer.append("\t" + anElement.eName);
			tableBuffer.append("\t" + anElement.symbol);
			tableBuffer.append("\t" + anElement.b);
			for (int j = 0; j < 3; j++) {
				tableBuffer.append("\t" + anElement.isotope[j]);
				tableBuffer.append("\t" + f.format(anElement.m[j]) + 
						"\t" + f.format(anElement.a[j]));
			}

		}

		return new String(tableBuffer);
	}  // exportTable
	
	
	/**
	 * Generates a String representation of the TableOfIsotopes data in XML format.  
	 * This String can be used
	 * to generate a new TableOfIsotopes configuration file.
	 * @return a String representation of the TableOfIsotopes in XML format
	 */
	public String exportXML() {

		DecimalFormat f = new DecimalFormat("0.000000");
		
		StringBuffer xmlBuffer = new StringBuffer("<?xml version='1.0' encoding='utf-8'?>\n\n");
		xmlBuffer.append("<!DOCTYPE table_of_isotopes SYSTEM \"table_of_isotopes.dtd\">\n\n");
		xmlBuffer.append("<table_of_isotopes>\n");
		
		for(Element anElement : eList) { 
			// print the table of isotopes
			xmlBuffer.append("  <chemical_element atomic_number='" + anElement.atNo + 
					"' name='" + anElement.eName +
					"' id='" + anElement.symbol + 
					"' abundant_isotopes='" + anElement.b + 
					"' pseudo='" + anElement.isPseudo + "'>\n");
			
			for (int j = 0; j < 3; j++) {
				xmlBuffer.append("    <isotope symbol='" + anElement.isotope[j] +
						"' mass='" + anElement.m[j] + 
						"' abundance='" + f.format(anElement.a[j]) + "'>\n");;
			}
		}
		xmlBuffer.append("</table_of_isotopes>\n");

		return new String(xmlBuffer);
	}  // end method exportXML
	
	
	
	/**
	 * gets an instance of the <b>Element</b> class using its symbol as the identifier.
	 * @param id the symbol of the <b>Element</b> instance.  This can be a conventional symbol (e.g., "C")
	 * for an element or a non-conventional symbol (e.g., "*C") for a pseudoelement.
	 * @return the instance of the <b>Element</b>
	 */
	public Element getElement(String id) {
		// 
		Element defaultE = eList.get(0);
		for(Element anElement : eList)  
			if (anElement.symbol.equals(id)) 
				return anElement;

		return defaultE;
	} // getElement
	
	/**
	 * fetches an array of the Element instances in the TableOfIsotopes.
	 * @param pseudoOnly if true, only pseudo elements are retrieved.
	 * @return the filtered array of Element intances
	 */
	public Element[] getElements(boolean pseudoOnly) {
		if (pseudoOnly) {
			Vector<Element> pseudoList = new Vector<Element>();
			for (int i = 0; i < eList.size(); i++) 
				if (eList.get(i).isPseudo) 
					pseudoList.add(eList.get(i));
			Element[] x = new Element[pseudoList.size()];
			return pseudoList.toArray(x);
		} else {
			Element[] x = new Element[eList.size()];
			return eList.toArray(x);
		}
	}
	
	
	/**
	 * sets the abundances of the isotopes of an element or pseudoelement.  Usually, this should be used only
	 * for pseudoelements, as the natural abundances of isotopes of normal elements should be specified
	 * in the configuration file.  The order of abundances must match the order of isotopes that has already
	 * been established in the initial definition of the pseudoelement.  If this cannot lead to the desired
	 * result, a new pseudoelement can be generated using the <b>addElement</b> method.
	 * @param id the id of the Element (e.g.,. "*C") to be modified 
	 * @param abundance an ordered array of isotope abundances, the components of which must add up to 1.0
	 * @param b the number isotopes of the Element that have significant abundance
	 */
	public void setElementAbundances(String id, double[] abundance, int b) {
		// for enriched elements, assume there are exactly 2 significantly abundant isotopes
		// enrichment specifies the unnatural abundance of the isotope that has a low natural abundance

		Element theElement = getElement(id);
		// for enriched elements, assume there are only 2 isotopes
		//  could make this more general, but it would make it more complicated
		theElement.b = b;
		for (int i = 0; i < abundance.length; i++) {
			theElement.a[i] = abundance[i];
		}
	} // setElementAbundances
	
	
	
	/**
	 * 
	 * An inner class that is contained in the class <b>TableOfIsotopes</b>,  specifying the name, symbol
	 * and isotopic composition of an element that is listed in the TableOfIsotopes.
	 *
	 */
	public class Element {
		
		/**
		 * an flag specifying whether the element is a pseudoelement or a natural element
		 */
		private boolean isPseudo;
		/**
		 * an ordered array, sorted from most abundant to least abundant, specifying the abundance of each isotope of the Element.
		 */
		private double[] a;
		/**
		 * the number of significantly abundant isotopes for each element.  For example, natural abundance
		 * carbon has two significantly abundant isotopes (12C and 13C) along with one isotope (14C)
		 * whose abundance is usually below the detection limit of the mass spectrometer.  In this case,
		 * b = 2.
		 */
		private int b;
		/**
		 * an ordered array, sorted from most abundant to least abundant, specifying the precise mass of each isotope of the Element
		 */
		private double[] m;
		/**
		 * an ordered array, sorted from most abundant to least abundant, specifying the name of each isotope of the Element
		 */
		private String[] isotope;
		/**
		 * the name of the element itself. (e.g. "carbon")
		 */
		private String  eName;
		/**
		 * the symbol of the element. (e.g., "C"), which serves as a unique identifier for the element
		 */
		private String symbol;
		/**
		 * the atomic number of the element. (e.g., atNo = 6 for carbon).
		 */
		private int  atNo;
		
		
		/**
		 * constructs an instance of the class Element.
		 * @param a an ordered array, sorted from most abundant to least abundant, specifying the abundance of each isotope of the Element
		 * @param b the number of significantly abundant isotopes for each element.  For example, natural abundance
		 * carbon has two significantly abundant isotopes (12C and 13C) along with one isotope (14C)
		 * whose abundance is usually below the detection limit of the mass spectrometer.  In this case,
		 * b = 2.
		 * @param m an ordered array, sorted from most abundant to least abundant, specifying the precise mass of each isotope of the Element
		 * @param isotope an ordered array, sorted from most abundant to least abundant, specifying the name of each isotope of the Element
		 * @param eName the name of the element itself (e.g. "carbon")
		 * @param symbol the symbol of the element (e.g., "C"), which serves as a unique identifier for the element
		 * @param atNo the atomic number of the element (e.g., atNo = 6 for carbon)
		 */
		public Element (double[] a, int b, double[] m, String[] isotope, String eName, String symbol, int atNo, boolean isPseudo) {
			this.a = a;
			this.b = b;
			this.m = m;
			this.isotope = isotope;
			this.eName = eName;
			this.symbol = symbol;
			this.atNo = atNo;
			this.isPseudo = isPseudo;
		} // end Element constructor
		
		
		/**
		 * returns the mass of an isotope of the element, identified by its rank in the arrays
		 * that define the properties of the isotopes.  The rank of the most abundant isotope is 0.
		 * @param rank the position of the isotope in the arrays that define the properties of the isotopes
		 * @return the precise mass of the isotope
		 */
		public double getM(int rank) {
			return this.m[rank];
		}  // end method getM

		
		/**
		 * returns the abundance of an isotope of the element, identified by its rank in the arrays
		 * that define the properties of the isotopes.  The rank of the most abundant isotope is 0.
		 * @param rank the position of the isotope in the arrays that define the properties of the isotopes
		 * @return the normalized abundance of the isotope
		 */
		public double getA(int rank) {
			return this.a[rank];
		}  // end method getA

		
		/**
		 * fetches the number of significantly populated isotopes comprising the Element.
		 * @return the number of significantly populated isotopes 
		 */
		public int getB() {
			return this.b;
		} // end method getB

		
		/**
		 * fetches the name of the Element (e.g., "carbon").
		 * @return the name of the Element
		 */
		public String getElement() {
			return this.eName;
		} // end method getElement
		

		/**
		 * fetches the symbol of the Element (e.g., "C").  Some methods use this symbol to identify 
		 * an Element in the list of Elements in the TableOfIsotopes.
		 * @return  the symbol of the element.
		 */
		public String getSymbol() {
			return this.symbol;
		} // end method getSymbol
		
		
		/**
		 * fetches the name of the isotope of the element based on its rank in the ordered arrays,
		 * sorted from most abundant to least abundant,
		 * specifying the isotope properties.
		 * @param rank the position of the isotope in the arrays that define the properties of the isotopes
		 * @return the name of the selected isotope
		 */
		public String getIsotope(int rank) {
			return this.isotope[rank];
		} // end method getIsotope

		
		/**
		 * fetches the average mass of the element, computed as a weighted average of isotope masses.
		 * @return the average mass of the element
		 */
		public double getAvgMass() {
			double avgMass = 0;
			for (int rank = 0; rank < b; rank++) avgMass += m[rank] * a[rank];
			return avgMass;
		}  // end method getAvgMass
		
		/**
		 * fetches the monoisotopic mass of the element, which is the mass of the most abundant isotope.
		 * @return the monoisotopic mass of the element
		 */
		public double getMIMass() {
			double miMass = m[0];
			return miMass;
		} // end method getMIMass


	}

}