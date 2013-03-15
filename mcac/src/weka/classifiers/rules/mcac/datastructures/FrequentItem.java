package weka.classifiers.rules.mcac.datastructures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class FrequentItem {
	
	
	
	public final static FrequentItem EMPTY= new FrequentItem();
	
	public static FrequentItem getDefaultItem(Map<Integer, Integer> labelMap){
		FrequentItem result = new FrequentItem();
		for (Map.Entry<Integer, Integer> e : labelMap.entrySet()) {
			result.put(e.getValue(), e.getKey());
		}
		return result;
	}
	
	public static Calc calculate(FrequentItem item){
		
		int support = Integer.MIN_VALUE;
		double confidence = 0;
		int rowId = Integer.MAX_VALUE;
		int label = Integer.MAX_VALUE;
		
		//get the higest support
		for (Integer lbl : item.lbln.keySet()) {
			int sz = item.lbln.get(lbl).size();
			if( sz > support){
				support = sz;
				label = lbl;
			}
		}
		
		confidence = (double)support / item.lbln.size();
		rowId = Collections.min(item.lbln.values());
		
		Calc result = new Calc(support, confidence, rowId, label);
		return result;
		
	}
	
	
	
	public static FrequentItem intersect(FrequentItem item1,
			FrequentItem item2){
		Set<Integer> keys = new HashSet<>(item1.lbln.keySet());
		keys.retainAll(item2.lbln.keySet());
		
		if(keys.size() == 0)
			return EMPTY;
		
		FrequentItem result = new FrequentItem();
		
		for (Integer key : keys) {
			Set<Integer> set =new HashSet<>(item1.lbln.get(key));
			set.retainAll(item2.lbln.get(key));
			if(set.size() == 0) continue;
			result.lbln.putAll(key, set);
		}
		if(result.lbln.size() == 0)
			return EMPTY;
		else
		    return result;
	}
	
	
	
	final private Multimap<Integer, Integer> lbln;
	private Calc calc;

	
	public FrequentItem() {
		lbln = HashMultimap.create();
	}
	
	
	private Calc calculate(){
		this.calc = calculate(this);
		return calc;
	}
	
	public Calc getCalc(){
		return calc == null? calculate(): calc;
	}
	
	/**
	 * Put label, line
	 * @param lbl
	 * @param line
	 * @return
	 */
	public boolean put(int lbl, int line){
		return lbln.put(lbl, line);
	}
	
	public Collection<Integer> get(int lbl){
		return lbln.get(lbl);
	}
	
	public Collection<Integer> values(){
		return lbln.values();
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.addValue(
				Joiner.on(", ")
				.withKeyValueSeparator("->")
				.join(lbln.asMap())
				).toString();
	}
	
	public static void main(String[] args) {
		FrequentItem f = new FrequentItem();
		f.put(1,11);
		f.put(1,111);
		f.put(2, 22);
		f.put(7, 22);
		
		System.out.println(f );
		
	}
	
}

class Calc{
	public final int support;
	public final double confidence;
	public final int rowId;
	public final int label;
	
	public Calc(int support, double confidence, int rowId, int label) {
		this.support = support;
		this.confidence = confidence;
		this.rowId = rowId;
		this.label = label;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		.add("support", support)
		.add("confidence", confidence)
		.add("rowId", rowId)
		.add("label", label).toString();
	}
}

