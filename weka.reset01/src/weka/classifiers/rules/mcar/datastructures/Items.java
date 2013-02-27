package weka.classifiers.rules.mcar.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


@SuppressWarnings("serial")
public class Items extends HashMap<IntBag, FrequentItem>{
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		BagList list=new BagList(this.keySet());
		Collections.sort(list);
		
		StringBuffer sb=new StringBuffer();
		for (IntBag id : list) {
			sb.append(id+ "\t"+ get(id));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	
	public void calc(){
		for (FrequentItem fi : values()) {
			fi.calc();
		}
	}
}