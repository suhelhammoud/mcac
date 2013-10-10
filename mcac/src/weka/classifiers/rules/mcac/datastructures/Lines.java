package weka.classifiers.rules.mcac.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.rules.mcac.datastructures.RuleComparator.RANK_ID;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Lines extends ConcurrentHashMap<Integer, RuleID>{
	
	
	private static final Logger logger = LoggerFactory.getLogger(Lines.class);
	
	public static enum LABEL_RULE {SAME_LABEL, ANY_LABEL};
	public final RANK_ID ruleRank;
	public final RuleComparator comparator;
	public final LABEL_RULE labelRule;
	
	public Lines(int initialCapacity, RANK_ID rank
			, LABEL_RULE labelRule){
		super(initialCapacity);
		this.ruleRank = rank;
		this.comparator = RuleComparator.of(ruleRank);
		this.labelRule = labelRule;
		
		logger.info("Init Lines with initialCapacity {}, Comparator {} and LABEL_RULE {}", 
				initialCapacity, comparator.getClass().getName(), labelRule);
		
	}
	
	public static Lines of(int initialCapacity, 
			RuleComparator.RANK_ID rank, LABEL_RULE labelRule){
		return new Lines(initialCapacity, rank, labelRule);
	}
	
	private void insertOrReplace(Integer key, RuleID value) {
	    for (;;) {
	        RuleID oldValue = putIfAbsent(key, value);
	        if (oldValue == null)
	            return;

	        final RuleID newValue = comparator.better(oldValue, value);
	        if (replace(key, oldValue, newValue))
	            return;
	    }
	}
	
	public void mapFrequentItem(ColumnID colid, FrequentItem feq){
		RuleID ruleid = RuleID.of(colid, feq);
		//logger.debug("toLines fi of colid:{}, rowId:{}",colid, ruleid.rowid  );
		
		Collection<Integer> lines;
		
		if(labelRule == LABEL_RULE.ANY_LABEL)
			lines = feq.values();
		else{// SAME_LABEL
			lines = feq.get(feq.getCalc().label);
		}
			
		for (Integer line : lines) {
			insertOrReplace(line, ruleid);
		}			
	}
	
	public void coverLinesWithRules(Collection <Map<ColumnID,ColumnItems>> existingColumns){
//		clear();
		
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);
		
		logger.info("coverLinesWithRules pool with {} processors", nrOfProcessors);

		for (Map<ColumnID, ColumnItems> subMap : existingColumns) {
			for (final Map.Entry<ColumnID, ColumnItems> e : subMap.entrySet()) {
				
				exec.execute(new Runnable() {
					
					@Override
					public void run() {
						logger.debug("map lines in colid:{}, number of freq items {}", e.getKey(), e.getValue().size() );
						for (FrequentItem feq : e.getValue().values()) {
							mapFrequentItem(e.getKey(), feq);
						}
					}
				});
				
			}
			
		}

		exec.shutdown();
		
		try {
			boolean okDoneAllTasks = exec.awaitTermination(1800, TimeUnit.SECONDS);
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	};


	public static List<Integer> getNotCoverredLines(Collection<Integer> initialLines,
			Collection<Integer> coveredLines){
		int linesRemained = initialLines.size() - coveredLines.size();
		
		assert linesRemained >= 0;
		
		List<Integer> result = new ArrayList<>(linesRemained);
		
		for (Integer line : initialLines) {
			if(! coveredLines.contains(line))
				result.add(line);
		}

		return result;
	}
	
	
	
	public static FrequentItem getDefaultRule(){
		return null;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("Rule Rank", ruleRank)
				.add("label match", this.labelRule)
				.addValue(
						Joiner.on("\n")
						.withKeyValueSeparator("->")
						.join(this)).toString();
	}
	
	public static void main(String ... args){
		List<Integer> lst = Lists.newArrayList(1,2,3,4,5,6,7,8,9);
		for (final Integer i : lst) {
			System.out.println(i);
		}
		System.out.println("done");
	}
	
	
}
