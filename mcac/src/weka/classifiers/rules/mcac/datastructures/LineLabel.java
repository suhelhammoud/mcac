package weka.classifiers.rules.mcac.datastructures;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

@SuppressWarnings("serial")
public class LineLabel extends HashMap<Integer, Integer>{
	

}

class BiLabel  {
	private final BiMap<Integer, Integer> lblLine;// = ImmutableBiMap.of();
	private final BiMap<Integer, Integer> lineLbl;
	
	public BiLabel(Map<Integer, Integer> map) {
		lblLine = HashBiMap.create(map);
		lblLine.putAll(map);
		lineLbl = lblLine.inverse();
	}
	
	
	void test(){
	}
}
