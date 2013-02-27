package weka.classifiers.rules.mcar.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.rules.mcar.datastructures.RCounter.RTAG;

public class RCounter  implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3684695554478242509L;

	public static enum RTAG {CONF, SUPP, CARD, COL, ROW};
	
	private double[] values=new double[RTAG.values().length];
	
	public void clear(RTAG tag){
		values[tag.ordinal()]=0;
	}
	public void clearAll(){
		for (RTAG tag : RTAG.values()) {
			clear(tag);
		}
	}
	public double inc(RTAG tag){
		return ++values[tag.ordinal()];
	}
	
	public double get(RTAG tag){
		return values[tag.ordinal()];
	}
	
	public void add(RCounter r){
		for (RTAG tag : RTAG.values()) {
			values[tag.ordinal()] += r.values[tag.ordinal()];
		}
	}
	
	public double[] getValues(){
		return Arrays.copyOf(values, values.length);
	}
	@Override
	public String toString() {
		
		StringBuffer sb=new StringBuffer();
		for (RTAG tag : RTAG.values()) {
			sb.append("\t"+ tag.toString());
		}
		sb.append("\n");
		for (RTAG tag : RTAG.values()) {
			sb.append("\t"+ values[tag.ordinal()]);
		}

		return sb.toString();
	}
	
	public List<Double> getList(){
		List<Double> result=new ArrayList<Double>(values.length);
		for (Double d : values) {
			result.add(d);
		}
		return result;
		
	}
}

