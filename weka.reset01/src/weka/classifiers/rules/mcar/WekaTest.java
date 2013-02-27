package weka.classifiers.rules.mcar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instance;
import weka.core.Instances;

public class WekaTest {

	public static void main(String[] args)
	throws FileNotFoundException, IOException {
		Instances data=new Instances(new FileReader("data/weather.arff"));
		
		for (int i = 0; i < data.numInstances(); i++) {
			Instance inst = data.instance(i);
			System.out.println(inst);
//			inst.st
		}
	}
}
