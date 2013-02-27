package weka.classifiers.rules.mcar.datastructures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import weka.core.Instance;



public class McarRule implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 352308932426572544L;
	
	List<Integer> intCondition;
	List<String> condition;//new ArrayList<String>();
	List<String> labels;//=new ArrayList<String>();
	List<Integer> freqs;//=new ArrayList<Integer>();

	List<Double> doubleCondition;

	public McarRule copy(){
		McarRule result= new McarRule();
		result.intCondition=new ArrayList<Integer>(intCondition);
		result.condition = new ArrayList<String>(condition);
		result.labels = new ArrayList<String>(labels);
		result.freqs = new ArrayList<Integer>(freqs);
		result.doubleCondition = new ArrayList<Double>(doubleCondition);
		
		return result;
	}
	
	public static List<McarRule> copyList(List<McarRule> rules){
		List<McarRule> result=new ArrayList<McarRule>(rules.size());
		for (McarRule mcarRule : rules) {
			result.add(mcarRule.copy());
		}
		return result;
	}
	public McarRule(List<Integer> ib) {
		intCondition=new ArrayList<Integer>(ib);
		condition = new ArrayList<String>(ib.size());
		labels = new ArrayList<String>(ib.size());
		freqs = new ArrayList<Integer>(ib.size());
		doubleCondition=new ArrayList<Double>(ib.size());
	}
	public McarRule(){
		intCondition=new ArrayList<Integer>();
		condition = new ArrayList<String>();
		labels = new ArrayList<String>();
		freqs = new ArrayList<Integer>();
		doubleCondition=new ArrayList<Double>();
	}

//	public void addLabelFreq(String label,int freq){
//		labels.add(label);
//		freqs.add(freq);
//	}

	public void addLabelFreqDouble(String label,int freq, double value){
		labels.add(label);
		freqs.add(freq);
		doubleCondition.add(value);
	}


	/**
	 * 
	 * @param label
	 * @param freq
	 * @return true if the added the new label to the labels list.
	 */
	public boolean incLabelFreq(String label, int freq, double value){
		if (labels.contains(label)){
			int index = labels.indexOf(label);
			freqs.set(index, freqs.get(index)+freq);
			return false;
		}else{
			labels.add(label);
			freqs.add(freq);
			doubleCondition.add(value);
			return true;
		}
	}



	public void add(String cond){
		condition.add(cond);
	}


	public String toString(int maxAtt){
		StringBuffer sb=new StringBuffer();

		int maxIntCondition=1;
		try{
			maxIntCondition= Collections.max(intCondition);
		}catch (NoSuchElementException e) {
		}

		if(maxAtt < maxIntCondition){
			maxAtt = maxIntCondition;
		}
		List<String> c=new ArrayList<String>(maxAtt);
		for (int i = 0; i <= maxAtt; i++) {
			c.add("*");
		}
		for (int i = 0; i < intCondition.size(); i++) {
			int att = intCondition.get(i); 
			String attString = condition.get(i);
			c.set(att, attString);
		}
		
		for (String string : c) {
			sb.append("\t"+ string);
		}

		sb.append("(\tlbls:"+ labels.size()+")\t");
		
		for (int i = 0; i < labels.size(); i++) {
			sb.append(labels.get(i)+":"+freqs.get(i)+"\t");
		}
		return sb.toString().trim();
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < condition.size(); i++) {
			sb.append("\tc"+intCondition.get(i)+"="+condition.get(i));
		}
		for (int i = 0; i < labels.size(); i++) {
			sb.append("\t"+labels.get(i)+"("+freqs.get(i)+")");
		}
		return sb.toString().trim();
	}

	public boolean accept(Instance cond){
		if(cond==null || cond.numAttributes()==0)return false;
		if(intCondition.size()==0)return true;
		if(intCondition.get(intCondition.size()-1) > cond.numAttributes()-1){
			System.err.println(" error in accept "+ cond);
			System.err.println("intCondition="+intCondition);
			System.err.println("condition="+condition);
			return false;
		}
		for (int i = 0; i < intCondition.size(); i++) {
			int index=intCondition.get(i);
			if( ! condition.get(i).equals(cond.toString(index)))
				return false;
		}
		return true;
	}

	public boolean accept(List<String> cond){
		if(cond==null || cond.size()==0)return false;
		if(intCondition.get(intCondition.size()-1) > cond.size()){
			System.err.println(" error in accept "+ cond);
			System.err.println("intCondition="+intCondition);
			System.err.println("condition="+condition);
			return false;
		}

		for (int i = 0; i < intCondition.size(); i++) {
			int index=intCondition.get(i);
			if( ! condition.get(i).equals(cond.get(index)))
				return false;
		}
		return true;
	}
	
	public static McarRule groupRules(List<McarRule> rules){
		McarRule finalRule=new McarRule();
		for (McarRule mcarRule : rules) {
			for (int i = 0; i < mcarRule.labels.size(); i++) {
				finalRule.incLabelFreq(mcarRule.labels.get(i),
						mcarRule.freqs.get(i), mcarRule.doubleCondition.get(i));
			}
		}
		
		return finalRule;
	}
	public static double[] distributionForInstance2(Instance instance, List<McarRule> rules)
	throws IOException{
		McarRule finalRule=groupRules(rules);
		double[] dist = new double[instance.numClasses()];

		double sum =0.0;
		for (Integer i :finalRule. freqs) {
			sum+=i;
		}
		
		for (int i = 0; i < finalRule.labels.size(); i++) {
			double value= finalRule.doubleCondition.get(i);
			double freq = finalRule.freqs.get(i);
			double membership = freq/sum;
			dist[(int)value]=membership;
		}
		return dist;
	}
	
	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] dist = new double[instance.numClasses()];

		double sum =0.0;
		for (Integer i : freqs) {
			sum+=i;
		}
		
		for (int i = 0; i < labels.size(); i++) {
			double value= doubleCondition.get(i);
			double freq = freqs.get(i);
			double membership = freq/sum;
			dist[(int)value]=membership;
		}
		return dist;
	}
	public double classifyInstance(Instance inst){
		int maxIndex=0;
		int maxValue=freqs.get(maxIndex);
		for (int i = 0; i < freqs.size(); i++) {
			if(freqs.get(i)> maxValue){
				maxIndex = i;
				maxValue = freqs.get(i);
			}
		}

		return doubleCondition.get(maxIndex);
	}

	public List<String> predict(List<String> cond){
		List<String> result=new ArrayList<String>();
		result.add(""+predict1(cond));
		result.add(""+predictP(cond));

		for (int i = 0; i < labels.size(); i++) {
			result.add(""+labels.get(i));
			result.add(""+freqs.get(i));
		}

		return result;
	}


	public double predict1(List<String> cond){
		String cLabel=cond.get(cond.size()-1);
		if(labels.get(0).equals(cLabel))
			return 1;
		else
			return 0;
	}

	public double predictP(List<String> cond){

		String cLabel=cond.get(cond.size()-1);
		int sum=0;
		for (int i = 0; i < labels.size(); i++) {
			String label=labels.get(i);
			int freq= freqs.get(i);
			sum+=freq;

			if(cLabel.equals(label)){
				return (double)freq/sum;
			}
		}
		return 0;

	}

	public static double predictOneInstance(List<McarRule> rules, Instance inst, BufferedWriter out){

		return 0.0;
	}
	public static List<Double> predictResults(List<McarRule> rules,List<String> lines, String outFile) throws IOException{

		BufferedWriter out= null;
		if(outFile !=null){
			out=new BufferedWriter (new FileWriter(outFile));
		}


		List<Double> result=new ArrayList<Double>();
		double p1=0,p2=0;

		for (String s : lines) {
			String[] parts=s.split(",");
			List<String> cond=new ArrayList<String>();
			for (int i = 1; i < parts.length; i++) {
				cond.add(parts[i]);
			}

			for (McarRule rule : rules) {
				if ( rule.accept(cond)){
					StringBuffer sb=new StringBuffer(s);
					sb.append(" ,");
					for (String i : rule.predict(cond)) {
						sb.append(","+i);
					}
					sb.append("\n");

					p1+=rule.predict1(cond);
					p2+=rule.predictP(cond);

					if(out != null){
						out.write(sb.toString());
					}
					break;
				}
			}
		}
		double sz= lines.size();
		p1=(double)p1/sz;
		p2=(double)p2/sz;

		result.add(p1);
		result.add(p2);

		if(out !=null){
			for (Double d : result) {
				out.write("\n"+d);
			}
			out.close();
		}

		return result;
	}
}


