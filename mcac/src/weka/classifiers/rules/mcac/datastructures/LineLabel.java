package weka.classifiers.rules.mcac.datastructures;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

@SuppressWarnings("serial")
public class LineLabel extends HashMap<Integer, Integer>{
	
	private final BiMap<Integer, Integer> lblLine;// = ImmutableBiMap.of();
	private final BiMap<Integer, Integer> lineLbl;
	
	public LineLabel(Map<Integer, Integer> llnmap) {
		lblLine = HashBiMap.create(llnmap);
		lblLine.putAll(llnmap);
		lineLbl = lblLine.inverse();
	}
	
	public Integer getLable(Integer line){
		return lineLbl.get(line);
	}
	
	public Integer getLine(Integer label){
		return lblLine.get(label);
	}
	
	void test(){
	}
}
