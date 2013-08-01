package locationBased;

import java.util.Iterator;


public class Test {
	public static void main(String[] args){
		//genCityAvg();
		
		BusinessMapOverallTest bmot = new BusinessMapOverallTest();
		bmot.getMapFromJason("yelp_test_set_business.json");
		bmot.storeMap("test_busi_rec");
		
//		StreetMapTrain streetMapTrain = new StreetMapTrain();
//		streetMapTrain.getMapFromJason("yelp_training_set_business.json");
//		streetMapTrain.storeMap("training_street");
//		streetMapTrain.storeVisualStreetAvg();
		
//		CateMapTrain cateMapTrain = new CateMapTrain();
//		cateMapTrain.getMapFromJason("yelp_training_set_business.json");
//		cateMapTrain.storeMap("training_cate");
//		cateMapTrain.storeVisualCateAvg();
		
//		CityMapTest cityMapTest = new CityMapTest();
//		cityMapTest.loadMap("test_city");
//		cityMapTest.printInfo();
//		
//		String city = cityMapTest.cityMap.keySet().iterator().next();
//		CityMapTest map1 = cityMapTest.filterCity(city);
//		map1.printInfo();
	}
	
	
	
	public static void findExistance(){
		CityMapTest cityMapTest = new CityMapTest();
		cityMapTest.loadMap("test_city");
		CityMapTrain cityMapTrain = new CityMapTrain();
		cityMapTrain.loadMap("training_city");
		
		Iterator<String> it = cityMapTest.cityMap.keySet().iterator();
		while(it.hasNext()){
			String city = it.next();
			if(! cityMapTrain.cityMap.containsKey(city)) System.err.println("NOT HERE: "+city);
		}
	}
	
	public static void getTestCity(){
		CityMapTest cityMapTest = new CityMapTest();
		cityMapTest.getMapFromJason("yelp_test_set_business.json");
		cityMapTest.storeMap("test_city");
		cityMapTest.printInfo();
	}
	
	public static void genCityAvg(){
		CityMapTrain cityMapTrain = new CityMapTrain();
		cityMapTrain.getMapFromJason("yelp_training_set_business.json");
		cityMapTrain.storeMap("training_city");
		cityMapTrain.storeVisualCityAvg();
	}
}

