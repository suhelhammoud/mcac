package weka.classifiers.rules.mcac.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;


public class ColumnID{
	
	final public static ColumnID ZERO = of();
	
	public static List<Integer> getAtomics(ColumnID col){
		List<Integer> result = new ArrayList<>(col.size());
		for (int id : col.ids) {
			result.add(id);
		}
		return result;
	}
	
	
	public static ColumnID join( ColumnID col1,ColumnID col2){
		if(col1.size() != col2.size())
			return ZERO;
		if(col1.equals(ZERO) )
			return ZERO;
				
		
		Set<Integer> set1 = col1.asSet();
		Set<Integer> set2 = col2.asSet();
		
		set1.addAll(set2);
		if(set1.size() != set2.size()+1)
			return ZERO;
		
		return of(set1);
	}
	
	public static ColumnID of(Collection<Integer> ids){
		return of(Ints.toArray(ids));
	}

	public static ColumnID of(int ... ids){
		return new ColumnID(ids);
	}
	

	
	final private int[] ids;
	
	public ColumnID(int ... ids) {
		int[] sortedID = Arrays.copyOf(ids, ids.length);

		this.ids = sortedID;
		Arrays.sort(sortedID);
	}
	
	public int size() {
		return ids.length;
	}

	public int get(int i) {
		return ids[i];
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(ids);
	}

	@Override
	public String toString() {
		return "["+Joiner.on(", ").join(this.asList())+"]";
	}

	/**
	 * 
	 * @return first none zero id
	 */
	public int getAtomic(){
		return ids[0];
	}

	public List<Integer> asList(){
		List<Integer> result= new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			result.add(get(i));
		}
		return result;
	}
	public Set<Integer> asSet(){
		Set<Integer> result= new HashSet<>();
		for (int i = 0; i < size(); i++) {
			result.add(get(i));
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(this == obj) return true;
		
		if(!(obj instanceof ColumnID))
			return false;
		
		ColumnID that =(ColumnID)obj;
		
		if(this.size() != that.size())
			return false;
		if(this.size() == 0 && that.size()== 0)
			return true;
		
		for (int i = 0; i < size(); i++) {
			if( this.get(i) != that.get(i))
				return false;
		}
		return true;
	}
	
	
	
	
	public static void main(String[] args) {
		ColumnID col1 = of(1,2,4,5);
		ColumnID col2 = of(3,11, 2, 4);
		System.out.println(col1);
		System.out.println(col2);
		System.out.println(join(col1, col2));
	}
	
}

