package genMatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import collaborativeFilter.MyFloatMatrix;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataMatrix.MyMatrix;


public class GenMatrix {
	private MyMatrix matrix = new MyMatrix();
	private static String fileName = "yelp_training_set_review.json";
	public void getMatrixFromFile(){
		matrix.getMapFromFile();
		System.out.print("user len: "+matrix.getUserLen()+
				"\nbusiness len: "+matrix.getBusinessLen()+
				"\nsize: "+matrix.getSize() + "\n\n");
	}
	public static void getMatrixFromTestJson(MyFloatMatrix fMatrix, String pathName, String fileName){
		File file;
		EmptyReview rev;
		if(pathName.equalsIgnoreCase("")) file = new File(fileName);
		else file = new File(pathName+"/"+fileName);
		String jsonString;
		try {
			BufferedReader fileStream = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
			while((jsonString = fileStream.readLine()) != null){
				rev = new Gson().fromJson(jsonString, EmptyReview.class);
				//System.out.print(rev);
				if(rev.user_id.length()!=22) System.out.print(rev);
				
				fMatrix.putById(rev.user_id, rev.business_id, -1);
			}
			System.out.print("user len: "+fMatrix.getUserLen()+
					"\nbusiness len: "+fMatrix.getBusinessLen()+
					"\nsize: "+fMatrix.getSize());
			fMatrix.storeMapToFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void getMatrixFromJson(){
		File file;
		ReviewRec rev;
		file = new File(fileName);
		String jsonString;
		//open the file
		try {
			BufferedReader fileStream = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
			while((jsonString = fileStream.readLine()) != null){
				rev = new Gson().fromJson(jsonString, ReviewRec.class);
				//System.out.print(rev);
				if(rev.user_id.length()!=22) System.out.print(rev);
				
				matrix.putById(rev.user_id, rev.business_id, rev.stars);
			}
			System.out.print("user len: "+matrix.getUserLen()+
					"\nbusiness len: "+matrix.getBusinessLen()+
					"\nsize: "+matrix.getSize());
			matrix.storeMapToFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		MyFloatMatrix fMatrix = new MyFloatMatrix();
		fMatrix.setFilePathName("Overall/MyFloatMatrixEmpty");
		getMatrixFromTestJson(fMatrix, "", "yelp_test_set_review.json");
	}
}

class ReviewRec{
	public String user_id;
	public String review_id;
	public String business_id;
	public int stars;
	
	public String toString(){
		return(
			"user_id: " + user_id +
			"\nbusiness_id: " + business_id +
			"\nreview_id: " + review_id +
			"\nstars: " + stars + "\n"
		);
	}
}

class EmptyReview{
	String user_id;
	String business_id;
	
	public String toString(){
		if (user_id == null || business_id == null) return "";
		else return ("user id: "+user_id+"\tbusiness id: "+business_id);
	}
}

