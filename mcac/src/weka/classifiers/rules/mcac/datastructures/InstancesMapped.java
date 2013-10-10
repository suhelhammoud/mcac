package weka.classifiers.rules.mcac.datastructures;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instance;
import weka.core.Instances;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

/**
 * Map arff file to weka.core.Instances object.
 * Init existingColumns : Map<Integer, Map<ColumnID,ColumnItems>>
 * intCols: mapping all items to integer of first occurances
 * @author suheil
 *
 */
public class InstancesMapped {

	
	private static final Logger logger = LoggerFactory
			.getLogger(InstancesMapped.class);
	
	public static InstancesMapped of(String filename){
		return new InstancesMapped(instancesOf(filename));
	}

	
	public InstancesMapped(Instances data){
		this.instances = data;
		existingColumns = new ArrayList<>();
	}
	
//	public InstancesMapped(Instances data, int labelIndex){
//		this.instances = data;
//		existingColumns = new HashMap<>();
//	}
	
	
	
	public final Instances instances;
	
	/** Map<ColumnID.size Map<ColumnID, ColumnItems>>  */
	final public List<Map<ColumnID,ColumnItems>> existingColumns;

	/**Dataset mapped to integer number represents the first occurances
	 *Map<Attribute, Map<Line Number, First Occurance> > */
	private Map<Integer, Map<Integer, Integer>> intCols;
	
	public Map<Integer, Integer> getIntCol(int att){
		return intCols.get(att);
	}

	public List<Integer> getColsIndexes(){
		return new ArrayList<>(intCols.keySet());
	}
	
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
//		intCols = mapInstances(instances);
		intCols = mapInstancesParallel(instances);
	}
	
	public void setClassIndex(int index){
		instances.setClassIndex(index);
	}
	public int getClassIndex(){
		int lblIndex = instances.classIndex();
		if(lblIndex == -1)
			return Collections.max(getColsIndexes());
		else
			return lblIndex;
	}
	
	
	public static Map<Integer, Map<Integer, Integer>> mapInstancesParallel(final Instances data){
		
		logger.info("Mapped instances in parallel");
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		logger.debug("statred parallel thread pool with {} processors", nrOfProcessors);

		final int numInstances = data.numInstances(); 
		final int numAttributes = data.numAttributes();

		//initialize the return result
		final Map<Integer, Map<Integer, Integer>> result= new HashMap<>(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			result.put(i, new HashMap<Integer,Integer>((numInstances)));
		}

		
		//setup imap data structure to keep the first occurrence 
		List<Map<Double, Integer>> imaps=
				new ArrayList<Map<Double,Integer>>(numAttributes);

		for (int i = 0; i < numAttributes; i++) {
			final int att = i;
			exec.execute(new Runnable() {
				
				@Override
				public void run() {
					Map<Integer, Integer> attResult = new HashMap<>(numInstances);
					Map<Double, Integer> imap = new HashMap<>();
					for (int lineNumber = 0; lineNumber < numInstances; lineNumber++) {
						Instance inst = data.instance(lineNumber);
						Double value = inst.value(att);
						if (value== Instance.missingValue())
							continue;
						Integer firstOcc = imap.get(value);
						if (firstOcc == null){
							firstOcc = lineNumber;
							imap.put(value, lineNumber);
						}
						
						attResult.put(lineNumber, firstOcc);
					}
					
					logger.debug("att {} distinct values {}", att, imap.size());
					
					synchronized(result){
						result.put(att, attResult);
					}
				}
				
			});
		}

		exec.shutdown();
		try {
			boolean okDoneAllTasks = exec.awaitTermination(1800,
					TimeUnit.SECONDS);// TODO adjust timing
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			logger.error("exception with message {} ", e.toString());
			e.printStackTrace();
		}
		
		logger.info("mapped {} attributes ", result.size());

		return result;

	}
	/**
	 * Map dataset (not sparsed elements) to intCol format
	 * @param data: Weka Instances
	 * @return Map<Attribute, Map<Line Number, First Occurance> >
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
