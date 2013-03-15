package weka.classifiers.rules.mcac.datastructures;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import weka.filters.unsupervised.attribute.ReplaceMissingValues;


public class RuleHolder {


	private volatile RuleID ruleid;
	final RuleComparator comparator;
	
	public RuleHolder(RuleComparator comparator) {
		this.comparator = comparator;
	}
	
	
	
	
	synchronized public void updateRule(RuleID rl){
		ruleid = rl == null ? ruleid
				:comparator.better(ruleid, rl);
		
		ConcurrentHashMap<Integer, Integer> test;
		
	}
	
	
	
	
	
	public RuleID getRuleID(){
		return ruleid;
	}
}
