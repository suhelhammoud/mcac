package weka.classifiers.rules.mcar.datastructures;

import java.util.ArrayList;
import java.util.List;

public class ToItemsResult{
	final public Items rules,items;
	public ToItemsResult(Items rules, Items items) {
		this.rules=rules;
		this.items=items;
	}
	public ToItemsResult(){
		this.rules=new Items();
		this.items=new Items();
	}
}

