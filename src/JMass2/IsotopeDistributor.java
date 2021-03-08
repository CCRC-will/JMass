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

/**
 * @author William S. York
 *
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * Calculates a distribution of isotopologs for a molecule.
 * The result is in the form of two ordered arrays, which specify data pairs corresponding 
 * to the mass and abundance of each isotopolog.  The isotopic formula of each isotopolog 
 * is specified in a third ordered array of Strings.  These arrays or their elements can be retrieved 
 * using various "get" methods.
 * 
 * <p>Sets of isotopologs with nearly the same mass are also combined 
 * to generate a list of centroid peak abundances and masses.  The centroid masses
 * are separated by approximately one Dalton, as it is assumed that the resolution is
 * sufficient to separate isotopologs that differ in mass by approximately
 * one Dalton but not sufficient to separate the isotopologs that differ in mass by less 
 * than 0.1 Daltons.  The centroid pattern thus corresponds to a typical mass spectral peak list.</p>
 * 
 * <p> The molecular formula of the molecule whose isotopolog distribution is to be simulated
 * can be specified in several different formats, which are documented in the various
 * <b>generateIsotopologDistribution</b> methods.
 * 
 */
public class IsotopeDistributor {
	  

	/**
	 * string representation of the molecular formula whose isotopolog distribution is being simulated
	 */
	private String molFormulaStr;
	
	/**
	 * the initial unsorted array of isotopolog abundances
	 */
	private Vector<Double> abundance;
	
	/**
	 * an array of isotopolog abundances, sorted according to abundance
	 */
	private double[] sortedAbundance;
	
	/**
	 * an array of isotopolog abundances, sorted according to abundance, containing
	 * only the most abundant isotopologs, which together satisfy the 
	 * abundance <b>coverage</b> parameter
	 */
	private  double[] briefAbundance;
	
	/**
	 * the initial unsorted array of isotopolog masses
	 */
	private  Vector<Double> mass;
	
	/**
	 * an array of isotopolog masses, sorted according to abundance
	 */
	private  double[] sortedMass;
	
	/**
	 * an array of isotopolog masses, sorted according to abundance, containing
	 * only the most abundant isotopologs, which together satisfy the 
	 * abundance <b>coverage</b> parameter
	 */
	private  double[] briefMass;

	/**
	 * the initial unsorted array of isotopolog composition Strings
	 */
	private  Vector<String> compStr;
	
	/**
	 * an array of isotopolog composition strings, sorted according to abundance
	 */
	private  String[] sortedCompStr;
	
	/**
	 * an array of isotopolog composition strings, sorted according to abundance, containing
	 * only the most abundant isotopologs, which together satisfy the 
	 * abundance <b>coverage</b> parameter
	 */
	private  String[] briefCompStr;
	
	/**
	 * an array of isotopolog cumulative abundances, sorted according to abundance, containing
	 * only the most abundant isotopologs, which together satisfy the 
	 * abundance <b>coverage</b> parameter
	 */
	private  double[] briefCumulative;
	
    /**
     * an array of centroid (abundance weighted average) masses, 
     * each corresponding to a set of isotopologs that have nearly
     * the same mass and thus are not usually separated in a mass spectrometer
     */
	private double[] centroidMass;
	
    /**
     * an array of centroid peak abundances,
     * each corresponding to a set of isotopologs that have nearly
     * the same mass and thus are not usually separated in a mass spectrometer
     */
	private double[] centroidAbundance;
	
    /**
     * an array of centroid populations,
     * each corresponding to the number of isotopologs in a set that have nearly
     * the same mass and thus are not usually separated in a mass spectrometer
     */
	private int[] centroidN;
	
	/**
	 * the number of isotopologs that satisfy the <b>coverage</b> parameter
	 */
	private int coverageCount;
	
	/**
	 * the fraction of the isotopolog abundance to be accounted for in the  
	 * brief arrays describing the simulation results
	 */
	private double coverage;
	
	/**
	 * a sorted array of isotopolog cumulative abundances
	 */
	private double[] cumulative;
	
	/**
	 * an instance of the <b>TableOfIsotopes</b> class, which provides the 
	 * fundamental chemical information required to perform the isotopolog
	 * distribution calculation
	 */
	private TableOfIsotopes toi;
	
	/**
	 * the average (chemical) mass of the molecule being simulated
	 */
	private double avgMass = 0;
	
	/**
	 * the monoisotopic mass of the molecule being simulated
	 */
	private double monoisotopicMass = 0;
	
	
	/**
	 * an array of int with length equal to the number of isotopes considered in the simulation, specifying
	 * the actual number atoms of of a particular isotope in the isotopolog being calculated.
	 * The iComp array holds one integer for each level of recursion, and that value
	 * is set equal to the value of the variable i, which is sequentially assigned several  
	 * different values for each recursion lever.  That is, at each recursion level,
	 * a 'for' loop is executed, within which i is incremented.  The value of iComp[d] 
	 * (where d is the recursion depth) is set to for each loop cycle.  At each leaf recursion, 
	 * the isotopic composition of the istopomer corresponding to that leaf is inferred from
	 * the values in iComp and recorded as one of the parameters describing the isotopomer. 
	 */
	private int[] iComp;
	
	/**
	 * an array of int with length equal to the number of isotopes considered in the simulation, specifying 
	 * the maximum possible number of each isotope in the molecule. The values in
	 * n are closely related to (but not the same as) the elemental composition. 
	 */
	private int[] n;
	
	/**
	 * an array of int with length equal to the number of isotopes considered in the simulation, specifying
	 * whether the isotope corresponding to a given recursion level is the same element as the element
	 * of the isotope corresponding to the parent recursion level.
	 */
	private int[] same;
	
	/**
	 * the minimum isotopolog (fractional) abundance of an isotopolog.  When the abundance of an isotopolog
	 * is below cutoff, the loop that increments i is exited and the calculation is truncated, preventing
	 * isotopologs that have very small abundances from being considered.  This results in very much more
	 * efficient calculation of the significantly abundant isotopologs.
	 */
	private double cutoff;
	
	/**
	 * the maximum number of recursion levels.  The value of depth corresponds to the number of distinct
	 * isotopes that are considered in the simulation.
	 */
	private int depth = 0;
	
	/**
	 * the number of isotopologs that are recorded.
	 */
	private int count;
	
	/**
	 * an array of String with length equal to the number of isotopes considered in the simulation, 
	 * specifying the chemical symbol of the isotope.  For example, "13C" or "18O".
	 */
	private String[] isoName;
	
	/**
	 * an array of double with length equal to the number of isotopes considered in the simulation, 
	 * specifying the mass of the MOST abundant isotope of the element corresponding to the 
	 * isotope.  For example, if the isotope is 13C, massM would be 12.00000.
	 */
	private double[] massM;
	
	/**
	 * an array of double with length equal to the number of isotopes considered in the simulation,
	 * specifying the difference between the mass of the isotope and the mass of the most abundant
	 * isotope of the same element.  For example, for the isotope 13C, delta is 1.00335483.
	 */
	private double[] delta;
	
	/**
	 * an array of double with length equal to the number of isotopes considered in the simulation,
	 * specifying the probability (fractional abundance) of the isotope.
	 */
	private double[] p;
	
	/**
	 * an array of double with length equal to the number of isotopes considered in the simulation,
	 * specifying the probability (fractional abundance) of the most common isotope of the element.
	 */
	private double[] pM;
	
	/**
	 * the monoisotopic mass of the isotopolog with the lowest molecular mass in the simulation.
	 */
	private double minM;
	
	/**
	 * the monoisotopic mass of the isotopolog with the highest molecular mass in the simulation.
	 */
	private double maxM;
	
	/**
	 * the probability (fractional abundance) of the monoisotopic istopolog. 
	 * The monoisotopic istopolog contains only the most abundant isotopes of each element
	 * in the molecule.
	 */
	private  double monoP;
    
	  
    /**
     * constructs a new <b>IsotopeDistributor</b>, requiring an instance of the
     * <b>TableOfIsotopes</b> class.
     *  @param toi provides fundamental chemical information
     *  required to calculate the distribution.
     */
	public IsotopeDistributor(TableOfIsotopes toi) {
		this.toi = toi;
	}
	  
	
	
	/**
	 * at recursion-tree leaf, adds an isotopolog to the temporary arrays
	 * @param theMass the mass of the isotopolog
	 * @param theProb the probability (i.e., fracitonal abundance) of the isotopolog
	 */
	private void addHit (double theMass, double theProb) {
		
		StringBuffer isoCompStrBuf = new StringBuffer();
		this.mass.add(theMass);
		abundance.add(theProb);
		minM = Math.min(theMass, minM);
		maxM = Math.max(theMass, maxM);
		for (int q = 0; q < depth; q++) if (iComp[q] > 0)
			isoCompStrBuf.append("(" + isoName[q] + ")" + iComp[q] + " ");
		this.compStr.add(new String(isoCompStrBuf));
		count++;
	}
	
	/**
	 * a recursive method that serves as the heart of the simulation algorithm.  The mass and 
	 * abundance (i.e., probability) of an isotopolog is calculated by recursive traversal. Each 
	 * each time the method is recursively called, another isotope is introduced to the isotopolog.
	 * Thus, each isotopolog corresponds to a leaf of the recursion tree, which is encountered 
	 * whenever there are no more isotopes to add to the isotopolog. 
	 * 
	 * @param d the current depth of the recursion
	 * @param iParent the stoichiometry for the isotope corresponding
	 * to the parent of the current recursion.  If there is no parent recursion, 
	 * iParent is zero. 
	 * @param mass the partial mass of the isotopolog calculated by parent recursions of the method
	 * @param prob the partial probability of the isotopolog calculated by parent recursions of the method
	 */
	private void addIsotope(int d, int iParent, double mass, double prob) {
		
		// iComp[], depth, same[] and n[] are global variables
		double localMass = mass;
		double localProbability = prob;
		double lastProb = 0;
		int nowM;

		for (int i = 0; i < (1 + this.n[d] - this.same[d] * iParent); i++ ) {
			// iComp is a GLOBAL variable containing current isotope composition
			this.iComp[d] = i;
			
			// nowM is the number of atoms that currently are in the most common isotope for the element
			//  to calculate nowM, subtract i (the number of atoms of the current isotope) 
			//  from the maximum number of atoms n[d] possible for the current isotope
			//    also, if the isotope of the parent recursion is the same element as the 
			//    isotope of the current recursion, subtract the number of atoms (i.e., iParent) 
			//    of the parent isotope
	        nowM = n[d] - i - this.same[d] * iParent;
	        // if probability is above cutoff or probability is increasing in this loop
	        if ((localProbability > cutoff) || (lastProb < localProbability) )  {
	        	if (d == this.depth - 1) { // d can have values 0 .. (depth - 1)
	        		addHit(localMass, localProbability);
	        	} else {   // RECURSION
	        		addIsotope(d + 1, i, localMass, localProbability);
	        	}
	        } else break;
	        //  for first cycle, i = 0, so passed probability and mass was used
	        // calculate probability and mass for next cycle of loop (i > 0)
	        localMass = localMass + this.delta[d];
	        lastProb = localProbability;
	        // nowM and (i+1) are used to adjust the multinomial factor of the probability
	        //   p[d] and pM[d] are used to adjust the abundance factor of the probability
	        //   Incrementing i corresponds to replacing one atom of the most
	        //    abundant isotope of the element with the local isotope
	        localProbability = localProbability * nowM * this.p[d] / (this.pM[d] * (i + 1));
		}	
	}  // end method addIsotope


	/**
	 * a convenience method to print a summary of the simulation to std-out.  This
	 * method may be useful for debugging.
	 */
	public void showInfo() {
		// prints array contents - also helpful for deciphering algorithm logic
		System.out.print("\nElemental Composition: " + this.molFormulaStr);

		System.out.print("\ncutoff: " + this.cutoff);

		System.out.print("\ndepth (number of rare isotopes considered): " + this.depth);

		System.out.print("\nisotopes considered:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.isoName[q]);

		System.out.print("\nmaximum number of each isotope:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.n[q]);

		System.out.print("\nmass of isotope:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.massM[q]);

		System.out.print("\nboolean indicating whether the isotope is the same element as the last isotope in the list:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.same[q]);

		System.out.print("\nmass difference of isotope compared to most abundant isotope:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.delta[q]);

		System.out.print("\nfractional abundance of the isotope:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.p[q]);

		System.out.print("\nfractional abundance of the most abundant isotope of the element:\n");
		for (int q = 0; q < this.depth; q++) System.out.print(" " + this.pM[q]);
		System.out.println("\n");

		System.out.print("All isotopologs in order of abundance\ncount Mass     Abundance   Cum\t       Composition\n");
		double pSum = 0;
		for (int i = 0; i < sortedAbundance.length; i++ ) {
			pSum += sortedAbundance[i];
			//  note that output lists the first index at 1, not 0, so these integers cannot be used as array indices
			System.out.format("%-5d%9.4f%9.4f%9.4f\t%s\n", i+1, sortedMass[i], sortedAbundance[i], cumulative[i], sortedCompStr[i]);
		}
		
		System.out.print("\nIsotopologs required for " + 100*this.coverage + " % coverage in order of abundance\ncount Mass     Abundance   Cum\t       Composition\n");
		pSum = 0;
		for (int i = 0; i < briefAbundance.length; i++ ) {
			pSum += briefAbundance[i];
			//  note that output lists the first index at 1, not 0, so these integers cannot be used as array indices
			System.out.format("%-5d%9.4f%9.4f%9.4f\t%s\n", i+1, briefMass[i], briefAbundance[i], briefCumulative[i], briefCompStr[i]);
		}
		
		System.out.println("\nCentroid data for all calculated isotopomers\n mass      Abundance   Cum    Number of Isotopomers");
		pSum = 0;
		for(int q = 0; q < centroidAbundance.length; q++) {
			pSum += centroidAbundance[q];
			System.out.format("%9.4f%9.4f%9.4f%4d\n",centroidMass[q], centroidAbundance[q], pSum, centroidN[q]);
		}
		System.out.print("\n\n");
	}	 
	
	/**
	 * generates an isotopolog distribution with back compatibility to old versions of jMass.
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param elementCompString represents the molecular formula of the molecule whose isotopolog distribution
	 * is to be simulated.  This string can have two formats:
	 * <ul><li>a space-separated list of integers mapped to the elements and pseudoelements in the TableOfIsotopes<br>
	 * For example, "0 3 0 0 10 0 0 0 0 2 0 1" might represent C~3 H10 C2 O1, where C~ is a pseudoelement (enriched carbon)</li>
	 * <li> a space-separated list of chemical symbols and stoichiometric coefficients<br>
	 * For example, "C 6 H 12 O 6 Na 1" would represent a sodiated hexose molecule</li></ul>
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 */
	public int generateIsotopologDistribution(String elementCompString, double cutoff) {
		// this version of the method takes a SINGLE String (elementCompString) to specify elemental composition

		double coverage = 0.999;
		boolean verbose = false;
		return generateIsotopologDistribution(elementCompString, cutoff, coverage, verbose);
	} // end generateIsotopeDistribution

	/**
	 * generates an isotopolog distribution with back compatibility to old versions of jMass.
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param elementCompString represents the molecular formula of the molecule whose isotopolog distribution
	 * is to be simulated.  This string can have two formats:
	 * <ul><li>a space-separated list of integers mapped to the elements and pseudoelements in the TableOfIsotopes<br>
	 * For example, "0 3 0 0 10 0 0 0 0 2 0 1" might represent C~3 H10 C2 O1, where C~ is a pseudoelement (enriched carbon)</li>
	 * <li> a space-separated list of chemical symbols and stoichiometric coefficients<br>
	 * For example, "C 6 H 12 O 6 Na 1" would represent a sodiated hexose molecule</li></ul>
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @param coverage the fraction of the total isotopolog abundance that is reported in abbreviated lists
	 * @param verbose a boolean specifying whether to send a summary of the simulation to std-out
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 */	  
	public int generateIsotopologDistribution(String elementCompString, double cutoff, double coverage, boolean verbose)  {
		// this version of the method takes a SINGLE String (elementCompString) to specify elemental composition
		// elementCompString can either list a set of integers or list as set of symbols and numbers
		ElementComposition ec = new ElementComposition(this.toi, elementCompString); 
		return generateIsotopologDistribution(ec, cutoff, coverage, verbose);
	} // end generateIsotopeDistribution


	/**
	 * generates an isotopolog distribution, specifying the molecular formula as an array of integers
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param fullStoichiometry represents the molecular formula of the molecule whose isotopolog distribution
	 * is to be simulated.  The elements of this array specify stoichiometric coefficients that are mapped 
	 * to the elements and pseudoelements in the TableOfIsotopes<br>
	 * For example, {0, 3, 0, 0, 10, 0, 0, 0, 0, 2, 0, 1} might represent C~3 H10 C2 O1, 
	 * where C~ is a pseudoelement (enriched carbon)
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @param coverage the fraction of the total isotopolog abundance that is reported in abbreviated lists
	 * @param verbose a boolean specifying whether to send a summary of the simulation to std-out
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 */		  
	public  int generateIsotopologDistribution(int[] fullStoichiometry, double cutoff, double coverage, boolean verbose)  {
		// this version of the method takes an array of int[] to specify elemental composition

		ElementComposition ec = new ElementComposition(this.toi, fullStoichiometry);
		return generateIsotopologDistribution(ec, cutoff, coverage, verbose);
	}  // end generateIsotopeDistribution


	/**
	 * generates an isotopolog distribution, specifying the molecular formula as an array of Strings.
	 * Each string in the array specifies a chemical symbol AND a number, separated by a space.
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param stoichiometryString represents the molecular formula of the molecule whose isotopolog distribution
	 * is to be simulated.  The elements of this array are Strings, each of which specifies a chemical symbol 
	 * AND a number, separated by a space.<br>
	 * For example, {"C 12", "H 22", "O 11"} would represent a disaccharide comoposed of two hexose residues.
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @param coverage the fraction of the total isotopolog abundance that is reported in abbreviated lists
	 * @param verbose a boolean specifying whether to send a summary of the simulation to std-out
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 */	  
	public int generateIsotopologDistribution(String[] stoichiometryString, double cutoff, double coverage, boolean verbose)  {
		// this version of the method takes an ARRAY of String[] to specify elemental composition
		//  

		ElementComposition ec = new ElementComposition(this.toi, stoichiometryString);
		return generateIsotopologDistribution(ec, cutoff, coverage, verbose);
	} // end generateIsotopeDistribution
	  

	/**
	 * generates an isotopolog distribution, specifying the molecular formula as an array of Strings and an array of integers.
	 * Each string in the array specifies a chemical symbol.  Each interger in the array specifies a stoichiometric coefficient.
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param elementSymbol represents the elements in the molecular formula of the molecule whose isotopolog distribution
	 * is to be simulated.  The elements of this array are Strings, each of which specifies a chemical symbol.<br>
	 * For example, {"C", "H", "O"} would represent the elements in a disaccharide composed of two hexose residues.
	 * @param stoichiometry represents the stoichiometric coefficients of elements in the molecular formula of the molecule
	 * whose isotopolog distribution is to be simulated.  The elements of this array are integers, each of which specifies 
	 * the number of atoms of the element in the molecule.<br>
	 * Given the elementSymbol array shown above, {"12", "22", "11"} would represent the number of C, H, and O 
	 * atoms in a disaccharide composed of two hexose residues.
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @param coverage the fraction of the total isotopolog abundance that is reported in abbreviated lists
	 * @param verbose a boolean specifying whether to send a summary of the simulation to std-out
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 */	  
	public int generateIsotopologDistribution(String[] elementSymbol, int[] stoichiometry, double cutoff, double coverage, boolean verbose)  {
		// this version of the method takes an ARRAY of String[] to specify elements and an ARRAY of int[] to specify stoichiometry
		//  all other versions of the method eventually call this version

		ElementComposition ec = new ElementComposition(this.toi, elementSymbol, stoichiometry);
		return generateIsotopologDistribution(ec, cutoff, coverage, verbose);
	}

	  
	  
	/**
	 * generates an isotopolog distribution, specifying the molecular formula as an Object of the class ElementComposition.
	 * The results are stored in the following data structures:
	 * 	<b>briefAbundance</b>, <b>briefMass</b>, <b>briefCompStr</b>, <b>briefCumulative</b>, <b>monoisotopicMass</b>, 
	 *	<b>avgMass</b>, <b>mass</b>, <b>abundance</b>, <b>compStr</b>, 	<b>sortedAbundance</b>, <b>sortedMass</b>, 
	 *	<b>cumulative</b>, <b>sortedCompStr</b>
	 * @param ec an instance of ElementComposition for the molecule whose isotopolog distribution is to be simulated.
	 * @param cutoff the minimum fractional abundance for reporting an isotopolog
	 * @param coverage the fraction of the total isotopolog abundance that is reported in abbreviated lists
	 * @param verbose a boolean specifying whether to send a summary of the simulation to std-out
	 * @see #mass
	 * @see #abundance
	 * @see #compStr
	 * @see #briefMass
	 * @see #briefAbundance
	 * @see #briefCompStr
	 * @see #briefCumulative
	 * @see #cumulative
	 * @see #sortedMass
	 * @see #sortedAbundance
	 * @see #sortedCompStr
	 * @see #monoisotopicMass
	 * @see #avgMass
	 * @return the number of isotopologs that were identified.  This corresponds to the parameter <i>count</i>.
	 */	  	  
	public int generateIsotopologDistribution(ElementComposition ec, double cutoff, double coverage, boolean verbose)  {
	    Integer[] order;
		this.cutoff = cutoff;
		this.coverage = coverage;
		// reinitialize depth in case this method has been called before
		this.depth = 0;

		// first, determine the recursion depth, i.e., the total number of different isotopes used
		for(int i = 0; i < ec.coefficient.length; i++) 
			if (ec.coefficient[i] > 0) depth += ec.eList.get(i).getB() - 1;

		// next, create arrays that describe the state of the system, based on the 
		//     composition and configuration parameters
		//  the most abundant isotopes for each element are NOT INCLUDED in the following arrays
		//  only the less abundant "alternate isotopes" are included

		// the arrays have length = depth and are ZERO-INDEXED

		// isoName is the name of the isotope
		isoName = new String[depth];
		// n is the maximum possible number of each isotope in the molecule
		//  n is closely related to (but not the same as) the elemental composition
		n = new int[depth];
		//  massM is the mass of the MOST abundant isotope of the same element
		massM = new double[depth];
		// same is a flag indicating that the isotope is the third most abundant for the element
		// that is, the current isotope corresponds to the SAME element as the last element in the array
		same = new int[depth];
		//  delta is the increase in mass when the most abundant isotope is replaced by the isotope
		delta = new double[depth];
		//  p is the abundance (probability) of the isotope
		p = new double[depth];
		// pM is the abundance (probability) of the most abundant isotope for the element 
		pM = new double[depth];

		//  iComp holds the isotope composition during the recursion 
		//  iComp changes during the calculation, the arrays defined above do not
		iComp = new int[depth];

		//  fill in values for the arrays
		//  this step makes the recursion very simple to implement
		//  each array has a value for each isotope that should be considered
		int ii = 0;  // ii is isotope index
		TableOfIsotopes.Element theElement;
		monoP = 1.0;
		for(int i = 0; i < ec.coefficient.length; i++)   // for each element in the molecule
			if (ec.coefficient[i] > 0) {  // if the element is present in the molecule
				// for each (minor) isotope for the element - getB > total # of isotopes for element
				//  ignore the most common isotope 
				theElement = ec.eList.get(i);
				// use the abundance of the most abundant isotope of theElement 
				//   to calculate the monoisotopic abundance for the molecule
				monoP *= Math.pow(theElement.getA(0), ec.coefficient[i]);
				// set parameters for each isotope (indexed by ii) that is considered
				for (int j = 1; j < theElement.getB(); j++)  {  
					isoName[ii] = theElement.getIsotope(j);
					n[ii] = ec.coefficient[i];
					if (j > 1) same[ii] = 1;
					massM[ii] = theElement.getM(j);
					delta[ii] = theElement.getM(j) - theElement.getM(0);
					p[ii] = theElement.getA(j);
					pM[ii] = theElement.getA(0);
					// increment the isotope counter
					ii++;
				}
			}

		// reinitialize the parameters in case this method has been used before
		this.monoisotopicMass = ec.getMonoisotopicMass();
		this.avgMass = ec.getAverageMass();
		mass = new Vector<Double>();
		abundance = new Vector<Double>();
		compStr = new Vector<String>();		
		count = 0;
		minM = monoisotopicMass;
		maxM = monoisotopicMass;

		// adjust cutoff for very large molecules
		this.cutoff = cutoff * monoP;


		// THE FOLLOWING FUNCTION IS THE HEART OF THE ALGORITHM
		//   recursively calculate masses and probabilities of isotopomers

		addIsotope(0, 0, monoisotopicMass, monoP);


		// initialize int[] order and define the order array according to abundance

		order = new Integer[abundance.size()];

		// Populate order array.
		for(int z=0; z<order.length; z++) {
			order[z] = z;
		}

		Arrays.sort(order, 
			new Comparator<Integer>() {   
				public int compare(Integer b, Integer a) {
					return Double.compare(abundance.get(a), abundance.get(b));
				}
			}
		);

		sortedAbundance = new double[count];
		sortedMass = new double[count];
		cumulative = new double[count];
		sortedCompStr = new String[count];

		double pSum = 0;
		for (int i = 0; i < count; i++) {
			//  process data in order defined by sortedAbundance
			sortedAbundance[i] = this.abundance.get(order[i]);
			pSum += sortedAbundance[i];
			sortedMass[i] = this.mass.get(order[i]);
			cumulative[i] = pSum;
			sortedCompStr[i] = compStr.get(order[i]);
		}

		this.mass = null;
		this.abundance = null;
		this.compStr = null;

		int centroidSize = 1 + (int)Math.round(maxM) - (int)Math.round(minM); 
		centroidMass = new double[centroidSize];
		centroidAbundance = new double[centroidSize];
		centroidN = new int[centroidSize];
		for (int q = 0; q < centroidSize; q++) {
			centroidMass[q] = 0;
			centroidAbundance[q] = 0;
			centroidN[q] = 0;
		}

		// count the number of isotopologs needed for coverage and calculate calculate centroid data
		pSum = 0;
		coverageCount = 0;
		for(int i = 0; i < count; i++) {
			pSum += sortedAbundance[i];
			if (pSum < coverage) coverageCount++;   		
			// centroidID is an integer that maps each isotopologue to a bin
			//   centroidID is calculated by rounding off the mass difference between the lowest mass
			//     isotopologue and the current isotopologue
			int centroidID = (int)Math.round(sortedMass[i] - minM);
			centroidAbundance[centroidID] += sortedAbundance[i];
			centroidMass[centroidID] +=  sortedMass[i] * sortedAbundance[i];
			centroidN[centroidID]++;
		}
		// add one more isotopolog to coverageCount to exceed coverage 
		coverageCount++;

		// normalize each centroid mass - to calculate meaningful masses, 
		// the sum of the abundances of the components of each centroid must be 1.0
		for(int q = 0; q < centroidSize; q++) {
			centroidMass[q] = centroidMass[q] / centroidAbundance[q];
		}

		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < ec.coefficient.length; i++) if (ec.coefficient[i] > 0) {
			sb.append(ec.eList.get(i).getSymbol());
			sb.append(ec.coefficient[i]);
			sb.append(" ");
		}
		this.molFormulaStr = new String(sb);

		this.briefAbundance = new double[coverageCount];
		this.briefMass = new double[coverageCount];
		this.briefCompStr = new String[coverageCount];
		this.briefCumulative = new double[coverageCount];
		
		for (int i = 0; i < coverageCount; i++) {
			this.briefAbundance[i] = this.sortedAbundance[i];
			this.briefMass[i] = this.sortedMass[i];
			this.briefCompStr[i] = this.sortedCompStr[i];
			this.briefCumulative[i] = this.cumulative[i];
		}
		
		if (verbose) {
			showInfo();
		}

		return count;

	}   // end generateIsotopeDistribution
	
	/**
	 * fetches the abundances of all of the isotopologs identified in the simulation.
	 * @return the sorted array of isotope abundances
	 */
	public double[] getAbundances() {
		return this.sortedAbundance;
	}

	/**
	 * fetches the most abundant isotopolog abundances.
	 * @return an abbreviated, sorted array contaning the abundances of the most abundant isotopologs,
	 * which together satisfy the isotopolog coverage parameter. 
	 */
	public double[] getBriefAbundances() {
		return this.briefAbundance;
	}


	/**
	 * fetches the abundance of the ith most abundant isotopolog
	 * @param i the index of the isotopolog
	 * @return the abundance of the ith most abundant isotopolog
	 */
	public double getAnAbundance(int i) {
		return this.sortedAbundance[i];
	}

	/**
	 * fetches the masses of all of the isotopologs identified in the simulation.
	 * @return the sorted array of isotope masses
	 */	
	public double[] getMasses() {
		return sortedMass;
	}

	/**
	 * fetches the most abundant isotopolog masses.
	 * @return an abbreviated, sorted array contaning the masses of the most abundant isotopologs,
	 * which together satisfy the isotopolog coverage parameter. 
	 */
	public double[] getBriefMasses() {
		return briefMass;
	}

	/**
	 * fetches the mass of the ith most abundant isotopolog
	 * @param i the index of the isotopolog
	 * @return the mass of the ith most abundant isotopolog
	 */
	public double getAMass(int i) {
		return this.sortedMass[i];
	}
	
	/**
	 * fetches the number of isotopologs that were identified in the simulation.
	 * @return the number of isotopologs
	 */
	public int getCount() {
		return count;
	}
	
	
	/**
	 * fetches the number of isotopologs that were required to satisfy the coverage parameter.
	 * @return the number of required isotopologs
	 */
	public int getCount2Cover() {
		return coverageCount;
	}
	
	/**
	 * fetches the coverage parameter.
	 * @return the coverage parameter
	 */
	public double getCoverage() {
		return coverage;
	}
	
	/**
	 * fetches the cumulative abundances for all of the isotopologs identified in the simulation.
	 * @return the cumulative abudance array
	 */
	public double[] getCumulatives() {
		return this.cumulative;
	}
	
	/**
	 * fetches the cumulative abundances for the isotopologs that were required to satisfy the coverage parameter.
	 * @return the brief cumulative abudance array
	 */
	public double[] getBriefCumulatives() {
		return this.briefCumulative;
	}

	/** 
	 * fetches the cumulative abundance for the ith most abundant isotopolog.
	 * @param i the index of the isotopolog
	 * @return the cumulative abundance for the isotopolog
	 */
	public double getACumulative(int i) {
		return this.cumulative[i];
	}
	
	/**
	 * fetches an array of Strings specifying the isotopic compositions of the isotopologs.
	 * This array is sorted according to the abundance of the isotopologs.
	 * @return an array specifying the isotopic compositions of the isotopologs
	 */
	public String[] getCompositions() {
		return (String[])this.compStr.toArray();
	}
	
	/**
	 * fetches an abbreviated array of Strings specifying the isotopic compositions of those isotopologs
	 * that were required to satisfy the coverage parameter.
	 * @return the abbreviated cumulative abudance array
	 */
	public String[] getBriefCompositions() {
		return this.briefCompStr;
	}


	/** 
	 * fetches the String representation of the isotopic composition of the ith most abundant isotopolog.
	 * @param i the index of the isotopolog
	 * @return the String representation of the isotopolog
	 */
	public String getAComposition(int i) {
		return this.compStr.get(i);
	}
	
	/**
	 * fetches the number of centroid peaks assembled from the complete set of isotopologs.
	 * @return the number of centroid peaks
	 */
	public int getNumberOfCentroids() {
		return centroidAbundance.length;
	}
	
	/**
	 * fetches an array specifying the centroid masses of the assembled centroid peaks.
	 * @return the centroid mass array
	 */
	public double[] getCentroidMasses () {
		return centroidMass;
	}
	
	
	/**
	 * fetches an array specifying the number of isotopomers used to calculate each centroid 
	 * @return an array specifying the isotopomer count for each centroid
	 */
	public int[] getCentroidN () {
		return centroidN;
	}

	
	/**
	 * fetches the mass of the ith centroid peak.
	 * @param i the index of the centrod peak
	 * @return the mass of the centroid peak
	 */
	public double getACentroidMass(int i) {
		return this.centroidMass[i];
	}

	/**
	 * fetches an array specifying the abundances of the assembled centroid peaks.
	 * @return the centroid peak abundance array
	 */	
	public double[] getCentroidAbundances () {
		return centroidAbundance;
	}

	/**
	 * fetches the abundance of the ith centroid peak.
	 * @param i the index of the centrod peak
	 * @return the abundance of the centroid peak
	 */	
	public double getACentroidabundance(int i) {
		return this.centroidAbundance[i];
	}



	/**
	 * generates a spectrum whose range is based on the span of isotopolog masses and the charge. 
	 * The signal peak width and gap between data points are specified as arguments.
	 * @param abundance an array of isotopolog abundances
	 * @param mass an array of isotopolog masses, with one-to-one correspondence to the abundance array
	 * @param z the electronic charge of the ions
	 * @param gStep the distance in m/z units between simulated spectral data points
	 * @param peakWidth the peak width at half maximum (FWHM) of the gaussian line shape simulation for each isotopolog
	 * @param yThreshold the minimum signal that is returned in the spectral simulation; lower signal values are returned as zeros
	 * @return an ArrayList containing an array of m/z values and a corresponding array of simulated signal intensities
	 */
	public ArrayList<double[]> generateSpectrum (double[] abundance, double[] mass, int z, double gStep,
			double peakWidth, double yThreshold) {
		
		// creat an array of m/z values appropriate for the istopolog array
		double upperLimit = 0.0;
		double lowerLimit = 1e9;
		for (int j = 0; j < mass.length; j++) {  // j indexes the isotopomers
			// calculate limits for the m/z array
			upperLimit = Math.max(upperLimit, mass[j]/z);
			lowerLimit = Math.min(lowerLimit, mass[j]/z);
		}

		upperLimit = Math.ceil(upperLimit) + 1;
		lowerLimit = Math.floor(lowerLimit) - 1;
		
		return generateSpectrum(abundance, mass, z, gStep, lowerLimit, upperLimit, peakWidth, yThreshold);
	}

	
	/**
	 * generates a spectrum whose range is explicitly specified as a lower limit and an upper limit. 
	 * The signal peak width and gap between data points are specified as arguments.
	 * @param abundance an array of isotopolog abundances
	 * @param mass an array of isotopolog masses, with one-to-one correspondence to the abundance array
	 * @param z the electronic charge of the ions
	 * @param lowerLimit the lower limit of the m/z range for the simulation
	 * @param upperLimit the upper limit of the m/z range for the simulation
	 * @param gStep the distance in m/z units between simulated spectral data points
	 * @param peakWidth the peak width at half maximum (FWHM) of the gaussian line shape simulation for each isotopolog
	 * @param yThreshold the minimum signal that is returned in the spectral simulation; lower signal values are returned as zeros
	 * @return an ArrayList containing an array of m/z values and a corresponding array of simulated signal intensities
	 */
	public ArrayList<double[]> generateSpectrum (double[] abundance, double[] mass, int z, double gStep,
			double lowerLimit, double upperLimit, double peakWidth, double yThreshold) {
	
		// calculate the number of data points in the spectrum and generate the m/z array
		int points = (int)Math.ceil((upperLimit - lowerLimit) / gStep);
		double[] mz = new double[points];
		for (int i = 0; i < points; i++) mz[i] = lowerLimit + i * gStep;  

		return generateSpectrum(abundance, mass, mz, z, peakWidth, yThreshold);
	}  // end generateSpectrum method

	
	/**
	 * generates a spectrum based on lists of isotopolog abundances and masses and the charge, with
	 * m/z values supplied as an array. 
	 * The signal peak width is specified as an argument.
	 * @param abundance an array of isotopolog abundances
	 * @param mass an array of isotopolog masses, with one-to-one correspondence to the abundance array
	 * @param mz an array of m/z values for which the spectral signal intensities are to be simulated
	 * @param z the electronic charge of the ions
	 * @param peakWidth the peak width at half maximum (FWHM) of the gaussian line shape simulation for each isotopolog
	 * @param yThreshold the minimum signal that is returned in the spectral simulation; lower signal values are returned as zeros
	 * @return an ArrayList containing an array of m/z values and a corresponding array of simulated signal intensities
	 */
	public ArrayList<double[]> generateSpectrum (double[] abundance, double[] mass, double[] mz, int z,
			double peakWidth, double yThreshold) {

		ArrayList<double[]> spectrum = new ArrayList<double[]>(2);  // list containing mz and abundance
		double y;  // current value of y  (abundance)
		double deltaSqr;  // square of difference    mz[j] - x
		double ef;  // exponential factor used for gaussian peak generation
		double normalizer;
		double[] mzList = new double[mass.length];  // mzList is a list of m/z values for the isotopologs
		for (int j = 0; j < mass.length; j++) mzList[j] = mass[j]/z;
		//  calculate m/z for each isotopomer and set up m/z limits for spectrum


		double[] signal = new double[mz.length];   

		// precalculate parameters for gaussian line:
		//   sigma is 0.4247 * FWHM
		double sigma = 0.4247 * peakWidth;
		//   exponential factor is -0.5 / (sigma^2)
		ef = -0.5 / (sigma * sigma);
		normalizer = 1 / (sigma * Math.sqrt(2 * Math.PI));
		

		// calculate and save the spectrum
		for (int i = 0; i < mz.length; i++) {
			// calculate each data point in the spectrum, traversing to rightLimit
			y = 0;  // y is the abundance value of the current spectral data point
			for (int j = 0; j < abundance.length; j++) {  // j indexes the isotopomers
				deltaSqr = (mzList[j] - mz[i]) * (mzList[j] - mz[i]);
				// y is a sum of probability-weighted gaussian terms
				y += abundance[j] * Math.exp(ef * deltaSqr);
			}
			if (y < yThreshold) y = 0;  // don't bother returning infintesimal y values
			signal[i] = y;
		}
		
		// normalization could be done in the last loop, but it's more efficient to do
		//   the multiplication only once per m/z value, as in the next line
		for (int i = 0; i < mz.length; i++) signal[i] *= normalizer;

		spectrum.add(mz);
		spectrum.add(signal);

		return spectrum;
	} // end generateSpectrum method
    

	/**
	 * fetch the average (chemical) mass of the molecule.
	 * @return the average (chemical) mass of the molecule
	 */
    public double getAvgMass () {
  	     return this.avgMass;
  	  }


	/**
	 * fetch the monoisotopic mass of the molecule.
	 * @return the monoisotopic mass of the molecule
	 */
    public double getMonoisotopicMass () {
       return this.monoisotopicMass;
 	  }


}
