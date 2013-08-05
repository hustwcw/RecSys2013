package locationBased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class CityMapTest {
	public HashMap<String, Set<BusiCityRecTest> > cityMap;
	public double minLong=0,maxLong=0,minLat=0,maxLat=0;
	public int count = 0;
	public String jsonFileName = null;
	public String filePathName = null;
	public String fileName = "cityMap.hashmap";
	
	public CityMapTest() {
		cityMap = new HashMap<String, Set<BusiCityRecTest> >();
	}
	
	private void refreshLocRangeAndCount(){
		Iterator<BusiCityRecTest> it = getAllBusiRec().iterator();
		BusiCityRecTest rec;
		boolean first = true;
		count = 0;
		while(it.hasNext()){
			rec = it.next();
			count ++;
			if(!first){
				if(minLong > rec.longitude) minLong = rec.longitude;
				if(maxLong < rec.longitude) maxLong = rec.longitude;
				if(minLat > rec.latitude) minLat = rec.latitude;
				if(maxLat < rec.latitude) maxLat = rec.latitude;
			}
			else{
				first = false;
				minLong=maxLong=rec.longitude;
				minLat=maxLat=rec.latitude;
			}
		}
		
	}
	
	public void clear(){
		if(cityMap != null) cityMap.clear();
		minLong=maxLong=minLat=maxLat=0;
	}
	
	
	public void storeMap(){
		if(cityMap == null || cityMap.isEmpty()) return;
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			if(filePathName != null && (!filePathName.equalsIgnoreCase(""))){
				File file = new File(filePathName);
				if(! file.exists()) file.mkdirs();
			}
			
			fo = new FileOutputStream(filePathName+"/"+fileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityMap);
			oo.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void storeMap(String pathName){
		this.filePathName = pathName;
		storeMap();
	}
	
	public void loadMap(){
		clear();
		FileInputStream fi;
		ObjectInputStream oi;
		try {
			fi = new FileInputStream(filePathName+"/"+fileName);
			oi = new ObjectInputStream(fi);
			cityMap = (HashMap<String, Set<BusiCityRecTest> >)oi.readObject();
			fi.close();
			oi.close();
			refreshLocRangeAndCount();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void loadMap(String pathName){
		this.filePathName = pathName;
		loadMap();
	}
	
	public Set<BusiCityRecTest> getAllBusiRec(){
		Set<BusiCityRecTest> allSet = new HashSet<BusiCityRecTest>();
		Iterator<Set<BusiCityRecTest> > it = cityMap.values().iterator();
		while(it.hasNext()){
			allSet.addAll(it.next());
		}
		return allSet;
	}
	
	public void getMapFromJason(){
		cityMap.clear();
		File inFile = new File(jsonFileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			BusiCityRecTest rec;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiCityRecTest.class);
				if(rec.city.equalsIgnoreCase("Fountain Hls")) rec.city = "Fountain Hills";
				else if(rec.city.equalsIgnoreCase("Scottsdale ")) rec.city = "Scottsdale";
				
				if(! rec.state.equalsIgnoreCase("AZ")) System.err.println("other state: "+rec.state);
				if(cityMap.containsKey(rec.city)){
					cityMap.get(rec.city).add(rec);
				}
				else{
					Set<BusiCityRecTest> newSet = new HashSet<BusiCityRecTest>();
					newSet.add(rec);
					cityMap.put(rec.city, newSet);
				}
			}
			refreshLocRangeAndCount();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void getMapFromJason(String fileName){
		jsonFileName = fileName;
		getMapFromJason();
	}
	
	public void printInfo(){
		System.out.println("total cities: "+cityMap.size());
		Iterator<String> it = cityMap.keySet().iterator();
		while(it.hasNext()){
			String city = it.next();
			System.out.println(city+": "+cityMap.get(city).size());
		}
		
		System.out.println("long range: "+minLong+" ~ "+maxLong+": "+(maxLong - minLong));
		System.out.println("lat range: "+minLat+" ~ "+maxLat+": "+(maxLat - minLat));
	}
	
	public CityMapTest filterCity(String cityName){
		if(! cityMap.containsKey(cityName)){
			System.err.println("city not exist!");
			return null;
		}
		CityMapTest newMap = new CityMapTest();
		newMap.cityMap.put(cityName, cityMap.get(cityName));
		newMap.refreshLocRangeAndCount();
		return newMap;
	}
	
	public static void main(String[] args){
		CityMapTest cmt = new CityMapTest();
		cmt.getMapFromJason("yelp_test_set_business.json");
		cmt.storeMap("test_city");
	}
}

class BusiCityRecTest implements Serializable{
	private static final long serialVersionUID = 2141583506234686339L;
	
	public String city;
	public String state;
	public String name;
	public String business_id;
	public double longitude;
	public double latitude;
	
}
