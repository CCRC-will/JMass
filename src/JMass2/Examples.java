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
import java.util.ArrayList;


/**
 * This is an example application to show how to use the JMass2 API.  The command-line arguements are as follows:
 * <br> args[0] the name of an xml file specifying the monomers in the MonomerTable
 * <br> args[1] the name of an xml file specifying the elements in the TableOfIsotopes
 * <br> args[2] the name of an xml file specifying the configuration of the MassSearcher
 * @param args command line arguments 
 */

public class Examples  {

	/**
	 * An instance of IsotopeDistributor used to illustrate the API
	 */
	private static IsotopeDistributor dist;
	
	/**
	 * An instance of TableOfIsotopes used to illustrate the API
	 */
	private static TableOfIsotopes toi;
	
	/**
	 * An instance of MonomerTable used to illustrate the API
	 */	
	private static MonomerTable mt;
	
	/**
	 * An instance of MassSearcher used to illustrate the API
	 */	
	private static MassSearcher searcher;

	/**
	 * This is an example application to show how to use the JMass2 API.
	 * <br> args[0] the name of an xml file specifying the monomers in the MonomerTable
	 * <br> args[1] the name of an xml file specifying the elements in the TableOfIsotopes
	 * <br> args[2] the name of an xml file specifying the configuration of the MassSearcher
	 * @param args command line arguments 
	 * 
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Examples examples = new Examples(args[0], args[1], args[2]);
		
		String pageDivider = new String("**********************************************************************************\n");

		System.out.println("\t\t\t\tpage 1");
		String table = toi.prettyTable();
		System.out.print(table);

		double[] a = new double[3];
		a[0] = 0.991;
		a[1] = 1 - a[0];
		a[2] = 0;
		int b = 2;

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 2\n");
		toi.setElementAbundances("*C", a, b);
		System.out.println("\n\nChanged *C enrichment to " + 100*a[0] + "% 13C");

		a = new double[3];
		a[0] = 0.888;
		a[1] = 1 - a[0];
		a[2] = 0;
		toi.setElementAbundances("*O", a, b);
		System.out.println("Changed *O enrichment to " + 100*a[0] + "% 18O");

		a = new double[3];
		a[0] = 0.997;
		a[1] = 1 - a[0];
		a[2] = 0;
		toi.setElementAbundances("*N", a, b);
		System.out.println("Changed *N enrichment to " + 100*a[0] + "% 15N\n\n");

		table = toi.prettyTable();
		System.out.print(table);
		
		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 3\n");

		System.out.println("\n\nExport of Table as tab-separated text.  This can be copied and pasted into a spread sheet, where it will be easier to read.\n**********\n");
		table = toi.exportTable();
		System.out.print(table);
		
		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 4\n");

		System.out.println("\n\nExport of Table as XML.  This can be used to generate a new configuration file.\n**********\n");		
		table = toi.exportXML();
		System.out.print(table);
		
		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 5\n");

		String[] eNames = toi.getElementNames();
		String[] eSymbols = toi.getElementSymbols();
		System.out.println("\n\n\nIllustrate getElementNames and getElementSymbols methods\n");
		for (int i=0; i<eSymbols.length; i++) System.out.println(eSymbols[i] + " represents " + eNames[i]);


		double cutoff = 1e-5;
		double coverage = 0.996;
		boolean verbose = true;

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 6\n");
		System.out.println("\n\nIllustrate calculation of isotopic distribution for various representations of the elemental compostion\n");
		String elementCompString = new String("0 0 0 0 24 0 0 0 0 12 0 12");
		System.out.println("\nInput is a String:   \"" + elementCompString +"\"");
		dist.generateIsotopologDistribution(elementCompString, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 7\n");
		int stoichiometry[] = {1, 0, 0, 2, 7, 0, 0, 0, 0, 3, 0, 1, 2};
		System.out.print("\nInput is an array of int:   {");
		for (int i=0; i<stoichiometry.length; i++) {
			System.out.print(stoichiometry[i]);
			if (i < stoichiometry.length-1) System.out.print(", ");
		}
		System.out.println("}");
		dist.generateIsotopologDistribution(stoichiometry, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 8\n");
		elementCompString = "H 1 O 1";
		System.out.println("\nInput is the String:   \"" + elementCompString + "\"");
		dist.generateIsotopologDistribution(elementCompString, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 9\n");
		String[] elementSymbol = {"C", "H", "O"};
		int[] stoichiometry2 = {6, 10, 5};
		System.out.print("\nInput is an array of String :    {");
		for (int i=0; i<elementSymbol.length; i++) {
			System.out.print("\"" + elementSymbol[i] + "\"");
			if (i < elementSymbol.length-1) System.out.print(", ");
		}
		System.out.print("}\nand an array of int:  {");
		for (int i=0; i<stoichiometry2.length; i++) {
			System.out.print(stoichiometry2[i]);
			if (i < stoichiometry2.length-1) System.out.print(", ");
		}
		System.out.println("}");
		

		dist.generateIsotopologDistribution(elementSymbol, stoichiometry2, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 10\n");
		String[] stoichiometryString = {"C 156", "H 306", "O 140", "N 6", "Na 3"};
		System.out.print("\nInput is an array of the following Strings:   {");
		for (int i=0; i<stoichiometryString.length; i++) {
			System.out.print("\"" + stoichiometryString[i] + "\"");
			if (i < stoichiometryString.length-1) System.out.print(", ");
		}
		System.out.println("}");
		dist.generateIsotopologDistribution(stoichiometryString, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 11\n");
		// generate a spectrumCheck corresponding to the isotopolog distribution just calculated
		double[] abundance = dist.getAbundances();
		double[] mass = dist.getMasses();
		int z = 3;
		double gStep = 0.01;
		double peakWidth = 0.1;
		double yThreshold = 0.0001;
		ArrayList<double[]> spectrum = dist.generateSpectrum(abundance, mass, z, gStep, peakWidth, yThreshold);
		double[] mz = spectrum.get(0);
		double[] signal = spectrum.get(1);
		System.out.println("Generate a Spectrum using the isotopologs listed on the last page, with charge of " +
				z + ", a gap of " + gStep + " m/z units between points and a peak width of " + peakWidth + " m/z units.\n");
		System.out.println("This can by copied to a spread sheet to generate a plot or the arrays containing this information" +
				"can be passed to another method for plotting or analysis.\n");
		for (int i = 0; i < mz.length; i++) 
			System.out.format("%9.4f%9.4f\n", mz[i], signal[i]);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 12\n");
		System.out.println("Tab-separated String representation of the MonomerTable\n\n");
		String monomerTypes = mt.exportTypes();
		System.out.println(monomerTypes);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 13\n");
		System.out.println("XML String representation of the MonomerTable\n\n");
		String monomerXML = mt.exportXML();
		System.out.println(monomerXML);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 14\n");
		System.out.println("Illustration of ElementComposition manipulations");

		elementCompString = new String("H 2 O 2");
		ElementComposition mfTest = new ElementComposition(toi, elementCompString);
		System.out.println("\nInitial composition is: \"" + mfTest.getElementalCompositionString() + "\"");
		elementCompString = new String("H 1 C 9");
		ElementComposition mfTest2 = new ElementComposition(toi, elementCompString);
		System.out.println("\nAtoms to be added: \"" + mfTest2.getElementalCompositionString() + "\"");
		mfTest.addCompositionAtoms(mfTest2);
		System.out.println("\nComposition after adding the atoms is: \"" + mfTest.getElementalCompositionString() + "\"");

		String atomString = new String("O");
		int n = 55;
		System.out.println("\nAdditional atoms to be added: \"" + atomString + " " + n + "\"");
		mfTest.addAtoms(atomString, n);
		System.out.println("\nComposition after adding more atoms is: \"" + mfTest.getElementalCompositionString() + "\"");

		atomString = new String("O");
		n = 7;
		System.out.println("\nAtoms to be removed: \"" + atomString + " " + n + "\"");
		mfTest.removeAtoms(atomString, n);
		System.out.println("\nComposition after removing atoms is: \"" + mfTest.getElementalCompositionString() + "\"");

		System.out.println("\nInitial composition is: \"" + mfTest.getElementalCompositionString() + "\"");
		mfTest2 = new ElementComposition(toi, elementSymbol, stoichiometry2);
		System.out.println("\nAtoms to be removed: \"" + mfTest2.getElementalCompositionString() + "\"");
		mfTest.removeCompositionAtoms(mfTest2);
		System.out.println("\nComposition after removing the atoms is: \"" + mfTest.getElementalCompositionString() + "\"");

		atomString = new String("C");
		n = 4;
		System.out.println("\nAdditional atoms to be removed: \"" + atomString + " " + n + "\"");
		int r = mfTest.removeAtoms(atomString, n);
		System.out.println("\nComposition after removing more atoms is: \"" + mfTest.getElementalCompositionString() + "\"");
		if (r < 0) System.out.println("Tried to remove " + n + " " + atomString + " atoms when fewer were present.  The result is zero " + atomString + " atoms");


		elementCompString = new String("C 67 H 121 O 19");
		mfTest = new ElementComposition(toi, elementCompString);
		System.out.print("\nCopying ElementComposition using same TableOfIsotopes " + toi + "\nInitial Molecular Formula is  ");
		System.out.println(mfTest + " : \"" + mfTest.getElementalCompositionString() + "\" with monoisotopic mass " + mfTest.getMonoisotopicMass());
		ElementComposition mfCopy = mfTest.copy();
		System.out.print("Copied ElementComposition is  ");
		System.out.println(mfCopy + " : \"" + mfCopy.getElementalCompositionString() + "\" with monoisotopic mass " + mfCopy.getMonoisotopicMass());


		TableOfIsotopes toi2 = new TableOfIsotopes("isotopes-5.xml");
		System.out.print("\nCopying ElementComposition using different TableOfIsotopes " + toi2 + "\nInitial Molecular Formula is  ");
		System.out.println(mfTest + " : \"" + mfTest.getElementalCompositionString() + "\" with monoisotopic mass " + mfTest.getMonoisotopicMass());
		mfCopy = mfTest.copy(toi2);
		System.out.print("Copied ElementComposition is  ");
		System.out.println(mfCopy + " : \"" + mfCopy.getElementalCompositionString() + "\" with monoisotopic mass " + mfCopy.getMonoisotopicMass());

		
		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 15\n");
	
		System.out.println(searcher.exportConfig());

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 16\n");
		double target = 1562.5;
		double tolerance = 0.5;
		
		System.out.println("Illustrate a mass search - target is " + target + " and tolerance is " + tolerance + "\n");

		int count = searcher.findCompositions(target, tolerance, 0);

		MonomerComposition[] hits = searcher.getHits();
		double[] errs = searcher.getErrs();;

		System.out.println("The intermediate results are shown on page 18.");

		System.out.println("\n\nUse the first result to calculate a set of isotopologs");

		elementCompString = searcher.getElementCompositionString(hits[0]);
		dist.generateIsotopologDistribution(elementCompString, cutoff, coverage, verbose);

		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 17\n");
		// generate a spectrumCheck corresponding to the isotopolog distribution just calculated
		abundance = dist.getAbundances();
		mass = dist.getMasses();
		z = 2;
		gStep = 0.01;
		peakWidth = 0.05;
		yThreshold = 0.0001;
		spectrum = dist.generateSpectrum(abundance, mass, z, gStep, peakWidth, yThreshold);
		mz = spectrum.get(0);
		signal = spectrum.get(1);
		
		System.out.println("Generate a Spectrum using the isotopologs listed on the last, with charge of " +
				z + ", a gap of " + gStep + " m/z units between points and a peak width of " + peakWidth + " m/z units.\n");
		System.out.println("This can by copied to a spread sheet to generate a plot or the arrays containing this information" +
				"can be passed to another method for plotting or analysis.\n");

		
		for (int i = 0; i < mz.length; i++) 
			System.out.format("%9.4f%9.4f\n", mz[i], signal[i]);


		System.out.println("\n\n" + pageDivider + "\t\t\t\tpage 18\n");
		System.out.println("\n\nResults of search for monoisotopic mass " + target + " with tolerance " + tolerance);
		DecimalFormat f = new DecimalFormat("0.000 ");
		for (int i = 0; i < count; i++) {
			MonomerComposition theHit = hits[i];

			System.out.println("\nMatch " + (i+1) + ":   error: " + f.format(errs[i]) +
					" \nmonoisotopic mass: " + f.format(searcher.getMonoisotopicMass(theHit)) +
					"\taverage mass: " + f.format(searcher.getAverageMass(theHit)) +
					"\nelemental composition: \"" + searcher.getElementCompositionString(theHit) + "\"" +
					"\nresidue composition: \"" + theHit.getBriefMonomerCompositionString() + "\"");
		}

	}



	/**
	 * constructs an instance of the Examples application.
	 * @param monomerFile the name of an xml file specifying the monomers in the MonomerTable
	 * @param isotopeFile the name of an xml file specifying the elements in the TableOfIsotopes
	 * @param configXML the name of an xml file specifying the configuration of the MassSearcher
	 * @see MonomerTable
	 * @see TableOfIsotopes
	 * @see MassSearcher
	 */
	public Examples(String monomerFile, String isotopeFile, String configXML) {

		/*
		String monosaccFile = new String("monomers.xml");
		String isotopeFile = new String("isotopes-5.xml");
		String configXML = new String("msConfig.xml");
		*/
		// System.out.println(XMLStr);


		// this.toi = new TableOfIsotopes(isotopeFile);
		toi = new TableOfIsotopes(isotopeFile);
		dist = new IsotopeDistributor(toi);
		mt = new MonomerTable(monomerFile, toi);



		// can use file or explicit string for xml configuration data

		/*
	    //  the following specifies an explicit xml string for the configuration of the searcher
	    String configXML = new String("" +
"<gmass_config tolerance='100.0' type='0' z_min='1' z_max='4' no_adduct='true' h_ion='true' na_ion='true' k_ion='true'>" +
"	<residue_list derivative='Me' end_structure='alditol'>" +
"		<monosaccharide abbrev='Pent' minimum_number='0' maximum_number='5'/>" +
"		<monosaccharide abbrev='dHex' minimum_number='0' maximum_number='2'/>" +
"		<monosaccharide abbrev='Hex' minimum_number='2' maximum_number='8'/>" +
"		<monosaccharide abbrev='HexA' minimum_number='0' maximum_number='2'/>" +
"		<monosaccharide abbrev='MeHexA' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='HexNAc' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='NeuNAc' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='NeuNGc' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='Hex15NAc' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='Neu15NAc' minimum_number='0' maximum_number='0'/>" +
"		<monosaccharide abbrev='Neu15NGc' minimum_number='0' maximum_number='0'/>" +
"	</residue_list>" +
"</gmass_config>");
		 */


		//  the following specifies an xml file that holds the configuration of the searcher
		


		searcher  = new MassSearcher(mt, configXML);

	}
}

