package trainingSetSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Splitter {
	String pathName = "training_set_split";
	public void splitReview(){
		HashMap<ReviewDateRec, String> revMap = new HashMap<ReviewDateRec, String>();
		File inFile = new File("yelp_training_set_review.json");
		ReviewDateRec rec;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, ReviewDateRec.class);
				revMap.put(rec, jsonString);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ReviewDateRec[] tmp = new ReviewDateRec[1];
		List<ReviewDateRec> revList = Arrays.asList(revMap.keySet().toArray(tmp));
		
		Collections.sort(revList, new Comparator<ReviewDateRec>() {
			@Override
			public int compare(ReviewDateRec o1, ReviewDateRec o2) {
				return o1.date.compareTo(o2.date);
			}
		});
		
		try {
			File file = new File(pathName);
			if(! file.exists()) file.mkdirs();
			file = new File(pathName+"/training_set_review.json");
			BufferedWriter fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file)));
			int i=0;
			for(i=0;i<206916;i++){
				rec = revList.get(i);
				fileStream.write(revMap.get(rec) + "\n");
			}
			fileStream.close();
			
			file = new File(pathName+"/test_set_review.json");
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file)));
			while(i<revList.size()){
				rec = revList.get(i);
				i++;
				fileStream.write(revMap.get(rec)+"\n");
			}
			fileStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitBusiness(){
		Set<String> busiSet = new HashSet<String>();
		
		File inFile = new File(pathName+"/training_set_review.json");
		BusiIdRec rec;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiIdRec.class);
				busiSet.add(rec.business_id);
			}
			reader.close();
			
			File outFileTest = new File(pathName+"/test_set_business.json");
			File outFileTrain = new File(pathName+"/training_set_business.json");
			BufferedWriter writerTest = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTest)));
			BufferedWriter writerTrain = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTrain)));
			
			inFile = new File("yelp_training_set_business.json");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiIdRec.class);
				if(busiSet.contains(rec.business_id))
					writerTrain.write(jsonString+"\n");
				else writerTest.write(jsonString+"\n");
			}
			reader.close();
			writerTest.close();
			writerTrain.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitUser(){
		Set<String> userSet = new HashSet<String>();
		
		File inFile = new File(pathName+"/training_set_review.json");
		UserIdRec rec;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, UserIdRec.class);
				userSet.add(rec.user_id);
			}
			reader.close();
			
			File outFileTest = new File(pathName+"/test_set_user.json");
			File outFileTrain = new File(pathName+"/training_set_user.json");
			BufferedWriter writerTest = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTest)));
			BufferedWriter writerTrain = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTrain)));
			
			inFile = new File("yelp_training_set_user.json");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, UserIdRec.class);
				if(userSet.contains(rec.user_id))
					writerTrain.write(jsonString+"\n");
				else writerTest.write(jsonString+"\n");
			}
			reader.close();
			writerTest.close();
			writerTrain.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitCheckin(){
		Set<String> busiSet = new HashSet<String>();
		
		File inFile = new File(pathName+"/training_set_review.json");
		BusiIdRec rec;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiIdRec.class);
				busiSet.add(rec.business_id);
			}
			reader.close();
			
			File outFileTest = new File(pathName+"/test_set_checkin.json");
			File outFileTrain = new File(pathName+"/training_set_checkin.json");
			BufferedWriter writerTest = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTest)));
			BufferedWriter writerTrain = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFileTrain)));
			
			inFile = new File("yelp_training_set_checkin.json");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, BusiIdRec.class);
				if(busiSet.contains(rec.business_id))
					writerTrain.write(jsonString+"\n");
				else writerTest.write(jsonString+"\n");
			}
			reader.close();
			writerTest.close();
			writerTrain.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new Splitter().splitCheckin();
	}
}

class ReviewDateRec {
	String review_id;
	String date;
}

class BusiIdRec {
	String business_id;
}

class UserIdRec {
	String user_id;
}

