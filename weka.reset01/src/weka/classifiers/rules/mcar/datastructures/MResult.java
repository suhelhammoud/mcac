package weka.classifiers.rules.mcar.datastructures;

import java.io.Serializable;
import java.util.List;

import weka.classifiers.rules.mcar.datastructures.RCounter.RTAG;

public class MResult implements Serializable{
	
	public double confidence, support;
	public boolean isOldMCAR=true;
	public boolean addDefaultClass=true;
	public boolean validateS = true;
	public boolean isVerbos=false;
	
	public int numberOfRules=0;
	public RCounter rcounter=new RCounter();
	public List<McarRule> rules;
	public double runTime;
	
	public double numCorrectClassification;
	public double numWrongClassification;
	public double numUnClassified;
	
	public double numClassified;

	public StringBuffer logger;
	

	
	public void add(MResult r){
		rcounter.add(r.rcounter);
		runTime+= r.runTime;
		numCorrectClassification+= r.numCorrectClassification;
		numWrongClassification += r.numWrongClassification;
		numUnClassified += r.numUnClassified;
		numClassified += r.numClassified;
		
		if(isVerbos)
			logger.append(r.logger);
	}
	public void log(String s){
		if(isVerbos && logger !=null)
			logger.append(s);
	}
	
	
	public String printRules(){
		StringBuffer sb=new StringBuffer();
		for (McarRule rule : rules) {
			sb.append("\n"+rule);
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		if(isVerbos)
			sb.append(logger);
		
		sb.append("\ntnumRules\ttime\taccuracy\tcorrect\twrong\tunclassifed\tall");
		for (RTAG tag : RTAG.values()) {
			sb.append("\t"+tag.toString());
		}
		
		sb.append("\n"+numberOfRules);
		sb.append("\t"+runTime);
		sb.append("\t"+(numCorrectClassification/numClassified));
		sb.append("\t"+numCorrectClassification);
		sb.append("\t"+numWrongClassification);
		sb.append("\t"+numUnClassified);
		sb.append("\t"+numClassified);
		
		for (RTAG tag : RTAG.values()) {
			sb.append("\t"+ rcounter.get(tag));
		}
		
		return sb.toString();
	}
}
