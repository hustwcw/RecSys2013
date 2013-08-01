package userSplit;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import dataMatrix.MyMatrix;

public class SplitKMeans {
	private MyMatrix matrix;
	private Center[] centers;
	private HashSet<Integer>[] sets;
	private int businessNum = 0;
	private int userNum = 0;
	private int k;
	private String visibleFileName = "k-meansSets.txt";
	private String matrixFilePrefixName = "Sets";
	private static final int INTERATION_TIMES = 5;
	public void split(int k, MyMatrix matrix){
		int i,j;
		this.k = k;
		this.matrix = matrix;
		//System.out.println("matrix get test: "+ matrix.get(0,0));
		matrix.getMapFromFile();
		businessNum = matrix.getBusinessLen();
		userNum = matrix.getUserLen();
		
		init();
		
		for(i=0;i<INTERATION_TIMES;i++){
			//for(j=0;j<k;j++) System.out.println("c"+j+": "+centers[j]+"\n");
			classify();
			refreshCenters();
		}
		
		//System.out.println("\n\ncenters:");
		for(i=0;i<k;i++) System.out.println("set"+i+": "+sets[i].size());
		System.out.println("error: " + getError());
	}
	
	public void setVisibleFileName(String name){
		visibleFileName = name;
	}
	public void setMatrixFilePrefixName(String name){
		matrixFilePrefixName = name;
	}
	
	public void storeMatrixFile(){
		int i,j,userPos;
		Iterator<Integer> it;
		MyMatrix[] mToStore = new MyMatrix[k];
		String bID,uID;
		for(i=0;i<k;i++){
			mToStore[i] = new MyMatrix();
			it = sets[i].iterator();
			while(it.hasNext()){
				userPos = it.next();
				for(j=0;j<businessNum;j++){
					if(matrix.containsKey(userPos, j)){
						uID = matrix.getUserID(userPos);
						bID = matrix.getBusinessID(j);
						mToStore[i].putById(uID, bID, matrix.get(userPos, j));
					}
				}
			}
		}
		for(i=0;i<k;i++){
			mToStore[i].setFilePathName(matrixFilePrefixName+i);
			mToStore[i].storeMapToFile();
		}
	}
	
	public void storeVisibleFile(){
		int i,j;
		int userPos,star;
		Iterator<Integer> it;
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(visibleFileName)));
			writer.write("set i:\nuser pos: (business pos, star)\n\n");
			for(i=0;i<k;i++){
				it = sets[i].iterator();
				writer.write("\nset "+i+": \n");
				while(it.hasNext()){
					userPos = it.next();
					writer.write("user "+userPos+": ");
					for(j=0;j<businessNum;j++){
						star = matrix.get(userPos, j);
						if(star != 0) writer.write("("+j+", "+star+") ");
					}
					writer.write("\n");
				}
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initTest(){
		int i;
		centers = new Center[k];
		sets = new HashSet[k];
		for(i=0;i<k;i++){
			centers[i] = new Center(businessNum);	
			sets[i] = new HashSet<Integer>();
		}
		
		centers[0].vector = new double[]{5,5,5,4.5,5,1,1.5,1,1,1};
		centers[1].vector = new double[]{1,1,1,2,1,5,5,4.5,5,5};
	}
	
	private void init(){
		int i,j;
		centers = new Center[k];
		sets = new HashSet[k];
		for(i=0;i<k;i++){
			centers[i] = new Center(businessNum);	
			sets[i] = new HashSet<Integer>();
		}
		
		Random rand = new Random();
		for(i=0;i<k;i++){
			for(j=0;j<businessNum;j++) centers[i].vector[j] = rand.nextDouble()*4+1;
		}
	}
	
	private void classify(){
		int i,j;
		int minPos=0;
		double minDist=0;
		double dist;
		for(i=0;i<k;i++) sets[i].clear();
		for(i=0;i<userNum;i++){
			for(j=0;j<k;j++){
				dist = calcDist(i, j);
				if(dist<minDist || j==0){
					minDist = dist;
					minPos = j;
				}
			}
			sets[minPos].add(i);
		}
		//for(i=0;i<k;i++) System.out.println(sets[i]);
	}
	
	private void refreshCenters(){
		int i,j;
		Iterator<Integer> it;
		double[] tmpD = new double[businessNum];
		double star;
		int currentPos;
		int[] count = new int[businessNum];
		for(i=0;i<k;i++){
			it = sets[i].iterator();
			for(j=0;j<businessNum;j++){
				tmpD[j] = 0;
				count[j] = 0;
			}
			while(it.hasNext()){
				currentPos = it.next();
				for(j=0;j<businessNum;j++){
					if(matrix.containsKey(currentPos, j)){
						star = (double)matrix.get(currentPos, j);
						tmpD[j] += star;
						count[j] ++;
					}
				}
			}
			for(j=0;j<businessNum;j++){
				//System.out.println("debug count: "+count[j]);
				if(count[j] != 0) centers[i].vector[j] = tmpD[j]/count[j];
			}
		}
	}
	
	public double getError(){
		int i,pos;
		Iterator<Integer> it;
		double dist=0;
		for(i=0;i<k;i++){
			it = sets[i].iterator();
			while(it.hasNext()){
				pos = it.next();
				dist += calcDist(pos, i);
			}
		}
		
		return dist/userNum;
	}
	
	private double calcDist(int userPos, int centerPos){
		double dist=0;
		double star;
		double star2;
		int count=0;
		for(int i=0; i<businessNum; i++){
			star = (double)matrix.get(userPos, i);
			star2 = centers[centerPos].vector[i];
			//System.out.println("star: "+star+"  star2: "+star2);
			if(star != 0 && star2 != 0){
				dist += Math.abs(star-star2);
				count++;
			}
		}
		if(count == 0) return 4;		//TODO
		else return dist/count;
	}
}

class Center{
	int index;
	double[] vector;
	public Center(int length){
		vector = new double[length];
		for(int i=0;i<length;i++) vector[i] = 0;
	}
	public String toString(){
		String ret = "{";
		int i;
		for(i=0;i<vector.length;i++) ret = ret + vector[i] +", ";
		return ret+"}";
	}
}
