package weka.classifiers.rules.mcac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import weka.classifiers.Classifier;
import weka.classifiers.rules.mcac.datastructures.ColumnID;
import weka.classifiers.rules.mcac.datastructures.ColumnItems;
import weka.classifiers.rules.mcac.datastructures.InstancesMapped;
import weka.classifiers.rules.mcac.datastructures.Lines;
import weka.classifiers.rules.mcac.datastructures.Lines.LABEL_RULE;
import weka.classifiers.rules.mcac.datastructures.RuleComparator.RANK_ID;
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
	private static final Logger logger = LoggerFactory
			.getLogger(McacDriver.class);

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
		
		logger.info("frequent items of oreder {} ", order);

		
		
		final Map<ColumnID, ColumnItems> result = Collections
				.synchronizedMap(new HashMap<ColumnID, ColumnItems>());// TODO
																		// estimate
																		// the
																		// initial
																		// capacity

		final Map<ColumnID, ColumnItems> inColumns = data.existingColumns
				.get(order - 1);// TODO use collection directly

		ColumnID[] ids = inColumns.keySet().toArray(new ColumnID[inColumns.size()]);

		List<ColumnID> idsCombined = ColumnID.combined(ids);
		
		logger.info("candidate columnIDs : {} out of: {} lower", idsCombined.size(), ids.length);
		// check no combination
		if (idsCombined.size() == 0)
			return result;

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		logger.debug("statred parallel thread pool with {} processors", nrOfProcessors);
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

		logger.info("actuall columnsTem generated are {}", result.size());
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

		logger.debug("statred parallel thread pool with {} processors", nrOfProcessors);

		List<Integer> aId = data.getColsIndexes();
		aId.remove(Integer.valueOf(data.getClassIndex()));
		
		for (final Integer key : aId) {
			exec.execute(new Runnable() {

				@Override
				public void run() {
					logger.debug("generate atomic {}", key);
					ColumnItems col = ColumnItems.of(key);
					col.generateAtomicValues(data.getIntCol(key), data.getLabels(), data.getMinSupport());

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
		
		logger.info("actuall atomic columnsTem generated are {}", results.size());

		return results;
	}
	/**
	 * changes are applied on subMap, frequent item not survived to be deleted
	 * columnsItems with zero frequent items are also deleted
	 * @param subMap
	 * @param minConf
	 */

	public static void filterNotSurrvivedConfidences(
			Map<ColumnID, ColumnItems> subMap, final double minConf) {

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		logger.info("filterNotSurvivedConfidences thread pool with {} processors", nrOfProcessors);

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
		logger.info("finished filtering individual columns out of {}", subMap.size());


		// prune zero columns
		for (Iterator<Map.Entry<ColumnID, ColumnItems>> iter = subMap
				.entrySet().iterator(); iter.hasNext();) {
			final ColumnItems col = iter.next().getValue();
			if (col.size() == 0)
				iter.remove();
		}
		
		logger.info("prune zero columns remaned is {}", subMap.size());

	}

	/**
	 * changes added to data.existingColumns
	 * 
	 * @param data
	 */
	public static void generateFrequentItems(InstancesMapped data) {
		
		
		Map<ColumnID, ColumnItems> atomics = generateAtomicFrequentItems(data);
		logger.info("number of atomic columns passed the support threshold: {}", atomics.size());
		
		if (atomics.size() == 0)
			return;// No more heigher order columns

		data.existingColumns.add(atomics);

		final int maxOrder = data.getColsIndexes().size()-1;
		
		logger.info("MaxOrder value: {}", maxOrder);
		for (int order = 1; order < maxOrder; order++) {
			Map<ColumnID, ColumnItems> subMap = generateOrderedFrequentItems(
					data, order);
			if (subMap.size() == 0)
				return;// No more heigher order columns

			data.existingColumns.add( subMap);//TODO check order here

			logger.debug("filter not survived {}", order-1);
			// TODO save more memroy delete frequentitems which are not rules
			filterNotSurrvivedConfidences(data.existingColumns.get(order-1),
					data.getMinConfidence());

		}
		logger.debug("filter not survived {}", maxOrder-1);

		//filter the heighest order columns
		filterNotSurrvivedConfidences(data.existingColumns.get(maxOrder-1),
				data.getMinConfidence());

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

	
	public static void main(String[] args) {
		InstancesMapped data = InstancesMapped.of("data/in/contact.arff");
		data.toIntCols();
		
		data.setMinSupport(1);
		data.setMinConfidence(0.10);
		
		logger.debug("data size {} \n {}", data.instances.numInstances(),data.toString());
		
		logger.info("going to generate frequent items");
		generateFrequentItems(data);
		
		
		Lines lines = Lines.of(data.instances.numInstances(), RANK_ID.CONF_SUPP_CARD,
				LABEL_RULE.SAME_LABEL);
		
		logger.info("lines with instances {}, rank:{}, labelMatch: {}"
				, data.instances.numInstances(), lines.ruleRank, lines.labelRule);
		
		lines.coverLinesWithRules(data.existingColumns);
		
		
		
		
		Map<Integer,Integer> labelColumn = data.getIntCol(data.getClassIndex());

		Set<Integer> notCoveredSet = lines.getNotCoveredLines(labelColumn.keySet());
		//get uncoverred lines
		
		
		// get default rule
		logger.debug("output\n "+ lines);
		logger.info("done");
		
//		logger.info( 
//				Objects.toStringHelper("results")
//				.addValue(
//				Joiner.on("\n============>")
//				.join(data.existingColumns)
//				).toString());
//
//		logger.info("done");
		
		
	}
}
