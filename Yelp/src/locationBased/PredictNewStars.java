package locationBased;

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
		fMatrix.setFilePathName("Overall/MyFloatMatrixEmpty");
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
			if(!matrix.containsUserId(uID)){
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
				if(star != emptyValue){
					if(matrix.containsBusinessId(eles[1])){
						double w1,w2;
						BusiCateRecTrain rec = cateMapTrain.busiRecMap.get(eles[1]);
						w1 = Math.log10(rec.review_count);
						w2 = 1.5;
						res[pos-1] = (float) ((w1*res[pos-1]+w2*star)/(w1+w2));
					}
					else res[pos-1] = star;
				}
				fileStream.write("\n"+eles[2]+","+res[pos-1]);
			}
			reader.close();
			fileStream.flush();
			fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private double predictStarsTmp1(String bID){
		String[] cates = testBusiRec.getCates(bID);
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
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
	}
	private double predictStarsTmp2(String bID){
		String[] cates = testBusiRec.getCates(bID);
		double weights = 0,totalStars = 0;
		double avg,MRSE;
		double revCntFact;
		if(cates != null){
			System.out.println("cate not null!");
			for(int i=0;i<cates.length;i++){
				if(cateMapTrain.cateAvg.containsKey(cates[i])){
					avg = cateMapTrain.cateAvg.get(cates[i]);
					MRSE = cateMapTrain.cateMRSE.get(cates[i]);
					
					if(MRSE < overallMRSE && MRSE != 0){
						revCntFact = Math.log10(cateMapTrain.cateRevCnt.get(cates[i])/MRSE);
						weights += revCntFact;
						totalStars += revCntFact*avg;
					}
				}
			}
		}
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
	}
	private double predictStarsTmp3(String bID){
		String[] cates = testBusiRec.getCates(bID);
		double weights = 0,totalStars = 0;
		double avg,MRSE;
		double revCntFact;
		if(cates != null){
			System.out.println("cate not null!");
			for(int i=0;i<cates.length;i++){
				if(cateMapTrain.cateAvg.containsKey(cates[i])){
					avg = cateMapTrain.cateAvg.get(cates[i]);
					MRSE = cateMapTrain.cateMRSE.get(cates[i]);
					
					if(MRSE < overallMRSE && MRSE != 0){
						revCntFact = cateMapTrain.cateIDF.get(cates[i]);
						weights += revCntFact;
						totalStars += revCntFact*avg;
					}
				}
			}
		}
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
	}
	private double predictStarsTmp4(String bID){
		String[] cates = testBusiRec.getCates(bID);
		double weights = 0,totalStars = 0;
		double avg,MRSE;
		double revCntFact;
		if(cates != null){
			System.out.println("cate not null!");
			for(int i=0;i<cates.length;i++){
				if(cateMapTrain.cateAvg.containsKey(cates[i])){
					avg = cateMapTrain.cateAvg.get(cates[i]);
					MRSE = cateMapTrain.cateMRSE.get(cates[i]);
					
					if(MRSE < overallMRSE && MRSE != 0){
						revCntFact = cateMapTrain.cateTFAvg.get(cates[i])*
								cateMapTrain.cateIDF.get(cates[i]);
						weights += revCntFact;
						totalStars += revCntFact*avg;
					}
				}
			}
		}
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
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
		if(totalStars == 0){
			//System.out.println(cnt++);
			return overallAvg;
		}
		else return totalStars/weights;
	}
	
	public void storeVisualBusinessInfo(String fileName){
		Iterator<dataMatrix.Pair> it = matrix.getKeySets().iterator();
		dataMatrix.Pair p;
		String bID;
		HashMap<String, Double> sums = new HashMap<String, Double>();
		HashMap<String, Double> cnts = new HashMap<String, Double>();
		HashMap<String, Double> busiRevAvg = new HashMap<String, Double>();
		while(it.hasNext()){
			p = it.next();
			bID = matrix.getBusinessID(p.y);
			if(! sums.containsKey(bID)){
				sums.put(bID, (double)matrix.get(p.x, p.y));
				cnts.put(bID, 1.0);
			}
			else{
				sums.put(bID, sums.get(bID)+(double)matrix.get(p.x, p.y));
				cnts.put(bID, cnts.get(bID)+1.0);
			}
		}
		
		Iterator<String> its = sums.keySet().iterator();
		while(its.hasNext()){
			bID = its.next();
			busiRevAvg.put(bID, sums.get(bID)/cnts.get(bID));
		}
		
		File file = new File("weightPredict/" + fileName);
		BufferedWriter fileStream;
		Iterator<BusiCateRecTrain> itb = cateMapTrain.busiRecMap.values().iterator();
		BusiCateRecTrain rec;
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file)));
			fileStream.write("BusinessId,AvgFromJason,AvgFromReviews," +
					"Avg[log(RevCnt)/MRSE],Avg[log(RevCnt/MRSE)],Avg[IDF],Avg[AvgTF*IDF],ReviewCnt");
			while(itb.hasNext()){
				rec = itb.next();
				bID = rec.business_id;
				fileStream.write("\n"+rec.business_id+
						","+rec.stars+","+busiRevAvg.get(bID)+
						","+predictStarsTmp1(bID)+
						","+predictStarsTmp2(bID)+
						","+predictStarsTmp3(bID)+
						","+predictStarsTmp4(bID)+","+rec.review_count);
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void storeVisualBusinessInfoTest(String fileName){
		String bID;		
		File file = new File("weightPredict/" + fileName);
		BufferedWriter fileStream;
		Iterator<String> it = testBusiRec.busiRecMap.keySet().iterator();
		try {
			fileStream = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file)));
			fileStream.write("BusinessId," +
					"Avg[log(RevCnt)/MRSE],Avg[log(RevCnt/MRSE)],Avg[IDF],Avg[AvgTF*IDF]," +
					"newAvgs...");
			while(it.hasNext()){
				bID = it.next();
				double tmp1 = predictStarsTmp1(bID);
				double tmp2 = predictStarsTmp2(bID);
				double tmp3 = predictStarsTmp3(bID);
				double tmp4 = predictStarsTmp4(bID);
				fileStream.write("\n"+bID+
						","+tmp1+
						","+tmp2+
						","+tmp3+
						","+tmp4+
						","+(1.351406*tmp1-1.516091)+
						","+(1.352317*tmp2-1.517384)+
						","+(1.301367*tmp3-1.334024)+
						","+(1.306046*tmp4-1.351651));
			}
			fileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PredictNewStars pns = new PredictNewStars();
		pns.init();
//		pns.predict();
//		pns.genCSV();
		pns.storeVisualBusinessInfoTest("testBusiAvgInfos.csv");
	}

}
