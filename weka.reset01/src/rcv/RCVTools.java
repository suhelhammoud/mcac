package rcv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class RCVTools {
	/**
	 * 47336
47237

101
	 * @param data
	 * @param keepIndex
	 * @return
	 */

	public static Instances removeAtts(Instances data, int keepIndex){
		Remove rm= new Remove();
		try {
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}                          // inform filter about dataset **AFTER** setting options

		List<Integer> indices=new ArrayList(100);
		for (int i =0 ; i < 101; i++) {
			if(i == keepIndex)continue;
			int index=47236+i;
			indices.add(index);
		}
		int[] toRemove=new int[indices.size()];
		for (int i = 0; i < toRemove.length; i++) {
			toRemove[i]=indices.get(i);
		}
		rm.setAttributeIndicesArray(toRemove);
		Instances result=null;
		try {
			rm.setInputFormat(data);
			result=Filter.useFilter(data, rm);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void save(Instances data, String filename){
		BufferedWriter writer=null;
		try {
			writer = new BufferedWriter(
			        new FileWriter(filename));
			writer.write(data.toString());
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String datapath="data/rcv/1/rcv1subset1-train.arff";
		Instances data = new Instances(new FileReader(datapath));

		Instance inst= data.instance(data.numInstances()-1);
//		System.out.println(inst.valueSparse(indexOfIndex));
	}

	public static void SplitMulti(String datapath) throws IOException,
			FileNotFoundException {
		Instances data = new Instances(new FileReader(datapath));
	
		for (int i = 0; i <= 100; i++) {
			System.out.println(" i :"+i);
			Instances outdata= removeAtts(data, i);
			save(outdata, "data/rcv/out/"+i+".arff");
		}
	}
}
