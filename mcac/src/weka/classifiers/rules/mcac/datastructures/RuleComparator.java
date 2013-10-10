package weka.classifiers.rules.mcac.datastructures;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



abstract public class RuleComparator implements Comparator<RuleID>{
	
	protected static final Logger logger = LoggerFactory
			.getLogger(RuleComparator.class );
	
	public static enum RANK_ID {
		CONF_SUPP_CARD,
		CONF_CARD_SUPP,
		SUPP_CONF_CARD,
		SUPP_CARD_CONF,
		CARD_CONF_SUPP,
		CARD_SUPP_CONF,
		SUPP_RCARD_CONF
	}

	public static RuleComparator of(RANK_ID rank){
		logger.info("comparator is using rank of:{}", rank.name());
		switch (rank) {
		case CONF_SUPP_CARD:
			return new Compare_CONF_SUPP_CARD();
			
		case CONF_CARD_SUPP:
			return new Compare_CONF_CARD_SUPP();
			
		case SUPP_CONF_CARD:
			return new Compare_SUPP_CONF_CARD();
			
		case SUPP_CARD_CONF:
			return new Compare_SUPP_CARD_CONF();
			
		case CARD_CONF_SUPP:
			return new Compare_CARD_CONF_SUPP();
			
		case CARD_SUPP_CONF:
			return new Compare_CARD_SUPP_CONF();
				
		case SUPP_RCARD_CONF:
			return new Compare_SUPP_RCARD_CONF();

		default:
			return new Compare_CONF_SUPP_CARD();
		}
	}


	public RuleID better(RuleID r1, RuleID r2){
		int cmp = compare(r1, r2);
		if(cmp > 0)
			return r1;
		else
			return r2;
	}	
}


class Compare_CONF_SUPP_CARD extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		diff = r1.support - r2.support ;
		if(diff != 0) return diff;

		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;

		return 0;
	}
}


class Compare_CONF_CARD_SUPP extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff ;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;

		diff = r1.support - r2.support ;
		if(diff != 0) return diff;

		return 0;
	}
	
}


class Compare_SUPP_CONF_CARD extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		
		diff = r1.support - r2.support ;
		if(diff != 0) return diff;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;

		return 0;
	}
}


class Compare_SUPP_CARD_CONF extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		
		diff = r1.support - r2.support ;
		if(diff != 0) return diff;
		
		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		return 0;
	}
}


class Compare_CARD_CONF_SUPP extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		
		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		diff = r1.support - r2.support ;
		if(diff != 0) return diff;

		return 0;
	}
}


class Compare_CARD_SUPP_CONF extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		
		diff = r1.colid.size() - r2.colid.size(); 
		if(diff != 0) return diff;
		
		diff = r1.support - r2.support ;
		if(diff != 0) return diff;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		return 0;
	}
}


class Compare_SUPP_RCARD_CONF extends RuleComparator{
	@Override
	public int compare(RuleID r1, RuleID r2) {
		int diff;
		
		diff = r1.support - r2.support ;
		if(diff != 0) return diff;
		
		diff = r2.colid.size() - r1.colid.size(); 
		if(diff != 0) return diff;
		
		diff = Double.compare(r1.confidence, r2.confidence);
		if(diff != 0) return diff;

		return 0;
	}
}

