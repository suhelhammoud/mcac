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

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Lines extends ConcurrentHashMap<Integer, RuleID>{
	
	public static enum LABEL_RULE {SAME_LABEL, ANY_LABEL};
	
	public final RuleComparator comparator;
	public final LABEL_RULE labelRule;
	
	public Lines(int initialCapacity, RuleComparator comparator
			, LABEL_RULE labelRule){
		super(initialCapacity);
		this.comparator = comparator;
		this.labelRule = labelRule;
		
	}
	
	public static Lines of(int initialCapacity, 
			RuleComparator.RANK_ID rank, LABEL_RULE labelRule){
		return new Lines(initialCapacity, RuleComparator.of(rank), labelRule);
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
	
	public void coverRules(Collection <Map<ColumnID,ColumnItems>> columns){
//		clear();
		
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);
		

		for (Map<ColumnID, ColumnItems> map : columns) {
			for (final ColumnItems colitems : map.values()) {
				final ColumnID colid = colitems.colid;
				
				for (final FrequentItem fitem : colitems.values()) {
					exec.execute(new Runnable() {
						
						@Override
						public void run() {
							mapFrequentItem(colid, fitem);
						}
					});
				}
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
	
	
	public static void main(String ... args){
		List<Integer> lst = Lists.newArrayList(1,2,3,4,5,6,7,8,9);
		for (final Integer i : lst) {
			System.out.println(i);
		}
		System.out.println("done");
	}
}
