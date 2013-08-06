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
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class BusinessMapOverallTest {
	public HashMap<String, BusiOverallRecTest> busiRecMap;
	public int count = 0;
	public String jsonFileName = null;
	public String filePathName = null;
	public String fileName = "busiRecMap.hashmap";
	
	public BusinessMapOverallTest() {
		busiRecMap = new HashMap<String, BusiOverallRecTest>();
	}
	
	public void clear(){
		busiRecMap.clear();
	}
	
	public void storeMap(){
		if(busiRecMap == null || busiRecMap.isEmpty()) return;
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			if(filePathName != null && (!filePathName.equalsIgnoreCase(""))){
				File file = new File(filePathName);
				if(! file.exists()) file.mkdirs();
			}
			
			fo = new FileOutputStream(filePathName+"/"+fileName);
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
			fi = new FileInputStream(filePathName+"/"+fileName);
			oi = new ObjectInputStream(fi);
			busiRecMap = (HashMap<String, BusiOverallRecTest>)oi.readObject();
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
	
	public void getMapFromJason(){
		busiRecMap.clear();
		File inFile = new File(jsonFileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			BusiOverallRecTest rec;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiOverallRecTest.class);
				if(rec.city.equalsIgnoreCase("Fountain Hls")) rec.city = "Fountain Hills";
				else if(rec.city.equalsIgnoreCase("Scottsdale ")) rec.city = "Scottsdale";
				rec.streetFilter();

				busiRecMap.put(rec.business_id, rec);
			}
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
	public String getCityName(String bID){
		if(busiRecMap.containsKey(bID)) return busiRecMap.get(bID).city;
		else return null;
	}
	public String getStreetName(String bID){
		if(busiRecMap.containsKey(bID)) return busiRecMap.get(bID).streetName;
		else return null;
	}
	
	public static void main(String[] args){
		BusinessMapOverallTest bmot = new BusinessMapOverallTest();
		bmot.getMapFromJason("yelp_test_set_business.json");
		bmot.storeMap("test_busi_rec");
	}
	
}

class BusiOverallRecTest implements Serializable{
	private static final long serialVersionUID = -8826183908137961493L;
	
	public String city;
	public String state;
	public String name;
	public String business_id;
	public String[] categories;
	public double longitude;
	public double latitude;
	public String full_address;
	
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
