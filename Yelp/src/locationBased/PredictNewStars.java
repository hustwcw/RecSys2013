package locationBased;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import collaborativeFilter.MyFloatMatrix;
import collaborativeFilter.Pair;
import dataMatrix.MyMatrix;

public class PredictNewStars {
	MyFloatMatrix fMatrix;
	MyMatrix matrix;
	StreetMapTrain streetMapTrain;
	CateMapTrain cateMapTrain;
	CityMapTrain cityMapTrain;
	BusinessMapOverallTest testBusiRec;
	float emptyValue;
	static int cnt = 0;
	public static final double overallMRSE = 1.2170074388896859;
	public static final int cntBoud = 20;
	public static final double overallAvg = 3.766723;
	
	public void init(){
		fMatrix = new MyFloatMatrix();
		fMatrix.setFilePathName("UIPCC/Overall/MyFloatMatrixEmpty");
		fMatrix.getMapFromFile();
		emptyValue = fMatrix.get(0, 0);
		System.out.println("empty value: "+emptyValue);
		
		matrix = new MyMatrix();
		matrix.setFilePathName("origin");
		matrix.getMapFromFile();
		
		streetMapTrain = new StreetMapTrain();
		streetMapTrain.loadMap("training_street");
		
		cateMapTrain = new CateMapTrain();
		cateMapTrain.loadMap("training_cate");
		
		cityMapTrain = new CityMapTrain();
		cityMapTrain.loadMap("training_city");
		
		testBusiRec = new BusinessMapOverallTest();
		testBusiRec.loadMap("test_busi_rec");
	}
	public void predict(){
		Iterator<Pair> it = fMatrix.getKeySets().iterator();
		Pair pair;
		while(it.hasNext()){
			pair = it.next();
			String uID = fMatrix.getUserID(pair.x);
			String bID = fMatrix.getBusinessID(pair.y);
			if(!matrix.containsUserId(uID) && !matrix.containsBusinessId(bID)){
				fMatrix.putByIndex(pair.x, pair.y, (float)predictStars(uID,bID));
			}
		}
	}
	
	public void genCSV(){
		try {
			File inFile = new File("BiasSVD_lrate0.0004_factor20_iter320.csv");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			reader.readLine();
			String line;
			String[] eles;
			float[] res = new float[22956];
			while((line = reader.readLine()) != null){
				eles = line.split(",");
				int pos = Integer.valueOf(eles[0]);
				res[pos-1] = Float.valueOf(eles[1]);
			}
			reader.close();

			float star;
			inFile = new File("IdLookupTable.csv");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			reader.readLine();
			File outFile = new File("weightPredict/Overall/SubmissionRes.csv");
			BufferedWriter fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outFile)));
			fileStream.write("RecommendationId,Stars");
			
			int posX,posY;
			while((line = reader.readLine()) != null){
				eles = line.split(",");
				int pos = Integer.valueOf(eles[2]);
				posX = fMatrix.getUserIndex(eles[0]);
				posY = fMatrix.getBusinessIndex(eles[1]);
				star = fMatrix.get(posX, posY);
				if(star != emptyValue)
					res[pos-1] = star;
				fileStream.write("\n"+eles[2]+","+res[pos-1]);
			}
			reader.close();
			fileStream.flush();
			fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double predictStars(String uID,String bID){
		String[] cates = testBusiRec.getCates(bID);
		String city = testBusiRec.getCityName(bID);
		String street = city+" "+testBusiRec.getStreetName(bID);
		double weights = 0,totalStars = 0;
		double avg,MRSE;
		double revCntFact;
		if(cates != null){
			System.out.println("cate not null!");
			for(int i=0;i<cates.length;i++){
				if(cateMapTrain.cateAvg.containsKey(cates[i])){
					avg = cateMapTrain.cateAvg.get(cates[i]);
					MRSE = cateMapTrain.cateMRSE.get(cates[i]);
					revCntFact = Math.log10(cateMapTrain.cateRevCnt.get(cates[i]));
					if(MRSE < overallMRSE && MRSE != 0){
						weights += revCntFact/MRSE;
						totalStars += revCntFact*avg/MRSE;
					}
				}
			}
		}
//		
//		if(cityMapTrain.cityAvg.containsKey(city)){
//			System.out.println("city not null!");
//			avg = cityMapTrain.cityAvg.get(city);
//			MRSE = cityMapTrain.cityMRSE.get(city);
//			if(MRSE < overallMRSE && MRSE != 0){
//				weights += 1/MRSE;
//				totalStars += avg/MRSE;
//			}
//		}
//		
//		if(street != null && streetMapTrain.streetAvg.containsKey(street)){
//			System.out.println("street not null!");
//			avg = streetMapTrain.streetAvg.get(street);
//			MRSE = streetMapTrain.streetMRSE.get(street);
//			if(MRSE < overallMRSE && MRSE != 0){
//				weights += 1/MRSE;
//				totalStars += avg/MRSE;
//			}
//		}
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
	}
	
	public static void main(String[] args) {
		PredictNewStars pns = new PredictNewStars();
		pns.init();
		pns.predict();
		pns.genCSV();
	}

}
