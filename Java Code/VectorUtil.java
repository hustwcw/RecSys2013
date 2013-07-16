package com.xyshen;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class VectorUtil {
	
//	private static final String Suffix = ".dat";
	
	//将Ｍatrix中第column提取出来
	public static double[] assignVector(double[][] matrix, int column) {
		int rowSize = matrix.length;      //
		double[] vector = new double[rowSize];
		
		for(int t = 0; t < rowSize; t++) {            //从０开始而不是１
			vector[t] = matrix[t][column];            //列向量
		}
		
		return vector;
	}
	
	//将Matrix中第row行提取出来
	public static double[] assignRowVector(double[][] matrix, int row) {
		int columnSize = matrix[0].length;        //
		double[] rowVector = new double[columnSize];
		
		for(int t = 0; t < columnSize; t++) {
			rowVector[t] = matrix[row][t];
		}
		
		return rowVector;
	}//...
	
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
	public static int[] nonZeroIndex(double[] vector) {
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
	public static double multipleVector(double[] vectorU,double[] vectorS) {
		double result = 0.0;
		int size = vectorU.length;
		for(int i = 0; i < size; i++) {
			result = result + vectorU[i] * vectorS[i];
		}
		
		return result;
	}
	
	//计算标题与向量乘积
	public static double[] multipleVector(double a, double[] vector) {
		
		for(int i = 0; i < vector.length; i++) {
			vector[i] = a * vector[i];
		}
		
		return vector;
	}
	
	//计算向量元素之和:double
	public static double sumVector(double[] vector) {
		double sum = 0.0;
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
	public static double[] sumVector(double[] vector1, double[] vector2) {
		int size = vector1.length;
		double[] vectorSum  = new double[size];
		
		for(int i = 0; i < size; i++) {
			vectorSum[i] = vector1[i] + vector2[i]; 
		}
		
		return vectorSum;
	}
	
	//计算两向量之差
	public static double[] distractVector(double[] vector1, double[] vector2) {
		int size = vector1.length;
		double[] vectorDiff = new double[size];
		
		for(int i = 0; i < size; i++) {
			vectorDiff[i] = vector1[i] - vector2[i];
		}
		
		return vectorDiff;
	}
	
	//初始化向量，全赋为0.0
	public static double[] initVector(int D) {
		double[] vector = new double[D];
		
		for(int i = 0; i < vector.length; i++) {
			vector[i] = 0.0;
		}
		
		return vector;
	}
	
	// matrix multiplication
	public static double[][] computeMatrixUS(double[][] MatrixU, double[][] MatrixS) {
		int rowSize = MatrixU.length;
		int colUSize = MatrixU[0].length;
		int colSSize = MatrixS[0].length;
		
		double[][] matrixUS = new double[colUSize][colSSize];
		for(int j = 0; j < colUSize; j++) {
			for(int k = 0; k < colSSize; k++) {
				
				double entry = 0;
				for(int d = 0; d < rowSize; d++) {
					entry = entry + MatrixU[d][j] * MatrixS[d][k];
				}
				matrixUS[j][k] = entry;
			}
		}//
		
		return matrixUS;
	}
	
	//计算两个矩阵的减法
	public static double[][] distractMatrix(double[][] matrix1, double[][] matrix2) {
		
		int rowSize = matrix1.length;
		int columnSize = matrix1[0].length;
		
		double[][] resultMatrix = new double[rowSize][columnSize];
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				resultMatrix[i][j] = matrix1[i][j] - matrix2[i][j];
			}
		}
		
		return resultMatrix;
		
	}

	//compute frobenius norm
	public static double computeFrobenius(double[][] matrix) {
		double fNorm = 0;
		int rowSize = matrix.length;
		int columnSize = matrix[0].length;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j<columnSize;j++) {
				fNorm = fNorm + matrix[i][j]*matrix[i][j];
			}
		}
		
		return fNorm;
	}
	
	//输出matrix以验证
	public static void outputMatrix(double[][] matrix) {
		
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
	public static int[] sortForTopK(double[] simVector, int topK) {
		int[] neighborVector = new int[topK];
		int size = simVector.length;
		
		double max;
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
	
	public static double[] filterTopK(double[] vector, int TopK) {
		
		int size = vector.length;
		double[] vectorTemp = new double[size];
		for(int i = 0; i < size; i++) {
			vectorTemp[i] = vector[i];
		}
		
		int count = 0; 
		double topKMax = 0.0;
		for(int i = 0; i < size-1; i++) {
			
			if(count >= TopK) {
				break;	
			}	
			
		    double max = vectorTemp[i];
		    int indexMax = i;
			for(int j = i+1; j < size; j++) {
				if(vectorTemp[j] > max) {
					max = vectorTemp[j];
					indexMax = j;
				}
			}//j...
			
			double temp = vectorTemp[indexMax];
			topKMax = temp;
			vectorTemp[indexMax] = vectorTemp[i];
			vectorTemp[i] = temp;
			count++;
			
		}//i...
		
		for(int i = 0; i < size; i++) {
			if(vector[i] < topKMax) {
				vector[i] = 0;
			}
		}
		
		return vector;
	}
	
	//输出matrix以验证
	public static void outputMatrix(double[][] matrix, int rowSize) {
		int columnSize = matrix[0].length;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
		
	}
	
	//输出matrix以验证
	public static void outputMatrix(double[][] matrix, String fileName) 
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
	public static void outputVector(double[] vector, String fileName) throws IOException {
		
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
	public static double computeMidRMSE(double[][] newRTMatrix, double[][] oldRTMatrix) {
		
		double midRMSE = 0.0;
		
		int rowSize = newRTMatrix.length;
		int columnSize = newRTMatrix[0].length;
		int totalSize = rowSize * columnSize;
		
		for(int i = 0; i < rowSize; i++) {
			for(int j = 0; j < columnSize; j++) {
				midRMSE = midRMSE + 
						(newRTMatrix[i][j] - oldRTMatrix[i][j]) * (newRTMatrix[i][j] - oldRTMatrix[i][j]); 
			}
		}
		
		midRMSE = Math.sqrt(midRMSE/totalSize);
		
		return midRMSE;
	}//computeMidRMSE(...)
	
	//计算最终的RMSE与MAE值
	public static double[] computeMAE_RMSE(double[][] cutMatrix, double[][] preMatrix) {
		
		double[] MAE_RMSE = new double[2]; //
		
		int rowSize = cutMatrix.length;
		int columnSize = cutMatrix[0].length;
		
		System.out.println(rowSize + " " + columnSize);
		
		double MAE = 0.0;
		double RMSE = 0.0;
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
		MAE_RMSE[1] = Math.sqrt(RMSE/N);
		
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
	
	//从文件中载入指定文件的内容到指定的矩阵,double[][]
	public static double[][] loadMatrix(String fileName, int rowSize, int columnSize) 
			throws IOException {
		double[][] matrix = new double[rowSize][columnSize];
		
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
					matrix[userNum][j] = Double.valueOf(splitResult[j]);
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
	
	//主函数
	public static void main(String[] args) {
		
	}
	
}
