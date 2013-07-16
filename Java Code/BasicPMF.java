package com.xyshen;
import java.io.IOException;

public class BasicPMF {
		
		private static final int UserSize = 339;
		private static final int ServiceSize = 5825;
		
		private static final double Lamda = 0.001; //lamda_u = lamda_v 
		private static final double Sparsity = 0.05; //matrix density
		private static final int D = 10;  //latent factor dim
		private static final double Lrate = 0.0045;  //learning rate
		
		private static final int MaxIterNum = 24;  //下降次数
		private static final double iterThreshold = 0.005;
		
		private static final double IterNum = 1;
		
		private static final String PrefixRT = "rtmatrix";
		private static final String PrefixCut = "cutmatrix";
		private static String Suffix = ".dat";
		
		private static String SparseRTFile = PrefixRT + Sparsity + Suffix;
		private static String CutMatrixFile = PrefixCut + Sparsity + Suffix;
		
		private static String FolderName = "BasicPMF";
		private static String spiltMark = "\\";
		private static String ResultFile = FolderName + spiltMark + "BasicPMF_" + Sparsity + Suffix;
		
		private static double[][] SparseRTMatrix = new double[UserSize][ServiceSize];
		
		private static double[][] CutRTMatrix = new double[UserSize][ServiceSize];
		
		private static double[][] MatrixU = new double[D][UserSize];  //latent user feature matrix
		private static double[][] MatrixS = new double[D][ServiceSize];  //latent item feature matrix
		
		BasicPMF() {
			//初始化
			for(int i = 0; i < D; i++) {
				for(int j = 0 ; j < UserSize; j++) {
					MatrixU[i][j] = Math.random();
				}
			}
			
			//初始化
			for(int i = 0; i < D; i++) {
				for(int j = 0; j < ServiceSize; j++) {
					MatrixS[i][j] = Math.random();
				}
			}
		}
		
		//
		private void loadMatrix() throws IOException {
			
			SparseRTMatrix = VectorUtil.loadMatrix(SparseRTFile, UserSize, ServiceSize);
			CutRTMatrix = VectorUtil.loadMatrix(CutMatrixFile, UserSize, ServiceSize);
			
			System.out.println("Files Load End~");
		}
		
		//
		private void computeBasicPMF() {
			double midRMSE = 10.0f;
			double[][] oldRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS); //根据随机数生成的初始目标矩阵
			
			int count = 0; 
			while((midRMSE > iterThreshold) && (count < MaxIterNum)) {   //只要满足其中之一的条件就停止了，否则不停止
				
				//计算MatrixU
				for(int i = 0; i < UserSize; i++) {
					double[] vectorU = VectorUtil.assignVector(MatrixU, i);//第i列
					double[] term1 = VectorUtil.initVector(D);
					
					for(int j = 0; j < ServiceSize; j++) {
						
						if(SparseRTMatrix[i][j] != 0) {
							double[] vectorS = VectorUtil.assignVector(MatrixS, j);
							
							double term1Num = VectorUtil.multipleVector(vectorU, vectorS);
							term1Num = term1Num - SparseRTMatrix[i][j];
							double[] term1Vector = VectorUtil.multipleVector(term1Num, vectorS);
							
							term1 = VectorUtil.sumVector(term1, term1Vector);
						}
						
					}//for(j...)
					
					double[] term2 = VectorUtil.multipleVector(Lamda, vectorU);
				
					double[] DerivativeVector = VectorUtil.initVector(D);
					DerivativeVector = VectorUtil.sumVector(term1, term2);
					
					//梯度下降
					for(int d = 0; d < D; d++) {
						MatrixU[d][i] = MatrixU[d][i] - Lrate * DerivativeVector[d];
					}
				}//for(i...)
				
				//计算MatrixS
				for(int j = 0; j < ServiceSize; j++) {
					
					double[] vectorS = VectorUtil.assignVector(MatrixS, j);
					double[] term1 = VectorUtil.initVector(D);
					
					//every user
					for(int m = 0; m < UserSize; m++) {
						
						if(SparseRTMatrix[m][j] != 0) {
							double[] vectorU = VectorUtil.assignVector(MatrixU, m);
							
							double term1Num = VectorUtil.multipleVector(vectorU, vectorS);
							term1Num = term1Num - SparseRTMatrix[m][j];
							
							double[] term1Vector = VectorUtil.multipleVector(term1Num, vectorU);
							term1 = VectorUtil.sumVector(term1, term1Vector);
						}//if(...)
						
					}//for(...)
					
					double[] term2 = VectorUtil.multipleVector(Lamda, vectorS);
					
					double[] DerivativeVector = VectorUtil.initVector(D);
					DerivativeVector = VectorUtil.sumVector(term1, term2);
					
					for(int d = 0; d < D; d++) {
						MatrixS[d][j] = MatrixS[d][j] - Lrate * DerivativeVector[d];
					}
				}//for(j...)
				
				//Matrix
				double[][] newRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS);
				
				midRMSE = VectorUtil.computeMidRMSE(newRTMatrix, oldRTMatrix);
				
				System.out.println("midRMSE : " + midRMSE + " ; count : " + count);
				
				oldRTMatrix = newRTMatrix;
				count++;
				
			}//while(...)
			
		}//computeBasicPMF()
		
		private void computRMSE_MAE() throws IOException {
			
			double[][] preRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS); //The final rating
			
			double[] MAE_RMSE = VectorUtil.computeMAE_RMSE(CutRTMatrix, preRTMatrix);
			
			double MAE = MAE_RMSE[0];
			double RMSE = MAE_RMSE[1];
			
			System.out.println("MAE : "+ MAE);
			System.out.println("RMSE : " + RMSE);
			
			String resultRecord = Sparsity + "  " 
					+ Lrate + "  " + MaxIterNum + "  " + MAE + "  " + RMSE;
			
			VectorUtil.outputFinalRMSE_MAE(ResultFile, resultRecord);
		}
		
		//main function
		public static void main(String[] args) throws IOException {
			
			for(int i = 0; i < IterNum; i++) {
				BasicPMF basicPMF = new BasicPMF();
			
				basicPMF.loadMatrix();
				basicPMF.computeBasicPMF();
				basicPMF.computRMSE_MAE();
			}
			
			System.out.println("Program ends~");
		}
	}

