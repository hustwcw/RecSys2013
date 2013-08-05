package genderSearcher;

import genderSearcher.GenderKind.Gender;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SearchGender {
	Set<UserName> recSet;
	HashMap<String, Gender> genderIdMap;
	HashMap<String, Gender> genderNameMap;
	String filePathName;
	String nameSetfileName = "nameSet.set";
	String genderIdMapfileName = "genderIdMap.hashmap";
	String genderNameMapfileName = "genderNameMap.hashmap";
	
	public SearchGender() {
		recSet = new HashSet<UserName>();
		genderIdMap = new HashMap<String, GenderKind.Gender>();
		genderNameMap = new HashMap<String, GenderKind.Gender>();
	}
	
	public void storeVisualGenderDict(){
		if(genderIdMap.isEmpty() || recSet.isEmpty()) return;
		
		File outFile = new File(filePathName + "/genderDictionary.csv");
		BufferedWriter fileStream;
		Iterator<String> it = genderIdMap.keySet().iterator();
		String uID;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("userId,gender");
			while(it.hasNext()){
				uID = it.next();
				fileStream.write("\n"+uID+","+genderIdMap.get(uID));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void storeMap(){
		if(genderIdMap.isEmpty() || recSet.isEmpty()) return;
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			if(filePathName != null && (!filePathName.equalsIgnoreCase(""))){
				File file = new File(filePathName);
				if(! file.exists()) file.mkdirs();
			}
			
			fo = new FileOutputStream(filePathName+"/"+nameSetfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(recSet);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+genderIdMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(genderIdMap);
			oo.close();
			
			fo = new FileOutputStream(filePathName+"/"+genderNameMapfileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(genderNameMap);
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
		genderIdMap.clear();
		recSet.clear();
		FileInputStream fi;
		ObjectInputStream oi;
		try {
			fi = new FileInputStream(filePathName+"/"+nameSetfileName);
			oi = new ObjectInputStream(fi);
			recSet = (HashSet<UserName>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+genderIdMapfileName);
			oi = new ObjectInputStream(fi);
			genderIdMap = (HashMap<String, GenderKind.Gender>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+"/"+genderNameMapfileName);
			oi = new ObjectInputStream(fi);
			genderNameMap = (HashMap<String, GenderKind.Gender>)oi.readObject();
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
	
	private void getNameMap(){
		genderNameMap.clear();
		File inFile = new File("mf.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String line;
			String[] eles;
			
			reader.readLine();
			while((line = reader.readLine()) != null){
				eles = line.split("\t");
				//System.out.println(eles[0].toLowerCase());
				if(eles[1].equalsIgnoreCase("m"))
					genderNameMap.put(eles[0].toLowerCase(), Gender.MALE);
				else if(eles[1].equalsIgnoreCase("f"))
					genderNameMap.put(eles[0].toLowerCase(), Gender.FEMALE);
				else
					System.err.println("wrong txt format");
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Gender searchGender(String name){
		try {
			System.out.println("searching name: "+name);
			Document doc = Jsoup.connect("http://ename.dict.cn/"+name).get();
			//System.out.println(doc.toString());
			Elements eles = doc.getElementsByTag("label");
			for(Element e : eles){
				String text = e.text();
				if(text.equalsIgnoreCase("Ãû×ÖÀà±ð£º")){
					Element genderE = e.nextElementSibling().children().get(0);
					if(genderE.attr("class").equalsIgnoreCase("male")) return Gender.MALE;
					else if(genderE.attr("class").equalsIgnoreCase("female")) return Gender.FEMALE;
				}
			}
			return Gender.UNKNOWN;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void refreshAllGenders(){
		genderIdMap.clear();
		Iterator<UserName> it = recSet.iterator();
		UserName rec;
		String uID;
		Gender g;
		String[] eles;
		int cnt=0;
		while(it.hasNext()){
			System.out.println("#"+(++cnt));
			rec = it.next();
			uID = rec.user_id;
			String name = nameFormatter(rec.name);
			g = Gender.UNKNOWN;
			
			if(name.contains("miss") || name.contains("mrs")) g=Gender.FEMALE;
			else if(name.contains("mr ")) g=Gender.MALE;
			else{
				eles = name.split(" ");
				if(genderNameMap.containsKey(eles[0]))
					g = genderNameMap.get(eles[0]);
				else g = searchGender(eles[0]);
				
				if(g == null) System.err.println("gender null!");
				else genderNameMap.put(eles[0], g);
			}
			
			genderIdMap.put(uID, g);
		}
		
	}
	
	private String nameFormatter(String name){
		name = name.toLowerCase();
		
		String testreg = "[^a-zA-Z\\s]";
        Pattern matchsip = Pattern.compile(testreg);
        Matcher mp = matchsip.matcher(name);
        name = mp.replaceAll("");
        return name;
	}
	
	public void getNameGenderMapFromJason(){
		File inFile = new File("yelp_test_set_user.json");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			UserName name;
			while((jsonString = reader.readLine()) != null){
				name = new Gson().fromJson(jsonString, UserName.class);
				recSet.add(name);
			}
			reader.close();
			
			inFile = new File("yelp_training_set_user.json");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			while((jsonString = reader.readLine()) != null){
				name = new Gson().fromJson(jsonString, UserName.class);
				recSet.add(name);
			}
			reader.close();
			getNameMap();
			refreshAllGenders();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printInfo(){
		int fCnt=0,mCnt=0,upCnt=0,ukCnt=0;
		Iterator<Gender> it = genderIdMap.values().iterator();
		while(it.hasNext()){
			switch(it.next()){
			case MALE:
				mCnt++;
				break;
				
			case FEMALE:
				fCnt++;
				break;
				
			case UNPROCESSED:
				upCnt++;
				break;
				
			case UNKNOWN:
				ukCnt++;
				break;
			}
		}
		
		System.out.println("MALE: "+mCnt);
		System.out.println("FEMALE: "+fCnt);
		System.out.println("UNKNOWN: "+ukCnt);
		System.out.println("UNPROCESSED: "+upCnt);
	}
	
	public static void main(String[] args) {
		SearchGender sg = new SearchGender();
		sg.loadMap("genderMap");
//		sg.storeVisualGenderDict();
//		sg.getNameGenderMapFromJason();
//		sg.storeMap("genderMap");
		sg.printInfo();
		
		//System.out.println(new SearchGender().searchGender("AZ"));
	}
	
	class UserName implements Serializable{
		private static final long serialVersionUID = 49133782402772917L;
		
		String name;
		String user_id;
	}
}
