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

import weka.classifiers.Classifier;
import weka.classifiers.rules.mcac.datastructures.ColumnID;
import weka.classifiers.rules.mcac.datastructures.ColumnItems;
import weka.classifiers.rules.mcac.datastructures.InstancesMapped;
import weka.classifiers.rules.mcac.datastructures.RuleID;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Used for reading the weka arff file to weka.
 * 
 * @author suheil
 * 
 */
public class McacDriver {

	/**
	 * 
	 * @param data
	 *            , instances, mapped integers, and parameters
	 * @param order
	 *            candidate rule size
	 * @return Map<ColumnsID, MapOfFrequentItems>
	 */
	public static Map<ColumnID, ColumnItems> generateOrderedFrequentItems(
			final InstancesMapped data, int order) {

		final Map<ColumnID, ColumnItems> result = Collections
				.synchronizedMap(new HashMap<ColumnID, ColumnItems>());// TODO
																		// estimate
																		// the
																		// initial
																		// capacity

		final Map<ColumnID, ColumnItems> inColumns = data.existingColumns
				.get(order - 1);// TODO use collection directly

		List<ColumnID> ids = new ArrayList<>(data.existingColumns
				.get(order - 1).keySet());

		List<ColumnID> idsCombined = ColumnID.combined(ids);
		// check no combination
		if (idsCombined.size() == 0)
			return result;

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (final ColumnID colid : idsCombined) {
			exec.execute(new Runnable() {

				@Override
				public void run() {
					ColumnItems col = ColumnItems.join(colid,
							inColumns.get(colid.dropLast()),
							inColumns.get(colid.dropFirst()),
							data.getMinSupport(), data.getMinConfidence());

					if (col == ColumnItems.ZERO)
						return;

					synchronized (result) {
						Object success = result.put(colid, col);
						assert success == null;
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
			e.printStackTrace();
		}

		return result;
	}

	public static Map<ColumnID, ColumnItems> generateAtomicFrequentItems(
			final InstancesMapped data) {

		final Map<ColumnID, ColumnItems> results = Collections
				.synchronizedMap(new HashMap<ColumnID, ColumnItems>(
						data.instances.numAttributes()));// TODO estimate the
															// initial capacity

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (final Integer key : data.intCols.keySet()) {
			exec.execute(new Runnable() {

				@Override
				public void run() {
					ColumnItems col = ColumnItems.of(key);
					col.generateAtomicValues(data);

					if (col.size() == 0)
						return;

					synchronized (results) {
						Object success = results.put(ColumnID.of(key), col);
						assert success == null;// column not previously mapped
					}
					;
				}
			});
		}

		exec.shutdown();
		try {
			boolean okDoneAllTasks = exec.awaitTermination(1800,
					TimeUnit.SECONDS);// TODO adjust timing
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return results;
	}

	public static void filterNotSurrvivedConfidences(
			Map<ColumnID, ColumnItems> subMap, final double minConf) {

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (Iterator<Map.Entry<ColumnID, ColumnItems>> iter = subMap
				.entrySet().iterator(); iter.hasNext();) {
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
			boolean okDoneAllTasks = exec.awaitTermination(180,
					TimeUnit.SECONDS);
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// prune zero columns
		for (Iterator<Map.Entry<ColumnID, ColumnItems>> iter = subMap
				.entrySet().iterator(); iter.hasNext();) {
			final ColumnItems col = iter.next().getValue();
			if (col.size() == 0)
				iter.remove();
		}

	}

	/**
	 * changes added to data.existingColumns
	 * 
	 * @param data
	 */
	public static void generateFrequentItems(InstancesMapped data) {
		Map<ColumnID, ColumnItems> atomics = generateAtomicFrequentItems(data);

		if (atomics.size() == 0)
			return;// No more heigher order columns

		data.existingColumns.put(1, atomics);

		for (int order = 2; order < data.intCols.size(); order++) {
			Map<ColumnID, ColumnItems> subMap = generateOrderedFrequentItems(
					data, order);
			if (subMap.size() == 0)
				return;// No more heigher order columns

			data.existingColumns.put(order, subMap);

			// TODO save more memroy delete frequentitems which are not rules
			filterNotSurrvivedConfidences(data.existingColumns.get(order-1),
					data.getMinConfidence());

		}
	}
	
	public static Classifier getClassifier(InstancesMapped data){
		return new Classifier() {
			
			@Override
			public void buildClassifier(Instances data) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public double classifyInstance(Instance instance) throws Exception {
				// TODO Auto-generated method stub
				return super.classifyInstance(instance);
			}
		};
		
	}

	public static Map<Integer, RuleID> mapRuleSurvivedRulesToLines(
			InstancesMapped data) {

		return null;
	}

}
