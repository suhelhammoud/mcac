package weka.classifiers.rules.mcar.datastructures;





import java.text.DecimalFormat;
import java.util.*;



//TODO change to HashMap later
@SuppressWarnings("serial")
public class FrequentItem extends HashMap<Integer, List<Integer>> {

	public static enum TAG{support,occ,minline, confidence};
	public final static int TAG_LENGTH= TAG.values().length; 

	int support,occ,minLine;
	int maxLabel;
	
	public int getMaxLabel(){
		return maxLabel;
	}

	public List<Integer> getLabelsForSupport(int supp){
		List<Integer> result=new ArrayList<Integer>(keySet().size());
		for (Map.Entry<Integer, List<Integer>> e : entrySet()) {
			if(e.getValue().size()>= supp)
				result.add(e.getKey());
		}
		return result;
	}

	public int getConfidene(){
		return (int)(Integer.MAX_VALUE* (double)support/occ);
	}
	public int getSupport() {
		return support;
	}

	public int getMinLine() {
		return minLine;
	}
	//	public void setMinLine(int minLine) {
	//		this.minLine = minLine;
	//	}

	public void set(Map<Integer, List<Integer>> mp){
		clear();
		putAll(mp);
	}



	@Override
	public String toString() {	
		DecimalFormat f = new DecimalFormat("0.000");
		StringBuffer sb=new StringBuffer("{");

		sb.append("occ="+occ+", sup="+getSupport() +" , conf="+f.format((double)getConfidene()/Integer.MAX_VALUE)+", minLine="+minLine );
		for (Map.Entry<Integer,List<Integer>> e : entrySet()) {
			List<Integer> sorted=new ArrayList<Integer>(e.getValue());
			Collections.sort(sorted);
			sb.append("\t"+e.getKey()+":"+ sorted);
		}
		sb.append("}");
		return sb.toString();
	}


	public boolean add(Integer label, int line){
		List<Integer> list=get(label);
		if(list==null){
			list=new ArrayList<Integer>();
			put(label,list);
			list.add(line);
			return true;
		}else{
			if(list.contains(line)){
				return false;
			}
			else{
				list.add(line);
				return true;
			}
		}

	}

	public boolean addArray(Integer label, int... arr){
		List<Integer> list=get(label);
		if(list==null){
			list=new ArrayList<Integer>(arr.length);
			put(label,list);
			for(int i: arr)list.add(i);
			return true;
		}

		boolean result = false;
		for (int i : arr){
			if(! list.contains(i))
				result =list.add(i);
		}
		return result;
	}


	public void addMap(Map<Integer, List<Integer>> map){
		for (Map.Entry<Integer, List<Integer>> e : map.entrySet()) {
			addList(e.getKey(),e.getValue());
		}
	}

	public boolean addList(Integer label,List<Integer> list) {
		List<Integer> local=get(label);
		if (local==null){
			put(label, new ArrayList<Integer>(list));
			return true;
		}else{
			boolean result=false;
			for (int i : list){
				if(! local.contains(i))
					result=local.add(i);

			}
			return result;
		}
	}
	
	//TODO to delete later
	<T> boolean containsAny(List<T> list1,List<T> list2){
		HashSet<T> set1=new HashSet<T>(list1);
		return set1.removeAll(list2);
	}

	//max laber,allOccs, lowest line
	public int[] calc(){
		int max=-1; int lbl=-1;
		int min=Integer.MAX_VALUE;
		int counter=0;
		for (Integer label : keySet()) {
			List<Integer> i=get(label);
		
			int sz=i.size();
			if (sz>max){
				max=sz;//support
				lbl=label;//maxlabel
			}
			counter+=sz;//allOcc

			int ln=	Collections.min(i);
			if(ln < min)min=ln;//minLine
		}
		support=max;
		occ=counter;
		minLine=min;
		maxLabel=lbl;
		
		int confidence=getConfidene();
		int[] result=new int[TAG_LENGTH];
		result[TAG.support.ordinal()]=max;
		result[TAG.occ.ordinal()]=counter;
		result[TAG.minline.ordinal()]=minLine;
		result[TAG.confidence.ordinal()]=confidence;
		return result;
	}


}
