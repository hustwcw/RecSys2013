package userSplit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataMatrix.MyMatrix;

public class SplitUser {
	private String fileName = "testDataForKMeans.txt";
	private MyMatrix matrix = new MyMatrix();
	public void splitUser(){
		matrix.setFilePathName("Origin");
		matrix.getMapFromFile();
		System.out.println(matrix.getSize());
		SplitKMeans skm = new SplitKMeans();
		skm.split(5, matrix);
		//skm.storeVisibleFile();
		//skm.setMatrixFilePrefixName("SmallSets");	//file name prefix
		//skm.storeMatrixFile();
	}
	public void splitTest(){
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
				System.out.print(rev);
				
				matrix.putById(rev.user_id, rev.business_id, rev.stars);
			}
			System.out.print("user len: "+matrix.getUserLen()+
					"\nbusiness len: "+matrix.getBusinessLen()+
					"\nsize: "+matrix.getSize() + "\n\n");
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SplitKMeans skm = new SplitKMeans();
		skm.split(2, matrix);
		//skm.storeVisibleFile();
		skm.storeMatrixFile();
	}
}

class ReviewRec{
	public String user_id;
	public String business_id;
	public int stars;
	
	public String toString(){
		return(
			"user_id: " + user_id +
			"\nbusiness_id: " + business_id +
			"\nstars: " + stars + "\n"
		);
	}
}

