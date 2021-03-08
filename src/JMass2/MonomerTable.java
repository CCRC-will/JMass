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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * The MonomerTable class implements a data repository that specifies a collection
 * of monomer types that can be combined to represent a molecule composed of
 * monomer residues.  MonomerTable thus defines a list of instances of the 
 * inner class MonomerType, each of which which specifies a name and abbreviation 
 * along with an <i>incremenental</i> ElementComposition corresponding to the atoms that 
 * would be added to a hypothetical oligomer when one instance of the <b>underivatized</b>
 * MonomerType is added to the oligomer.  In addition, the number of 
 * derivitization sites added per instance of MonomerType is specified.  
 * A MonomerTable also specifies a collection of derivatives that can be used to
 * chemically modify the MonomerType(s).  Together, these objects provide the basis for
 * completely describing the composition of a native oligomer as an 
 * instance of the <b>MonomerComposition</b> class.  To fully described a derivatized
 * oligomer, it must be put in the context of the parameters specified in the MassSearcher
 * class, where methods to define use instances of <b>Derivative</b> are implemented.
 *
 */
public class MonomerTable {

	/**
	 * an instance of the java Document class that contains information about the monomers
	 * that are described in the MonomerTable
	 */
	static Document document;

	/**
	 * a list of instances of the inner class MonomerType that are described by this MonomerTable
	 */
	private Vector<MonomerType> typeList = new Vector<MonomerType>();
	
	/**
	 * a reusable instance of MonomerType
	 */
	private MonomerType mt;
	
	/**
	 * a list of instances of the inner class Derivative, described by the MonomerTable
	 */
	private Vector<Derivative> derList = new Vector<Derivative>();
	
	/**
	 * a reusable instance of Derivative
	 */
	private Derivative der;
	
	/**
	 * an instance of TableOfIsotopes used as a basis for specifying the chemical
	 * properties of Derivatives and MonomerTypes
	 */
	TableOfIsotopes toi;


	/**
	 * generates a new instance of MonomerTable, using a specified sources of chemical information.
	 * @param fileOrXML a String that either specifies an XML file containing the chemical
	 * descriptions of the instances of  MonomerType in the MonomerTable or the XML encoding of this
	 * information.  This constructor first attempts to find a file named by fileOrXML.  If the file is
	 * found, the constructor attempts to parse it.  If no such
	 * file exists, the constructor attempts to parse the fileOrXML string directly.
	 * @param toi an instance of the TableOfIsotopes class that is used to specify the
	 * chemical properties of the atoms that comprise instances of MonomerType
	 */
	public MonomerTable(String fileOrXML, TableOfIsotopes toi) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);

		// factory.setValidating(true);   
		// factory.setNamespaceAware(true);

		this.toi = toi;

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			File msFile = new File(fileOrXML);

			if(msFile.canRead()) {
				document = builder.parse(msFile);
			} else {
				document = builder.parse(new InputSource(new StringReader(fileOrXML)));
			}

			org.w3c.dom.NodeList monosaccharides = document.getElementsByTagName("monomer");
			int n = monosaccharides.getLength();
			for (int i = 0; i < n; i++) { // for each monomer
				org.w3c.dom.NamedNodeMap msAttributes = monosaccharides.item(i).getAttributes();
				String msName = msAttributes.getNamedItem("name").getNodeValue();
				String msAbbrev = msAttributes.getNamedItem("abbrev").getNodeValue();
				String eSites = msAttributes.getNamedItem("e_sites").getNodeValue();
				String aSites = msAttributes.getNamedItem("a_sites").getNodeValue();

				org.w3c.dom.NodeList elements = monosaccharides.item(i).getChildNodes();
				int[] coefficient = new int[elements.getLength()];
				Vector<TableOfIsotopes.Element> eList = new Vector<TableOfIsotopes.Element>(elements.getLength());
				for (int j = 0; j < coefficient.length; j++) { // for each element [j]
					org.w3c.dom.NamedNodeMap elementAtts = elements.item(j).getAttributes();
					String eSymbol = elementAtts.getNamedItem("symbol").getNodeValue();
					coefficient[j] = Integer.parseInt(elementAtts.getNamedItem("number").getNodeValue());

					if (coefficient[j] > 0) {  //  for each element
						eList.add(toi.getElement(eSymbol));
					}
				}
				ElementComposition ec = new ElementComposition(toi, eList, coefficient);


				//  instantiate a new MonomerType
				this.mt = new MonomerType(msName, msAbbrev, ec,
						Integer.parseInt(eSites),
						Integer.parseInt(aSites));
				//  add the MonomerType to the list
				this.typeList.add(mt);
			}

			// get the derivative definitions from the file

			org.w3c.dom.NodeList derivatives = document.getElementsByTagName("derivative");
			for (int i = 0; i < derivatives.getLength(); i++) { // for each derivative
				org.w3c.dom.NamedNodeMap subAttributes = derivatives.item(i).getAttributes();
				String subName = subAttributes.getNamedItem("name").getNodeValue();
				String subAbbrev = subAttributes.getNamedItem("abbrev").getNodeValue();
				String subType = subAttributes.getNamedItem("type").getNodeValue();

				org.w3c.dom.NodeList elements = derivatives.item(i).getChildNodes();
				int[] coefficient = new int[elements.getLength()];
				Vector<TableOfIsotopes.Element> eList = new Vector<TableOfIsotopes.Element>(elements.getLength()); 
				for (int j = 0; j < coefficient.length; j++) { // for each element [j]
					org.w3c.dom.NamedNodeMap elementAtts = elements.item(j).getAttributes();
					String eSymbol = elementAtts.getNamedItem("symbol").getNodeValue();
					coefficient[j] = Integer.parseInt(elementAtts.getNamedItem("number").getNodeValue());
					if (coefficient[j] > 0) {  //  for each element
						eList.add(toi.getElement(eSymbol));
					}
				}

				ElementComposition ec = new ElementComposition(toi, eList, coefficient);

				this.der = new Derivative(subName, subAbbrev, ec, subType);
				//  add the Derivative to the list
				this.derList.add(der);
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

	}  // end - MonomerTable constructor


	/**
	 * generates a String holding a tab-separated description of the MonomerType instances
	 * defined in the MonomerTable.  This String can be used for alternative processing or
	 * viewing by, e.g., copying it and pasting it into a spread sheet.
	 */
	public String exportTypes() {
		StringBuffer sb = new StringBuffer();
		sb.append("Defined " + typeList.size() + " monomer types\n\n");
		sb.append("Name\tAbbreviation\tMI delta\tAvg delta\tether sites\tacyl sites\tcomposition\n");
		for (int i = 0; i < typeList.size(); i++) {
			mt = (MonomerType)typeList.get(i);
			sb.append(mt.name + "\t" + mt.abbrev + "\t" +
					mt.monoisotopicIncrement + "\t" + mt.averageIncrement
					+ "\t" + mt.eSites + "\t" + mt.aSites  + "\t");
			for (int j = 0; j < mt.ec.coefficient.length; j++) {
				sb.append(mt.ec.eList.get(j).getSymbol() + " " + mt.ec.coefficient[j] + " ");
			}
			sb.append("\n");
		}
		sb.append("\nDefined " + derList.size() + " derivatives\n\n");
		sb.append("Name\tAbbreviation\tMI delta\tAvg delta\ttype\tcomposition\n");
		for (int i = 0; i < derList.size(); i++) {
			der = (Derivative)derList.get(i);
			sb.append(der.name + "\t" + der.abbrev + "\t" +
					der.monoisotopicIncrement + "\t" + der.averageIncrement
					+ "\t" + der.type
					+ "\t");
			for (int j = 0; j < der.ec.coefficient.length; j++) {
				sb.append(der.ec.eList.get(j).getSymbol() + " " + der.ec.coefficient[j] + " ");
			}
			sb.append("\n");

		}
		return new String(sb);
	}  // end method - printTypes


	/**
	 * generates a String holding an XML representation of the MonomerType instances
	 * defined in the MonomerTable.  The contents of this String can be saved in a file
	 * ar used directly to generate a new instance of MonomerTable.
	 */
	public String exportXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n\n" +
			"<!DOCTYPE residue_defs SYSTEM \"residue_defs.dtd\">\n\n" +
			"<residue_defs>\n\n" +
			"<!--\n" + 
			"  chemical compositions are for the anhydro-form (found in a polymer)\n" +
			"  e_sites are the number of etherification sites for each monomer in a polymer\n" +
			"  a_sites are the number of acylation sites for each monomer in a polymer\n" +
			"-->\n");
		for (int i = 0; i < typeList.size(); i++) {
			mt = (MonomerType)typeList.get(i);
			sb.append("  <monomer name='" + mt.name + "' abbrev='" + mt.abbrev + "' e_sites='" +
					mt.eSites + "' a_sites='" + mt.aSites  + "'>\n");
			for (int j = 0; j < mt.ec.coefficient.length; j++) {
				sb.append("    <component symbol='" + mt.ec.eList.get(j).getSymbol() + "' number='" + mt.ec.coefficient[j] + "'/>\n");
			}
			sb.append("  </monomer>\n\n");
		}
		
		for (int i = 0; i < derList.size(); i++) {
			der = (Derivative)derList.get(i);
			sb.append("  <derivative name='" + der.name + "' abbrev='" + der.abbrev + "' type='" + der.type + "'>\n");
			for (int j = 0; j < der.ec.coefficient.length; j++) {
				sb.append("    <component symbol='" + der.ec.eList.get(j).getSymbol() + 
					"' number='" + der.ec.coefficient[j] + "'/>\n");
			}
			sb.append("  </derivative>\n\n");
		}
		sb.append("</residue_defs>\n");
		return new String(sb);
	}  // end method - printTypes



	/**
	 * retrieves a MonomerType from the typeList using its abbreviation as an ID
	 * @param abbrev the abbreviation of the MonomerType
	 * @return the MonomerType
	 */
	public MonomerType getMonomerType(String abbrev) {
		  
		  MonomerType mType = null;
		  for (int i = 0; i < typeList.size(); i++) {
			  if (abbrev.equals(typeList.get(i).abbrev)) {
				  mType = typeList.get(i);
				  i = typeList.size();  //  exit loop
			  }
		  }
		  return mType;
	  } // end method getMonomerType

	  
	
	/**
	 * generate a copy instance of the MonomerType.  The copy is completely independent of the 
	 * MonomerType instance being copied, including its ElementComposition field,
	 * which can be modified without changing the initial instance of MonomerType.
	 * @param abbrev the abbreviation of the MonomerType being copied
	 * @return the copy MonomerType
	 */
	  public MonomerType copyMonomerType(String abbrev) {
		  // 
		  MonomerType source = getMonomerType(abbrev);
		  MonomerType copy = new MonomerType(new String(source.name),
				  new String(source.abbrev), source.ec.copy(), source.eSites, source.aSites);
		  return copy;
	  }	  // end method copyMonomerType
	  
	  
	  
	  /**
	   * retrieves a Derivative from the derList using its abbreviation as an ID
	   * @param abbrev the abbreviation of the Derivative instance to be retrieved
	   * @return the Derivative
	   */
	  public Derivative getDerivative(String abbrev) {
		  // 
		  Derivative der = null;
          for (int j = 0; j < derList.size(); j++) {
	          //  check to see if abbrev is same as abbrev of some Derivative in derList
	          if (abbrev.equals(derList.get(j).abbrev ) ) {
	              // get the instance of the Derivative
	              der = derList.get(j);
	              j = derList.size();  //  exit loop
	           }
          }
	        return der;
	  }  // end method getDerivative
	    
	  /**
	   * retrieves the array of Derivative instances (derList)
	   * @return the list of Derivative instances
	   */
	  public Derivative[] getDerivatives() {
		  Derivative[] x = new Derivative[derList.size()];
	        return (Derivative[])derList.toArray(x);
	  }  // end method getDerivatives
	    
	  
	  /**
	   * retrieves the array of MonomerType instances (typeList)
	   * @return the list of MonomerType instances
	   */
	  public MonomerType[] getMonomers() {
		  MonomerType[] x = new MonomerType[typeList.size()];
	        return (MonomerType[])this.typeList.toArray(x);
	  }  // end method getDerivative
	    

	 //  public <T> T[] toArray(T[] a)
	  /**
	   * MonomerType is an inner class of MonomerTable, specifying the chemical properties
	   * of monomeric residue.  An instance of MonomerTable contains a list of MonomerType instances.
	   * @author will
	   *
	   */
	  public class MonomerType {

		  /**
		   * the name of the MonomerType
		   */
		  String name;

		  /**
		   * the abbreviation of the MonomerType, which can be used to retrieve it using the getMonomerType method 
		   */
		  String abbrev;

		  /**
		   * the incremental average mass for the <b>underivatized</b> MonomerType
		   */
		  double averageIncrement;

		  /**
		   * the incremental monoisotopic mass for the <b>underivatized</b>  MonomerType
		   */
		  double monoisotopicIncrement; 

		  /**
		   * the number of available etherification sites for the MonomerType
		   */
		  int eSites;  

		  /**
		   * number of available acylation sites for the MonomerType
		   */
		  int aSites; 

		  /**
		   * an instance of ElementComposition used to define the (incremental) composition of the MonomerType
		   */
		  ElementComposition ec; 

		  /**
		   * generates a new instance of MonomerType based on a complete list of its properties.
		   * @param name the name of the MonomerType
		   * @param abbrev the abbreviation representing the MonomerType.  This is used to retrieve the MonomerType
		   * when the method getMonomerType is invoked.
		   * @param ec the incremental ElementComposition of the MonomerType
		   * @param eSites the number of etherification sites for the MonomerType
		   * @param aSites the number of esterification sites for the MonomerType
		   */
		  public MonomerType (String name, String abbrev,
				  ElementComposition ec, int eSites, int aSites) {

			  this.name = name;
			  this.abbrev = abbrev;
			  this.eSites = eSites;
			  this.aSites = aSites;
			  this.ec = ec;
			  monoisotopicIncrement = ec.getMonoisotopicMass();
			  averageIncrement = ec.getAverageMass();
		  } // end inner class constructor - MonomerType

	  }  // end inner class - MonomerType




	  /**
	   * Derivative is an inner class of MonomerTable, specifying the chemical properties
	   * of a derivative that can be used to modify a MonomerType.
	   * An instance of MonomerTable contains a list of Derivative instances.
	   */
	  public class Derivative {

		  /**
		   * the name of the Derivative
		   */
		  String name;
		  
		  /**
		   * the abbreviation for the derivative, which can be used to retrieve it using the getDerivative method.
		   */
		  String abbrev;
		  
		  /**
		   * the chemical type of the derivative (ester- or ether-linked).
		   */
		  String type;
		  
		  /**
		   * the average mass of the Derivative.  Each Derivative in a molecule replaces a hydrogen atom.
		   * For example, each "methyl" Derivative has a mass of ~15 Da, and adds a mass of ~14 Da to the
		   * molecule.
		   */
		  double averageIncrement;
		  
		  /**
		   * the monoisotopic mass of the Derivative.  Each Derivative in a molecule replaces a hydrogen atom.
		   * For example, each "methyl" Derivative has a mass of ~15 Da, and adds a mass of ~14 Da to the
		   * molecule.
		   */
		  double monoisotopicIncrement;
		  
		  /**
		   * an instance of ElementComposition specifying the atoms of the Derivative.
		   * Each Derivative in a molecule replaces a hydrogen atom.
		   * For example, each "methyl" Derivative has a composition of "C 1 H 3", and adds 
		   * the one C atom and two H atoms to the molecule.
		   */
		  ElementComposition ec;

		  /**
		   * generates a new instance of Derivative, based on a complete list of its properties.
		   * @param name the name of the Derivative
		   * @param abbrev the abbreviation of the Derivative, which can be used to retrieve it using the getDerivative method
		   * @param ec an instance of ElementComposition defining the atoms of the Derivative
		   * @param type the chemical type of the derivative (ester-linked, specified by "a" or ether-linked, specified by "e").
		   */
		  public Derivative (String name, String abbrev,
				  ElementComposition ec, String type) {
			  this.name = name;
			  this.abbrev = abbrev;
			  this.averageIncrement = ec.getAverageMass();
			  this.monoisotopicIncrement = ec.getMonoisotopicMass();
			  this.type = type;
			  this.ec = ec;
		  } // end inner class constructor - Derivative

	  } // end inner class  - Derivative

	  
	  

	}    // end class - MonomerTable

