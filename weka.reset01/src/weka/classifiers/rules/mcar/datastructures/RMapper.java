package weka.classifiers.rules.mcar.datastructures;

import java.util.HashSet;
import java.util.Set;

import weka.classifiers.rules.mcar.datastructures.RCounter.RTAG;

public class RMapper {
	Set<Integer> conf=new HashSet<Integer>();
	Set<Integer> supp=new HashSet<Integer>();
	Set<Integer> card=new HashSet<Integer>();
	Set<Integer> row= new HashSet<Integer>();
	Set<IntBag> col = new HashSet<IntBag>();
	
	RCounter counter=new RCounter();
	
	public boolean add(RTAG tag, Integer v){
		switch (tag) {
		case CONF: return conf.add(v);
		case SUPP: return supp.add(v);
		case CARD: return card.add(v);
		case ROW : return row.add(v);
		
		default:
			return false;
		}
	}
	
	public boolean add(IntBag v){
		return col.add(v);
	}
	
	public int get(RTAG tag){
		switch (tag) {
		case CONF: return conf.size();
		case SUPP: return supp.size();
		case CARD: return card.size();
		case ROW : return row.size();
		case COL : return col.size();
		default:
			return 0;
		}
	}
}
