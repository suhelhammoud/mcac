package weka.classifiers.rules.mcac.datastructures;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class InstancesMapped {

	
	public static InstancesMapped of(String filename){
		return new InstancesMapped(instancesOf(filename));
	}

	
	public InstancesMapped(Instances data){
		this.instances = data;
		existingColumns = new HashMap<>();
	}
	
	public final Instances instances;
	
	public Map<Integer, Map<Integer, Integer>> intCols;
	
	final public Map<Integer, Map<ColumnID,ColumnItems>> existingColumns;
	
	private int minSupport;
	private double minConfidence;
	
	
	public int getMinSupport(){
		return minSupport;
	}
	
	public double getMinConfidence(){
		return minConfidence;
	}

	public void setMinSupport(int minsupp){
		this.minSupport = minsupp;
	}
	
	public void setMinConfidence(double conf){
		this.minConfidence = conf;
	}
	public void toIntCols(){
		intCols = mapInstances(instances);
	}
	
	
	
	
	/**
	 * Map not sparse elements
	 * @param data
	 * @return
	 */
	public static Map<Integer, Map<Integer, Integer>> mapInstances(
			Instances data){
		final int numInstances = data.numInstances(); 
		final int numAttributes = data.numAttributes();

		//initialize the return result
		Map<Integer, Map<Integer, Integer>> result= new HashMap<>(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			result.put(i, new HashMap<Integer,Integer>((numInstances)));
		}

		//setup imap data structure to keep the first occurrence 
		List<Map<Double, Integer>> imaps=
				new ArrayList<Map<Double,Integer>>(numAttributes);

		for (int i = 0; i < numAttributes; i++) {
			imaps.add(new HashMap<Double, Integer>());
		}



		for (int lineNumber = 0; lineNumber < numInstances; lineNumber++) {
			Instance inst = data.instance(lineNumber);
			for (int att = 0; att < numAttributes; att++) {
				Double value = inst.value(att);
				if( value == Instance.missingValue())
					continue;
				Map<Double, Integer> imap = imaps.get(att);

				Integer firstOcc = imap.get(value);

				if(firstOcc == null){
					firstOcc =lineNumber;
					imap.put(value, lineNumber);
				}

				result.get(att).put(lineNumber, firstOcc);
			}
		}
		return result;

	}
	
	public static Instances instancesOf(String fileName){
		Instances data = null;
		try {
			data = new Instances(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	
	public String testPrintMappedInstances(){
		StringBuilder result = new StringBuilder();
		for (Map<Integer, Integer> m : intCols.values()) {
			result.append( Joiner.on("\n").withKeyValueSeparator("->").join(m));
			result.append("\n\n--------------------------------\n");
		}
		return result.toString();

	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("instances", instances.relationName())
				.add("minSupport", minSupport)
				.add("minConfidence", minConfidence)
				.add("atomics", intCols == null? "null": intCols.keySet())
				.addValue("\n").toString();
//				.add("intCols", 
//						Joiner.on("\n\t")
//						.withKeyValueSeparator("->").join(intCols))
//						
//				.toString();
		
	}
}