package org.bom.behave2unitgen.example.test;

/**
 * This test class belongs to the StockCalcStoryTest
 * 
 * @author Carsten Severin
 * @see StockCalcStoryTest
 */
public class StockCalculator {
	
	static boolean isStock_ON(double threhold, double tradedValue){
		
		if (threhold <=tradedValue){
			return true;
		}else{
			return false;
		}
		
	}
}
