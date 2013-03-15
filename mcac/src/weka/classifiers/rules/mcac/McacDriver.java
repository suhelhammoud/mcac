package weka.classifiers.rules.mcac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import weka.classifiers.rules.mcac.datastructures.ColumnID;
import weka.classifiers.rules.mcac.datastructures.ColumnItems;
import weka.classifiers.rules.mcac.datastructures.InstancesMapped;
import weka.classifiers.rules.mcac.datastructures.RuleID;

public class McacDriver {

	/**
	 * 
	 * @param data , instances, mapped integers, and parameters
	 * @param order
	 * @return
	 */
	public static Map<ColumnID,ColumnItems> generateOrderedFrequentItems(
			InstancesMapped data, int order){

		Map<ColumnID, ColumnItems> result = Collections.synchronizedMap(
				new HashMap<ColumnID, ColumnItems>());//TODO estimate the initial capacity

		List<ColumnItems> inColumns = new ArrayList<>(
				data.existingColumns.get(order-1).values());//TODO use collection directly

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (int i = 0; i < inColumns.size()-1; i++) {
			for (int j = i+1 ; j < inColumns.size(); j++) {

				JoinColumns joiner = new JoinColumns(data, inColumns.get(i), inColumns.get(j), result);

				exec.execute(joiner);
			}
			exec.shutdown();
			try {
				boolean okDoneAllTasks = exec.awaitTermination(180, TimeUnit.SECONDS);
				assert okDoneAllTasks;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	public static Map<ColumnID, ColumnItems> generateAtomicFrequentItems(InstancesMapped data){

		Map<ColumnID, ColumnItems> results = Collections.synchronizedMap(
				new HashMap<ColumnID, ColumnItems>(data.instances.numAttributes()));//TODO estimate the initial capacity

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (Integer key : data.intCols.keySet()) {
			AtomicColumns atomic = new AtomicColumns(data, key, results);
			exec.execute(atomic);
		}

		exec.shutdown();
		try {
			boolean okDoneAllTasks = exec.awaitTermination(180, TimeUnit.SECONDS);
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return results;
	}

	public static void filterNotSurrvivedConfidences(Map<ColumnID, ColumnItems> subMap, final double minConf){
		
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (Iterator<Map.Entry<ColumnID, ColumnItems>> iter = subMap.entrySet().iterator(); iter.hasNext();) {
			final ColumnItems col = iter.next().getValue();
			
			exec.execute(new Runnable() {
				@Override
				public void run() {	
					col.filterNotSurvived(minConf);
				}
			});
			
		}
		
		exec.shutdown();
		
		try {
			boolean okDoneAllTasks = exec.awaitTermination(180, TimeUnit.SECONDS);
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//prune zero columns
		for (Iterator<Map.Entry<ColumnID, ColumnItems>> iter = subMap.entrySet().iterator(); iter.hasNext();) {
			final ColumnItems col = iter.next().getValue();
			if(col.size() == 0) iter.remove();
		}
		
	}
//
//	public static void filterNotSurrvivedConfidences(InstancesMapped data, int order){
//		Map<ColumnID, ColumnItems> previousOrder = data.existingColumns.get(order);
//		
//		if(previousOrder.size() == 0)
//			data.existingColumns.remove(order);
//	}
	
	public static void generateFrequentItems(InstancesMapped data){
		Map<ColumnID, ColumnItems> atomics = generateAtomicFrequentItems(data);

		if(atomics.size() == 0) return;

		data.existingColumns.put(1, atomics);

		for (int order = 2; order < data.intCols.size(); order++) {
			Map<ColumnID, ColumnItems> joins = generateOrderedFrequentItems(data, order);			
			if(joins.size() == 0) return;
			
			data.existingColumns.put(order, joins);
			
			/** save more memroy delete frequentitems which are not rules */
			//filterNotSurrvivedConfidences(data, order-1);//TODO test for 
			
		}
	}

	public static Map<Integer, RuleID> mapRuleSurvivedRulesToLines(InstancesMapped data){
		
		return null;
	}


}

class AtomicColumns implements Runnable{
	final public int colid;
	final private InstancesMapped data;
	final private Map<ColumnID, ColumnItems> result;

	public AtomicColumns(InstancesMapped data, int colid,  Map<ColumnID, ColumnItems> result) {
		this.data = data;
		this.colid = colid;
		assert result != null;
		this.result = result;
	}

	@Override
	public void run() {
		ColumnItems col = ColumnItems.of(colid);
		col.generateAtomicValues(data);

		if(col.size() == 0) return;


		synchronized (result) {
			Object success = result.put(col.colid, col);
			assert success == null;// column not previously mapped
		};

	}

}

class JoinColumns implements Runnable{

	final private ColumnItems col1, col2;
	final private int minsupport;
	final private double minconfidence;
	final private Map<ColumnID, ColumnItems> result;


	public JoinColumns(InstancesMapped data, ColumnItems col1, ColumnItems col2, 
			Map<ColumnID, ColumnItems> result) {
		
		this.col1 = col1;
		this.col2 = col2;
		this.minsupport = data.getMinSupport();
		this.minconfidence = data.getMinConfidence();
		this.result = result;
	}



	@Override
	public void run() {
		ColumnItems col = ColumnItems.join(col1,
				col2, minsupport, minconfidence);	

		if(col == ColumnItems.ZERO)
			return;

		synchronized (result) {
			Object success = result.put(col.colid, col);
			assert success == null;
		}
	}

}
