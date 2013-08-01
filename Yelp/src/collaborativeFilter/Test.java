package collaborativeFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import dataMatrix.MyMatrix;
import genMatrix.GenMatrix;

public class Test {
	public static void main(String[] args) throws IOException {
		totalAvg();
		
//		C4UPCC upcc = new C4UPCC();
//		upcc.loadMatrix("Sets/0");
//		upcc.computeUPCC();
		
//		fMatrix.setFilePathName("UIPCC/Overall/MyFloatMatrix");
//		GenMatrix.getMatrixFromTestJson(fMatrix, "", "yelp_test_set_review.json");
		
//		MyMatrix matrix = new MyMatrix();
//		matrix.setFilePathName("SmallOrigin");
//		matrix.getMapFromFile();
//		System.out.println("map entry size: " + matrix.getSize());
//		System.out.println("map user size: " + matrix.getUserLen());
//		System.out.println("map service size: " + matrix.getBusinessLen());
		
//		float[] testF = {4,5,7,2,3,634,5,24,1};
//		float[] res = VectorUtil.filterTopK(testF, 4);
//		for(int i=0;i<res.length;i++) System.out.print(res[i] + ", ");
		
//		int[] ia = {1,2,3,5,7,10,11,12,20,22,23,25};
//		int[] ib = {0,4,5,7,10,12,23};
//		int[] res = VectorUtil.nonZeroPosBoth(ia, ib);
//		for(int i=0;i<res.length;i++) System.out.print(res[i] + ", ");
	}
	
	public static void totalAvg() throws IOException{
		File inFile = new File("yelp_training_set_review.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
		String jsonString;
		ReviewRec rev;
		int total = 0, cnt = 0;
		while((jsonString = reader.readLine()) != null){
			rev = new Gson().fromJson(jsonString, ReviewRec.class);
			total += rev.stars;
			cnt ++;
		}
		System.out.println("total avg: "+(double)total/cnt);
	}
	
	public static void seekNaNPos(){
		MyFloatMatrix mfm = new MyFloatMatrix();
		mfm.setFilePathName("UIPCC/Overall/MyFloatMatrixRes");
		mfm.getMapFromFile();
		MyMatrix[] mm = new MyMatrix[5];
		MyMatrix mma = new MyMatrix();
		mma.setFilePathName("origin");
		mma.getMapFromFile();
		for(int i=0;i<5;i++){
			mm[i] = new MyMatrix();
			mm[i].setFilePathName("Sets/"+i);
			mm[i].getMapFromFile();
		}
		Iterator<Pair> it = mfm.getKeySets().iterator();
		Pair pair;
		String uID,bID;
		Float star;
		while(it.hasNext()){
			pair = it.next();
			uID = mfm.getUserID(pair.x);
			bID = mfm.getBusinessID(pair.y);
			star = mfm.get(pair.x, pair.y);
			//System.out.println(star);
			if(star.isNaN()){
				System.out.println("NaN GET: uid :"+uID+" bid: "+bID);
				for(int i=0;i<5;i++)
					System.out.println("in set"+i+": uid: "+mm[i].containsUserId(uID)+" bid: "+mm[i].containsBusinessId(bID));
				System.out.println("in all : uid: "+mma.containsUserId(uID)+" bid: "+mma.containsBusinessId(bID));
			}
		}
	}
	
	public static void genCSV() throws IOException{
		MyFloatMatrix fMatrix = new MyFloatMatrix();
		fMatrix.setFilePathName("UIPCC/Overall/MyFloatMatrixRes");
		fMatrix.getMapFromFile();
		Iterator<Pair> it = fMatrix.getKeySets().iterator();
		Pair pair;
		String uID,bID;
		float star;
		File inFile = new File("IdLookupTable.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
		reader.readLine();
		
		File outFile = new File("UIPCC/Overall/SubmissionRes.csv");
		BufferedWriter fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
		fileStream.write("RecommendationId,Stars");
		
		String line;
		String[] eles;
		int posX,posY;
		while((line = reader.readLine()) != null){
			eles = line.split(",");
			posX = fMatrix.getUserIndex(eles[0]);
			posY = fMatrix.getBusinessIndex(eles[1]);
			star = fMatrix.get(posX, posY);
			fileStream.write("\n"+eles[2]+","+star);
		}
		reader.close();
		fileStream.flush();
		fileStream.close();
	}
	
	public static void calcPredictMyMatrix(){
		MyFloatMatrix fMatrix = new MyFloatMatrix();
		
		fMatrix.setFilePathName("UIPCC/Overall/MyFloatMatrixEmpty");
		fMatrix.getMapFromFile();
		int i,j,m,n,k=5;
		float[][] preMatrix;
		MyMatrix iMatrix = new MyMatrix();
		Set<Pair> keySets = fMatrix.getKeySets();
		Iterator<Pair> it;
		Pair pair;
		String filePath,uID,bID;
		boolean bu,bb;
		float userAvg;
		Map<String, Float> businessSums = new HashMap<String,Float>();
		Map<String, Integer> businessCnts = new HashMap<String,Integer>();
		
		for(i=0;i<k;i++){
			filePath = "UIPCC/Sets/"+i;
			preMatrix = VectorUtil.loadMatrixFloat(filePath, "PreMatrix");
			iMatrix.setFilePathName("Sets/"+i);
			iMatrix.getMapFromFile();
			for(m=0;m<iMatrix.getBusinessLen();m++){
				String bid = iMatrix.getBusinessID(m);
				for(n=0;n<iMatrix.getUserLen();n++){
					if(preMatrix[n][m] != 0){
						if(businessSums.containsKey(bid)){
							businessSums.put(bid, businessSums.get(bid)+preMatrix[n][m]);
							businessCnts.put(bid, businessCnts.get(bid)+1);
						}
						else{
							businessSums.put(bid, preMatrix[n][m]);
							businessCnts.put(bid, 1);
						}
					}
				}
			}
			
			it = keySets.iterator();
			while(it.hasNext()){
				pair = it.next();
				uID = fMatrix.getUserID(pair.x);
				bID = fMatrix.getBusinessID(pair.y);
				bu = iMatrix.containsUserId(uID);
				bb = iMatrix.containsBusinessId(bID);
				if(bu && bb){		//userId businessId 都在
					int xPos = iMatrix.getUserIndex(uID);
					int yPos = iMatrix.getBusinessIndex(bID);
					fMatrix.putByIndex(pair.x, pair.y, preMatrix[xPos][yPos]);
				}
				else if(bu && !bb){		//user id在， bid不在
					int xPos = iMatrix.getUserIndex(uID);
					userAvg = VectorUtil.calcAvgNonZeroFloat(preMatrix[xPos]);
					if(Float.isNaN(userAvg)){
						System.err.println("userAvg NaN !!!");
						return ;
					}
					fMatrix.putByIndex(pair.x, pair.y, userAvg);
				}
			}
			preMatrix = null;
			iMatrix.clear();
			System.gc();
		}
		
		float totalSum=0;
		int totalCnt=0;
		Iterator<String> stringIt = businessSums.keySet().iterator();
		while(stringIt.hasNext()){
			String id = stringIt.next();
			totalSum += businessSums.get(id);
			totalCnt += businessCnts.get(id);
		}
		Float totalAvg = totalSum/totalCnt;
		if(totalAvg.isNaN()){
			System.err.println("totalAvg NaN !!!");
			return ;
		}
		
		iMatrix.setFilePathName("origin");
		iMatrix.getMapFromFile();
		it = keySets.iterator();
		boolean hasPredicted;
		while(it.hasNext()){
			pair = it.next();
			uID = fMatrix.getUserID(pair.x);
			bID = fMatrix.getBusinessID(pair.y);
			bu = iMatrix.containsUserId(uID);
			bb = iMatrix.containsBusinessId(bID);
			hasPredicted = fMatrix.get(pair.x, pair.y)==-1 ? false : true;
			
			if(!hasPredicted){
				if(!bu && bb){
					if(businessCnts.get(bID) == null) System.out.println("bid: "+bID+"NULL POINTER IN MAP!!!");
					fMatrix.putByIndex(pair.x, pair.y, businessSums.get(bID)/businessCnts.get(bID));
				}
				else if(!bu && !bb)
					fMatrix.putByIndex(pair.x, pair.y, totalAvg);
				else{
					System.err.println("ERROR!!!!!!!!!!!!!");
					return;
				}
			}
		}
		fMatrix.setFilePathName("UIPCC/Overall/MyFloatMatrixRes");
		fMatrix.storeMapToFile();
	}
	
	public static void seeNaN(){
		float[][] fm = VectorUtil.loadMatrixFloat("UIPCC/Sets/0", "PreMatrix");
		for(int i=0;i<fm.length;i++){
			for(int j=0;j<fm[0].length;j++){
				if(fm[i][j] == 0) System.err.println("pos "+i+","+j+" zero");
				if(Float.isNaN(fm[i][j])) System.err.println("pos "+i+","+j+" NaN");
 			}
		}
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



