package weka.classifiers.rules.mcac.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

public class ColumnItemsTest {

	@Test
	public void testGenerateAtomicValues(){
		InstancesMapped data = InstancesMapped.of("data/in/contact.arff");
		
		data.setMinConfidence(0.0);
		data.setMinSupport(0);
		data.toIntCols();
		
		
		assertTrue(data.getColsIndexes().size() == 5);
		
		List<Integer> intColsKeys = new ArrayList<>(data.getColsIndexes());
		Collections.sort(intColsKeys);
		for (Integer key : intColsKeys) {
			ColumnItems col = ColumnItems.of(key);
			col.generateAtomicValues(data.getIntCol(key), data.getLabels(), data.getMinSupport());
		}
	}
	
	@Test
	public void testGenerateOccurances() {
			InstancesMapped data = InstancesMapped.of("data/in/contact.arff");
			
			data.toIntCols();
			
			assertTrue(data.getColsIndexes().size() == 5);
			
			int attIndex = 0;//data.numAttributes()-1;
			
			ColumnItems col = ColumnItems.of(attIndex);
			
//			System.out.println("AttIndex "+ attIndex + "\n"
//					+ Joiner.on("\n").withKeyValueSeparator("  ->  ")
//					.join(data.intCols.get(attIndex)));
			
			
			col.generateOccurances(data.getIntCol(attIndex), 
					data.getIntCol(data.instances.numAttributes()-1));
			
			String result ="ColumnItems{ColID:=[0], 0 -> FrequentItem{0->[0, 2, 4, 6], 1->[5, 1], 3->[7, 3]}\n	8 -> FrequentItem{0->[8, 10, 12, 14, 15], 1->[13, 9], 3->[11]}\n	16 -> FrequentItem{0->[17, 16, 18, 20, 23, 22], 1->[21], 3->[19]}}";
			assertEquals(col.toString(), result);
		
	}

}
