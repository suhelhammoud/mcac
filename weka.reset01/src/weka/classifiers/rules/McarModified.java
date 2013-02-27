package weka.classifiers.rules;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.io.File;

import com.sun.xml.internal.bind.v2.model.runtime.RuntimeReferencePropertyInfo;

import weka.associations.AprioriItemSet;
import weka.associations.ItemSet;
import weka.associations.LabeledItemSet;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.mcar.CDriver;
import weka.classifiers.rules.mcar.MR;
import weka.classifiers.rules.mcar.Tools;
import weka.classifiers.rules.mcar.datastructures.*;
import weka.classifiers.rules.mcar.datastructures.RCounter.RTAG;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Summarizable;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.beans.DataSource;

public class McarModified extends Classifier implements OptionHandler,
TechnicalInformationHandler, Summarizable {
	/** holds the classifier rules */
	List<McarRule> m_finalRules;
	//	McarRule m_defaultRule;
	private RCounter r_counter=new RCounter();
	public RCounter getR_counter() {
		return r_counter;
	}

	private RANK_ID rankType=RANK_ID.CONF_SUPP_CARD;
	
	public RANK_ID getRankType() {
		return rankType;
	}

	public void setRankType(RANK_ID rankType) {
		this.rankType = rankType;
	}

	StringBuffer logger;
	int CONDITION_LENGTH = 0;

	MResult mResult=new MResult();

	/** for serialization */
	private static final long serialVersionUID = -2359258629829116560L;

	/**
	 * is sparse
	 */
	protected boolean m_isSparse=false;

	/** The minimum support. */
	protected double m_minSupport=0.05;

	/**
	 * minimum confidence
	 */
	protected double m_confidence=0.35;


	/** The maximum number of rules that are output. */
	protected int m_numRules;


	/** Report progress iteratively */
	protected boolean m_verbose;

	/** count the rules with the same conf, support, card, col, row */
	protected boolean m_validateSignificance=false;

	/** keep ranking as in old mcar */
	protected boolean m_oldMCAR=true;
	/**
	 * Add default rule for unclassified instances
	 */
	protected boolean m_addDefaultRule=true;

	//	/** Only the class attribute of all Instances. */
	//	protected Instances m_onlyClass;

	/** The class index. */
	protected int m_classIndex;


	/**
	 * Returns a string describing this associator
	 * 
	 * @return a description of the evaluator suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Class implementing Modified MCAR algorithm. "
		+ "adapted as explained in the second reference.\n\n"
		+ "For more information see:\n\n"
		+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		TechnicalInformation additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "F. Thabatah and S. Hammoud");
		result.setValue(Field.TITLE,
		"Scalable Algorithm for Multi-label Classification using Association");
		result.setValue(Field.BOOKTITLE, "----");
		result.setValue(Field.YEAR, "2010");
		result.setValue(Field.PAGES, "---");
		result.setValue(Field.PUBLISHER, "----");

		additional = result.add(Type.TECHREPORT);
		additional.setValue(Field.AUTHOR, "F. Thabatah and S. Hammoud");
		additional
		.setValue(Field.TITLE,
		"IScalable Algorithm for Multi-label Classification using Association");
		additional.setValue(Field.BOOKTITLE, "---");
		additional.setValue(Field.YEAR, "2020");
		additional.setValue(Field.PAGES, "--");
		additional.setValue(Field.PUBLISHER, "---");

		return result;
	}

	/**
	 * Resets the options to the default values.
	 */
	public void resetOptions() {
		m_confidence=0.35;
		m_minSupport=0.05;
		m_verbose = false;
		m_oldMCAR = true;
		m_validateSignificance=false;
		m_addDefaultRule= true;
		m_numRules = 10;
		m_classIndex = -1;
		m_isSparse=false;

	}


	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {

		String s_numRules = "\tThe required number of rules. (default = " + m_numRules + ")";

		String s_confidance = "\tThe minimum confidence of a rule. (default = "+ m_confidence + ")";

		String s_minSupport = "\tThe lower bound for the minimum support. (default = "+ m_minSupport + ")";

		String s_classIndex = "\tClass index. (default = "+ m_classIndex + ")";

		String s_verbos = "\tReport progress iteratively. (default =" +m_verbose+ ")";

		String s_validateSignificance ="\t count rules of " +
		"the same conf, supp, card, col, row. ( default = "+ m_validateSignificance+")";

		String s_defaultRule = "\tAdd default Rule for unclassified instances. (default = "+m_addDefaultRule+")";

		String s_oldMCAR = "\tKeep ranking as in old MCAR. ( defalt = "+m_oldMCAR+")";
		String s_isSparse = "\tUsing sparse data set. ( defalt = "+m_isSparse+")";

		FastVector newVector = new FastVector(10);

		newVector.addElement(new Option(s_confidance, "C", 1,"-C <minimum confidence of a rule>"));
		newVector.addElement(new Option(s_minSupport, "M", 1,"-M <lower bound for minimum support>"));
		newVector.addElement(new Option(s_verbos, "V", 0,"-V"));
		newVector.addElement(new Option(s_oldMCAR, "O", 0,"-O"));
		newVector.addElement(new Option(s_validateSignificance, "G", 0,"-G"));
		newVector.addElement(new Option(s_defaultRule, "D", 0,"-D"));
		newVector.addElement(new Option(s_numRules, "N", 1, "-N <required number of rules output>"));
		newVector.addElement(new Option(s_classIndex, "c", 1,"-c <the class index>"));
		newVector.addElement(new Option(s_isSparse, "S", 0,"-S"));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		resetOptions();
		String numRulesString = Utils.getOption('N', options);
		String minConfidenceString = Utils.getOption('C', options);
		String minSupportString = Utils.getOption('M', options);
		String classIndexString = Utils.getOption('c', options);
		String s_verbos = Utils.getOption("V", options);

		if (numRulesString.length() != 0) {
			m_numRules = Integer.parseInt(numRulesString);
		}
		if (classIndexString.length() != 0) {
			m_classIndex = Integer.parseInt(classIndexString);
		}

		if (minConfidenceString.length() != 0) {
			m_confidence = (new Double(minConfidenceString)).doubleValue();
		}

		if (minSupportString.length() != 0) {
			m_minSupport = (new Double(minSupportString))
			.doubleValue();
		}

		m_verbose = Utils.getFlag('V', options);

		m_validateSignificance =Utils.getFlag("G", options);

		m_addDefaultRule= Utils.getFlag("D", options);

		m_oldMCAR = Utils.getFlag("O", options);

		m_isSparse = Utils.getFlag("S", options);
	}

	/**
	 * Gets the current settings of the Modified McarObject object.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {

		String[] options = new String[20];
		int current = 0;

		options[current++] = "-C";
		options[current++] = "" + m_confidence;
		options[current++] = "-M";
		options[current++] = "" + m_minSupport;
		if (m_oldMCAR)
			options[current++] = "-O";

		if (m_verbose)
			options[current++] = "-V";
		if (m_validateSignificance)
			options[current++] = "-G";
		if( m_addDefaultRule)
			options[current++] = "-D";
		if(m_isSparse)
			options[current++] = "-S";

		options[current++] = "-N";
		options[current++] = "" + m_numRules;
		options[current++] = "-c";
		options[current++] = "" + m_classIndex;

		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}

	public void setIsSparse(boolean b){
		m_isSparse=b;
	}
	public boolean getIsSparse(){
		return m_isSparse;
	}
	public String isSparseTipText(){
		return "True if dataset is using sparse data";
	}
	public void setOldMCAR(boolean b){
		m_oldMCAR= b;
	}
	public boolean getOldMCAR(){
		return m_oldMCAR;
	}
	public String oldMCARTipText(){
		return "Keep the ranking as in old MCAR ?";
	}
	public void setValidateSignificance(boolean b){
		m_validateSignificance=b;
	}

	public boolean getValidateSignificance(){
		return m_validateSignificance;
	}

	public String validateSignificanceTipText(){
		return " count the number of rules of the same conf, supp, card, col, and row";
	}

	public void setAddDefaultRule(boolean b){
		m_addDefaultRule=b;
	}
	public boolean getAddDefaultRule(){
		return m_addDefaultRule;
	}
	public String addDefaultRuleTipText() {
		return "add default rule for unclassified instances";
	}

	public void setMinSupport(double minSupp){
		m_minSupport= minSupp;
	}
	public double getMinSupport(){
		return m_minSupport;
	}
	public String minSupportTipText() {
		return "minimum support value (double)";
	}
	/**
	 * Sets the class index
	 * 
	 * @param index
	 *            the class index
	 */
	public void setClassIndex(int index) {
		m_classIndex = index;
	}

	/**
	 * Gets the class index
	 * 
	 * @return the index of the class attribute
	 */
	public int getClassIndex() {

		return m_classIndex;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String classIndexTipText() {
		return "Index of the class attribute. If set to -1, the last attribute is taken as class attribute.";

	}






	/**
	 * Get the value of minConfidence.
	 * 
	 * @return Value of minConfidence.
	 */
	public double getConfidence() {

		return m_confidence;
	}

	/**
	 * Set the value of minConfidence.
	 * 
	 * @param v
	 *            Value to assign to minConfidence.
	 */
	public void setConfidence(double v) {

		m_confidence = v;
	}

	public String confidenceTipText() {
		return "minimum confidence";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String numRulesTipText() {
		return "Number of rules to find.";
	}

	/**
	 * Get the value of numRules.
	 * 
	 * @return Value of numRules.
	 */
	public int getNumRules() {

		return m_numRules;
	}

	/**
	 * Set the value of numRules.
	 * 
	 * @param v
	 *            Value to assign to numRules.
	 */
	public void setNumRules(int v) {

		m_numRules = v;
	}


	/**
	 * Sets verbose mode
	 * 
	 * @param flag
	 *            true if algorithm should be run in verbose mode
	 */
	public void setVerbose(boolean flag) {
		m_verbose = flag;
	}

	/**
	 * Gets whether algorithm is run in verbose mode
	 * 
	 * @return true if algorithm is run in verbose mode
	 */
	public boolean getVerbose() {
		return m_verbose;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String verboseTipText() {
		return "If enabled the algorithm will be run in verbose mode.";
	}




	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1 $");
	}

	public static double[] cvRangeSupport(Instances data, McarModified mcar, 
			double start,double stop, double step, int iteration ) throws Exception{
		double[] result= new double[(int)((stop-start)/step)+1];
		for (int i = 0; i < result.length; i++) {
			mcar.setMinSupport(start+i*step);
			result[i]= cv(data,mcar, iteration);
		}

		return result;
	}

	public static double[] cvRangeConfidence(Instances data, McarModified mcar, 
			double start,double stop, double step, int iteration ) throws Exception{
		double[] result= new double[(int)((stop-start)/step)+1];
		for (int i = 0; i < result.length; i++) {
			mcar.setConfidence(start+i*step);
			result[i]= cv(data,mcar, iteration);
		}

		return result;
	}


	public static double cv(Instances data, McarModified mcar, int iteration) 
	throws Exception{
		double sum=0.0;
		for (int j = 0; j < iteration; j++) {
			MResult mresult=new MResult();
			sum+= McarModified.crossValidateModel(mcar, 
					data, 10, new Random(j),mresult);

		}
		sum /= iteration;

		return sum;
	}



	/**
	 * Main method.
	 * 
	 * @param args
	 *            the commandline options
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String filename="";
		if(args.length>0)
			filename= args[0];

		filename ="data/c/Lymph.arff";


		Instances data= new Instances(new FileReader(filename));

		data.setClassIndex(data.numAttributes()-1);
		McarModified mcar= getMCAR(1);
		mcar.setMinSupport(0.07);
		mcar.setConfidence(0.0);
		long t1=System.nanoTime();
		double dd= cv(data, mcar, 1);
		t1 -= System.nanoTime()-t1;
		System.out.println(dd);

		long t2 =System.nanoTime();
		eval(data, mcar);
		t2-= System.nanoTime()-t2;

		System.out.println("time "+ ((double)t2/(double)t1));
		if (true) {
			return;
		}
		ArrayList<double[]> results=new ArrayList<double[]>();

		for (double supp= 0.001 ; supp < 0.3; supp+=0.01) {
			System.out.println("support "+ supp);
			mcar.setMinSupport(supp);
			results.add( cvRangeConfidence(data, mcar, 0.00, 1.0, 0.1, 10));
		}

		for (double[] ds : results) {
			for (double d : ds) {
				System.out.print(d+"\t");
			}
			System.out.println();
		}
	}


	public static McarModified getMCAR(int index){
		McarModified mcar=new McarModified();
		switch (index) {

		case 1://fadi
			mcar.setVerbose(false);
			mcar.setOldMCAR(true);
			mcar.setAddDefaultRule(true);
			mcar.setValidateSignificance(false);
			return mcar;

		case 2://suhel
			mcar.setVerbose(false);
			mcar.setOldMCAR(false);
			mcar.setAddDefaultRule(true);
			mcar.setValidateSignificance(false);
			return mcar;

		default:
			return null;
		}
	}


	public double classifyInstanceMulti(Instance instance) {
		double[] dist=null;
		try {
			dist = distributionForInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		double label = instance.value(instance.numAttributes()-1);
		return dist[(int)label];
	}

	public double classifyInstanceSingle(Instance instance) {
		//		double[] dist=null;
		double pred=0;
		try {
			//			dist = distributionForInstance(instance);
			pred = super.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}

		double label = instance.value(instance.numAttributes()-1);
		if( label == pred)
			return 1.0;
		else
			return 0.0;
	}


	/**
	 * Predicts the class memberships for a given instance. If an instance is
	 * unclassified, the returned array elements must be all zero. If the class
	 * is numeric, the array must consist of only one element, which contains
	 * the predicted value. Note that a classifier MUST implement either this or
	 * classifyInstance().
	 * 
	 * @param instance
	 *            the instance to be classified
	 * @return an array containing the estimated membership probabilities of the
	 *         test instance in each class or the numeric prediction
	 * @exception Exception
	 *                if distribution could not be computed successfully
	 */
	public double[] distributionForInstance(Instance instance) throws Exception {

		for (McarRule rule : m_finalRules) {
			if (rule.accept(instance)) {
				return rule.distributionForInstance(instance);
			}
		}

		return new double[instance.numClasses()];

	}

	@Override
	public String toSummaryString() {

		return "toSummaryString( to be implemented later)";
	}


	public void log(StringBuffer sb, String s){
		if(m_verbose)
			sb.append(s);
	}

	@Override
	public void buildClassifier(Instances train) throws Exception {
		final int CLASSINDEX= train.numAttributes()-1;

		CONDITION_LENGTH =train.numAttributes();
		//		mResult=new MResult();

		//clear data structures
		m_finalRules = new ArrayList<McarRule>();
		logger = new StringBuffer();

		int minSupport= (int)Math.ceil(train.numInstances()* m_minSupport);
		int minConfidence=(int)(m_confidence * Integer.MAX_VALUE);


		Map<Integer, Items> rules=new TreeMap<Integer, Items>();

		//freq item space
		Items items=new Items();
		//line space
		Lines lines=new Lines();


		///init the data
		BagList bag = Tools.mapInstances(train);
		Lines initLines=new Lines(bag);
		lines.putAll(initLines);


		log(logger,"\nbag:\n"+ bag);
		log(logger,"\nlines:\n"+lines);

		int maxIteration=lines.values().iterator().next().size();

		for (int i = 1; i <= maxIteration; i++) {
			//			Items occ1 =MR.toOccurances(lines, i);
			ToItemsResult toItems=MR.toItems(lines, i, minSupport, minConfidence);
			rules.put(i, toItems.rules);
			lines=MR.toLines(toItems.items);


			log(logger,"\n-----------------------------------iteration "+ i+"----------------------------");
			log(logger,"\nitems :\n"+toItems.items);
			log(logger,"\nrules :\n"+rules.get(i));
			log(logger,"\nlines \n"+lines);

			if(lines.size()==0)break;
		}

		Items allRules=new Items();
		for (Items i : rules.values()) {
			allRules.putAll(i);
		}
		log(logger,"\n all rules collected \n"+allRules);

		if(m_verbose){
			Lines rLines=MR.toLines(allRules);
			log(logger,"\nrLines, all rules to line space:\n"+rLines);
		}

		Lines oneRLines=MR.rulesToLinesWithWeights(allRules, m_oldMCAR, rankType);
		log(logger,"\noneRLines: all rules to line space, ranked and picked\n"+oneRLines);


		if(m_validateSignificance){
			r_counter = countSimilar(allRules);
			mResult.rcounter.add(r_counter);
			//			logger.append("\nValidate Significance: total number of possible rule:"
			//					+ allRules.size()+"\n"+ rcount.toString());

		}

		Items rawRules=MR.toOccurances(oneRLines,1 );
		log(logger,"\nRanked rules in item space:\n"+rawRules);

		List<McarRule> finalRules=MR.strippedOrderedFilledFinal(rawRules, train);


		//		return finalRules;
		m_finalRules.addAll(finalRules); 

		if(m_addDefaultRule){
			Set<Integer> classifiedLines=new HashSet<Integer>(oneRLines.size());
			for (Long labelLine : oneRLines.keySet()) {
				classifiedLines.add(
						(int)(labelLine & Integer.MAX_VALUE));
			}



			McarRule defaultRule=new McarRule();

			Set<Integer> unclassifiedLines=new HashSet<Integer>(
					train.numInstances()-classifiedLines.size()+1);
			for (int i = 0; i < train.numInstances(); i++) {
				if(classifiedLines.contains(i))continue;
				unclassifiedLines.add(i);
			}
			log(logger,"\nnumber of classified lines "+ classifiedLines.size());
			log(logger,"\nnumber of unclassified  lines"+ unclassifiedLines.size());

			for (Integer i : unclassifiedLines) {

				Instance inst= train.instance(i);
				defaultRule.incLabelFreq(inst.toString(CLASSINDEX),
						1, inst.value(CLASSINDEX));

			}

			if(unclassifiedLines.size()>0)
				m_finalRules.add(defaultRule);
		}

		m_numRules=m_finalRules.size();

		log(logger,"\n --------------------------------   end building mcan classifer---------------------- ");
	}

	/**
	 * Outputs the size of all the generated sets of itemsets and the rules.
	 * 
	 * @return a string representation of the model
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("number of rules "+ m_finalRules.size());
		sb.append("\n");
		
		sb.append(logger);
		sb.append("\nminimum confidence: "+ m_confidence);
		sb.append("\nminimum support: "+ m_minSupport);
		sb.append("\nAdd default rule for unclassified instances :"+ m_addDefaultRule);
		sb.append("\nverbos output: "+ m_verbose);
		sb.append("\nKeep old MCAR ranking: "+ m_oldMCAR);
		if(m_validateSignificance)
			sb.append("\nValidate Significance\n"+ r_counter.toString());

		if( m_finalRules == null ){
			sb.append("no rules generated yet");
			return sb.toString();
		}

		sb.append("\nNumber of generated Rules  :"+m_finalRules.size());

		sb.append("\nGenerated Rules :");

		for (McarRule rule : m_finalRules) {
			sb.append("\n"+rule.toString(CONDITION_LENGTH) );
		}

		return sb.toString();
	}

	public static double crossValidateModel(Classifier classifier,
			Instances instances, int numFolds, Random random,
			MResult mresult) 
	throws Exception {

		double result=0.0;
		long numOfInstances=0;

		// Make a copy of the data we can reorder
		Instances data = new Instances(instances);
		//TODO check random
		data.randomize(new Random(System.nanoTime()));
		if (true || data.classAttribute().isNominal() 
				|| data.attribute(data.numAttributes()-1).isNominal()) {
			data.stratify(numFolds);
		}



		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			Instances train = data.trainCV(numFolds, i, random);
			//			setPriors(train);
			McarModified copiedClassifier = (McarModified) Classifier.makeCopy(classifier);
			copiedClassifier.buildClassifier(train);
			//			System.out.println("rresult of fold "+copiedClassifier.mResult);
			mresult.add(copiedClassifier.mResult);

			Instances test = data.testCV(numFolds, i);
			double[] evResults=copiedClassifier.evaluate( test, mresult, false);

			double sum=0.0;
			for (double d : evResults) {
				sum+=d;
			}
			result+=sum;
			numOfInstances+=evResults.length;
		}
		//		m_NumFolds = numFolds;

		return result/(double)numOfInstances;
	}

	public double[] evaluate(Instances data, MResult mresult, boolean isMulti)  {

		double predictions[] = new double[data.numInstances()];


		// Need to be able to collect predictions if appropriate (for AUC)

		for (int i = 0; i < data.numInstances(); i++) {
			if(isMulti)
				predictions[i] = classifyInstanceMulti(data.instance(i));
			else
				predictions[i] = classifyInstanceSingle(data.instance(i));

			mresult.numClassified++;
			mresult.numCorrectClassification+= predictions[i];

			mresult.log("\n"+i+"\t"+ predictions[i]);
		}

		return predictions;
	}

	public static IntBag getColFromId(IntBag id){
		IntBag result = new IntBag(id.size()-1);
		for (int i = 0; i < id.size()-1; i++) {
			result.add(id.get(i));
		}
		return result ;
	}
	public static RCounter countSimilar(Items rules){
		RCounter count=new RCounter();
		RMapper rmap=new RMapper();
		for (IntBag id : rules.keySet()) {
			FrequentItem fi= rules.get(id);
			//TODO check if confidence is already calculated
			int iconf = fi.getConfidene();

			if (rmap.add(RTAG.CONF,iconf)){
				count.inc(RTAG.CONF);

				//TODO check if support is already calculated
				int isupp = fi.getSupport();
				if(rmap.add(RTAG.SUPP, isupp)){
					count.inc(RTAG.SUPP);

					int icard = id.size()-1;

					if(rmap.add(RTAG.CARD,icard)){
						count.inc(RTAG.CARD);

						int irow = id.get(id.size()-1);

						if(rmap.add(RTAG.ROW, irow)){
							count.inc(RTAG.ROW);

							IntBag icol = getColFromId(id);

							if( rmap.add(icol)){
								count.inc(RTAG.COL);
							}
						}
					}

				}
			}

		}

		return count;
	}

	public static void eval(Instances newData, Classifier tree) throws Exception{
		Evaluation eval = new Evaluation(newData);
		//		 eval.crossValidateModel(tree, newData, 10, new Random(2));
		double r=0.0;

		for (int i = 0; i < 1; i++) {
			eval.crossValidateModel(tree, newData, 20, new Random(i));
			r+=eval.pctCorrect();
		}
		r/=10.0;
		System.out.println(r);
	}
	public static List<McarModified> batchBuildClassifier(McarModified classifer,Instances train, double...confs)
	throws Exception{
		List<McarModified> result=new ArrayList<McarModified>(confs.length);

		for (double cnf : confs) {

			McarModified mcarcopy = new McarModified();
			mcarcopy.setMinSupport(classifer.getMinSupport());
			mcarcopy.setConfidence(cnf);
			mcarcopy.setAddDefaultRule(classifer.getAddDefaultRule());
			mcarcopy.setOldMCAR(classifer.getOldMCAR());
			mcarcopy.setValidateSignificance(classifer.getValidateSignificance());
			mcarcopy.rankType = classifer.rankType;
			result.add(mcarcopy);
		}

		final int CLASSINDEX= train.numAttributes()-1;

		//TODO check here
		//		CONDITION_LENGTH =train.numAttributes();
		//		mResult=new MResult();

		
		
		int[] minConfidences = new int[confs.length];
		for (int i = 0; i < minConfidences.length; i++) {
			minConfidences[i]= (int)(confs[i] * Integer.MAX_VALUE);
			
		}

		int minSupport= (int)Math.ceil(train.numInstances()* classifer.getMinSupport());



		List<Items> rules=new ArrayList<Items>(confs.length);
		for (int i = 0; i < confs.length; i++) {
			rules.add(new Items());
		}

		//freq item space
		Items items=new Items();
		//line space
		Lines lines=new Lines();


		///init the data
		BagList bag = Tools.mapInstances(train);
		Lines initLines=new Lines(bag);
		lines.putAll(initLines);




		int maxIteration=lines.values().iterator().next().size();

		for (int iteration = 1; iteration <= maxIteration; iteration++) {
			//			Items occ1 =MR.toOccurances(lines, i);
			ToItemsResultList toItems=MR.toItemsList(lines, iteration, minSupport, minConfidences);
			for (int j = 0; j < toItems.rules.size(); j++) {
				rules.get(j).putAll(toItems.rules.get(j));
			}
			lines=MR.toLines(toItems.items);

			if(lines.size()==0)break;
		}

		Items allRules=new Items();

//		for (int i = 0; i < rules.size(); i++) {
//			System.out.print(rules.get(i).size()+"\t");
//		}
//		System.out.println();
		
//		for (int clsIndex = confs.length-1; clsIndex >= 0; clsIndex--) {
		for (int clsIndex = 0; clsIndex < confs.length; clsIndex++) {

			allRules.putAll(rules.get(clsIndex));
			
			

			Lines oneRLines=MR.rulesToLinesWithWeights(allRules, classifer.m_oldMCAR,classifer.getRankType());

			if(classifer.m_validateSignificance){
				classifer.r_counter = countSimilar(allRules);
			}

			Items rawRules=MR.toOccurances(oneRLines,1 );

			List<McarRule> finalRules=MR.strippedOrderedFilledFinal(rawRules, train);


			//		return finalRules;

			if(classifer.m_addDefaultRule){
				Set<Integer> classifiedLines=new HashSet<Integer>(oneRLines.size());
				for (Long labelLine : oneRLines.keySet()) {
					classifiedLines.add(
							(int)(labelLine & Integer.MAX_VALUE));
				}



				McarRule defaultRule=new McarRule();

				Set<Integer> unclassifiedLines=new HashSet<Integer>(
						train.numInstances()-classifiedLines.size()+1);
				for (int i = 0; i < train.numInstances(); i++) {
					if(classifiedLines.contains(i))continue;
					unclassifiedLines.add(i);
				}

				for (Integer i : unclassifiedLines) {

					Instance inst= train.instance(i);
					defaultRule.incLabelFreq(inst.toString(CLASSINDEX),
							1, inst.value(CLASSINDEX));

				}

				if(unclassifiedLines.size()>0)
					finalRules.add(defaultRule);
			}
			
			result.get(clsIndex).m_finalRules= McarRule.copyList(finalRules);
			result.get(clsIndex).setNumRules(finalRules.size());
		}

		Collections.reverse(result);
		return result;
	}

	
	
}
