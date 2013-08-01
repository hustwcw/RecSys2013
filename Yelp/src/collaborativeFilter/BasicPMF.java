package collaborativeFilter;
import java.io.IOException;

import dataMatrix.MyMatrix;

public class BasicPMF {
		
		private int UserSize;
		private int ServiceSize;
		
		private static final float Lamda = (float)0.1; //lamda_u = lamda_v 
		private static final float Sparsity = (float)0.2; //matrix density
		private static final int D = 20;  //latent factor dim
		private static final float Lrate = (float)0.001;  //learning rate
		
		private static final int MaxIterNum = 30;  //下降次数
		private static final float iterThreshold = (float)0.005;
		
		private static final float IterNum = 5;
		
		private static final String PrefixRT = "rtmatrix";
		private static final String PrefixCut = "cutmatrix";
		private static String Suffix = ".dat";
		
		private static String SparseMatrixFile = PrefixRT + Sparsity + Suffix;
		private static String CutMatrixFile = PrefixCut + Sparsity + Suffix;
		
		private static String FolderName = "BasicPMF";
		private static String spiltMark = "/";
		private static String ResultFile = FolderName + spiltMark + "BasicPMF_" + Sparsity + "_" + Lamda + Suffix;
		
		private float[][] SparseRTMatrix;
//		private static float[][] CutRTMatrix = new float[UserSize][ServiceSize];
		
		private float[][] MatrixU;  //latent user feature matrix
		private float[][] MatrixS;  //latent item feature matrix
		
		private MyMatrix matrix;
		
		public void initMatrixUS() {
			MatrixU = new float[D][UserSize];
			MatrixS = new float[D][ServiceSize];
			//初始化
			for(int i = 0; i < D; i++) {
				for(int j = 0 ; j < UserSize; j++) {
					MatrixU[i][j] = (float)Math.sqrt((4*Math.random()+1)/D);
				}
			}
			
			//初始化
			for(int i = 0; i < D; i++) {
				for(int j = 0; j < ServiceSize; j++) {
					MatrixS[i][j] = (float)Math.sqrt((4*Math.random()+1)/D);
				}
			}
		}
		
		
		
		//
		public void loadMatrix(String prefixName) throws IOException {
			matrix = new MyMatrix();
			matrix.setFilePathName(prefixName);
			matrix.getMapFromFile();
			UserSize = matrix.getUserLen();
			ServiceSize = matrix.getBusinessLen();
			SparseRTMatrix = matrix.toMatrix();
			initMatrixUS();
			
			System.out.println("Files Load End~");
		}
		
		//
		public void computeBasicPMF() {
			float midRMSE = 10.0f;
			//float[][] oldRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS); //根据随机数生成的初始目标矩阵
			
			int count = 0; 
			while((midRMSE > iterThreshold) && (count < MaxIterNum)) {   //只要满足其中之一的条件就停止了，否则不停止
				
				//计算MatrixU
				for(int i = 0; i < UserSize; i++) {
					float[] vectorU = VectorUtil.assignVector(MatrixU, i);//第i列
					float[] term1 = VectorUtil.initVector(D);
					
					for(int j = 0; j < ServiceSize; j++) {
						
						if(SparseRTMatrix[i][j] != 0) {
							float[] vectorS = VectorUtil.assignVector(MatrixS, j);
							
							float term1Num = VectorUtil.multipleVector(vectorU, vectorS);
							term1Num = term1Num - SparseRTMatrix[i][j];
							float[] term1Vector = VectorUtil.multipleVector(term1Num, vectorS);
							
							term1 = VectorUtil.sumVector(term1, term1Vector);
						}
						
					}//for(j...)
					
					float[] term2 = VectorUtil.multipleVector(Lamda, vectorU);
				
					float[] DerivativeVector = VectorUtil.sumVector(term1, term2);
					
					//梯度下降
					for(int d = 0; d < D; d++) {
						MatrixU[d][i] = MatrixU[d][i] - Lrate * DerivativeVector[d];
					}
				}//for(i...)
				
				//计算MatrixS
				for(int j = 0; j < ServiceSize; j++) {
					
					float[] vectorS = VectorUtil.assignVector(MatrixS, j);
					float[] term1 = VectorUtil.initVector(D);
					
					//every user
					for(int m = 0; m < UserSize; m++) {
						
						if(SparseRTMatrix[m][j] != 0) {
							float[] vectorU = VectorUtil.assignVector(MatrixU, m);
							
							float term1Num = VectorUtil.multipleVector(vectorU, vectorS);
							term1Num = term1Num - SparseRTMatrix[m][j];
							
							float[] term1Vector = VectorUtil.multipleVector(term1Num, vectorU);
							term1 = VectorUtil.sumVector(term1, term1Vector);
						}//if(...)
						
					}//for(...)
					
					float[] term2 = VectorUtil.multipleVector(Lamda, vectorS);
					
					float[] DerivativeVector = VectorUtil.sumVector(term1, term2);
					
					for(int d = 0; d < D; d++) {
						MatrixS[d][j] = MatrixS[d][j] - Lrate * DerivativeVector[d];
					}
				}//for(j...)
				
				//Matrix
				float[][] newRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS);
				
				//midRMSE = VectorUtil.computeMidRMSE(newRTMatrix, oldRTMatrix);
				
				System.out.println("midRMSE : " + midRMSE + " ; count : " + count);
				
				//oldRTMatrix = newRTMatrix;
				count++;
				
			}//while(...)
			
		}//computeBasicPMF()
		
		
//		public void computRMSE_MAE() throws IOException {
//			
//			float[][] preRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS); //The final rating
//			
//			float[] MAE_RMSE = VectorUtil.computeMAE_RMSE(CutRTMatrix, preRTMatrix);
//			
//			float MAE = MAE_RMSE[0];
//			float RMSE = MAE_RMSE[1];
//			
//			System.out.println("MAE : "+ MAE);
//			System.out.println("RMSE : " + RMSE);
//			
//			String resultRecord = Sparsity + "  " 
//					+ Lrate + "  " + MaxIterNum + "  " + MAE + "  " + RMSE;
//			
//			VectorUtil.outputFinalRMSE_MAE(ResultFile, resultRecord);
//		}
//		
		//main function
		public static void main(String[] args) throws IOException {
			
			for(int i = 0; i < IterNum; i++) {
				BasicPMF basicPMF = new BasicPMF();
			
				//basicPMF.loadMatrix();
				basicPMF.computeBasicPMF();
			}
			
			System.out.println("Program ends~");
		}
	}

