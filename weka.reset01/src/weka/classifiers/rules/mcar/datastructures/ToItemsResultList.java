package weka.classifiers.rules.mcar.datastructures;

import java.util.ArrayList;
import java.util.List;

public class ToItemsResultList{
	final public Items items;
	final public List<Items> rules;
	
	public ToItemsResultList(List<Items> rules, Items items) {
		this.rules=rules;
		this.items=items;
	}
	public ToItemsResultList(int num){
		this.items=new Items();
		this.rules=new ArrayList<Items>(num);
		for (int i = 0; i < num; i++) {
			rules.add(new Items());
		}
	}
}
