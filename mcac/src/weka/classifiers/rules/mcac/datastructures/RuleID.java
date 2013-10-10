package weka.classifiers.rules.mcac.datastructures;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

/**
 * Instances of this class are to be used as immutable objects
 * @author suheil
 *
 */
public class RuleID{
	public final ColumnID colid;
	public final int rowid;
	public final int support;
	public final double confidence;
	
	private int hashCode;
	
	
	public RuleID(ColumnID colid, int rowid, int support, double confidence){
		this.colid = colid;
		this.rowid = rowid;
		this.support = support;
		this.confidence = confidence;
		
		this.hashCode = Objects.hashCode(colid, rowid, support, confidence);
	}
	
	public static RuleID of(ColumnID colid, FrequentItem item){
		Calc calc = item.getCalc();
		return new RuleID(colid, calc.rowId , calc.support, calc.confidence);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)return true;
		
		if( ! (obj instanceof RuleID))
			return false;
		
		RuleID that = (RuleID)obj;
		return this.rowid == that.rowid 
				&& this.colid.equals(that.colid)
				&& this.support == that.support
				&& Math.abs(this.confidence - that.confidence) < 1e-6 ;
		
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "<"+colid+","+rowid+" ,conf:"+confidence+", supp="+support+ " >";
	}
	
}


