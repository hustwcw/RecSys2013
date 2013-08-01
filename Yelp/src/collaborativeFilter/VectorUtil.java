package collaborativeFilter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class VectorUtil {
	
//	private static final String Suffix = ".dat";
	public static float calcAvgNonZeroFloat(float[] vector){
		float sum=0;
		int cnt=0,len=vector.length;
		for(int i=0;i<len;i++){
			if(vector[i] != 0){
				sum += vector[i];
				cnt++;
			}
		}
		if(cnt==0) System.err.println("cnt is zero!!!");
		if(Float.isNaN(sum/cnt)) System.err.println("sum="+sum+" cnt="+cnt);
		return sum/cnt;
	}
	public static float calcAvgNonZeroColFloat(float[][] matrix, int colIndex){
		float sum=0,len=matrix.length;
		for(int i=0;i<len;i++) sum += matrix[i][colIndex];
		
		return sum/len;	
	}
	public static float calcSumColFloat(float[][] matrix, int colIndex){
		float sum=0,len=matrix.length;
		for(int i=0;i<len;i++) sum += matrix[i][colIndex];
		
		return sum;
	}
	//将Ｍatrix中第column提取出来
	public static float[] assignVector(float[][] matrix, int column) {
		int rowSize = matrix.length;      //
		float[] vector = new float[rowSize];
		
		for(int t = 0; t < rowSize; t++) {            //从０开始而不是１
			vector[t] = matrix[t][column];            //列向量
		}
		
		return vector;
	}
	
	//将Matrix中第row行提取出来
	public static float[] assignRowVector(float[][] matrix, int row) {
		return matrix[row];
	}//...
	
	public static int nonZeroNums(float[] vector){
		int i,sum=0;
		for(i=0;i<vector.length;i++){
			if(vector[i]!=0) sum++;
		}
		return sum;
	}
	public static int[] nonZeroPos(float[] vector){
		List<Integer> posList = new LinkedList<Integer>();
		int i,tmp;
		for(i=0;i<vector.length;i++){
			if(vector[i] != 0) posList.add(i);
		}
		int[] ret = new int[posList.size()];
		Iterator<Integer> it = posList.iterator();
		i=0;
		while(it.hasNext()){
			tmp = it.next();
			ret[i] = tmp;
			i++;
		}
		return ret;
	}
	public static int[] nonZeroPosBoth(int[] v1, int[] v2){
		List<Integer> posList = new LinkedList<Integer>();
		int i1,i2,t1,t2;
		i1=i2=0;
		while(i1<v1.length && i2<v2.length){
			t1 = v1[i1];
			t2 = v2[i2];
			if(t1 == t2){
				posList.add(t1);
				i1++;
				i2++;
			}
			else if(t1 < t2) i1++;
			else i2++;
		}
		int[] ret = new int[posList.size()];
		Iterator<Integer> it = posList.iterator();
		int tmp,i=0;
		while(it.hasNext()){
			tmp = it.next();
			ret[i] = tmp;
			i++;
		}
		return ret;
		
	}
	
	//共同调用过的服务
	public static int[] nonZeroIndexBoth(int[] nonZeroIndexA, int[] nonZeroIndexU) {
		 
		 int size = nonZeroIndexA.length;
		 int[] nonZeroIndexAU = new int[size];
		 for(int i = 0; i < size; i++) {
			if((nonZeroIndexA[i] != 0) && (nonZeroIndexU[i] != 0)) {
				nonZeroIndexAU[i] = 1;
			} else {
				nonZeroIndexAU[i] = 0;
			}
		 }//for(i...)
		 
		 return nonZeroIndexAU;
	}//...
	
	//调用过的服务的下标
	public static int[] nonZeroIndex(float[] vector) {
		int size = vector.length;
		int[] nonZeroIndex = new int[size];
		for(int i = 0; i < size; i++) {
			if(vector[i] != 0) {
				nonZeroIndex[i] = 1;      //非零元素
			}
		}
		
		return nonZeroIndex;
	}
	
	//计算两个向量的内积
	public static float multipleVector(float[] vectorU,float[] vectorS) {
		float result = 0;
		int size = vectorU.length;
		for(int i = 0; i < size; i++) {
			result = result + vectorU[i] * vectorS[i];
		}
		
		return result;
	}
	
	//计算标题与向量乘积
	public static float[] multipleVector(float a, float[] vector) {
		
		for(int i = 0; i < vector.length; i++) {
			vector[i] = a * vector[i];
		}
		
		return vector;
	}
	
	//计算向量元素之和:float
	public static float sumVector(float[] vector) {
		float sum = 0;
		for(int i = 0; i < vector.length; i++) {
			sum = sum + vector[i];
		}
		
		return sum;
	}
	
	//计算向量元素之和:sum
	public static int sumVector(int[] vector) {
		int sum = 0; 
		for(int i = 0; i < vector.length; i++) {
			sum = sum + vector[i];
		}
		
		return sum;
	}
	
	//计算两向量之和
	public static float[] sumVector(float[] vector1, float[] vector2) {
		int size = vector1.length;
		float[] vectorSum  = new float[size];
		
		for(int i = 0; i < size; i++) {
			vectorSum[i] = vector1[i] + vector2[i]; 
		}
		
		return vectorSum;
	}
	
	//计算两向量之差
	public static float[] distractVector(float[] vector1, float[] vector2) {
		int size = vector1.length;
		float[] vectorDiff = new float[size];
		
		for(int i = 0; i < size; i++) {
			vectorDiff[i] = vector1[i] - vector2[i];
		}
		
		return vectorDiff;
	}
	
	//初始化向量，全赋为0.0
	public static float[] initVector(int D) {
		float[] vector = new float[D];
		
		for(int i = 0; i < vector.length; i++) {
			vector[i] = 0;
		}
		
		return vector;
	}
	
	// matrix multiplication
	public static float[][] computeMatrixUS(float[][] MatrixU, float[][] MatrixS) {
		int rowSize = MatrixU.length;
		int colUSize = MatrixU[0].length;
		int colSSize = MatrixS[0].length;
		
		float[][] matrixUS = new float[colUSize][colSSize];
		for(int j = 0; j < colUSize; j++) {
			for(int k = 0; k < colSSize; k++) {
				
				float entry = 0;
				for(int d = 0; d < rowSize; d++) {
					entry = entry + MatrixU[d][j] * MatrixS[d][k];
				}
				matrixUS[j][k] = entry;
			}
		}//
		
		return matrixUS;
	}
	
	//计算两个矩阵的减法
	public static float[][] distractMatrix(float[][] matrix1, float[][] matrix2) {
		
		int rowSize = matrix1.length;
		int columnSize = matrix1[0].length;
		
		float[][] resultMatrix = new float[rowSize][columnSize];
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				resultMatrix[i][j] = matrix1[i][j] - matrix2[i][j];
			}
		}
		
		return resultMatrix;
		
	}
	
	//输出matrix以验证
	public static void outputMatrix(float[][] matrix) {
		
		int rowSize = matrix.length;
		int columnSize = matrix[0].length;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	//
	public static int[] sortForTopK(float[] simVector, int topK) {
		int[] neighborVector = new int[topK];
		int size = simVector.length;
		
		float max;
		int index;
		for(int i = 0; i < size - 1; i++) {
			
			max = simVector[i];
			index = i; 
			for(int j = i+1; j < size; j++) {
				
				if(simVector[j] > max) {
					max = simVector[j];
					index = j;
				}//if(...)
				
			}//for(j...)
			
			simVector[index] = simVector[i];
			simVector[i] = max;
			neighborVector[i] = index;
			if(i == (topK - 1)) {
				break;
			}
		}//for(i...)
		
		return neighborVector;
	}//sortForTopK(...)
	
	private static int getMinFloatPos(float[] vector){
		if(vector.length == 0) return -1;
		
		int i,minPos = 0;
		float min = vector[0];
		for(i=1;i<vector.length;i++){
			if(vector[i] < min){
				min = vector[i]; 
				minPos = i;
			}
		}
		return minPos;
	}
	public static float[] filterTopK(float[] vector, int TopK) {
		float[] topKValue = new float[TopK];
		int[] topKIndex = new int[TopK];
		int i,size = vector.length;
		float[] ret = new float[size];
		float cur;
		int minPos;
		
		if(TopK >= size){
			for(i=0;i<size;i++) ret[i] = vector[i];
			return ret;
		}
		
		for(i=0;i<TopK;i++){
			topKValue[i] = vector[i];
			topKIndex[i] = i;
		}
		minPos = getMinFloatPos(topKValue);
		
		for(i=TopK;i<size;i++){
			cur = vector[i];
			if(cur > topKValue[minPos]){
				topKValue[minPos] = cur;
				topKIndex[minPos] = i;
				minPos = getMinFloatPos(topKValue);
			}
		}
		
		for(i=0;i<TopK;i++){
			ret[topKIndex[i]] = topKValue[i];
		}
		return ret;
	}
	public static int[] filterTopKIndex(float[] vector, int TopK) {
		float[] topKValue = new float[TopK];
		int[] topKIndex = new int[TopK];
		int i,size = vector.length;
		float cur;
		int minPos;
		
		if(TopK >= size) return null;
		
		for(i=0;i<TopK;i++){
			topKValue[i] = vector[i];
			topKIndex[i] = i;
		}
		minPos = getMinFloatPos(topKValue);
		
		for(i=TopK;i<size;i++){
			cur = vector[i];
			if(cur > topKValue[minPos]){
				topKValue[minPos] = cur;
				topKIndex[minPos] = i;
				minPos = getMinFloatPos(topKValue);
			}
		}
		
		return topKIndex;
	}
	
	//输出matrix以验证
	public static void outputMatrix(float[][] matrix, int rowSize) {
		int columnSize = matrix[0].length;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
		
	}
	
	//输出matrix以验证
	public static void outputMatrix(float[][] matrix, String fileName) 
			throws FileNotFoundException {
		
		String writeFileName = fileName;
		PrintStream printStream = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(writeFileName)));
		
		int rowSize = matrix.length;
		int columnSize = matrix[0].length;
		System.out.println(rowSize);
		
		for(int i = 0 ; i < rowSize; i++) {
			for(int j = 0 ; j < columnSize; j++) {
				printStream.print(matrix[i][j] + " ");
				printStream.flush();
//				System.out.print(matrix[i][j] + " ");
			}
			printStream.println();
			printStream.flush();

		}
		printStream.close();
	}//
	
	//输出matrix以验证
	public static void outputMatrix(int[][] matrix, String fileName) 
				throws FileNotFoundException {
			
		String writeFileName = fileName;
		PrintStream printStream = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(writeFileName)));
			
		int rowSize = matrix.length;
		int columnSize = matrix[0].length;
//		System.out.println(rowSize);
			
		for(int i = 0 ; i < rowSize; i++) {
			for(int j = 0 ; j < columnSize; j++) {
				
				printStream.print(matrix[i][j] + " ");
				printStream.flush();
				
			}
			printStream.println();
			printStream.flush();
		}
		
		printStream.close();
	}//
	
	//打印出vector
		public static void outputVector(int[] vector, String fileName) throws IOException {
			
			int size = vector.length;
			PrintStream printStream = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(fileName, true))); //append
			for(int i = 0; i < size; i++) {
				if(vector[i] == 0) {
					printStream.print((int)vector[i] + " ");
				} else {
					printStream.print(vector[i] + " ");
				}
				printStream.flush();
			}
			printStream.println();
			printStream.flush();
			
		}//
	
	//打印出vector
	public static void outputVector(float[] vector, String fileName) throws IOException {
		
		int size = vector.length;
		PrintStream printStream = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(fileName, true))); //append
		for(int i = 0; i < size; i++) {
			if(vector[i] == 0) {
				printStream.print((int)vector[i] + " ");
			} else {
				printStream.print(vector[i] + " ");
			}
			printStream.flush();
		}
		printStream.println();
		printStream.flush();
		
	}//
	
	//输出RMSE/MAE到文件中
	public static void outputFileRMSE_MAE(String fileName, String outputContent) throws IOException {
		
		PrintStream printStream = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(fileName, true)));    //追加的方式
		
		printStream.print(outputContent);
		printStream.println();
		
		printStream.close();
	}//outputFileRMSE_MAE(...)
	
	
	
	//计算两个矩阵差的RMSE
	public static float computeMidRMSE(float[][] newRTMatrix, float[][] oldRTMatrix) {
		
		float midRMSE = 0;
		
		int rowSize = newRTMatrix.length;
		int columnSize = newRTMatrix[0].length;
		int totalSize = rowSize * columnSize;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				midRMSE = midRMSE + 
						(newRTMatrix[i][j] - oldRTMatrix[i][j]) * (newRTMatrix[i][j] - oldRTMatrix[i][j]); 
			}
		}
		
		midRMSE = (float)Math.sqrt(midRMSE/totalSize);
		
		return midRMSE;
	}//computeMidRMSE(...)
	
	//计算最终的RMSE与MAE值
	public static float[] computeMAE_RMSE(float[][] cutMatrix, float[][] preMatrix) {
		
		float[] MAE_RMSE = new float[2]; //
		
		int rowSize = cutMatrix.length;
		int columnSize = cutMatrix[0].length;
		
		System.out.println(rowSize + " " + columnSize);
		
		float MAE = 0;
		float RMSE = 0;
		int N = 0; 
		for(int i = 0; i < rowSize; i++) {
			
			for(int j = 0; j < columnSize; j++) {   //等于0的那些值是在RTMatrix中的
				if(cutMatrix[i][j] != 0) {
					RMSE = RMSE + (cutMatrix[i][j] - preMatrix[i][j]) * (cutMatrix[i][j] - preMatrix[i][j]);
					MAE = MAE + Math.abs(cutMatrix[i][j] - preMatrix[i][j]);
					N++;
					
				}//
			}//for(j...)
		}//for(i...)
		
		MAE_RMSE[0] = MAE/N;
		MAE_RMSE[1] = (float)Math.sqrt(RMSE/N);
		
		return MAE_RMSE;
	}//computeRMSE_MAE()
	
	//输出RMSE/MAE到文件中
	public static void outputFinalRMSE_MAE(String fileName, String resultRecord) 
			throws IOException {
			
		PrintStream printStream = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(fileName,true)));    //append to the end
			
		printStream.print(resultRecord);
		printStream.flush();
		printStream.println();
		printStream.flush();
			
	}//outputFileRMSE_MAE(...)
	
	//从指定的文件中载入Sring List到String列表
	public static String[] loadStringList(String fileName, int listSize) 
			throws IOException {
		String[] strList = new String[listSize];
		
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));
		
		String readLine = new String("");
		int lineNum = 0;
		while((readLine = bufferedReader.readLine()) != null) {
			strList[lineNum] = readLine;
			lineNum++;
		}
		
		return strList;
	}//
	
	//从文件中载入指定文件的内容到指定的矩阵,float[][]
	public static float[][] loadMatrix(String fileName, int rowSize, int columnSize) 
			throws IOException {
		float[][] matrix = new float[rowSize][columnSize];
		
		BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName)));
			
			String readLine = new String("");
			int userNum = 0;
			while((readLine = bufferedReader.readLine()) != null) {
				if(userNum >= rowSize) {
//					System.err.println("Overflow");
					break;
				}
				String[] splitResult = readLine.split(" ");
				
				for(int j = 0; j < columnSize; j++) {
					matrix[userNum][j] = Float.valueOf(splitResult[j]);
				}
				
				userNum++;
			}//
		
		return matrix;
	}
	
	//从文件中载入指定文件的内容到指定的矩阵,int[][]
	public static int[][] loadMatrixInt(String fileName, int rowSize, int columnSize) 
			throws IOException {
		int[][] matrix = new int[rowSize][columnSize];
		
		BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName)));
			
			String readLine = new String("");
			int userNum = 0;
			while((readLine = bufferedReader.readLine()) != null) {
				if(userNum >= rowSize) {
					break;
				}
				String[] splitResult = readLine.split(" ");
				
				for(int j = 0; j < columnSize; j++) {
					matrix[userNum][j] = Integer.valueOf(splitResult[j]);
				}
				
				userNum++;
			}//
		
		return matrix;
	}
	
	public static void storeMatrixFloat(float[][] fMatrix, String filePath, String fileName){
		FileOutputStream fo;
		ObjectOutputStream oo;
		
		File file = new File(filePath);
		if(! file.exists()) file.mkdirs();
		
		try {
			fo = new FileOutputStream(filePath+"/"+fileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(fMatrix);
			oo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static float[][] loadMatrixFloat(String filePath, String fileName){
		float[][] fMatrix = null;
		FileInputStream fi;
		ObjectInputStream oi;
		try {
			fi = new FileInputStream(filePath+"/"+fileName);
			oi = new ObjectInputStream(fi);
			
			fMatrix = (float[][])oi.readObject();
			fi.close();
			oi.close();
			
			System.out.println("row num: "+fMatrix.length);
			System.out.println("col num: "+fMatrix[0].length);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return fMatrix;
	}
		
	//主函数
	public static void main(String[] args) {
		
	}
	
}
