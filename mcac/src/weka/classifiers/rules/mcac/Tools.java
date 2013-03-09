package weka.classifiers.rules.mcac;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import weka.core.Instance;
import weka.core.Instances;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

class AttributeMapper extends RecursiveTask<List<Map<Integer, Integer>>>{

	final int attIndex;
	final Instances data;
	final int lastAtt;

	public AttributeMapper(Instances data, int attIndex, int lastAtt) {
		this.data = data;
		this.attIndex = attIndex;
		this.lastAtt = lastAtt;
	}
	
	@Override
	protected List<Map<Integer, Integer>> compute() {
		if(attIndex == lastAtt)
			return computeDirectly();
		else{
			AttributeMapper left = new AttributeMapper(data, attIndex, attIndex );
			AttributeMapper right = new AttributeMapper(data, attIndex+1, lastAtt );
			
			right.fork(); 
			
			List<Map<Integer, Integer>> leftAns =  left.compute();
			
			List<Map<Integer, Integer>> rightAns = right.join();
			
			leftAns.addAll(rightAns);
			
			return leftAns;
		}
	}
	
	private List<Map<Integer, Integer>> computeDirectly(){
		List<Map<Integer, Integer>> result = new ArrayList<>();
		result.add(Tools.mapAttribute(data, attIndex));
		return result;
	}



}

public class Tools {



	public static List<Map<Integer, Integer>> mapInstancesForkJoin(
			final Instances data){
		final int numAttributes = data.numAttributes();

		 ForkJoinPool fjPool = new ForkJoinPool(8);
		 
		  return fjPool.invoke(new AttributeMapper(data, 0, numAttributes-1));

	}
	
	public static Map<Integer, Integer> mapAttribute(
			Instances data, int attIndex){
		final int numInstances = data.numInstances(); 
		assert attIndex < data.numAttributes();

		Map<Integer, Integer> result = new HashMap<>(numInstances);

		Map<Double, Integer> imap = new HashMap<>();

		for (int lineNumber = 0; lineNumber < numInstances; 
				lineNumber++) {
			Double value = data.instance(lineNumber).value(attIndex);
			if( value == Instance.missingValue())
				continue;

			Integer firstOcc = imap.get(value);

			if(firstOcc == null){
				firstOcc =lineNumber;
				imap.put(value, lineNumber);
			}

			result.put(lineNumber, firstOcc);
		}

		return result;

	}

	

	

	
	public static void main(String[] args) {
//		
	}
	
	static void testMapInstancesForkJoin(Instances data){
		System.out.println(data.numAttributes());
		System.out.println(data.numInstances());
		
		List<Map<Integer, Integer>> result = mapInstancesForkJoin(data);
		
		System.out.println("total Number "+ result.size());
//		printMap(result);
		
	}
	
}
