package weka.classifiers.rules.mcac.datastructures;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;


@SuppressWarnings("serial")
public class ColumnItems extends LinkedHashMap<Integer, FrequentItem>{

	public final ColumnID colid;
	
	final public static ColumnItems ZERO = of();

	//	public final Instances data;

	public static ColumnItems of(ColumnID colid){
		return new ColumnItems(colid);
	}
	public static ColumnItems of(int ...clids){
		return new ColumnItems(ColumnID.of(clids));
	}

	public static ColumnItems of(Collection<Integer> clids){
		return new ColumnItems(ColumnID.of(clids));
	}

	private ColumnItems(ColumnID colid) {
		this.colid = colid;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("ColID:", colid)
				.addValue( Joiner.on("\n\t")
						.withKeyValueSeparator(" -> ")
						.join(this))
						.toString();
	}

	public static void main(String[] args) {

		testGenerateAtomic();


	}

	private static void testGenerateAtomic(){
		InstancesMapped data = InstancesMapped.of("data/in/contact.arff");

		data.setMinSupport(0);
		data.setMinConfidence(0.0);

		data.toIntCols();

		for (Integer key : data.intCols.keySet()) {
			ColumnItems col = ColumnItems.of(key);
			List<Integer> itemsId = col.generateAtomicValues(data);

			System.out.println(col);
		}

	}
	private static void testColumnItems() {
		InstancesMapped data = InstancesMapped.of("data/in/contact.arff");

		data.setMinSupport(0);
		data.setMinConfidence(0.0);

		data.toIntCols();

		int attIndex = 0;//data.numAttributes()-1;

		ColumnItems col = ColumnItems.of(attIndex);

		System.out.println("AttIndex "+ attIndex + "\n"
				+ Joiner.on("\n").withKeyValueSeparator("  ->  ")
				.join(data.intCols.get(attIndex)));


		col.generateOccurances(data.intCols.get(attIndex), 
				data.intCols.get(data.instances.numAttributes()-1));

		System.out.println("map Item 1 col \n" + col.toString());
	}

	//TODO change return value to void
	public List<Integer> generateAtomicValues(InstancesMapped data){

		assert colid.size() == 1;
		int atomicIndex = colid.getAtomic();
		int labelIndex = data.instances.numAttributes()-1;



		List<Integer> result = new ArrayList<>(
				data.intCols.get(atomicIndex).size());//trying to guess max list size

		generateOccurances(data.intCols.get(atomicIndex), 
				data.intCols.get(labelIndex));

		Iterator<Integer> iter = keySet().iterator();
		while(iter.hasNext()){
			int itemID = iter.next();
			FrequentItem item = get(itemID);
			Calc calc = item.getCalc();
			if(calc.support < data.getMinSupport()){
				iter.remove();
				continue;
			}

			if(calc.confidence < data.getMinConfidence()){
				continue;
			}
			result.add(itemID); 
		}
		
		
		return result;
	}
	
	public static ColumnItems join(ColumnItems col1, ColumnItems col2, 
			int minsupp, double minconf){
		
		ColumnID colid = ColumnID.join(col1.colid, col2.colid);
		if(colid ==  ColumnID.ZERO) return ZERO;
		
		List<Integer> candidateItemsID = new ArrayList<>(col1.keySet());
		candidateItemsID.retainAll(col2.keySet());
		
		if(candidateItemsID.size() == 0)
			return ZERO;
		
		ColumnItems result = of(colid);
		for (Integer itemID : candidateItemsID) {
			FrequentItem freq = FrequentItem.intersect(col1.get(itemID), col2.get(itemID));
			if( freq == FrequentItem.EMPTY) continue;
			
			Calc calc = freq.getCalc();
			if(calc.support < minsupp) continue;
			
			result.put(calc.rowId, freq);
		}
		
		if(result.size() == 0)
			return ZERO;
		
		return result;
	}
	

	/**
	 * remove items did not get over the confidence threshold
	 * Could be used for saving memory from extra data to optimize
	 * the application performance
	 * Should not be used until generating the next higher frequent items is 
	 * completely finished
	 * @param minConf
	 */
	public void filterNotSurvived(double minConf){
		for (Iterator<Entry<Integer, FrequentItem>> iter =
				this.entrySet().iterator(); iter.hasNext();) {
			if(iter.next().getValue().getCalc().confidence < minConf)
				iter.remove();
		}
	}
	
	public static List<Integer> generateValues(InstancesMapped data, 
			ColumnItems col1, ColumnItems col2, int minSupp, double minConf){
		List<Integer> result = new ArrayList<>();
		
		
		List<Integer> candidateItemsID = new ArrayList<>(col1.keySet());
		candidateItemsID.retainAll(col2.keySet());
		
		if(candidateItemsID.size() == 0)
			return result;
		
		
		
		assert ColumnID.join(col1.colid, col2.colid).size() == col1.size()+1;
		
		return result;
		
	}

	public void generateOccurances(Map<Integer, Integer> itemMap,
			Map<Integer, Integer> labelMap){
		//paranoid about the start condition !
		clear();
		for (Map.Entry<Integer, Integer> e : itemMap.entrySet()) {
			Integer line = e.getKey();
			Integer itemId = e.getValue();
			FrequentItem item = get(itemId);
			if(item == null){
				item = new FrequentItem();
				this.put(itemId, item);
			}
			item.put(labelMap.get(line), line);
		}
	}

	

}
