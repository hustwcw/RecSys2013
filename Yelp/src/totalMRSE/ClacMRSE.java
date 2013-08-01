package totalMRSE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ClacMRSE {

	public void calcMRSE(String fileName){
		Set<ReviewRec> revSet = new HashSet<ClacMRSE.ReviewRec>();
		File inFile = new File(fileName);
		double totalStars=0;
		int totalRevCnt=0;
		ReviewRec rec = new ReviewRec();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String jsonString;
			while((jsonString = reader.readLine()) != null){
				rec = new Gson().fromJson(jsonString, ReviewRec.class);
				revSet.add(rec);
				totalStars += rec.stars;
				totalRevCnt ++;
			}
			reader.close();
			
			double totalAvg = totalStars/totalRevCnt;
			Iterator<ReviewRec> it = revSet.iterator();
			double err = 0;
			while(it.hasNext()){
				rec = it.next();
				double tmp = ( ((double)rec.stars) - totalAvg );
				err += tmp*tmp;
			}
			System.out.println(Math.sqrt(err/totalRevCnt));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ClacMRSE().calcMRSE("training_set_split/training_set_review.json");

	}

	class ReviewRec {
		String review_id;
		int stars;
	}
}
