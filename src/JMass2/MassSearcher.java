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


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.Arrays;
// import java.text.DecimalFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;

/**
 * The MassSearcher class provides a context for searching for an oligomer composition given a 
 * monoisotopic or average mass and a set of structural parameters, which are defined as XML code.  
 * A set of MonomerType instances that can comprise the oligomer are selected from an instance of
 * MonomerTable, and the minimum and maximum number of each type MonomerType is constrained.  In addition,
 * the structure at the reducing end is specified. The search is implemented by the <b>findCompositions</b>
 * method, which generates a local list of hits, defined by their MonomerCompositions and mass errors.   
 */

public class MassSearcher {

	/**
	 * a list of MonomerComposition instances matching the target mass.  The MonomerType instances in this list comprise
	 * the theoretical oligomers that are generated and whose masses are compared to the target mass
	 */
	Vector<MonomerComposition> hits;

	/**
	 * the initial array of errors (theoretical mass minus target mass) for oligomers that match
	 * the target mass within the specified tolerance
	 */
	Vector<Double> errs;

	/**
	 * An instance of the java Document class that defines the MonomerType instances to be used
	 * to calculate oligomer masses, along with other chemical information such as the reducing
	 * end structure and the chemical derivative
	 */
	static Document document;

	/**
	 * an instance of MonomerTable containing chemical properties and identifiers of MonomerType 
	 * instances.  MonomerType instances must be selected from this MonomerTable.
	 */
	MonomerTable mt;

	/**
	 * the number of MonomerTypes that are considered in the simulation, which corresponds to the depth of the
	 * recursion of the combineMonomers method
	 */
	int depth;

	/**
	 *  the minimum value of the stoichiometric coefficient of each MonomerType to be included in the search
	 */
	int[] minN; 

	/**
	 * the maximum value of the stoichiometric coefficient of each MonomerType to be included in the search
	 */
	int[] maxN; 

	/**
	 * the list of MonomerType instances that are considered in the search
	 */
	Vector<MonomerTable.MonomerType> mtList; 

	/**
	 * the increment in the monoisotopic mass that accompanies the addition of a single copy of each monomerType 
	 * in the search.  This explicitly accounts for the derivative specified in this MassSearcher.
	 */
	double[] monoisotopicIncrement;  

	/**
	 * the increment in the average (chemical)  mass that accompanies the addition of a single copy of each monomerType 
	 * in the search.  This explicitly accounts for the derivative specified in this MassSearcher.
	 */
	double[] averageIncrement;  // averageIncrement holds the average increment of each  monosaccharideType to be searched

	/**
	 * an array of integers specifying the number of sites on each monomer that can be chemically modified by the selected derivative.
	 */
	int[] nSites; 
	
	/**
	 * the Derivative that was used to chemically modify the oligomer (e.g., by permethylation)
	 * 
	 */
	MonomerTable.Derivative der;

	/**
	 * the abbreviation of the Derivative that was selected for the search
	 */
	String derAbbrev; 

	/**
	 * a String specifying the structure at the reducing end of the oligomer.  This can have values "reducing",
	 * "alditol", "derivatized" or "none"
	 */
	String endStruc;

	/**
	 * the monoisotoic mass that is contributed to the oligomer by the reducing end structure, 
	 * including any Derivatives that are present there
	 */
	double redEndMassMI;

	/**
	 * the monoisotoic mass that is contributed to the oligomer by the non-reducing end structure, 
	 * including any Derivatives that are present there
	 */
	double nonredEndMassMI;

	/**
	 * the average (chemical) mass that is contributed to the oligomer by the reducing end structure, 
	 * including any Derivatives that are present there
	 */
	double redEndMassAvg;

	/**
	 * the average (chemical) mass that is contributed to the oligomer by the non-reducing end structure, 
	 * including any Derivatives that are present there
	 */
	double nonredEndMassAvg;

	/**
	 * an ElementComposition corresponding to the atoms that are contributed to the oligomer by the reducing 
	 * end structure, including any Derivatives that are present there
	 */
	ElementComposition redEndComposition;

	/**
	 * an ElementComposition corresponding to the atoms that are contributed to the oligomer by the non-reducing 
	 * end structure, including any Derivatives that are present there.
	 */
	ElementComposition nonredEndComposition;

	/**
	 * an array of ElementComposition instances that specify the atoms that are added to the 
	 * oligomer when a single copy of each MonomerType is added
	 */
	ElementComposition[] incrementalEC;

	/**
	 * an instance of MonomerComposition that is used as a template for each "hit" identified by the search.
	 * The MonomerTypes in this MonomerComposition are static, including only those monomerType instances that
	 * are included in the search.  However, the stoichiometric coefficients vary for each
	 * unique hit.
	 */
	MonomerComposition monomerCompositionTemplate; 



	/**
	 * constructs and configures a new MassSearcher.
	 * @param mt the MonomerTable that defines the monomers in the theoretical oligomers whose mass is compared
	 * to the target mass
	 * @param configXML a String that specifies either (1) the name of an XML file containing the configuration 
	 * information for the new MassSearcher, or (2) an XML representation of the configuration itself.  The 
	 * method first attempts to open a file using <b>configXML</b> as the file name.  If the file cannot be
	 * opened, the method attempts to parse the <b>configXML</b> String and use the information it contains
	 * to configure the new MassSearcher.
	 */
	public MassSearcher(MonomerTable mt, String configXML) {
		this.mt = mt;

		// can extend this to take either an XML string or XML file

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);

		// factory.setValidating(true);   
		// factory.setNamespaceAware(true);


		try {

			DocumentBuilder builder = factory.newDocumentBuilder();

			// can specify a file name or an explicit xml string to configure the searcher
			File msFile = new File(configXML);

			if(msFile.canRead()) {
				// the configXML string names a file that the application can access
				document = builder.parse(msFile);
			} else {
				System.out.println("did not find file, parsing passed XML string.");
				// the configXML string does NOT name a file that the application can access
				// try parsing configXML as a String explicitly specifying XML
				document = builder.parse(new InputSource(new StringReader(configXML)));
			}

			
			org.w3c.dom.NodeList residueList = document.getElementsByTagName("residue_list");

			org.w3c.dom.Node resConfig = residueList.item(0);
			org.w3c.dom.NamedNodeMap resConfigAtts = resConfig.getAttributes();
			this.derAbbrev = resConfigAtts.getNamedItem("derivative").getNodeValue();
			this.der = mt.getDerivative(derAbbrev);                        	    

			// read the end structure info
			this.endStruc = resConfigAtts.getNamedItem("end_structure").getNodeValue();
			//  endStruc specifies the structure of the reducing end or modified reducing end
			initializeEnds();

			// read the monosaccharideType info from XML
			org.w3c.dom.NodeList monomers = document.getElementsByTagName("monomer");
			depth = monomers.getLength();
			this.mtList = new Vector<MonomerTable.MonomerType>(depth);
			this.incrementalEC = new ElementComposition[depth];
			this.monoisotopicIncrement = new double[depth];
			this.averageIncrement = new double[depth];
			this.minN = new int[depth];
			this.maxN = new int[depth];
			this.nSites = new int[depth];    

			for (int i = 0; i < depth; i++) { // i indexes the different MonomerTypes to search
				org.w3c.dom.NamedNodeMap mtAttributes = monomers.item(i).getAttributes();
				String mtA = mtAttributes.getNamedItem("abbrev").getNodeValue();
				// mtA is the MonomerType abbreviation specified in the input XML

				this.minN[i] = Integer.parseInt(mtAttributes.getNamedItem("minimum_number").getNodeValue());
				this.maxN[i] = Integer.parseInt(mtAttributes.getNamedItem("maximum_number").getNodeValue());
				// find the MonomerType that matches the current line in the input file
				mtList.add(mt.copyMonomerType(mtA));

				
				setMassIncrement(i);
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
	}  // end constructor method - MassSearcher

	
	/**
	 * Sets up the end structures, including derivative, monomer compositions and masses
	 */
	private void initializeEnds() {
		//  "reducing"  - OH is added to the reducing end (There is no derivative there!)
		if (this.endStruc.equals("reducing")) {
			String formulaString = new String("H 1 O 1");
			this.redEndComposition = new ElementComposition(mt.toi, formulaString);
		} // end of code for "reducing"
		//  "alditol" - one H-atom, one O atom, and two derivatives are added to the reducing end
		if (this.endStruc.equals("alditol")) {
			// add one O and one H and two derivatives
			String formulaString = new String("H 1 O 1");;
			this.redEndComposition = new ElementComposition(mt.toi, formulaString);
			this.redEndComposition.addCompositionAtoms(der.ec);
			this.redEndComposition.addCompositionAtoms(der.ec);
		} // end of code for "alditol"

		//  "derivatized" - one O atom and one derivative is added to the reducing end
		if (this.endStruc.equals("derivatized")) {
			// add one O and one derivative
			String formulaString = new String("O 1");
			this.redEndComposition = new ElementComposition(mt.toi, formulaString);
			this.redEndComposition.addCompositionAtoms(der.ec);
		}  // end of code for "derivatized"
		this.redEndMassMI = redEndComposition.getMonoisotopicMass();  
		this.redEndMassAvg = redEndComposition.getAverageMass();

		// define nonreducing end - one copy of derivative
		this.nonredEndComposition = der.ec.copy();
		this.nonredEndMassMI = nonredEndComposition.getMonoisotopicMass();  
		this.nonredEndMassAvg = nonredEndComposition.getAverageMass();
	}

	
	/**
	 * sets the incremental mass for a MonomerType when chemically modified by the specified
	 * derivative.  This method modifies two arrays, monoisotopicIncrement and averageIncrement.
	 * @param i the index (within mtList) of the monomer whose incremental mass is to be set.
	 * @see #monoisotopicIncrement
	 * @see #averageIncrement
	 * @see #mtList
	 */
	public void setMassIncrement(int i) {
		//  get the incremental molecular formula for each MonomerType (underivatized for now)
		// need to copy this EC, or original instance will be modified below
		//  This will prevent recalculation of the EC from first principles
		incrementalEC[i] = mtList.get(i).ec.copy();

		//  set the number of derivitization sites for this monosaccharideType
		if (this.der.type.equals("e")) this.nSites[i] = mtList.get(i).eSites;
		if (this.der.type.equals("a")) this.nSites[i] = mtList.get(i).aSites;
		// adjust the incremental molecular formula of this MonomerType to include derivatives
		//  That is, remove one H and add one derivative for each site

		if (nSites[i] > 0) {
			for (int j = 0; j < this.nSites[i]; j++) {
				incrementalEC[i].removeCompositionAtoms(new ElementComposition(mt.toi, "H 1"));
				incrementalEC[i].addCompositionAtoms(der.ec);
			}
		} else { // the number of derivatization sites is less than zero
			for (int j = this.nSites[i]; j < 0; j++) {
				incrementalEC[i].addCompositionAtoms(new ElementComposition(mt.toi, "H 1"));
				incrementalEC[i].removeCompositionAtoms(der.ec);
			}
		}
		this.monoisotopicIncrement[i] = incrementalEC[i].getMonoisotopicMass();
		this.averageIncrement[i] = incrementalEC[i].getAverageMass();
	}

	
	

	/**
	 * serializes the configuration information for the MassSearcher as a Human-readable String.
	 * @return the configuration String
	 */
	public String exportConfig(){
		DecimalFormat f = new DecimalFormat("0.000 ");

		StringBuffer configString = new StringBuffer("\n\n");
		configString.append(this.depth + " monomer types are defined in the following context:");
		configString.append("\nderivative is " + this.derAbbrev);
		configString.append("\nreducing end structure is " + this.endStruc + ", which has monoisotopic mass " + f.format(this.redEndMassMI));
		configString.append("\nnon-reducing end structure has monoisotopic mass " + f.format(this.nonredEndMassMI) + "\n");


		for (int i = 0; i < this.depth; i++) {
			configString.append("\nabbreviation: " + this.mtList.get(i).abbrev + 
					"\tminimum number: " + this.minN[i] +
					"\tmaximum number: " + this.maxN[i] +
					"\tincremental monoisotopic mass:  " + f.format(this.monoisotopicIncrement[i]) +
					"\tincremental composition: " + this.incrementalEC[i].getElementalCompositionString());
		}
		return new String (configString);
	}  // end method - exportConfig



	/**
	 * initiates a mass search, given a target mass, the search tolerance, the form of the search mass
	 * (0 -> monoisotopic, 1 -> average) and the initial size of the array holding the
	 * error for each "hit".  This method calls the recursive method <b>combineMonomers</b>
	 * which implements the actual search and modifies several fields of the MassSearcher
	 * according to the results. 
	 * of the search.
	 * @see #combineMonomers(double, double, int[], double, int[], int[], int, double[], int)
	 * @param target the mass to be searched.
	 * @param tolerance the maximum difference between an oligomer mass and the target mass
	 * that is accepted as a "hit"
	 * @param massForm the form of the target mass (0 -> monoisotopic, 1 -> average) 
	 * @return the number of oligomers that satisfied the search
	 */
	public int findCompositions(double target, double tolerance, int massForm) {

		// reinitialize the data structures that will contain the results
		this.hits = new Vector<MonomerComposition>();
		this.errs = new Vector<Double>();
		int count = 0;
		double  runningTotal = 0;
		// start with the end groups to calculate the mass, depending on massType
		if (massForm == 0) {  // if massType is monoisotopic
			runningTotal = this.nonredEndMassMI + this.redEndMassMI;
		} else {  // if massType is average
			runningTotal = this.nonredEndMassAvg + this.redEndMassAvg;
		}

		this.monomerCompositionTemplate = new MonomerComposition(this.mt);
		//  monomerCompositionTemplate is used to generate a MonomerComposition for each "hit"
		//    monomerCompositionTemplate contains all of the monosaccharideTypes that 
		//    are allowed to contribute to the searched structure

		int j = 0;  // j is index of residues that are included in the calculation
		for (int i = 0; i < this.depth; i++) {  // for all the monosaccharides in the SearchConfiguration
			if (this.maxN[i] > 0) j++;
		}
		double[] delta = new double [j];  // delta is a short array of incremental masses (MI or Avg)
		int[] shortMin = new int [j];  // shortMin is the minimum number of residues in short list
		int[] shortMax = new int [j];  // shortMax is the maximum number of residues in short list
		int[] searchCoefficient = new int [j];
		j = 0;
		for (int i = 0; i < this.depth; i++) {  // for all the monosaccharides in the SearchConfiguration
			if (this.maxN[i] > 0) {  // process only those that will add to the mass of the molecule
				this.monomerCompositionTemplate.addResidues(this.mtList.get(i), this.minN[i]);
				shortMin[j] = minN[i];
				shortMax[j] = maxN[i];
				searchCoefficient[j] = minN[i];
				if (massForm == 0) {  // if massType is monoisotopic
					delta[j] = this.monoisotopicIncrement[i];
				} else {  // if massType is average
					delta[j] = this.averageIncrement[i];
				}

				// include the masses of the initial composition in the initial mass
				runningTotal += searchCoefficient[j] * delta[j];
				j++;
			}
		}

		count = combineMonomers(target, tolerance, 
				searchCoefficient, runningTotal,
				shortMin, shortMax,
				0, delta, count);

		return count;
	}  // end method - findCompositions




	/**
	 * implements the mass search using a recursive algorithm.  The parameters used are 
	 * assembled by the <b>findCompositions</b> method for convenience.  When a match is 
	 * identified, appropriate values are added to two Vectors (<b>hits</b> and <b>errs</b>)
	 * of this MassSearcher and the value of the <b>count</b> filed is incremented.  This information 
	 * allows the chemical features of each "hit" (e.g., mass, ElementComposition, etc) to be inferred.
	 * @see #findCompositions(double, double, int)
	 * @param target the mass to be searched
	 * @param tolerance the maximum difference between an oligomer mass and the target mass
	 * that is accepted as a "hit" 
	 * @param searchCoefficient an array of int that holds the running total stoichiometric
	 * coefficients for those MoomerTypes that are included in the search.
	 * @param runningTotal holds the running total mass at each stage though the calculation
	 * @param searchMin an array of int that holds the minimum stoichiometric coefficient for
	 * each MonomerType that is included in the search
	 * @param searchMax an array of int that holds the maximum stoichiometric coefficient for
	 * each MonomerType that is included in the search
	 * @param lev the current recursion level
	 * @param delta an array of incremental masses for each MonomerType that is included in the search
	 * @param count the number of hits that have been identified
	 * @return the number of monomer combinations that match the target mass within the specified tolerance
	 */
	private int combineMonomers(double target, double tolerance, 
			int[] searchCoefficient, double runningTotal,
			int[] searchMin, int[] searchMax,
			int lev, double[] delta, int count) {

		// make a local copy of the coefficients so these do not get overwritten by recursion instances of the method

		int[] localCoefficient = Arrays.copyOf(searchCoefficient, searchCoefficient.length);

		for (int i = minN[lev]; i <= searchMax[lev] + 1; i++) {
			// System.out.println(" level is " + lev + " count is " + count + " mass is " + runningTotal);
			if (lev + 1 < localCoefficient.length) {
				count = combineMonomers(target, tolerance, localCoefficient, runningTotal,
						searchMin, searchMax, lev + 1, delta, count);
			} else {
				// System.out.println("Testing results");
				// System.out.print(runningTotal + " " + Math.abs(runningTotal-target) + "\n");
				if (Math.abs(runningTotal - target) < tolerance) {  // A HIT!!!
					// set up the hitComposition so it contains the appropriate monomer definitions in the appropriate order
					MonomerComposition hitComposition = monomerCompositionTemplate.copyFull();
					// set the stoichiometric coefficients for the hitComposition
					hitComposition.coefficient = Arrays.copyOf(localCoefficient, localCoefficient.length);                	
					hits.add(hitComposition);	
					errs.add(runningTotal - target);
					count++;
				}

			}

			if ( (runningTotal - target) > tolerance) {
				// if the running total is already too big, break because it can only get bigger
				break;
			}

			runningTotal += delta[lev];
			localCoefficient[lev]++; 
			// these are not tested against target when (i == shortMax[lev])
		}

		return count;
	}  // end method combineMonomers



	/**
	 * fetches the array of MonomomerComposition instances that are generated by a search
	 * @return the array of MonomomerComposition instances
	 */
	public MonomerComposition[] getHits() {
		MonomerComposition[] hitArray = new MonomerComposition[this.hits.size()];
		for (int i = 0; i < hitArray.length; i++ )
			hitArray[i] = this.hits.get(i);
		return hitArray;
	}

	
	/**
	 * fetches the array of mass errors (theoretical mass minus target mass) that are generated by a search
	 * @return the array of errors
	 */
	public double[] getErrs() {
		
		double[] theErrs = new double[this.errs.size()];
		for (int i = 0; i < theErrs.length; i++ )
			theErrs[i] = this.errs.get(i);
		
		return  theErrs;  	
	}

	
	/**
	 * calculates the monoisotopic mass of an oligomer with a specified MonomerType composition <i> in the
	 * context of the configuration</i> of this <b>MassSearcher</b>
	 * @param mc the MonomerComposition of an oligomer
	 * @return the monoisotopic mass of the oligomer
	 */
	public double getMonoisotopicMass(MonomerComposition mc) {
		ElementComposition theEC = getElementComposition(mc);
		return theEC.getMonoisotopicMass();
	} // end method - getMonoisotopicMass


	/**
	 * calculates the average mass of an oligomer with a specified MonomerType composition <i> in the
	 * context of the configuration</i> of this <b>MassSearcher</b>
	 * @param mc the MonomerComposition of an oligomer
	 * @return the average mass of the oligomer
	 */
	public double getAverageMass(MonomerComposition mc) {
		ElementComposition theEC = getElementComposition(mc);
		return theEC.getAverageMass();
	} // end method - getAverageMass


	/**
	 * calculates the ElementComposition of the oligomer specified by a MonomerComposition <b>in the 
	 * context</b> of the global parameters (e.g., derivative, reducing end structure) that are defined
	 * in this <b>MassSearcher</b>
	 * @param mc an instance of MonomerComposition defining the MonomerTypes and their stoichiometric
	 * coefficients in the oligomer
	 * @return the ElementComposition
	 */
	public ElementComposition getElementComposition(MonomerComposition mc) {

		// ec is the ElementComposition returned by this method
		ElementComposition ec = new ElementComposition(mt.toi);

		for (int i = 0; i < mc.typeList.size(); i++) {
			//  set the number of derivitization sites for each monosaccharideType
			int ns = 0;  // number of derivatization sites
			if (this.der.type.equals("e")) ns = mc.typeList.get(i).eSites;
			if (this.der.type.equals("a")) ns = mc.typeList.get(i).aSites;
			// adjust the incremental molecular formula of this MonomerType to include derivatives
			//  That is, remove one H and add one derivative for each site
			// define resDeltaEC element composition with coefficients to account for each derivatization site
			// start with the derivative itself
			ElementComposition resDeltaEC = this.der.ec.copy();
			// include protons that are replaced
			resDeltaEC.addCompositionAtoms(new ElementComposition(mt.toi, "H -1"));
			// @@@
			// multiply all coefficients of resDeltaEC by ns 
			for (int j = 0; j < resDeltaEC.coefficient.length; j++) 
				resDeltaEC.coefficient[j] = ns * resDeltaEC.coefficient[j];

			// define the fully corrected residue Element Composition (resEC)
			ElementComposition resEC = mc.typeList.get(i).ec.copy();
			System.out.println("initial residue composition: " + resEC.getElementalCompositionString());
			resEC.addCompositionAtoms(resDeltaEC);
			System.out.println("i is " + i + "   ns is " + ns);

			/*
			if (ns > 0) { 
				for (int j = 0; j < ns; j++) {
					resEC.removeCompositionAtoms(new ElementComposition(mt.toi, "H 1"));
					resEC.addCompositionAtoms(this.der.ec);
				}
			} else {
				for (int j = ns; j < 0; j++) {
					resEC.addCompositionAtoms(new ElementComposition(mt.toi, "H 1"));
					resEC.removeCompositionAtoms(this.der.ec);
				}

			}
			*/
			
			
			// @@@ It looks like an elemental composition must have zero or more atoms of each element
			System.out.println("final residue composition: " + resEC.getElementalCompositionString());
			// add atoms for the type, taking coefficient into account
			for (int counter = 0; counter < mc.coefficient[i]; counter++) 
				ec.addCompositionAtoms(resEC);
			System.out.println("partial composition (no ends): " + ec.getElementalCompositionString());
		}
		ec.addCompositionAtoms(this.redEndComposition);
		ec.addCompositionAtoms(this.nonredEndComposition);
		System.out.println("composition (with ends): " + ec.getElementalCompositionString());

		return ec;
	} // end method - getElementComposition



	/**
	 * generates a human-readable String that specifies the Elemental Composition of an
	 * oligomer with a specified MonomerComposition.  This method is implemented within
	 * the <b>MassSearcher</b> class because the atoms in the oligomer depend on parameters
	 * that are specified within this class, including the Derivative and the reducing-end
	 * structure. The String that is returned can be used to instantiate an instance of
	 * ElementComposition or to initiate a simulation of the corresponding isotopolog
	 * distribution.
	 * @see ElementComposition
	 * @see IsotopeDistributor
	 * @param mc an instance of MonomerComposition defining the MonomerTypes and their 
	 * stoichiometric coefficients in the oligomer
	 * @return the human-readable elemental composition String
	 */
	public String getElementCompositionString(MonomerComposition mc) {
		ElementComposition ef = getElementComposition(mc);
		// note that the next line calls the getElementalCompositionString method.
		//  This is NOT recursive, as the methods have different names.
		String efs = ef.getElementalCompositionString();
		return efs;
	} // end method getElementCompositionString
	
	
	/**
	 * sets the reducing end structure of the oligomer.
	 * @param endString a String (e.g., "alditol" or "reducing") specifying the reducing end of the oligomer
	 */
	public void setEndStructure(String endString) {
		this.endStruc = endString;
		initializeEnds();
	}

	/**
	 * sets the derivative (chemical modification) of the monomers.
	 * @param derivString a String (e.g., "Me" or "none") specifying the id of the derivative to be used
	 */
	public void setDeriv(String derivString) {
		this.derAbbrev = derivString;
		this.der = mt.getDerivative(derivString); 
		for (int i = 0; i < mtList.size(); i++) {
			setMassIncrement(i);
		}
	}


}
