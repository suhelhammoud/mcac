package weka.classifiers.rules.mcar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.rules.mcar.datastructures.BagList;
import weka.classifiers.rules.mcar.datastructures.IntBag;
import weka.core.Instance;
import weka.core.Instances;


public class Tools {
	
//	static transient Logger logger=Logger.getLogger(Tools.class);

	
	
	
//	public static Map<Integer, List<String>> readFileToItems(List<String> lines){
//		Map<Integer,List<String>> result=new LinkedHashMap<Integer, List<String>>();
//		if(lines.size()==0)return result;
//		for (String line : lines) {
//			String[] parts=line.split(",");
//			if(parts.length<2)continue;
//			int lineNumber=Integer.valueOf(parts[0]);
//			List<String> items=new ArrayList<String>(parts.length-1);
//			for (int i = 1; i < parts.length; i++) {
//				items.add(parts[i]);
//			}
//			result.put(lineNumber, items);
//		}
//		return result;
//	}
//	public static Map<Integer, List<String>> readFileToItems(String filename){
//		
//		return readFileToItems(Tools.readFile(filename));
//	}

	
	
//	public static 

	
	/**
	 * 
	 * @param data
	 * @return list of:  [line1, firstOcc, firstOCC,    ,label FirstOCC]
	 */
	public static BagList mapInstances(Instances data){
		BagList result= new BagList(data.numInstances());
		
		List<Map<Double, Integer>> imaps=
			new ArrayList<Map<Double,Integer>>(data.numAttributes());

		for (int i = 0; i < data.numAttributes(); i++) {
			imaps.add(new HashMap<Double, Integer>());
		}
		
		for (int lineNumber = 0; lineNumber < data.numInstances(); lineNumber++) {
			IntBag line =new IntBag(data.numAttributes()+1);
			line.add(lineNumber);
			Instance inst = data.instance(lineNumber);
			for (int att = 0; att < inst.numAttributes(); att++) {
				Double value = inst.value(att);
				if( value== Instance.missingValue())
					continue;
				Integer firstOcc = imaps.get(att).get(value);
				
				if(firstOcc == null){
					firstOcc =lineNumber;
					imaps.get(att).put(value, lineNumber);
				}
				
				line.add(firstOcc);
			}
			result.add(line);
		}
		return result;
		
	}
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//addLines("data/in", "data/out");
//		mapAllFiles("data/out", "data/map");
		
		Instances data= new Instances(new FileReader("data/weather.nominal.arff"));

		System.out.println(data);
		System.out.println();
		BagList lines= mapInstances(data);
		
		for (IntBag line : lines) {
			System.out.println(line);
		}
		
	}
}

