package weka.classifiers.rules.mcar.datastructures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


@SuppressWarnings("serial")
public class BagList extends ArrayList<IntBag> {

	public BagList(int i) {
		super(i);
	}

	public BagList(Collection c) {
		super(c);
	}
	public BagList(){
		
	}
	
	@Override
	public String toString() {
		if(size()==0)return "[]";
		StringBuffer result= new StringBuffer("["+ get(0).toString()+",");
		for (int i = 1; i < size(); i++) {
			result.append("\n"+get(i).toString()+",");
		}
		result.append("]");
		return result.toString();
	}
}
