package weka.classifiers.rules.mcac.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;

public class FrequentItemTest {

	@Test
	public void testCalc() {
		
		FrequentItem item = new FrequentItem();
		item.put(11, 2);
		item.put(11, 4);
		item.put(11, 6);
		item.put(11, 1);
		
		item.put(22, 23);
		item.put(22, 22);
		item.put(22, 24);
		
		item.put(33, 8);
		item.put(33, 4);
		item.put(33, 22);
		item.put(33, 44);
		item.put(33, 7);
		
		Calc calc = item.getCalc();
		
//		System.out.println(item);
//		System.out.println(calc);
		
		assertTrue(calc.support == 5);
		assertTrue(calc.confidence == 5.0/ 12.0);
		assertTrue(calc.label == 33);
		assertTrue(calc.rowId == 1);
	}

}
