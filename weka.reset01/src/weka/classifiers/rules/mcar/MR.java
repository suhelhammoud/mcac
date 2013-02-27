package weka.classifiers.rules.mcar;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.classifiers.rules.mcar.datastructures.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.GUIChooser;



public class MR {

	public static final int RCARD_INDEX=100;

	public static ToItemsResultList toItemsList(Lines lines,
			int iteration, int minSupport, int[] minConfidence){
		
		int[] copyConf = new int[minConfidence.length];
		for (int i = 0; i < copyConf.length; i++) {
			copyConf[i]=minConfidence[minConfidence.length-1-i];
		}
		
		ToItemsResultList results= new ToItemsResultList(copyConf.length);
		Items  tmpItems=toOccurances(lines,iteration);

		for (Map.Entry<IntBag, FrequentItem> e : tmpItems.entrySet()) {
			FrequentItem fi=e.getValue();
			fi.calc();
			if(fi.getSupport()< minSupport){
				continue;
			}
			IntBag id=e.getKey();
			id.remove(id.size()-1);
			//if(iteration > 1) id.remove(id.size()-1);
			id.remove(id.size()-1);
			id.add(fi.getMinLine());
			results.items.put(id,fi);
			
			int conf= fi.getConfidene();

			for (int i = 0; i < copyConf.length; i++) {
				if(conf >= copyConf[i]){
					results.rules.get(i).put(id, fi);
					break;
				}
			}
			
		}
		
//		for (Items  lst : results.rules) {
//			System.out.print("\t"+ lst.size());
//		}
//		System.out.println();
		
		return results;
	}
	public static ToItemsResult toItems(Lines lines,
			int iteration,int minSupport, int minConfidence){
		
		ToItemsResult result = new ToItemsResult();
		
		Items  tmpItems=toOccurances(lines,iteration);

		for (Map.Entry<IntBag, FrequentItem> e : tmpItems.entrySet()) {
			FrequentItem fi=e.getValue();
			fi.calc();
			if(fi.getSupport()< minSupport){
				continue;
			}
			IntBag id=e.getKey();
			id.remove(id.size()-1);
			//if(iteration > 1) id.remove(id.size()-1);
			id.remove(id.size()-1);
			id.add(fi.getMinLine());
			result.items.put(id,fi);
			if (fi.getConfidene() < minConfidence)continue;
			result.rules.put(id, fi);
		}
		return result;
	}
	public static Items toOccurances(Lines lines, int iteration){
		Items items=new Items();
		for (Map.Entry<Long, BagList> e : lines.entrySet()) {
			long labelLine=e.getKey();
			int label=(int)(labelLine >> Integer.SIZE);
			int line =(int)(labelLine & Integer.MAX_VALUE);

			BagList ids=Lines.compose(e.getValue(), iteration);
			for (int i = 0; i < ids.size(); i++) {
				IntBag id=ids.get(i);
				FrequentItem fi=items.get(id);
				if(fi==null)fi=new FrequentItem();
				fi.add(label, line);
				items.put(id, fi);
			}
		}
		return items;
	}


	public static Lines toLines(Items items){
		Lines result=new Lines();
		for (Map.Entry<IntBag, FrequentItem> e : items.entrySet()) {
			IntBag id=e.getKey();
			for (Map.Entry<Integer, List<Integer>> lns : e.getValue().entrySet()) {
				int label=lns.getKey();
				for (Integer line : lns.getValue()) {
					long labelLine=(long)label << Integer.SIZE | line;
					BagList list=result.get(labelLine);
					if(list== null)list=new BagList();
					list.add(id);
					result.put(labelLine, list);
				}
			}
		}
		return result;
	}

	

	public static Items finalRuleSet(Lines rLines){
		return toOccurances(rLines, 1);
	}
	
	
	/**
	 * rank rule and return based on rule condition (id) and
	 *  freqent item of the rutle
	 * @param id [c,a]
	 * @param fi 
	 * @return [conf,support,size, c, a]
	 */
	public static IntBag rank(IntBag id, FrequentItem fi, RANK_ID tag){
		IntBag result=new IntBag(id.size()+3);
		switch (tag) {
		case CONF_SUPP_CARD:
			result.add(fi.getConfidene());
			result.add(fi.getSupport());
			result.add(id.size());
			break;
		case CONF_CARD_SUPP:
			result.add(fi.getConfidene());
			result.add(id.size());
			result.add(fi.getSupport());
			break;
		case SUPP_CONF_CARD:
			result.add(fi.getSupport());
			result.add(fi.getConfidene());
			result.add(id.size());
			break;
		case SUPP_CARD_CONF:
			result.add(fi.getSupport());
			result.add(id.size());
			result.add(fi.getConfidene());
			break;
		case CARD_CONF_SUPP:
			result.add(id.size());
			result.add(fi.getConfidene());
			result.add(fi.getSupport());
			break;
		case CARD_SUPP_CONF:
			result.add(id.size());
			result.add(fi.getSupport());
			result.add(fi.getConfidene());
			break;
		case SUPP_RCARD_CONF:
			result.add(fi.getSupport());
			result.add(RCARD_INDEX-id.size());
			result.add(fi.getConfidene());
			break;
		default:
			System.err.println("rank id ");
			break;
		}

		
		for (Integer i : id) {// add  ruleId
			result.add(i);
		}
		return result;

	}
	
//	public static Rule getDefaultRule(Instances train, Collection<Integer> classifiedLines){
//		Rule result = new Rule();
//		
//		Map<Long, Integer> imap=new HashMap<Long, Integer>(train.attribute(-1).numValues());
//		
//		List<Integer> unclassifiedLines= new ArrayList<Integer>(train.numInstances()-rawRules.size());
//		List<Integer> unclassifiedLabels= new ArrayList<Integer>(train.numInstances()-rawRules.size());
//		for (int i = 0; i < train.numInstances(); i++) {
//			if(classifiedLines.contains(i))continue;
//			
//			label
//			if(oneRul)
//		}
//		return null;
//	}
	
	public static int stripIdAndGetLine(IntBag ib){
	    int line=ib.remove(ib.size()-1);//remove mine line,now id holds the columns id
		ib.remove(ib.size()-1);//remove mineline again
		
		ib.remove(0);//remove confidance
		ib.remove(0);//remove support
		ib.remove(0);//remove id

		return line;
	}
	

	
public static List<McarRule> strippedOrderedFilledFinal(Items rules,Instances trainData){
		
		List<McarRule> result=new ArrayList<McarRule>();
		
//		Map<Integer,List<String>> items=Tools.readFileToItems(sLines);
		
		BagList orderdRuleRanks=new BagList(rules.keySet());
		Collections.sort(orderdRuleRanks);
		Collections.reverse(orderdRuleRanks);
		
		for (IntBag ib : orderdRuleRanks) {
			FrequentItem fi=rules.get(ib);

			int line = stripIdAndGetLine(ib);
			McarRule rule=new McarRule(ib);
			
			//fill string condition
			Instance inst=trainData.instance(line);
			for (int col : ib) {
				rule.add(inst.toString(col));
			}
			
			//fill
			for (int label : fi.keySet()) {
				List<Integer> linesOfOccs=fi.get(label);
				Instance i=trainData.instance(label);
				String sClass = i.toString(trainData.numAttributes()-1);
				double lableDouble= i.value(trainData.numAttributes()-1);
				rule.incLabelFreq(sClass, linesOfOccs.size(), lableDouble);
			}
			result.add(rule);
		}
		
		return result;
	}
	
	
	/**
	 * Keep the best ranked rule which covered the line	
	 * @param items
	 * @return
	 */
	public static Lines rulesToLinesWithWeights(Items items, boolean oldMCar,RANK_ID rankType){
		Lines result=new Lines();
		for (Map.Entry<IntBag, FrequentItem> e : items.entrySet()) {
			IntBag id=e.getKey();
			FrequentItem fi=e.getValue();
			IntBag rankedId=rank(id, fi,rankType);

			//TODO delete later
			if(oldMCar)fi.calc();
			
			for (Map.Entry<Integer, List<Integer>> lns : fi.entrySet()) {
				int label=lns.getKey();
				//TODO delete later
				if (oldMCar && fi.getMaxLabel() != label)continue;
				
				for (Integer line : lns.getValue()) {
					long labelLine=(long)label << Integer.SIZE | line;
					BagList list=result.get(labelLine);
					
					if(list== null){
						list=new BagList();
						list.add(rankedId);
					}else{
						//TODO change criteria to get new rule ranking as well
						if(rankedId.compareTo(list.get(0))<0)continue;
						list.set(0, rankedId);
//						list.add(rankedId);
					}
					result.put(labelLine, list);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		weka.gui.GUIChooser.main(new String[]{});
	}

}



