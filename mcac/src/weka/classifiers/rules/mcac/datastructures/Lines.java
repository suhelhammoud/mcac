package weka.classifiers.rules.mcac.datastructures;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Lines extends ConcurrentHashMap<Integer, RuleID>{
	
	
	public final RuleComparator comparator;
	
	public Lines(int initialCapacity, RuleComparator comparator){
		this.comparator = comparator;
	}
	
	public static Lines of(int initialCapacity, RuleComparator.RANK_ID rank){
		return new Lines(initialCapacity, RuleComparator.of(rank));
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
		for (Integer line : feq.values()) {
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
							// TODO Auto-generated method stub
							mapFrequentItem(colid, fitem);
							
						}
					});
				}
			}
		}

		exec.shutdown();
		
		try {
			boolean okDoneAllTasks = exec.awaitTermination(180, TimeUnit.SECONDS);
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	};


	public static void main(String ... args){
		List<Integer> lst = Lists.newArrayList(1,2,3,4,5,6,7,8,9);
		for (final Integer i : lst) {
			System.out.println(i);
		}
		System.out.println("done");
	}
}
