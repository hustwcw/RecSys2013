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
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataMatrix.MyMatrix;
import dataMatrix.Pair;


public class StreetMapTrain {
	public HashMap<String, BusiStreetRecTrain> busiRecMap;
	public HashMap<String, Set<BusiStreetRecTrain> > streetMap;
	public HashMap<String, Double> streetAvg;
	public HashMap<String, Double> streetMRSE;
	public HashMap<String, Integer> streetRevCnt;
	public HashMap<String, Integer> streetBusiCnt;
	public int count = 0;
	public String jsonFileName = null;
	public String filePathName = null;
	public static final String busiRecMapfileName = "busiRecMap.hashmap";
	public static final String streetMapfileName = "streetMap.hashmap";
	public static final String streetAvgfileName = "streetAvg.hashmap";
	public static final String streetRevCntfileName = "streetRevCnt.hashmap";
	public static final String streetBusiCntfileName = "streetBusiCnt.hashmap";
	public static final String streetMRSEfileName = "streetMRSE.hashmap";
	
	public StreetMapTrain() {
		streetMap = new HashMap<String, Set<BusiStreetRecTrain> >();
		streetAvg = new HashMap<String, Double>();
		streetBusiCnt = new HashMap<String, Integer>();
		streetMRSE = new HashMap<String, Double>();
		streetRevCnt = new HashMap<String, Integer>();
		
		busiRecMap = new HashMap<String, StreetMapTrain.BusiStreetRecTrain>();
	}
	
	public void storeVisualStreetAvg(){
		if(filePathName == null || filePathName.equalsIgnoreCase("")) return;
		
		File outFile = new File(filePathName + "/StreetAvgStar.txt");
		BufferedWriter fileStream;
		Iterator<String> it = streetMap.keySet().iterator();
		String[] currentPos;
		String currentKey;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("City,Street,AvgStars,BusinessCnt,TotalReviewCnt,MRSE");
			while(it.hasNext()){
				currentKey = it.next();
				currentPos = currentKey.split(" ");
				fileStream.write("\n"+currentPos[0]+
						","+currentPos[1]+
						","+streetAvg.get(currentKey)+
						","+streetBusiCnt.get(currentKey)+
						","+streetRevCnt.get(currentKey)+
						","+streetMRSE.get(currentKey));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void refreshStreetStatInfo(){
		streetAvg.clear();
		streetBusiCnt.clear();
		streetMRSE.clear();
		streetRevCnt.clear();
		
		Iterator<String> itS = streetMap.keySet().iterator();
		Set<BusiStreetRecTrain> set_t;
		while(itS.hasNext()){
			String sName = itS.next();
			set_t = streetMap.get(sName);
			streetBusiCnt.put(sName, set_t.size());
		}
		
		MyMatrix matrix = new MyMatrix();
		matrix.setFilePathName("origin");
		matrix.getMapFromFile();
		
		HashMap<String, Integer> totalStars = new HashMap<String, Integer>();
		
		Iterator<Pair> it = matrix.getKeySets().iterator();
		Pair pair;
		String bID;
		String street;
		BusiStreetRecTrain rec;
		count = 0;
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			if(rec.streetName == null) continue;
			street = rec.city+" "+rec.streetName;
			count ++;
			if(totalStars.containsKey(street)){
				totalStars.put(street,totalStars.get(street)+matrix.get(pair.x, pair.y));
				streetRevCnt.put(street, streetRevCnt.get(street)+1);
			}
			else{
				totalStars.put(street,matrix.get(pair.x, pair.y));
				streetRevCnt.put(street, 1);
			}
		}
		itS = streetMap.keySet().iterator();
		while(itS.hasNext()){
			street = itS.next();
			if(!totalStars.containsKey(street)){
				System.err.println("STREET NOT FOUND");
				System.exit(1);
			}
			double total = totalStars.get(street);
			double count = streetRevCnt.get(street);
			streetAvg.put(street, total/count);
		}
		
		HashMap<String, Double> totalErr = new HashMap<String, Double>();
		it = matrix.getKeySets().iterator();
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			if(rec.streetName == null) continue;
			street = rec.city+" "+rec.streetName;
			double star = matrix.get(pair.x, pair.y);
			if(!streetAvg.containsKey(street)){
				System.err.println("STREET NOT FOUND:");
				System.out.println(street);
				System.exit(1);
			}
			double avg = streetAvg.get(street);
			if(totalErr.containsKey(street))
				totalErr.put(street,totalErr.get(street) + (star-avg)*(star-avg));
			else
				totalErr.put(street,(star-avg)*(star-avg));
		}
		itS = streetMap.keySet().iterator();
		while(itS.hasNext()){
			street = itS.next();
			double err = totalErr.get(street);
			double count = streetRevCnt.get(street);
			streetMRSE.put(street, Math.sqrt(err/count));
		}
		
//		Iterator<String> it1 = streetMap.keySet().iterator();
//		Iterator<BusiStreetRecTrain> it2;
//		BusiStreetRecTrain rec;
//		double totalStars = 0;
//		int revCnt = 0;
//		int busiCnt = 0;
//		count = 0;
//		String currentStreet;
//		
//		while(it1.hasNext()){
//			currentStreet = it1.next();
//			it2 = streetMap.get(currentStreet).iterator();
//			totalStars = 0;
//			revCnt = 0;
//			busiCnt = 0;
//			while(it2.hasNext()){
//				rec = it2.next();
//				totalStars += (rec.stars * rec.review_count);
//				revCnt += rec.review_count;
//				busiCnt ++;
//				count ++;
//			}
//			double avg = totalStars/revCnt;
//			streetAvg.put(currentStreet, avg);
//			streetRevCnt.put(currentStreet, revCnt);
//			streetBusiCnt.put(currentStreet, busiCnt);
//			
//			double errTotal=0;
//			it2 = streetMap.get(currentStreet).iterator();
//			while(it2.hasNext()){
//				rec = it2.next();
//				errTotal += (rec.stars - avg)*(rec.stars - avg)*rec.review_count;
//			}
//			double MRSE = Math.sqrt(errTotal/revCnt);
//			streetMRSE.put(currentStreet, MRSE);
//		}
	}
	
	public void clear(){
		streetMap.clear();
		streetAvg.clear();
		streetBusiCnt.clear();
		streetMRSE.clear();
		streetRevCnt.clear();
	}
	
	
	public void storeMap(){
		if(streetMap == null || streetMap.isEmpty()) return;
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			if(filePathName != null && (!filePathName.equalsIgnoreCase(""))){
				File file = new File(filePathName);
				if(! file.exists()) file.mkdirs();
			}
			
			fo = new FileOutputStream(filePathName+"/"+streetMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(streetMap);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+streetAvgfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(streetAvg);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+streetRevCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(streetRevCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+streetBusiCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(streetBusiCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+streetMRSEfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(streetMRSE);
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
			fi = new FileInputStream(filePathName+"/"+streetMapfileName);
			oi = new ObjectInputStream(fi);
			streetMap = (HashMap<String, Set<BusiStreetRecTrain> >)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+streetAvgfileName);
			oi = new ObjectInputStream(fi);
			streetAvg = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+streetRevCntfileName);
			oi = new ObjectInputStream(fi);
			streetRevCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+streetBusiCntfileName);
			oi = new ObjectInputStream(fi);
			streetBusiCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+streetMRSEfileName);
			oi = new ObjectInputStream(fi);
			streetMRSE = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+busiRecMapfileName);
			oi = new ObjectInputStream(fi);
			busiRecMap = (HashMap<String, BusiStreetRecTrain>)oi.readObject();
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
	
	public Set<BusiStreetRecTrain> getAllBusiRec(){
		Set<BusiStreetRecTrain> allSet = new HashSet<BusiStreetRecTrain>();
		Iterator<Set<BusiStreetRecTrain> > it = streetMap.values().iterator();
		while(it.hasNext()){
			allSet.addAll(it.next());
		}
		return allSet;
	}
	
	public void getMapFromJason(){
		streetMap.clear();
		count=0;
		File inFile = new File(jsonFileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			BusiStreetRecTrain rec;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiStreetRecTrain.class);
				
				if(rec.city.equalsIgnoreCase("Fountain Hls")) rec.city = "Fountain Hills";
				else if(rec.city.equalsIgnoreCase("Scottsdale ")) rec.city = "Scottsdale";
				
				rec.streetFilter();
				busiRecMap.put(rec.business_id, rec);
				if(rec.streetName == null) continue;
				count++;
				String fullName = rec.city+" "+rec.streetName;
				if(streetMap.containsKey(fullName)){
					streetMap.get(fullName).add(rec);
				}
				else{
					Set<BusiStreetRecTrain> newSet = new HashSet<BusiStreetRecTrain>();
					newSet.add(rec);
					streetMap.put(fullName, newSet);
				}
			}
			refreshStreetStatInfo();
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
	
	public String getStreetName(String bID){
		if(busiRecMap.containsKey(bID)) return busiRecMap.get(bID).streetName;
		else return null;
	}
	
	public void printInfo(){
		System.out.println("total streets: "+streetMap.size());
		Iterator<String> it = streetMap.keySet().iterator();
		while(it.hasNext()){
			String city = it.next();
			System.out.println(city+": "+streetMap.get(city).size());
		}
		
	}
	

	class BusiStreetRecTrain implements Serializable{
		private static final long serialVersionUID = -900464532125838257L;
		
		public String city;
		public String state;
		public String name;
		public String business_id;
		public String full_address;
		public double stars;
		public int review_count;
		
		public String streetName;
		public void streetFilter(){
			if(full_address==null || full_address.equalsIgnoreCase("")){
				System.err.println("full_address not initiated!");
				streetName = null;
				return;
			}
			
			String[] eles = full_address.split("\n");
			String[] streets = eles[0].split(" ");
			
			if(!isRightStreetFormat(streets)){
				if(eles.length >= 3) streets = eles[1].split(" ");
			}
			if(!isRightStreetFormat(streets)){
				System.err.println("full_address format err:");
				System.out.println(full_address);
				streetName = null;
				return;
			}
			
			streetName = "";
			for(int i=1;i<streets.length;i++) streetName = streetName+streets[i];
		}

		public boolean isRightStreetFormat(String strs[]){
			if(strs.length < 3) return false;
			
			strs[1] = strs[1].replaceAll("East", "");
			strs[1] = strs[1].replaceAll("South", "");
			strs[1] = strs[1].replaceAll("West", "");
			strs[1] = strs[1].replaceAll("North", "");
			strs[1] = strs[1].replaceAll("E.", "");
			strs[1] = strs[1].replaceAll("S.", "");
			strs[1] = strs[1].replaceAll("W.", "");
			strs[1] = strs[1].replaceAll("N.", "");
			strs[1] = strs[1].replaceAll("E", "");
			strs[1] = strs[1].replaceAll("S", "");
			strs[1] = strs[1].replaceAll("W", "");
			strs[1] = strs[1].replaceAll("N", "");
			
			if(!isNumeric(strs[0])) return false;
			
			return true;
		}
		public boolean isNumeric(String str){
			str = str.replaceAll("-", "");
		    Pattern pattern = Pattern.compile("[0-9]*");
		    return pattern.matcher(str).matches();
		}  
	}
}

