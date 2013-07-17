package org.bom.jbehaveasmunit.test;

public class StockCalculator {
	
	static boolean isStack_ON(double threhold, double tradedValue){
		
		if (threhold <=tradedValue){
			return true;
		}else{
			return false;
		}
		
	}
}
