package weka.classifiers.rules.mcar;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import weka.classifiers.rules.mcar.datastructures.BagList;
import weka.classifiers.rules.mcar.datastructures.FrequentItem;
import weka.classifiers.rules.mcar.datastructures.IntBag;
import weka.classifiers.rules.mcar.datastructures.Items;
import weka.classifiers.rules.mcar.datastructures.Lines;
import weka.classifiers.rules.mcar.datastructures.McarRule;
import weka.classifiers.rules.mcar.datastructures.RANK_ID;
import weka.classifiers.rules.mcar.datastructures.ToItemsResult;
import weka.core.Instances;



public class CDriver {


	public static List<List<String>> split(List<String> lines, int times){
		List<List<String>> all = new ArrayList<List<String>>();

		int partSize=lines.size()/times;
		if(partSize ==0){
			//			logger.error("part Size = 0");
			return all;
		}
		Iterator<String> iter=lines.iterator();

		for (int prt = 0; prt < times-1; prt++) {
			List<String> subLines=new ArrayList<String>();
			for (int i = prt*partSize; i < (prt+1)*partSize; i++) {
				subLines.add(iter.next());
			}
			all.add(subLines);			
		}

		List<String> subLines=new ArrayList<String>();
		while(iter.hasNext()){
			subLines.add(iter.next());
		}
		all.add(subLines);

		return all;
	}

	//	public static void cv(double dsupp, double dconf, List<String> train , String outFile){
	//		int times=4;
	//		List<List<String>> all=split(train, times);
	//		
	//		double p1=0,p2=0;
	//		for (int i = 0; i < all.size(); i++) {
	//			List<String> trainData=new ArrayList<String>(all.get(i));
	//			List<String> testData =new ArrayList<String>(all.size()-trainData.size());
	//			for (int j = 0; j < all.size(); j++) {
	//				if(j==i)continue;
	//				testData.addAll(all.get(j));
	//			}
	//			
	//			List<Double> dd=run(dsupp,dconf,trainData);
	//			p1+=dd.get(0);
	//			p2+=dd.get(1);
	//			
	//		}
	//		
	//		p1=(double)p1/all.size();
	//		p2=(double)p2/all.size();
	//		
	//		System.out.println("cv predict 1="+ p1);
	//		System.out.println("cv predict 2="+ p2);
	//
	//	}

	public static List<McarRule> run(double dsupp, double dconf, Instances train, boolean verbos, StringBuffer sb){
		int minSupport= (int)Math.ceil(train.numInstances()* dsupp);
		int minConfidence=(int)(dconf * Integer.MAX_VALUE);


		Map<Integer, Items> rules=new TreeMap<Integer, Items>();

		//freq item space
		Items items=new Items();
		//line space
		Lines lines=new Lines();


		///init the data
		BagList bag = Tools.mapInstances(train);
		Lines initLines=new Lines(bag);
		lines.putAll(initLines);

		if(verbos){
			sb.append("\nbag:\n"+ bag);
			sb.append("\nlines:\n"+lines);
		}
		int maxIteration=lines.values().iterator().next().size();

		for (int i = 1; i <= maxIteration; i++) {
			//			Items occ1 =MR.toOccurances(lines, i);
			ToItemsResult toItems=MR.toItems(lines, i, minSupport, minConfidence);
			rules.put(i, toItems.rules);
			lines=MR.toLines(toItems.items);

			if(verbos){
				sb.append("\n-----------------------------------iteration "+ i+"----------------------------");
				sb.append("\nitems :\n"+toItems.items);
				sb.append("\nrules :\n"+rules.get(i));
				sb.append("\nlines \n"+lines);
			}
			if(lines.size()==0)break;
		}

		Items allRules=new Items();
		for (Items i : rules.values()) {
			allRules.putAll(i);
		}

		Lines oneRLines=MR.rulesToLinesWithWeights(allRules, true,RANK_ID.CONF_SUPP_CARD);
		Items rawRules=MR.toOccurances(oneRLines,1 );
		List<McarRule> finalRules=MR.strippedOrderedFilledFinal(rawRules, train);


		if(verbos){
			Lines rLines=MR.toLines(allRules);
			sb.append("\n all rules collected \n"+allRules);
			sb.append("\nrLines, all rules to line space:\n"+rLines);

			sb.append("\noneRLines: all rules to line space, ranked and picked\n"+oneRLines);

			sb.append("\nRanked rules in item space:\n"+rawRules);
			
			sb.append("\n --------------------------------   end building mcan classifer---------------------- ");
		}

		
	
		
		return finalRules;
		//		try {
		//			List<Double> dd=Rule.predictResults(finalRules, Tools.readFile("data/mcar/example.txt"),
		//			"data/mcar/out.xls");
		//			System.out.println(dd);
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}


	}




	public static void main(String[] args) 
	throws FileNotFoundException, IOException {

		Instances train=new Instances(new FileReader("data/weather.nominal.arff"));


		StringBuffer sb=new StringBuffer();
		List<McarRule> finalRules = run(0.2, 0.0, train,true, sb );

		
		System.out.println(sb.toString());
		
		for (McarRule rule : finalRules) {
			System.out.println(rule.toString(3));
		}
		//			for (Rule rule : finalRules) {
		//				System.out.println(rule);
		//			}

	}

}
