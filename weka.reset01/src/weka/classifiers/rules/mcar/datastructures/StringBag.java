package weka.classifiers.rules.mcar.datastructures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

 

@SuppressWarnings({ "serial", "unchecked" })
public class StringBag extends ArrayList<String> implements Comparable{

	public StringBag(int i) {
		super(i);
	}

	public StringBag(Collection c) {
		super(c);
	}

	@Override
	public int compareTo(Object o) {
		List<String> other=(List)o;
		Iterator<String> i1=this.iterator();
		Iterator<String> i2=other.iterator();
		while (i1.hasNext() && i2.hasNext()){
			int dif= i1.next() .compareTo(i2.next());
			if(dif != 0)return dif;
		}
		return (other.size()-this.size());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
		    return true;
		if (!(o instanceof List))
		    return false;

		ListIterator<String> e1 = listIterator();
		ListIterator<String> e2 = ((List<String>) o).listIterator();
		while(e1.hasNext() && e2.hasNext()) {
			String o1 = e1.next();
		    Object o2 = e2.next();
		    if (!(o1==null ? o2==null : o1.equals(o2)))
			return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	 }
	
	
	@Override
	public int hashCode() {
		int hashCode = 1;
		Iterator<String> i = iterator();
		while (i.hasNext()) {
			String obj = i.next();
		    hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

}
