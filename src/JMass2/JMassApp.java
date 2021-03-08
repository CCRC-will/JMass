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


import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.Color;

import java.text.DecimalFormat;


/**
 * 
 * The JMassApp class implements a software application that utilizes the JMass API.
 * The user can search for an oligomer structure that matches a specified (monoisotopic
 * or average) m/z value, given a range for the stoichiometric coefficient for each  
 * monomer. The user can also
 * specify a monomer composition of an oligomer by entering the number of copies of
 * each monomer type from a list on the left side of the application.  
 * <p>The chemical
 * properties of these monomers are specified in an (XML format) input file.  
 * The user also sets parameters that define chemical modification of the oligomer and parameters that 
 * define the ionization of the oligomer during mass spectrometry.  
 * <p>Alternatively,
 * the user can specify the elemental composition of the oligomer or ion by entering
 * a molecular formula.  
 * <p>The software simulates the isotopolog distribution of the structure defined by these
 * parameters.  The results are shown in a text box. These include a list of isotopologs
 * and their relative abundances and a list of centroid peaks that are assembled by combining 
 * these isotopologs.  In addition, the mass spectrum for the isotopolog distribution is
 * simulated and displayed in another panel.
 * <p>The isotoplog distribution is calculated using an instance of TableOfIsotopes, which
 * is populated using chemical information contained in an (XML format) input file.
 *
 */
@SuppressWarnings("serial")
public class JMassApp extends Frame implements WindowListener, ActionListener {
	
	

	/**
	 * an instance of the TableOfIsotopes class that provides information about elements and pseudoelements and the 
	 * distribution of isotopes for each.
	 */
	TableOfIsotopes toi;
	
	/**
	 * an instance of the IsotopeDistributor class that provides methods for calculating isotoplog distributions
	 * and simulating mass spectral segments.
	 */
	IsotopeDistributor isoDist;
	
	/**
	 * the fraction of the total theoretical isotopolog population that is reported in the final results of an isotopolog
	 * distribution calculation.
	 */
	double coverage = 0.999;
	
	/**
	 * the minimum isotopolog fractional abundance that is considered in the isotopolog distribution calculation.
	 * Increasing the value of minAbundance makes the calculation more expensive while providing relative
	 * abundances and identities of rare isotopologs.
	 */
	double minAbundance = 0.000001;
	
	/**
	 * an instance of the MonomerTable class that defines the names, elemental compositions and derivatization 
	 * properties of the monomers to be used in the generation of an olgomer from its monomers.  mTab is
	 * populated with information provided as an (XML format) input file.
	 */
	MonomerTable mTab;
	
	/**
	 * an array of derivatives that can be used to chemically modify the monomers in the mTab. 
	 */
	MonomerTable.Derivative[] derList;
	
	/**
	 * an array of Strings that specify the abbreviations of the derivatives in the derList.
	 */
	String[] derivStr;
	
	/**
	 * an array Strings that specify the full names of the derivatives in the derList.
	 */
	String[] derivNameStr;
	
	/**
	 * a widget that allows the user to select derivatives from the derList.
	 */
	JComboBox derivBox;
	
	/**
	 * an array of instances of the class Element that correspond to pseudoelements, which 
	 * are chemically idental to real elements, but which have non-natural isotope abundances.  
	 */
	TableOfIsotopes.Element[] pseudoElements;

	/**
	 * an array of instances of the class MonomerType, which the user can access to specify
	 * a monomer composition for an olligomer. 
	 */
	MonomerTable.MonomerType[] monomers;
	
	/**
	 * an array of labels indicating the names of the monomers that can be combined to form an oligomer.
	 */
	JLabel[] mNameLab;
	
	/**
	 * an array of input fields in which the user enters the minimum number of each residue for a mass search.
	 */
	JTextField[] nMin;
	
	/**
	 * an array of input fields in which the user enters the maximum number of each residue for a mass search.
	 */
	JTextField[] nMax;

	/**
	 * an array of input fields in which the user enters the stoichiometric coefficients defining a monomer composition.
	 */
	JTextField[] comps;

	/**
	 * an input field into which the user enters a string specifying the target mass to be searched.
	 */
	JTextField targetField = new JTextField("342.0", 5);


	/**
	 * an input field into which the user enters a string specifying the target mass tolerance.
	 */
	JTextField toleranceField = new JTextField("1.0", 3);

	/**
	 * an input field into which the user enters a string specifying an elemental composition.
	 * The input must have a specific format, as in the following example:  "C 6 H 12 O 6".
	 */
	JTextField compField = new JTextField("C 6 H 12 O 6", 24);
	
	/**
	 * a button that, when clicked, initiates calculation of the isotopolog disribution corresponding
	 * to a molecular formula entered by the user. 
	 */
	JButton compGo = new JButton("Evaluate Formula");
	
	/**
	 * a label instructing how to simulate the results of a mass search.
	 */
	JLabel searchLabel = new JLabel("<html><center>Choose a result from the list below to<br>put its ionic formula in the formula box</center></html>");

	/**
	 * the initial String array to display in the cearchChoice JComboBox.
	 */
	String[] searchChoiceString = {"        No Search Results Available Yet        "};
	
	/**
	 * a widget that allows user to select a search result for simulation.
	 */
	JComboBox searchChoice = new JComboBox(searchChoiceString);
	

	/**
	 * an instance of the class MassSearcher that calculates the elemental composition of the oligomer
	 * or oligomer ion specified by the user.
	 */
	MassSearcher searcher;

	/**
	 * a graphical representation of the centroided peaks assembled from the isotopologs.
	 */
	CentroidSpectrum spectrumC = new CentroidSpectrum();
	
	/**
	 * a graphical representation of the profile-mode spectrum simulated using the isotopologs
	 */
	ProfileSpectrum spectrumP = new ProfileSpectrum(900, 50, 300, 50);
	
	/**
	 * a string describing the user interface 
	 */
	String info = new String("Enter the number of residues of each type in the list on the left, " +
			"set other parameters, then click \"Evaluate Monomer Composition\".\n" +
			"Alternatively, just enter a chemical formula in the box " +
			"below and click \"Evaluate Formula\".  The formula must " +
			"be in the format \"C 6 H 12 O 6\".");

	/**
	 * a label indicating that the step size for the spectral simulation should be entered into the adjacent box.
	 */
	JLabel stepLab = new JLabel("Step Size:");
	
	/**
	 * the field into which the step size is entered.
	 */
	JTextField stepField = new JTextField("0.0050");
	
	/**
	 * a label indicating that the peak width for the spectral simulation should be entered into the adjacent box.
	 */	
	JLabel pwLab = new JLabel("Peak Width:");	

	/**
	 * the field into which the peak width is entered.
	 */
	JTextField pwField = new JTextField("0.030");

	
	/**
	 * a label indicating that the adduct responsible for ionization should be selected from the adjacent box.
	 */	
	JLabel adductLab = new JLabel("Adduct:");
	
	/**
	 * an array of strings corresponding to the chemical species that can be selected as ionization adducts.
	 */
	String[] adductStr = { "none", "H", "Na", "K"};
	
	/** 
	 * the widget used to select the ionization adduct.
	 */
	JComboBox adductBox = new JComboBox(adductStr);
	
	/**
	 * an array of strings corresponding to the format of the target mass (monoisotopic or average).
	 */
	String[] targetFormatStr = { "monoisotopic m/z", "average m/z"};
	
	/** 
	 * the widget used to select the format of the target mass (monoisotopic or average).
	 */
	JComboBox targetFormatBox = new JComboBox(targetFormatStr);

	/**
	 * a label indicating that the ionic charge should be selected from the adjacent box.
	 */
	JLabel chargeLab = new JLabel("Charge:");
	
	/**
	 * an array of Strings specifying the charges that can be selected for the ion.
	 */
	String[] chargeStr = { "1", "2", "3", "4"};
	
	/**
	 * the widget used to select the charge of the ion.
	 */
	JComboBox chargeBox = new JComboBox(chargeStr);

	/*
	JLabel scarText = new JLabel("   Scars:");
	String[] scarStr = { "0", "1", "2", "3"};
	JComboBox scarBox = new JComboBox(scarStr);


	JLabel fText = new JLabel("   Main Fragment:");
	String[] fStr = { "none", "B", "C", "Y", "Z"};
	JComboBox fBox = new JComboBox(fStr);
	 */

	/**
	 * an array of Strings specifying the pseudoelements that may be contained in the oligomer.
	 */
	String[] pseudoElementStr;

	/**
	 * a label indicating that the isotopic enrichments for pseudoelements should be entered 
	 * into the adjacent boxes.
	 */
	JLabel isoLab = new JLabel("Isotopic Enrichments: (0.0 - 1.0)   ");
	
	/**
	 * an array of labels indicating the pseudoelement whose isotopic enrichment is to be specified.
	 */
	JLabel[] enrichmentLab = new JLabel[4];
	
	/**
	 * an array of text fields into which the user enters isotopic enrichment parameters.
	 */
	JTextField[] enrichmentField = new JTextField[4];
	
	/**
	 * a label indicating that the adjacent box holds choices for the chemical derivative of the oligomer.
	 */
	JLabel derivLab = new JLabel("Derivative:");

	/**
	 * a label indicating that the adjacent box holds choices for the end structure of the oligomer.
	 */
	JLabel endLab = new JLabel("End Structure:");
	
	/**
	 * an array of strings specifying the reducing-end structures that can be selected.
	 */
	String[] endStr = { "reducing", "alditol", "derivatized"};
	
	/**
	 * a pull-down selection widget to select the reducing-end structure.
	 */
	JComboBox endBox = new JComboBox(endStr);

	/**
	 * a checkbox to specify whether the simulated spectrum will be presented in the output box.
	 */
	JCheckBox spectrumCheck = new JCheckBox("List Spectrum");
	
	/**
	 * a checkbox to specify whether the centroided isotoplog peaks will be presented in the output box.
	 */
	JCheckBox centroidCheck = new JCheckBox("List Centroids");
	
	/**
	 * a checkbox to specify whether the isotopolog list will be presented in the output box.
	 */
	JCheckBox isotopologCheck = new JCheckBox("List Isotopologs");
	
	/**
	 * a checkbox to specify whether spectral data will be imported from the output box.
	 */
	JCheckBox importMZCheck = new JCheckBox("Import Spectral Data");

	/**
	 * a button that initiates calculation of the isotopomer distribution for the ion
	 * specified as a  collection of monomers (which may be derivatized),
	 * the selected reducing end structure and the ionization adduct that is specified.
	 */
	JButton goB = new JButton("Evaluate Monomer Composition");
	
	/**
	 * a button that initiates calculation of a search for a monomer composition
	 * corresponding to a specified target mass.  Monomers may be derivatized.
	 * The selected reducing end structure and the ionization adduct can also be specified.
	 */
	JButton searchB = new JButton("Search");

	/**
	 * a button that causes help text to be inserted into the output box.
	 */
	JButton hlp = new JButton("Help");
	
	/**
	 * a label that holds the elemental composition of the molecule or ion being simulated.
	 */
	JLabel compLab = new JLabel("<html><center>Elemental Composition (e.g, <font color='red'>C<sub>6</sub>H<sub>12</sub>O<sub>6</sub></font>) and Monoisotopic Mass Are Reported In This Space.&nbsp;&nbsp;</center></html>", SwingConstants.CENTER);
	
	/**
	 * a text box that holds the ascii output of the calculation/simulation.
	 */
	JTextArea outText = new JTextArea(18, 38);
	
	/**
	 * an array of m/z values that is imported for comparison to the simulated spectrum.
	 */
	double[] extMZ = {1.0};

	/**
	 * an array of abundance values that is imported for comparison to the simulated spectrum.
	 */
	double[] extAbund = {1.0};




	/**
	 * The main method called when the instance of JMassApp is invoked from the command line.  
	 * @param args the (literal string) arguments used when the instance of JMassApp is invoked.
	 * These include (in order):
	 * <br> the XML file specifying the contents of the TableOfIsotopes
	 * <br> the XML file specifying the contents of the MonomerTable
	 * <br> the XML file specifying the configuration of the MassSearcher
	 * <br> the coverage (optional, defaults to 0.999)
	 * <br> the minimum isotopolog abundance for consideration in the isotopolog distribution calculation 
	 * (optional, defaults to 0.000001).
	 */
	public static void main(String[] args) {
		String cf = "msConfig.xml";
		if (args.length > 2) cf = args[2];
		String mtf = "monomers.xml";
		if (args.length > 1) mtf = args[1];
		String tif = "isotopes-5.xml";
		if (args.length > 0) tif = args[0];
		JMassApp jma = new JMassApp("MS Simulator", tif, mtf, cf);

		if (args.length > 3) jma.coverage = Double.parseDouble(args[3]);
		if (args.length > 4) jma.minAbundance = Double.parseDouble(args[4]);
		// mf.setSize(400, 700);
		// jma.pack();
		// jma.setVisible(true);
	}


	/**
	 * constructs a new instance of the JMassApp class.
	 * @param appTitle the title of the application
	 * @param toiFile the name of an XML file containing the information required
	 * to populate the TableOfIsotpes.
	 * @param mTabStr the name of an XML file containing the information required
	 * to populate the MonomerTable.
	 * @param configStr the name of an XML file containing the information required
	 * to configure the application
	 */
	public JMassApp(String appTitle, String toiFile, String mTabStr, String configStr) {
	     JFrame frame = new JFrame(appTitle);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
		this.toi = new TableOfIsotopes(toiFile);
		this.isoDist = new IsotopeDistributor(toi);
		this.mTab = new MonomerTable(mTabStr, toi);
		this.searcher = new MassSearcher(mTab, configStr);

		this.derList = mTab.getDerivatives();
		this.derivStr = new String[derList.length];
		this.derivNameStr = new String[derList.length];
		for (int i = 0; i < derList.length; i++) {
			derivStr[i] = derList[i].abbrev;
			derivNameStr[i] = derList[i].name;
		}
		this.derivBox = new JComboBox(derivStr);

		pseudoElements = toi.getElements(true);
		pseudoElementStr = new String[pseudoElements.length];
		for (int i = 0; i < pseudoElements.length; i++){
			pseudoElementStr[i] = new String(pseudoElements[i].getSymbol());
		}

		monomers = mTab.getMonomers();

		this.mNameLab = new JLabel[monomers.length];
		this.comps = new JTextField[monomers.length];
		this.nMin = new JTextField[monomers.length];
		this.nMax = new JTextField[monomers.length];
		

		int vGap =  3;
		int hGap = 8;

		frame.setLayout(null);
		Insets insets = frame.getInsets();
		Dimension size;


		int leftMargin = 25 + insets.left;
		int topMargin = 20 + insets.top;
		int hCursor = leftMargin;
		int vCursor = topMargin;
		
		Color searchBackground = new Color(208, 255, 208);
		Color compBackground = new Color(255, 255, 208);
		Color formulaBackground = new Color(192, 255, 255);

		Color searchForeground = new Color(0, 192, 0);
		Color compForeground = new Color(192, 96, 0);
		Color formulaForeground = new Color(0, 0, 192);


		frame.add(targetFormatBox);
		size = targetFormatBox.getPreferredSize();
		targetFormatBox.setBackground(searchBackground);
		targetFormatBox.setToolTipText("Select the format of the target m/z to search");
		targetFormatBox.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(targetField);
		size = targetField.getPreferredSize();
		Dimension boxSize = targetField.getPreferredSize();
		targetField.setBackground(searchBackground);
		targetField.setToolTipText("Enter the target m/z to search");
		targetField.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width;
		
		JLabel pmLabel = new JLabel("<html>&#177;</html>");
		frame.add(pmLabel);
		size = pmLabel.getPreferredSize();
		pmLabel.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;
		
		frame.add(toleranceField);
		size = toleranceField.getPreferredSize();
		toleranceField.setBackground(searchBackground);
		toleranceField.setToolTipText("Enter the tolerance (m/z) of the target m/z");
		toleranceField.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;

		frame.add(searchB);
		size = searchB.getPreferredSize();
		searchB.setBackground(searchBackground);
		searchB.setForeground(searchForeground);
		searchB.setToolTipText("Click to search for monomer compositions matching target value");
		searchB.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor +=size.width + hGap;

		frame.add(goB);
		size = goB.getPreferredSize();
		goB.setBackground(compBackground);
		goB.setForeground(compForeground);
		goB.setToolTipText("Click to calculate spectral features of ion with specified monomer composition");
		goB.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor +=size.width + hGap;

		frame.add(compField);
		size = compField.getPreferredSize();
		compField.setBackground(formulaBackground);
		compField.setToolTipText("Enter Elemental Composition of an ion or molecule");
		compField.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(compGo);
		size = compGo.getPreferredSize();
		compGo.setForeground(formulaForeground);
		compGo.setToolTipText("Click to calculate spectral features of ion or molecule with specified elemental composition");
		compGo.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(hlp);
		size = hlp.getPreferredSize();
		hlp.setBounds(hCursor, vCursor, size.width, size.height);
		
		hCursor = leftMargin;
		vCursor = vCursor + size.height + vGap;

		int fs = 12;

		int[] hAlign = new int[5];
		
		
		// create widths and fields and get their maximun widths
		JLabel minLabel = new JLabel("Min");
		frame.add(minLabel);
		JLabel maxLabel = new JLabel("Max");
		frame.add(maxLabel);
		JLabel resLabel = new JLabel("Residue");
		frame.add(resLabel);
		JLabel nLabel = new JLabel("N");
		frame.add(nLabel);
		int maxBoxWidth = maxLabel.getPreferredSize().width;
		int maxLabelWidth = resLabel.getPreferredSize().width;
		for (int i = 0; i < monomers.length; i++) {
			nMin[i] = new JTextField("0",2);
			frame.add(nMin[i]);
			this.nMin[i].setBackground(searchBackground);
			nMax[i] = new JTextField("0",2);
			this.nMax[i].setBackground(searchBackground);
			frame.add(nMax[i]);
			comps[i] = new JTextField("0",2);
			this.comps[i].setBackground(compBackground);
			frame.add(comps[i]);
			size = comps[i].getPreferredSize();
			maxBoxWidth = Math.max(maxBoxWidth, size.width);
			mNameLab[i] = new JLabel(new String(monomers[i].abbrev), JLabel.LEFT);
			mNameLab[i].setVerticalAlignment(JLabel.CENTER);
			mNameLab[i].setFont(new Font("SansSerif", Font.PLAIN, fs));
			frame.add(mNameLab[i]);
	        size = mNameLab[i].getPreferredSize();
	        maxLabelWidth = Math.max(maxLabelWidth, size.width);
		}
		
		// position headers
		hAlign[0] = leftMargin;
		size = minLabel.getPreferredSize();
		minLabel.setBounds(hAlign[0], vCursor, size.width, size.height);
		
		hAlign[1] = hAlign[0] + maxBoxWidth + hGap;
		size = maxLabel.getPreferredSize();
		maxLabel.setBounds(hAlign[1], vCursor, size.width, size.height);
		
		hAlign[2] = hAlign[1] + maxBoxWidth + hGap;
		size = resLabel.getPreferredSize();
		resLabel.setBounds(hAlign[2], vCursor, size.width, size.height);

		hAlign[3] = hAlign[2] + maxLabelWidth + hGap;
		size = nLabel.getPreferredSize();
		nLabel.setBounds(hAlign[3], vCursor, size.width, size.height);
		
		hAlign[4] = hAlign[3] + maxBoxWidth + hGap;

		// position fields and labels

		int ls = 30;

		vCursor += size.height + vGap;
		for (int i = 0; i < monomers.length; i++) {
			int localVCursor = i * ls + vCursor;
			
	        size = nMin[i].getPreferredSize();
	        nMin[i].setBounds(hAlign[0], localVCursor, size.width, size.height);
	        
	        size = nMax[i].getPreferredSize();
	        nMax[i].setBounds(hAlign[1], localVCursor, size.width, size.height);
	        
	        size = mNameLab[i].getPreferredSize();
	        // Use same vertical size as associated boxes
	        mNameLab[i].setBounds(hAlign[2], localVCursor, size.width, boxSize.height);
	        
			size = comps[i].getPreferredSize();
			comps[i].setBounds(hAlign[3], localVCursor, size.width, size.height);

		}

	
		// move down one line
		hCursor = hAlign[4];
		
		int outBoxX = hCursor;
		int outBoxY = vCursor;
		
		frame.add(adductLab);
		size = adductLab.getPreferredSize();
		adductLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;

		
		frame.add(adductBox);
		size = adductBox.getPreferredSize();
		adductBox.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;
		
		frame.add(chargeLab);
		size = chargeLab.getPreferredSize();
		chargeLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;
		
		frame.add(chargeBox);
		size = chargeBox.getPreferredSize();
		chargeBox.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;
		
		
		frame.add(stepLab);
		size = stepLab.getPreferredSize();
		stepLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;
		
		frame.add(stepField);
		size = stepField.getPreferredSize();
		stepField.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;
		
		frame.add(pwLab);
		size = pwLab.getPreferredSize();
		pwLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;
		
		frame.add(pwField);
		size = pwField.getPreferredSize();
		pwField.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;
		
		outBoxX = Math.max(outBoxX, hCursor);
		// move down one line
		vCursor += size.height + 3 * vGap;
		// reset hCursor by reference to hAlign[4];
		hCursor = hAlign[4];;

		frame.add(isoLab);
		size = isoLab.getPreferredSize();
		isoLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;
		
		for (int i = 0; i < pseudoElementStr.length; i++) {
			enrichmentLab[i] = new JLabel(pseudoElementStr[i]);
			frame.add(enrichmentLab[i]);
			size = enrichmentLab[i].getPreferredSize();
			enrichmentLab[i].setBounds(hCursor, vCursor + 2 * vGap, size.width, size.height);
			hCursor += size.width+2;
			
			enrichmentField[i] = new JTextField(" 1.00");
			frame.add(enrichmentField[i]);
			size = enrichmentField[i].getPreferredSize();
			enrichmentField[i].setBounds(hCursor, vCursor, size.width, size.height);
			hCursor += size.width + hGap;

		}

		outBoxX = Math.max(outBoxX, hCursor);
		// move down one line
		vCursor += size.height + 3 * vGap;
		// reset hCursor by reference to hAlign[4];
		hCursor = hAlign[4]; ;
		
		frame.add(derivLab);
		size = derivLab.getPreferredSize();
		derivLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;

		frame.add(derivBox);
		size = derivBox.getPreferredSize();
		derivBox.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;

		
		frame.add(endLab);
		size = endLab.getPreferredSize();
		// endLab.setVerticalAlignment(JLabel.CENTER);
		endLab.setBounds(hCursor, vCursor, size.width, boxSize.height);
		hCursor += size.width;

		frame.add(endBox);
		size = endBox.getPreferredSize();
		endBox.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + 3 * hGap;
		
		outBoxX = Math.max(outBoxX, hCursor);
		// move down one line
		vCursor += size.height + 3 * vGap;
		// reset hCursor by reference to hAlign[4];
		hCursor = hAlign[4]; ;

		frame.add(spectrumCheck);
		size = spectrumCheck.getPreferredSize();
		spectrumCheck.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(centroidCheck);
		size = centroidCheck.getPreferredSize();
		centroidCheck.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(isotopologCheck);
		size = isotopologCheck.getPreferredSize();
		isotopologCheck.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;
		
		frame.add(importMZCheck);
		size = importMZCheck.getPreferredSize();
		importMZCheck.setBounds(hCursor, vCursor, size.width, size.height);
		hCursor += size.width + hGap;

		outBoxX = Math.max(outBoxX, hCursor);
		// move down one line
		vCursor += size.height + 3 * vGap;
		// reset hCursor by reference to hAlign[4];
		hCursor = hAlign[4];
		
		frame.add(compLab);
		size = compLab.getPreferredSize();
		compLab.setBounds(hCursor, vCursor + vGap, size.width, boxSize.height);

		outBoxX = Math.max(outBoxX, hCursor + size.width + hGap);
		// move down one line
		vCursor += size.height + 3 * vGap;
		
		frame.add(spectrumC);
		size = spectrumC.getPreferredSize();
		spectrumC.setBounds(hCursor, vCursor, size.width, size.height);
	
		hCursor += size.width + hGap;
		int localVCursor = vCursor;
		// calculate cursor position for spectrumP (below) NOW
		//  use localVCursor for searchLabel and searchChoice
		vCursor += size.height + 8 * vGap;
		
		
		frame.add(searchLabel);
		size = searchLabel.getPreferredSize();
		searchLabel.setBounds(hCursor, localVCursor, size.width, size.height);

		localVCursor += size.height + vGap;
		frame.add(searchChoice);
		size = searchChoice.getPreferredSize();
		searchChoice.setBounds(hCursor, localVCursor, size.width, size.height);

		// vCursor is already moved down; move hCursor back to left
		hCursor = hAlign[4];

		frame.add(spectrumP);
		size = spectrumP.getPreferredSize();
		spectrumP.setBounds(hCursor, vCursor, size.width, size.height);
		
		int frameHeight = vCursor + size.height + topMargin;
	
		outText.setEditable(true);
		JScrollPane scrollPane = new JScrollPane(outText); 
		
		frame.add(scrollPane);
		size = scrollPane.getPreferredSize();
		scrollPane.setBounds(outBoxX, outBoxY, size.width, size.height);	
		
		int frameWidth = outBoxX + size.width + leftMargin;


		frame.addWindowListener(this);
		searchB.addActionListener(this);
		goB.addActionListener(this);
		compGo.addActionListener(this);
		hlp.addActionListener(this);
		searchChoice.addActionListener(this);

		frame.setSize(frameWidth, frameHeight);
		frame.setVisible(true);

	}

	/**
	 * populates two arrays (extMZ and extAbund) that describe a spectrum using values from results box.
	 */
	public void setSpectrum() {	
		// get the spectrum values from the outText box
		String outTextString = outText.getText();
		String[] line;
		// use correct line separator
		String sep;
		if (outTextString.indexOf("\r") > -1) sep = "\r"; else sep = "\n";
		line = outTextString.split(sep);

		String[] lineData;
		Vector<Double> mzVector = new Vector<Double>();
		Vector<Double> abundVector = new Vector<Double>();
		for (int i = 1; i < line.length; i++) {
			//requires tab-delimited two-column data
			lineData = line[i].trim().split("\t");
			if ((lineData.length > 1) && (!Double.isNaN(Double.parseDouble(lineData[0]))) ) try {
				// System.out.println(lineData[0] + " -- " + lineData[1]);
				mzVector.add(Double.valueOf(lineData[0]).doubleValue());
				abundVector.add(Double.valueOf(lineData[1]).doubleValue());
			} catch (NumberFormatException ex) {
				System.out.println("Non-parsable data in line " + i + ": " + line[i] + "\n");
			} catch (ArrayIndexOutOfBoundsException ex) {
				System.out.println("array out of bounds in line " + i + ": " + line[i] + "\n");
			}
		}

		// pain in the butt conversion of Vector<Double> to double[]
		extMZ = DoubleVector2doubleArray(mzVector);
		extAbund = DoubleVector2doubleArray(abundVector);
	}	
	
	

	/**
	 * invokes actions based on user actions, such as button clicks.
	 */
	public void actionPerformed(ActionEvent e) { 

		
		DecimalFormat fmtr3 = new DecimalFormat("0.000");
		DecimalFormat fmtr4 = new DecimalFormat("0.0000");
		
		for (int i = 0; i < pseudoElements.length; i++) {
			String elementString = pseudoElements[i].getSymbol();
			double[] abundances = new double[2];
			abundances[0] = Double.parseDouble(enrichmentField[i].getText());
			abundances[1] = 1.0 - abundances[0];
			toi.setElementAbundances(elementString, abundances, 2);
		}

		
		String compStr = "";
		double miMass = 0;
		double avgMass = 0;
		ArrayList<double[]> results = null;
		String chargeStr = (String)chargeBox.getSelectedItem();
		int chargeVal = Integer.valueOf(chargeStr);
		String adductStr = (String)adductBox.getSelectedItem();

		if (e.getSource() == searchChoice) {
			int hitIndex = searchChoice.getSelectedIndex();
			MonomerComposition[] hits = searcher.getHits();
			MonomerComposition theHit = hits[hitIndex].copyBrief();
			
			ElementComposition ionEC = searcher.getElementComposition(theHit).copy();
			if (adductStr.compareTo("none") != 0) ionEC.addAtoms(adductStr, chargeVal);
			String ionECString = ionEC.getElementalCompositionString();
			compField.setText(ionECString);
		}
		
		if (e.getSource() == searchB) {
			
			double tolerance = Double.parseDouble(toleranceField.getText());
			String deriv = (String)derivBox.getSelectedItem();
			String endStruc = (String)endBox.getSelectedItem();

			tolerance = tolerance * chargeVal;
			
			StringBuffer xmlBuffer = new StringBuffer("<search_config tolerance='" + tolerance + "'>\n");
			xmlBuffer.append("  <residue_list derivative='" + deriv + "' end_structure='" + endStruc + "'>\n");
			int countMax = 0;
			for (int i = 0; i < monomers.length; i++) {
				xmlBuffer.append("    <monomer abbrev='" + monomers[i].abbrev + "'" +
						" minimum_number='" + nMin[i].getText() + "'" +
						" maximum_number='" + nMax[i].getText() + "'/>\n");
				countMax += Integer.parseInt(nMax[i].getText());
			}
			xmlBuffer.append("  </residue_list>\n</search_config>\n");
			String xmlString = xmlBuffer.toString();
			searcher = new MassSearcher(mTab, xmlString);
			
			System.out.println(xmlString);
			
			System.out.println(searcher.exportConfig());
			
			double target = Double.parseDouble(targetField.getText());
			
			// molTargetMass is the target mass of the molecule without any charge or adduct
			double molTargetMass = target;
									
			int ionFormat = targetFormatBox.getSelectedIndex();
			
			molTargetMass *= chargeVal;
			
			// subtract mass of adduct and define ion format
			if (adductStr.compareTo("none") != 0) {
				if (ionFormat == 0) {
					molTargetMass -= chargeVal * toi.getElement(adductStr).getMIMass();
				}
				if (ionFormat == 1)  {
					molTargetMass -= chargeVal * toi.getElement(adductStr).getAvgMass();
				}
			}
			

			int count = 0;
			outText.setText("");
			if (countMax == 0) {
				outText.append("No Stoichiometric Ranges Specified!\n");
			} else {
				count = searcher.findCompositions(molTargetMass, tolerance, ionFormat);
				MonomerComposition[] hits = searcher.getHits();
				double[] errs = searcher.getErrs();

				DecimalFormat f = new DecimalFormat("0.0000 ");
				outText.append("Results of search for ion " + targetFormatStr[ionFormat] + " " + 
						f.format(target) + "\nMolecular mass " +
						f.format(molTargetMass) + "\nTolerance  (m/z " + (tolerance  / chargeVal) +
						")  (mass  " + tolerance + ")" +
						"\nAdduct: " + adductStr + "\nCharge: " + chargeVal +
						"\nDerivative: " + deriv +
						"\nEnd Structure: " + endStruc);

				if (hits.length > 0) {
					
					Vector<String> resultStrVec = new Vector<String>();
					for (int i = 0; i < count; i++) {
						MonomerComposition theHit = hits[i].copyBrief();

						double miM = searcher.getMonoisotopicMass(theHit);
						String miMStr = f.format(miM);
						double avgM = searcher.getAverageMass(theHit);
						String avMStr = f.format(avgM);
						String monomerC = theHit.getBriefMonomerCompositionString();
						ElementComposition molEC = searcher.getElementComposition(theHit);
						String molECStr = molEC.getElementalCompositionString();
						ElementComposition ionEC = molEC.copy();
						if (adductStr.compareTo("none") != 0) ionEC.addAtoms(adductStr, chargeVal);
						// String ionECString = ionEC.getElementalCompositionString();
						double ionMiMZ = ionEC.getMonoisotopicMass() / chargeVal;
						String ionMiMZStr = f.format(ionMiMZ);
						double ionAvgMZ = ionEC.getAverageMass() / chargeVal;
						String ionAvgMZStr = f.format(ionAvgMZ);
						String errStr = f.format(errs[i] / chargeVal);
						String resultString = new String(monomerC + "  (error: " + errStr + ")");
						resultStrVec.add(resultString);
						
						outText.append("\n\n" + (i+1) + ". " + monomerC + 
								"\n   elemental composition: " + molECStr +
								"\n   monoisotopic mass: " + miMStr + "(m/z " + ionMiMZStr + ")" +
								"\n   average mass: " + avMStr + "(m/z " + ionAvgMZStr + ")" +
								"\n   " + targetFormatStr[ionFormat] + " error: " + errStr);
					}
					// String[] resultStrArray = {"a", "b"};
					// resultStrArray = (String[]) resultStrVec.toArray(resultStrArray);
					searchChoice.removeActionListener(this);
					searchChoice.removeAllItems();
					for (int j = 0; j < resultStrVec.size(); j++) {
						searchChoice.addItem(resultStrVec.get(j));
					}
					searchChoice.addActionListener(this);
				}
			}
			
		}

		
		
		if (e.getSource() == compGo) {

			ElementComposition ec = new ElementComposition(this.toi, compField.getText());
			miMass = ec.getMonoisotopicMass();
			avgMass = ec.getAverageMass();
			compStr = ec.getElementalCompositionString();

			String step = stepField.getText();
			String pw = pwField.getText();

			double stepVal = Double.parseDouble(step);
			double pwVal = Double.parseDouble(pw);
			isoDist.generateIsotopologDistribution(ec,  this.minAbundance, this.coverage, false);
			
			if (importMZCheck.isSelected()){
				setSpectrum();
				// generate spectrum passing mz array
				results = isoDist.generateSpectrum(isoDist.getAbundances(), isoDist.getMasses(), extMZ, chargeVal, pwVal, 0.0001);
				spectrumP.setData(results.get(0), results.get(1), extAbund);
			}  else {
				// generate spectrum (calculating new mz array)
				results = isoDist.generateSpectrum(isoDist.getAbundances(), isoDist.getMasses(), chargeVal, stepVal, pwVal, 0.0001);
				spectrumP.setData(results.get(0), results.get(1));
			}

		}
		
		
		if (e.getSource() == goB) {

			String step = stepField.getText();
			String pw = pwField.getText();


			String deriv = (String)derivBox.getSelectedItem();

			searcher.setDeriv(deriv);

			String endStruc = (String)endBox.getSelectedItem();
			searcher.setEndStructure(endStruc);

			int[] coefficient = new int[comps.length];
			for (int i = 0; i < comps.length; i++) {
				coefficient[i] = Integer.parseInt(comps[i].getText());
			}
			MonomerComposition mc = new MonomerComposition(mTab, mTab.getMonomers(), coefficient);
			ElementComposition ec = searcher.getElementComposition(mc);
			if (adductStr.compareTo("none") != 0)  ec.addAtoms(adductStr, chargeVal);
			// use getMonoisotopicMass and getAverageMass methods of ElementComposition here
			//   so that the adduct is included
			miMass = ec.getMonoisotopicMass();
			avgMass = ec.getAverageMass();
			compStr = ec.getElementalCompositionString();
			double stepVal = Double.parseDouble(step);
			double pwVal = Double.parseDouble(pw);

			//the following sets the values of several arrays in isoDist
			isoDist.generateIsotopologDistribution(ec,  this.minAbundance, this.coverage, false);
			// String mainFrag = (String)fBox.getSelectedItem();
			// int numScars = ((String) (scarBox.getSelectedItem())).parseInteger();


			if (importMZCheck.isSelected()){
				setSpectrum();
				// generate spectrum passing mz array
				results = isoDist.generateSpectrum(isoDist.getAbundances(), isoDist.getMasses(), extMZ, chargeVal, pwVal, 0.0001);
				spectrumP.setData(results.get(0), results.get(1), extAbund);
			}  else {
				// generate spectrum (calculating new mz array)
				results = isoDist.generateSpectrum(isoDist.getAbundances(), isoDist.getMasses(), chargeVal, stepVal, pwVal, 0.0001);
				spectrumP.setData(results.get(0), results.get(1));
			}


		}

		if ( (e.getSource() == compGo)  ||  (e.getSource() == goB) ){

			outText.setText("");

			String[] splitComp = compStr.split(" ");
			StringBuffer eCompStrB = new StringBuffer("<html><font color='red'><b>");

			for (int s = 0;  s < splitComp.length; s++) {
				if (s % 2 == 0) 
					eCompStrB.append(splitComp[s]);
				else
					if (splitComp[s].compareTo("1") != 0)
						eCompStrB.append("<sub>" + splitComp[s] + "</sub>");
			}


			eCompStrB.append("</b></font>&nbsp; &nbsp; &nbsp; Monoisotopic Mass: <font color='red'>" + fmtr4.format(miMass));
			eCompStrB.append("</font>&nbsp; &nbsp; &nbsp; Average Mass: <font color='red'>" + fmtr4.format(avgMass));
			eCompStrB.append("</b></font></html>");
			compLab.setText(eCompStrB.toString());

			spectrumC.setData(isoDist.getCentroidMasses(), isoDist.getCentroidAbundances(), miMass, chargeVal);
			spectrumC.repaint();


			spectrumP.repaint();



			if (isotopologCheck.isSelected()) {
				double[] iMass = isoDist.getBriefMasses();
				double[] iAbund = isoDist.getBriefAbundances();
				double[] iSum = isoDist.getBriefCumulatives();
				String[] iComp = isoDist.getBriefCompositions();
				int count = iMass.length;
				outText.append(count + " significant isotopologs in order of abundance\n mass\tAbundance\tSum\tComposition\n");
				for (int q = 0; q < count; q++) {
					// System.out.print(" " + q);
					outText.append(fmtr4.format(iMass[q]) + "\t" + fmtr3.format(iAbund[q]) + "\t" +
							fmtr3.format(iSum[q]) + " \t" + iComp[q] + "\n");
				}
				outText.append("\n\n");
			}


			if (centroidCheck.isSelected()) {
				double[] cMass = isoDist.getCentroidMasses();
				double[] cAbund = isoDist.getCentroidAbundances();
				int[] cN = isoDist.getCentroidN();
				int count = cN.length;
				int isoCount = isoDist.getCount();
				outText.append(count + " centroided peaks assembled from " + isoCount + " isotopologs\n  m/z\tAbundance\tPopulation\n");
				for (int q = 0; q < count; q++) {
					outText.append(fmtr4.format(cMass[q] / chargeVal) + "\t" + fmtr3.format(cAbund[q]) + "\t" + cN[q] + "\n");
				}
				outText.append("\n\n");

			}


			if (spectrumCheck.isSelected()) {
				double[] mz = results.get(0);
				double[] abundance = results.get(1);
				outText.append("Simulated Spectrum\n\n m/z          Abundance\n");
				if (importMZCheck.isSelected()){
					// keep the externally defined m/z and abundance in the text box, along with simulated spectrum
					for(int k = 0; k < mz.length; k++) {
						// System.out.format("%.4f %.4f\n", mz[k], abundance[k]);
						outText.append(fmtr4.format(mz[k]) + "\t" + fmtr3.format(extAbund[k]) + "\t" +fmtr3.format(abundance[k]) + "\n");
					}
				} else {
					// just put the simulated data in the text box
					for(int k = 0; k < mz.length; k++) {
						// System.out.format("%.4f %.4f\n", mz[k], abundance[k]);
						outText.append(fmtr4.format(mz[k]) + "\t" +fmtr3.format(abundance[k]) + "\n");
					}
				}

				outText.append("\n");
			}	 

		}

		if (e.getSource() == hlp) {
			String help1 = "copyright William York 2010-2012\nAll Rights Reserved \n\n" +
			"Specify the residue composition of the ion using the list\n" +
			"on the left.  Then choose the adduct that provides the ion charge\n" +
			"and specify the charge of the ion.\n\n" +
			"Alternatively, enter a molecular formula (e.g., \"C 6 H 12 O 6\")\n\n" +
			"The step size and peak width are properties of the \n" +
			"simulated spectrum.\n\n" +
			"If the molecule is derivatized using an isotopically-\n" +
			"enriched reagent or it contains an isotopically-enriched\n" +
			"monosaccharide, specify the isotope and the isotopic\n" +
			"purity of the reagent or residue.\n\n" +
			"When using a residue composition, the following derivatives can \n" +
			"be specified.  The following derivatives are defined in the current\n" +
			"configuration file:\n\n";

			String help2 = "\nThe following end structures are supported:\n" +
			"reducing:\ta hydroxyl group at the reducing end\n" +
			"derivatized:\ta derivative (e.g., methyl) at the reducing end\n" +
			"alditol:\tchemically (e.g., borohydride) reduced sugar \n\n" +
			"You can choose which data (spectrum, centroid and significant\n" +
			"isotopomers) to display in this window.\n\n" +
			"You can also import experimental data for comparison to the\n" +
			"simulated data using the following steps.\n" +
			"1) Clear all text from this window\n" +
			"2) Paste in (tab-separated) two-column data, as:\n\n" +
			"m/z	abundance\n\n" +
			"3) Check the \"import spectral data\" box\n" +
			"4) Run the simulation.";

        
			outText.setText(help1);

			for (int i = 0; i < derivStr.length; i++) {
				outText.append(derivStr[i] + " - " + derivNameStr[i] + "\n");
			}

			outText.append(help2);
		}
	}




	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}

	public void windowOpened(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}

	
	/**
	 * converts a Vector of Double to an array of double.
	 * @param vec the input vector
	 * @return the output array
	 */
	public double[] DoubleVector2doubleArray (Vector<Double> vec) {
		double[] doubleArray = new double[vec.size()];
		Double[] arrayOfDouble = new Double[vec.size()];
		arrayOfDouble = (Double[]) vec.toArray(arrayOfDouble);
		for (int s = 0; s < doubleArray.length; s++) {
			doubleArray[s] = arrayOfDouble[s];
		}
		return doubleArray;
	}





	/**
	 * a bar graph representation of the abundances of the centroided peaks assembled
	 * from all isotoplogs calculated for a structure. 
	 */
	private class CentroidSpectrum extends JPanel {
		
		/**
		 * the masses of the centroided isotopolog peaks.
		 */
		double[] masses = {0};
		
		/**
		 * the abundances of the centroided isotopolog peaks.
		 */
		double[] abundance = {0};
		
		/**
		 * the mass of the monoisotopic isotopolog peak.
		 */
		double miMass = 0;
		
		/**
		 * the ionic charge.
		 */
		double charge = 1.0;
		
		/**
		 * the baseline of the rendering, in pixels.
		 */
		int baseline = 90;
		
		/**
		 *  the left edge of the rendering in pixels.
		 */
		int leftEdge = 50;
		
		/**
		 * the width of the rendering in pixels.
		 */
		int width = 200;
		
		/**
		 * the right edge of the rendering in pixels.
		 */
		int rightEdge = leftEdge + width;
		
		/**
		 * the height of the rendering in pixels.
		 */
		int height = 100;


		/**
		 * generates a new instance of the CentroidSpectrum class
		 */
		public CentroidSpectrum() {
			this.setPreferredSize(new Dimension(width,height));
		}

		/**
		 * sets the data to be displayed as a CentroidSpectrum
		 * @param masses an array of isotopolog masses
		 * @param abundance an array of isotopolog abundances
		 * @param miMass the mass of the monoisotopic species (shown as a red bar) 
		 * @param charge the charge of the ion
		 */
		public void setData(double[] masses, double[] abundance, double miMass, double charge) { 
			this.masses = masses;
			this.abundance = abundance;		
			this.miMass = miMass;
			this.charge = charge;
		}

		/**
		 * renders the CentroidSpectrum.
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.blue);
			g.drawLine(leftEdge,baseline,rightEdge,baseline);
			int w = Math.round((width) / (this.masses.length + 1));
			double maxAbundance = 0;
			for (int i = 0; i < this.masses.length; i++) {
				maxAbundance =	Math.max(maxAbundance, this.abundance[i]);
			}	 
			for (int i = 0; i < this.masses.length; i++) {
				int h = (int)(Math.round(baseline * this.abundance[i] / maxAbundance));
				if (Math.abs((masses[i] - miMass) / charge) < (0.3 / charge)) g.setColor(Color.red);
				g.fillRect(leftEdge + i*w, baseline - h, w-3, h);
				g.setColor(Color.blue);
			}

		}

	} // end Inner class CentroidSpectrum






	// Inner class ProfileSpectrum
	/**
	 * an x,y graph of the simulated spectrum assembled from the calculated isotopologs.
	 */
	private class ProfileSpectrum extends JPanel {
		
		/**
		 * an array of m/z values for the spectrum
		 */
		double[] xVals = {0};
		
		/**
		 * an array of abundance values for the spectrum..
		 */
		double[] yVals1 = {0};
		
		/**
		 * the second array of abundance values for the spectrum.
		 */
		double[] yVals2 = {0};
		
		/**
		 * the width of the spectrum in pixels.
		 */
		int width = 900;
		
		/**
		 * the left margin of the spectrum in pixels.
		 */
		int margin = 50;
		
		
		/**
		 * the right edge of the spectrum in pixels.
		 */
		int rightEdge = margin + width;
		
		/**
		 * the height of the spectrum in pixels.
		 */
		int height = 300;
		
		/**
		 * the baseline of the spectrum in pixels.
		 */
		int baseline = height - 50;
		
		/**
		 * the mimimum value of the m/z coordinate.
		 */
		double minX = 0;
		
		/**
		 * the maximum value of the abundance coordinate.
		 */
		double maxX = 10;
		
		/**
		 * the m/z span of the spectrum.
		 */
		double xSpan = 10;
		
		/**
		 * the maximum value of the first set of abundance values.
		 */
		double maxY1 = 0;
		
		/**
		 * the maximum value of the second set of abundance values.
		 */
		double maxY2 = 0;


		/**
		 * generates a new instance of the ProfileSpectrum class.
		 * @param w the width of the rendered spectrum in pixels
		 * @param m the margin of the rendered spectrum in pixels
		 * @param h the height of the rendered spectrum in pixels
		 * @param b the baseline of the rendered spectrum in pixels
		 */
		public ProfileSpectrum(int w, int m, int h, int b) {
			// initialize the spectrum panel (set size, etc.)
			this.width = w;
			this.margin = m;
			this.height = h;
			this.baseline = this.height - b;
			this.rightEdge = this.margin + this.width;
			this.setPreferredSize(new Dimension(this.width+2*this.margin,this.height));
		}

		/**
		 * sets the data to be displayed as a ProfileSpectrum, when only one data set is plotted.
		 * @param xVals an array the m/z values of the spectrum
		 * @param yVals1 an array of the abundance values of the spectrum
		 */
		public void setData(double[] xVals, double[] yVals1) { 
			// enter m/z array and one corresponding abundance array
			this.xVals = xVals;
			this.yVals1 = yVals1;
			this.yVals2 = new double[1];
			this.minX = xVals[0];
			this.maxX = xVals[xVals.length-1];
			this.xSpan = maxX - minX;
			this.maxY1 = 0;
			for (int i = 0; i < xVals.length; i++) {
				this.maxY1 = Math.max(maxY1, yVals1[i]);
			}
		}

		
		/**
		 * sets the data to be displayed as a ProfileSpectrum, when two data sets are plotted for comparison.
		 * @param xVals an array the m/z values of the spectrum
		 * @param yVals1 an array of the first set of abundance values of the spectrum
		 * @param yVals2 an array of the second set of abundance values of the spectrum
		 */
		public void setData(double[] xVals, double[] yVals1, double[] yVals2) { 
			// enter m/z array and two corresponding abundance arrays
			this.xVals = xVals;
			this.yVals1 = yVals1;
			this.yVals2 = yVals2;
			this.minX = xVals[0];
			this.maxX = xVals[xVals.length-1];
			this.xSpan = maxX - minX;
			this.maxY1 = 0;
			for (int i = 0; i < xVals.length; i++) {
				this.maxY1 = Math.max(maxY1, yVals1[i]);
			}
			this.maxY2 = 0;
			for (int i = 0; i < xVals.length; i++) {
				this.maxY2 = Math.max(maxY2, yVals2[i]);
			}
		}

		
		/**
		 * draws the curve corresponding to a ProfileSpectrum.
		 * @param g the graphics context for the rendering
		 * @param xVals an array the m/z values of the spectrum
		 * @param yVals an array of the abundance values of the spectrum
		 * @param mxY the maximum value of the y coordinate
		 * @param m the margin of the spectrum in pixels
		 * @param w the width of the spectrum in pixels
		 * @param mnX the mimimum value of the x coordinate
		 * @param xS the span of the spectrum in m/z units
		 * @param b the baseline of the spectrum in pixels
		 */
		private void drawSpectrum(Graphics g, double[] xVals, double[] yVals, double mxY, int m, int w, double mnX, double xS, int b) {
			
			Point lastPoint = getPoint(xVals[0],yVals[0], mxY, m, w, mnX, xS, b);

			for (int i = 1; i < this.xVals.length; i++) {
				Point xy = getPoint(xVals[i],yVals[i], mxY, m, w, mnX, xS, b);
				g.drawLine(lastPoint.x, lastPoint.y, xy.x, xy.y);
				lastPoint = xy;
			}

		}

		/**
		 * calculates a graphics point (x,y) in pixels corresponding to a given m/z and abundance pair.
		 * @param xIn the m/z value
		 * @param yIn the abundance value
		 * @param mxY the maximum value of the abundance
		 * @param m the margin of the spectrum in pixels
		 * @param w the width of the spectrum in pixels
		 * @param mnX the mimimum value of m/z
		 * @param xS the m/z span of the spectrum
		 * @param b the baseline of the spectrum in pixels
		 * @return an x,y coordinate pair in pixels (int)
		 */
		private Point getPoint(double xIn, double yIn, double mxY, int m, int w, double mnX, double xS, int b) {
			Point xy = new Point();
			xy.x = getX(xIn, m, w, mnX, xS);
			xy.y = getY(yIn, b, mxY);
			return xy;
		}

		/**
		 * calculates the x-value of a graphics point (x,y) in pixels corresponding to a given m/z 
		 * @param xIn the m/z value
		 * @param m the margin of the spectrum in pixels
		 * @param w the width of the spectrum in pixels
		 * @param mnX the mimimum value of m/z
		 * @param xS the m/z span of the spectrum
		 * @return the x coordinate of the data pair in pixels (int)
		 */
		private int getX(double xIn, int m, int w, double mnX, double xS) {
			return (int)(m + Math.round(w * (xIn - mnX) / xS)); 
		}

		
		/**
		 * calculates the y-value of a graphics point (x,y) in pixels corresponding to a given abundance
		 * @param yIn the abundance value
		 * @param b the baseline of the spectrum in pixels
		 * @param mxY the maximum value of the abundance
		 * @return the y coordinate of the data pair in pixels (int)
		 */
		private int getY(double yIn, int b, double mxY) {
			return (int)(Math.round(b * (1 - (yIn / mxY)))); 
		}


		/**
		 * renders the spectrum and axis.
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// draw the first spectrum
			g.setColor(Color.blue);
			drawSpectrum(g, this.xVals, this.yVals1, this.maxY1, this.margin, this.width, this.minX, this.xSpan, this.baseline);

			if  (yVals2.length > 1) {
				// if the second spectrum exists, draw it too
				g.setColor(Color.red);
				drawSpectrum(g, this.xVals, this.yVals2, this.maxY2, this.margin, this.width, this.minX, this.xSpan, this.baseline - 15);				
			}

			// draw the horizontal axis
			g.setColor(Color.black);
			Point p1 = new Point(this.margin, this.baseline + 5);
			Point p2 = new Point(this.rightEdge, this.baseline + 5);
			Point pS = new Point(this.margin, this.baseline + 25);
			// draw baseline
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			// draw ticks and labels
			int lowTick = (int)Math.ceil(minX);
			int highTick = (int)Math.floor(maxX);
			for (int t = lowTick; t <= highTick; t++) {
				p1.x = getX(t, this.margin, this.width, this.minX, this.xSpan);
				p2.x = p1.x;
				p2.y = p1.y + 7;
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
				String tStr = Integer.toString(t);
				FontMetrics fm = g.getFontMetrics();
				pS.x = p1.x - (fm.stringWidth(tStr) / 2);
				g.drawString(tStr, pS.x, pS.y);
				p2.y = p1.y + 3;
				for (int st = 1; st < 5; st++) {
					p1.x = getX(t+(double)st/5, this.margin, this.width, this.minX, this.xSpan);
					p2.x = p1.x;
					g.drawLine(p1.x, p1.y, p2.x, p2.y);

				}
			}

		}

	}
}

