package locationBased;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataMatrix.MyMatrix;
import dataMatrix.Pair;


public class CityMapTrain {
	public HashMap<String, BusiCityRecTrain> busiRecMap;
	public HashMap<String, Set<BusiCityRecTrain> > cityMap;
	public HashMap<String, Double> cityAvg;
	public HashMap<String, Double> cityMRSE;
	public HashMap<String, Integer> cityRevCnt;
	public HashMap<String, Integer> cityBusiCnt;
	public int count = 0;
	public String jsonFileName = null;
	public String filePathName = null;
	public static final String busiRecMapfileName = "busiRecMap.hashmap";
	public static final String cityMapfileName = "cityMap.hashmap";
	public static final String cityAvgfileName = "cityAvg.hashmap";
	public static final String cityRevCntfileName = "cityRevCnt.hashmap";
	public static final String cityBusiCntfileName = "cityBusiCnt.hashmap";
	public static final String cityMRSEfileName = "cityMRSE.hashmap";
	
	public CityMapTrain() {
		cityMap = new HashMap<String, Set<BusiCityRecTrain> >();
		cityAvg = new HashMap<String, Double>();
		cityBusiCnt = new HashMap<String, Integer>();
		cityMRSE = new HashMap<String, Double>();
		cityRevCnt = new HashMap<String, Integer>();
		
		busiRecMap = new HashMap<String, CityMapTrain.BusiCityRecTrain>();
	}
	
	public void storeVisualCityAvg(){
		if(filePathName == null || filePathName.equalsIgnoreCase("")) return;
		
		File outFile = new File(filePathName + "/CityAvgStar.txt");
		BufferedWriter fileStream;
		Iterator<String> it = cityMap.keySet().iterator();
		String currentPos;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("City,AvgStars,BusinessCnt,TotalReviewCnt,MRSE");
			while(it.hasNext()){
				currentPos = it.next();
				fileStream.write("\n"+currentPos+
						","+cityAvg.get(currentPos)+
						","+cityBusiCnt.get(currentPos)+
						","+cityRevCnt.get(currentPos)+
						","+cityMRSE.get(currentPos));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void refreshCityStatInfo(){
		cityAvg.clear();
		cityBusiCnt.clear();
		cityMRSE.clear();
		cityRevCnt.clear();
		
		Iterator<String> itS = cityMap.keySet().iterator();
		Set<BusiCityRecTrain> set_t;
		while(itS.hasNext()){
			String sName = itS.next();
			set_t = cityMap.get(sName);
			cityBusiCnt.put(sName, set_t.size());
		}
		
		MyMatrix matrix = new MyMatrix();
		matrix.setFilePathName("origin");
		matrix.getMapFromFile();
		
		HashMap<String, Integer> totalStars = new HashMap<String, Integer>();
		
		Iterator<Pair> it = matrix.getKeySets().iterator();
		Pair pair;
		String bID;
		String city;
		BusiCityRecTrain rec;
		count = 0;
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			city = rec.city;
			count ++;
			if(totalStars.containsKey(city)){
				totalStars.put(city,totalStars.get(city)+matrix.get(pair.x, pair.y));
				cityRevCnt.put(city, cityRevCnt.get(city)+1);
			}
			else{
				totalStars.put(city,matrix.get(pair.x, pair.y));
				cityRevCnt.put(city, 1);
			}
		}
		itS = cityMap.keySet().iterator();
		while(itS.hasNext()){
			city = itS.next();
			if(!totalStars.containsKey(city)){
				System.err.println("CITY NOT FOUND");
				System.exit(1);
			}
			double total = totalStars.get(city);
			double count = cityRevCnt.get(city);
			cityAvg.put(city, total/count);
		}
		
		HashMap<String, Double> totalErr = new HashMap<String, Double>();
		it = matrix.getKeySets().iterator();
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			city = rec.city;
			double star = matrix.get(pair.x, pair.y);
			if(!cityAvg.containsKey(city)){
				System.err.println("CITY NOT FOUND:");
				System.out.println(city);
				System.exit(1);
			}
			double avg = cityAvg.get(city);
			if(totalErr.containsKey(city))
				totalErr.put(city,totalErr.get(city) + (star-avg)*(star-avg));
			else
				totalErr.put(city,(star-avg)*(star-avg));
		}
		itS = cityMap.keySet().iterator();
		while(itS.hasNext()){
			city = itS.next();
			double err = totalErr.get(city);
			double count = cityRevCnt.get(city);
			cityMRSE.put(city, Math.sqrt(err/count));
		}
	}
	
	public void clear(){
		cityMap.clear();
		cityAvg.clear();
		cityBusiCnt.clear();
		cityMRSE.clear();
		cityRevCnt.clear();
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
			
			fo = new FileOutputStream(filePathName+"/"+cityMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityMap);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cityAvgfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityAvg);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cityRevCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityRevCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cityBusiCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityBusiCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cityMRSEfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cityMRSE);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+busiRecMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(busiRecMap);
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
			fi = new FileInputStream(filePathName+"/"+cityMapfileName);
			oi = new ObjectInputStream(fi);
			cityMap = (HashMap<String, Set<BusiCityRecTrain> >)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cityAvgfileName);
			oi = new ObjectInputStream(fi);
			cityAvg = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cityRevCntfileName);
			oi = new ObjectInputStream(fi);
			cityRevCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cityBusiCntfileName);
			oi = new ObjectInputStream(fi);
			cityBusiCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cityMRSEfileName);
			oi = new ObjectInputStream(fi);
			cityMRSE = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+busiRecMapfileName);
			oi = new ObjectInputStream(fi);
			busiRecMap = (HashMap<String, BusiCityRecTrain>)oi.readObject();
			fi.close();
			oi.close();
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
	
	public Set<BusiCityRecTrain> getAllBusiRec(){
		Set<BusiCityRecTrain> allSet = new HashSet<BusiCityRecTrain>();
		Iterator<Set<BusiCityRecTrain> > it = cityMap.values().iterator();
		while(it.hasNext()){
			allSet.addAll(it.next());
		}
		return allSet;
	}
	
	public void getMapFromJason(){
		cityMap.clear();
		count=0;
		File inFile = new File(jsonFileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			BusiCityRecTrain rec;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiCityRecTrain.class);
				
				if(rec.city.equalsIgnoreCase("Fountain Hls")) rec.city = "Fountain Hills";
				else if(rec.city.equalsIgnoreCase("Scottsdale ")) rec.city = "Scottsdale";
				
				busiRecMap.put(rec.business_id, rec);
				count++;
				String city = rec.city;
				if(cityMap.containsKey(city)){
					cityMap.get(city).add(rec);
				}
				else{
					Set<BusiCityRecTrain> newSet = new HashSet<BusiCityRecTrain>();
					newSet.add(rec);
					cityMap.put(city, newSet);
				}
			}
			refreshCityStatInfo();
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
	
	public String getCityName(String bID){
		if(busiRecMap.containsKey(bID)) return busiRecMap.get(bID).city;
		else return null;
	}
	
	public void printInfo(){
		System.out.println("total citys: "+cityMap.size());
		Iterator<String> it = cityMap.keySet().iterator();
		while(it.hasNext()){
			String city = it.next();
			System.out.println(city+": "+cityMap.get(city).size());
		}
		
	}
	

	class BusiCityRecTrain implements Serializable{
		private static final long serialVersionUID = -900464532125838257L;
		
		public String city;
		public String state;
		public String name;
		public String business_id;
		public double longitude;
		public double latitude;
		public double stars;
		public int review_count;
		
	}
}

