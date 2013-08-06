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

import locationBased.StreetMapTrain.BusiStreetRecTrain;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataMatrix.MyMatrix;
import dataMatrix.Pair;


public class CateMapTrain {
	public HashMap<String, BusiCateRecTrain> busiRecMap;
	
	public HashMap<String, Set<BusiCateRecTrain> > cateMap;
	public HashMap<String, Double> cateAvg;
	public HashMap<String, Double> cateMRSE;
	public HashMap<String, Integer> cateRevCnt;
	public HashMap<String, Integer> cateBusiCnt;
	public int count = 0;
	public String jsonFileName = null;
	public String filePathName = null;
	public static final String busiRecMapfileName = "busiRecMap.hashmap";
	public static final String cateMapfileName = "cateMap.hashmap";
	public static final String cateAvgfileName = "cateAvg.hashmap";
	public static final String cateRevCntfileName = "cateRevCnt.hashmap";
	public static final String cateBusiCntfileName = "cateBusiCnt.hashmap";
	public static final String cateMRSEfileName = "cateMRSE.hashmap";
	public static final String cateTFAvgfileName = "cateTFAvg.hashmap";
	public static final String cateIDFfileName = "cateIDF.hashmap";
	
	public HashMap<String, Double> cateAvgMale;
	public HashMap<String, Double> cateMRSEMale;
	public HashMap<String, Integer> cateRevCntMale;
	public HashMap<String, Integer> cateBusiCntMale;
	
	public HashMap<String, Double> cateAvgFemale;
	public HashMap<String, Double> cateMRSEFemale;
	public HashMap<String, Integer> cateRevCntFemale;
	public HashMap<String, Integer> cateBusiCntFemale;
	
	public HashMap<String, Double> cateTFAvg;
	public HashMap<String, Double> cateIDF;
	
	public CateMapTrain() {
		cateMap = new HashMap<String, Set<BusiCateRecTrain> >();
		cateAvg = new HashMap<String, Double>();
		cateRevCnt = new HashMap<String, Integer>();
		cateBusiCnt = new HashMap<String, Integer>();
		cateMRSE = new HashMap<String, Double>();
		
		cateAvgMale = new HashMap<String, Double>();
		cateRevCntMale = new HashMap<String, Integer>();
		cateBusiCntMale = new HashMap<String, Integer>();
		cateMRSEMale = new HashMap<String, Double>();
		
		cateAvgFemale = new HashMap<String, Double>();
		cateRevCntFemale = new HashMap<String, Integer>();
		cateBusiCntFemale = new HashMap<String, Integer>();
		cateMRSEFemale = new HashMap<String, Double>();
		
		busiRecMap = new HashMap<String, BusiCateRecTrain>();
		cateTFAvg = new HashMap<String, Double>();
		cateIDF = new HashMap<String, Double>();
	}
	
	public void storeVisualTFIDF(String fileName){
		HashMap<String, Double> cateTFsum = new HashMap<String, Double>();
		Iterator<BusiCateRecTrain> it = busiRecMap.values().iterator();
		BusiCateRecTrain rec;
		while(it.hasNext()){
			rec = it.next();
			for(String cate : rec.categories){
				double TF = 1.0/(rec.categories.length);
				if(! cateTFsum.containsKey(cate))
					cateTFsum.put(cate, TF);
				else
					cateTFsum.put(cate,cateTFsum.get(cate)+TF);
			}
		}
		
		File outFile = new File(filePathName + "/" + fileName);
		BufferedWriter fileStream;
		Iterator<String> its = cateAvg.keySet().iterator();
		String currentCate;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("Category,AvgTF,IDF,AvgTF*IDF,BusinessCnt,MRSE");
			while(its.hasNext()){
				currentCate = its.next();
				int busiCnt = cateBusiCnt.get(currentCate);
				double avgTF = cateTFsum.get(currentCate)/busiCnt;
				double IDF = Math.log((double)busiRecMap.size() / busiCnt);
				fileStream.write("\n"+currentCate+
						","+avgTF+","+IDF+","+avgTF*IDF+
						","+busiCnt+","+cateMRSE.get(currentCate));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stroeVisualSomeCateInfo(String cateName){
		if(filePathName == null || filePathName.equalsIgnoreCase("")) return;
		
		File outFile = new File(filePathName + "/"+cateName+"CateInfo.csv");
		BufferedWriter fileStream;
		Iterator<BusiCateRecTrain> it = cateMap.get(cateName).iterator();
		BusiCateRecTrain rec;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("BusinessId,Longitude,Latitute,Stars,ReviewCnt,City,State");
			while(it.hasNext()){
				rec = it.next();
				fileStream.write("\n"+rec.business_id+
						","+rec.longitude+","+rec.latitude+
						","+rec.stars+","+rec.review_count+
						","+rec.city+","+rec.state);
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void storeVisualCateAvg(){
		if(filePathName == null || filePathName.equalsIgnoreCase("")) return;
		
		File outFile = new File(filePathName + "/CateAvgStar.txt");
		BufferedWriter fileStream;
		Iterator<String> it = cateAvg.keySet().iterator();
		String currentCate;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("Category,AvgStars,BusinessCnt,TotalReviewCnt,MRSE");
			while(it.hasNext()){
				currentCate = it.next();
				fileStream.write("\n"+currentCate+
						","+cateAvg.get(currentCate)+
						","+cateBusiCnt.get(currentCate)+
						","+cateRevCnt.get(currentCate)+
						","+cateMRSE.get(currentCate));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void refreshCateStatInfo(){
		cateAvg.clear();
		cateBusiCnt.clear();
		cateMRSE.clear();
		cateRevCnt.clear();
		cateTFAvg.clear();
		
		Iterator<String> itS = cateMap.keySet().iterator();
		Set<BusiCateRecTrain> set_t;
		while(itS.hasNext()){
			String cName = itS.next();
			set_t = cateMap.get(cName);
			cateBusiCnt.put(cName, set_t.size());
		}
		
		MyMatrix matrix = new MyMatrix();
		matrix.setFilePathName("origin");
		matrix.getMapFromFile();
		
		HashMap<String, Integer> totalStars = new HashMap<String, Integer>();
		
		Iterator<Pair> it = matrix.getKeySets().iterator();
		Pair pair;
		String bID;
		String cate;
		BusiCateRecTrain rec;
		count = 0;
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			if(rec.categories == null) continue;
			
			count ++;
			int star = matrix.get(pair.x, pair.y);
			for(int i=0;i<rec.categories.length;i++){
				cate = rec.categories[i];
				if(totalStars.containsKey(cate)){
					totalStars.put(cate,totalStars.get(cate)+star);
					cateRevCnt.put(cate, cateRevCnt.get(cate)+1);
				}
				else{
					totalStars.put(cate,star);
					cateRevCnt.put(cate, 1);
				}
			}
		}
		itS = cateMap.keySet().iterator();
		while(itS.hasNext()){
			cate = itS.next();
			if(!totalStars.containsKey(cate)){
				System.err.println("STREET NOT FOUND");
				System.exit(1);
			}
			double total = totalStars.get(cate);
			double count = cateRevCnt.get(cate);
			cateAvg.put(cate, total/count);
		}
		
		HashMap<String, Double> totalErr = new HashMap<String, Double>();
		it = matrix.getKeySets().iterator();
		while(it.hasNext()){
			pair = it.next();
			bID = matrix.getBusinessID(pair.y);
			rec = busiRecMap.get(bID);
			if(rec.categories == null) continue;
			
			double star = matrix.get(pair.x, pair.y);
			for(int i=0;i<rec.categories.length;i++){
				cate = rec.categories[i];
				if(!cateAvg.containsKey(cate)){
					System.err.println("CATE NOT FOUND:");
					System.out.println(cate);
					System.exit(1);
				}
				double avg = cateAvg.get(cate);
				if(totalErr.containsKey(cate))
					totalErr.put(cate,totalErr.get(cate) + (star-avg)*(star-avg));
				else
					totalErr.put(cate,(star-avg)*(star-avg));
			}
		}
		itS = cateMap.keySet().iterator();
		while(itS.hasNext()){
			cate = itS.next();
			double err = totalErr.get(cate);
			double count = cateRevCnt.get(cate);
			cateMRSE.put(cate, Math.sqrt(err/count));
		}

		HashMap<String, Double> cateTFsum = new HashMap<String, Double>();
		Iterator<BusiCateRecTrain> itb = busiRecMap.values().iterator();
		while(itb.hasNext()){
			rec = itb.next();
			for(String cateName : rec.categories){
				double TF = 1.0/(rec.categories.length);
				if(! cateTFsum.containsKey(cateName))
					cateTFsum.put(cateName, TF);
				else
					cateTFsum.put(cateName,cateTFsum.get(cateName)+TF);
			}
		}
		itS = cateMap.keySet().iterator();
		while(itS.hasNext()){
			cate = itS.next();
			cateTFAvg.put(cate, cateTFsum.get(cate)/cateBusiCnt.get(cate));
			cateIDF.put(cate, Math.log((double)busiRecMap.size() / cateBusiCnt.get(cate)));
		}
	}
	
	public void clear(){
		if(cateMap != null) cateMap.clear();
		if(cateAvg != null) cateAvg.clear();
		if(cateRevCnt != null) cateRevCnt.clear();
		if(cateBusiCnt != null) cateBusiCnt.clear();
	}
	
	public void storeMap(){
		if(cateMap == null || cateMap.isEmpty()) return;
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			if(filePathName != null && (!filePathName.equalsIgnoreCase(""))){
				File file = new File(filePathName);
				if(! file.exists()) file.mkdirs();
			}
			
			fo = new FileOutputStream(filePathName+"/"+cateMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateMap);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateAvgfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateAvg);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateRevCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateRevCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateBusiCntfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateBusiCnt);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateMRSEfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateMRSE);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+busiRecMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(busiRecMap);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateTFAvgfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateTFAvg);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+cateIDFfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(cateIDF);
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
			fi = new FileInputStream(filePathName+"/"+cateMapfileName);
			oi = new ObjectInputStream(fi);
			cateMap = (HashMap<String, Set<BusiCateRecTrain> >)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateAvgfileName);
			oi = new ObjectInputStream(fi);
			cateAvg = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateRevCntfileName);
			oi = new ObjectInputStream(fi);
			cateRevCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateBusiCntfileName);
			oi = new ObjectInputStream(fi);
			cateBusiCnt = (HashMap<String, Integer>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateMRSEfileName);
			oi = new ObjectInputStream(fi);
			cateMRSE = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+busiRecMapfileName);
			oi = new ObjectInputStream(fi);
			busiRecMap = (HashMap<String, BusiCateRecTrain>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateTFAvgfileName);
			oi = new ObjectInputStream(fi);
			cateTFAvg = (HashMap<String, Double>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+cateIDFfileName);
			oi = new ObjectInputStream(fi);
			cateIDF = (HashMap<String, Double>)oi.readObject();
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
	
	public Set<BusiCateRecTrain> getAllBusiRec(){
		Set<BusiCateRecTrain> allSet = new HashSet<BusiCateRecTrain>();
		Iterator<Set<BusiCateRecTrain> > it = cateMap.values().iterator();
		while(it.hasNext()){
			allSet.addAll(it.next());
		}
		return allSet;
	}
	
	public void getMapFromJason(){
		cateMap.clear();
		File inFile = new File(jsonFileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			BusiCateRecTrain rec;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiCateRecTrain.class);
				
				if(rec.city.equalsIgnoreCase("Fountain Hls")) rec.city = "Fountain Hills";
				else if(rec.city.equalsIgnoreCase("Scottsdale ")) rec.city = "Scottsdale";
				
				busiRecMap.put(rec.business_id, rec);
				for(int i=0;i<rec.categories.length;i++){
					String cate = rec.categories[i];
					if(cateMap.containsKey(cate)){
						cateMap.get(cate).add(rec);
					}
					else{
						Set<BusiCateRecTrain> newSet = new HashSet<BusiCateRecTrain>();
						newSet.add(rec);
						cateMap.put(cate, newSet);
					}
				}
				
			}
			refreshCateStatInfo();
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
	
	public String[] getCates(String bID){
		if(busiRecMap.containsKey(bID)) return busiRecMap.get(bID).categories;
		else return null;
	}
	
	public void printInfo(){
		System.out.println("total categories: "+cateMap.size());
		Iterator<String> it = cateMap.keySet().iterator();
		while(it.hasNext()){
			String city = it.next();
			System.out.println(city+": "+cateMap.get(city).size());
		}
		
	}
	
	public static void main(String[] args){
		CateMapTrain cmt = new CateMapTrain();
		cmt.getMapFromJason("yelp_training_set_business.json");
		cmt.storeMap("training_cate");
//		cmt.loadMap("training_cate");
//		//cmt.stroeVisualSomeCateInfo("Tanning");
//		cmt.storeVisualTFIDF("CateTFIDFInfo.csv");
		
	}
}

class BusiCateRecTrain implements Serializable{
	private static final long serialVersionUID = -900464532125838257L;
	
	public String city;
	public String state;
	public String name;
	public String business_id;
	public double longitude;
	public double latitude;
	public double stars;
	public int review_count;
	public String[] categories;
}
