package weka.classifiers.rules.mcar.datastructures;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.classifiers.rules.mcar.Tools;
import weka.core.Instances;



@SuppressWarnings("serial")
public class Lines extends HashMap<Long, BagList> {

	public Lines(){

	}
	public Lines(int size) {
		super(size);
	}
	
	public Lines(List<String> slines){
		fill(slines);
	}
	public Lines(BagList lines){
		fill(lines);
	}

	/**
	 * 
	 * @param lines: list of [line, a1,a2,...,an, lablel]
	 * 
	 * for each line will put
	 * [lable-line]---> [ [0,a1], [1,a2], ... [n,an] ]
	 */
	public void fill(BagList lines){
		for (IntBag bag : lines) {
			long labelLine = (long)bag.get(bag.size()-1) << Integer.SIZE
				| bag.get(0);
			BagList outbag = new BagList(bag.size()-2);
			
			for (int i = 1; i < bag.size()-1; i++) {
				IntBag ib=new IntBag(2);
				ib.add(i-1);
				ib.add(bag.get(i));
				outbag.add(ib);
			}
			put(labelLine,outbag);
		}
	}
	
	public void fill(List<String> sLines){
		for (String s : sLines) {
			String[] parts=s.split(",");

			if(parts.length <2)continue;
			BagList ids=new BagList(parts.length-2);

			//line number
			int line=Integer.valueOf(parts[0]);
			
			//label int value
			int label=Integer.valueOf(parts[parts.length-1]);
			
			long labelLine=(long)label << Integer.SIZE | line; 

			for (int i = 1; i < parts.length-1; i++) {
				IntBag id=new IntBag(2);

				//add column index, start from 0
				id.add(i-1);
				
				// add int value of item
				id.add(Integer.valueOf(parts[i]));
				ids.add(id);
			}
			put(labelLine, ids);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		List<Long> list=new ArrayList<Long>(this.keySet());

		Collections.sort(list);
		for (Long i : list) {
			BagList v=get(i);
			Collections.sort(v);
			sb.append((i & Integer.MAX_VALUE)+"\t"+ (i >> Integer.SIZE));
			for (IntBag lng : v) {
				sb.append("\t"+ lng);
			}
			sb.append("\n");
		}
		return sb.toString();
	}



	/**
	 *  
	 * @param value [c,a] col=0, att=1
	 * @return [c,a,a]
	 */
	public static BagList composeOne(BagList value){
		BagList result=new BagList();

		for (IntBag i : value) {
			IntBag ib=new IntBag(i);
			ib.add(ib.get(ib.size()-1));
			
			result.add(ib);
		}
		return result;
	}
	
	/**
	 * [c1,a1,a1]+[c2,a2,a2]=[c1,c2,a1,a2]
	 * @param value 
	 * @return 
	 */
	public static BagList composeTwo(BagList value){
		BagList result=new BagList();

		Collections.sort(value); 
		for (int i = 0; i < value.size()-1; i++) {
			for (int j = i+1; j < value.size(); j++) {
				IntBag id1=value.get(i);
				IntBag id2=value.get(j);
				
				IntBag id =new IntBag(4);
				
				id.add(id1.get(0));
				id.add(id2.get(0));
				
				id.add(id1.get(1));
				id.add(id2.get(1));
				
				result.add(id);
			}
		}
		return result;
	}

	
	public static int checkCompose(IntBag one, List<IntBag> list){
		int result=0;
		for (int i = 0; i < one.size(); i++) {
			IntBag temp = new IntBag(one);
			temp.remove(i);
			if(! list.contains(temp))
				result++;
		}
		return result;
	}
	public static BagList composeThreeAndMore(List<IntBag> value){
		BagList result=new BagList();

		if(value.size()==1)return result;
		//System.out.println("compose "+ value);

		Collections.sort(value);
		

		TreeMap<IntBag, BagList> left = new TreeMap<IntBag, BagList>();
		TreeMap<IntBag, BagList> right = new TreeMap<IntBag, BagList>();

		//for (int i = 0; i < line.size() - 1; i++) {// last item is the label
		for (IntBag ibag : value) {

			// item
			IntBag tmp = new IntBag(ibag);
			//remove rowId, and keep only ColIds
			tmp.remove(tmp.size()-1);
			
			/** remove last ColId */
			IntBag coreLeft = new IntBag(tmp);
			coreLeft.remove(coreLeft.size() - 1);
			
			/** remove first ColId */
			IntBag coreRight = new IntBag(tmp);
			coreRight.remove(0);

			/** map ibag in left hashtable*/
			BagList leftList = left.get(coreLeft);
			if (leftList == null)
				leftList = new BagList();
			leftList.add(ibag);
			left.put(coreLeft, leftList);

			/** map ibag in right hashtable*/
			BagList rightList = right.get(coreRight);
			if (rightList == null)
				rightList = new BagList();
			rightList.add(ibag);
			right.put(coreRight, rightList);
		}
//		System.out.println("left "+left);
//		System.out.println("right "+right);

		if(left.size()==0 || right.size()==0) return result;

		left.remove(left.firstKey());
		right.remove(right.lastKey());

//		left.remove(left.lastKey());
//		right.remove(right.firstKey());
		
		if(left.size()==0 || right.size()==0) return result;

		for (Map.Entry<IntBag, BagList> e : right.entrySet()) {

			BagList start = e.getValue();

			BagList stop = left.get(e.getKey());
			if (stop == null)
				continue;

			// System.out.println("start "+ start);
			// System.out.println("stop "+ stop);
			for (IntBag subItem1: start) {
				for (IntBag subItem2 : stop) {
					IntBag compound = new IntBag(subItem1.size() + 2);
					//add the left part of ColId
					for (int i = 0; i < subItem1.size() - 1; i++) {
						compound.add(subItem1.get(i));
					}
					compound.add(subItem2.get(subItem2.size() - 2));//add new colId
					
					compound.add(subItem1.get(subItem1.size() - 1));// add rowId1
					compound.add(subItem2.get(subItem2.size() - 1));// add rowId2
					
					result.add(compound);
					int checkInt = checkCompose(compound, value);
					if (checkInt > 1)
						System.out.println("error  "+ checkInt);
//					System.out.println("join "+ is1+"\t"+is2
//							+"=\t"+ compound);
				}
			}
		}
		//System.out.println(result);
		return result;
	}


	@SuppressWarnings("unchecked")
	public static BagList compose(BagList value, int iteration) {
		if(iteration==1) return composeOne(value);

		if(iteration == 2)return composeTwo(value);

		return composeThreeAndMore(value);
	}

	public static void main(String[] args) 
	throws FileNotFoundException, IOException {

		Instances data = new Instances(new FileReader("data/weather.nominal.arff"));
		
		BagList bag =Tools.mapInstances(data);
		System.out.println(bag);
		
		Lines lines = new Lines(bag);
		
		System.out.println("lines\n"+ lines);
	}
}
