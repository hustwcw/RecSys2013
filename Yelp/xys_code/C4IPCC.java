package com.xyshen;
import java.io.IOException;

public class C4IPCC {

		private static final int UserSize = 339; 
		private static final int ServiceSize = 5825;
		
		private static final int TopK = 15;             //Top k
		private static final double Sparsity = 0.2;      //稀疏度
		
		private static final String PrefixRTMatrix = "rtmatrix";
		private static final String PrefixCutMatrix = "cutmatrix";
		private static final String Suffix = ".dat";

		private static final String ResultFileName = "C4IPCCResult.dat";
		
		private static final String FolderName = "C4IPCC";
		private static final String spiltMarker = "//";
		
		private static final String PrefixIPCCSimFile = "c4ipccSimFile";//IPCCMatrix[][]
		private static final String IPCCPreMatrixFile = "c4ipccPreMatrixFile"; //PreMatrix[][]
		
		private static double[][] RTMatrix = new double[UserSize][ServiceSize];       //稀疏QoS矩阵
		private static double[][] CutMatrix= new double[UserSize][ServiceSize];
		private static double[][] PreMatrix = new double[UserSize][ServiceSize];   //待预测的值
		
		private static double[][] IPCCMatrix = new double[ServiceSize][ServiceSize];  //相似度矩阵
		
		private static double[] AverageService = new double[ServiceSize];         //每个Service被调用的QoS平均值
		
		//
		C4IPCC() {
			//初始化UPCCMatrix
			for(int i = 0; i < UserSize; i++) {
				for(int j = 0; j < ServiceSize; j++) {
					IPCCMatrix[i][j] = 0.0;
					PreMatrix[i][j] = 0.0;
				}//
			}//
		}
		
		//载入rtmatrix.dat与rtmatrix0.xx.dat两个文件
		private void loadMatrix() throws IOException {
			
			String fileName = PrefixRTMatrix + Sparsity + Suffix;
			RTMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
			fileName = PrefixCutMatrix + Sparsity + Suffix;
			CutMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
//			fileName = FolderName + spiltMarker + PrefixIPCCSimFile + Sparsity + Suffix;
//			IPCCMatrix = VectorUtil.loadMatrix(fileName, ServiceSize, ServiceSize);
			System.out.println("Files load end~");
		}//loadMatrix()
		
		//计算IPCC矩阵
		private void computeIPCC() throws IOException {
			
			for(int i = 0; i < ServiceSize; i++) {
				
				double[] vectorI = VectorUtil.assignVector(RTMatrix, i); //RTMatrix中第i列
				int[] nonZeroIndexI = VectorUtil.nonZeroIndex(vectorI);  //调用过i的用户下标
				double sumVectorI = VectorUtil.sumVector(vectorI);
				int nonZeroNumI = VectorUtil.sumVector(nonZeroIndexI);   //调用过i的用户数
				
				double averageI = 0.0;
				if(nonZeroNumI != 0) {
					averageI = sumVectorI/nonZeroNumI;
				}
				 
				AverageService[i] = averageI;            //平均值
				
				for(int j = i+1; j < ServiceSize; j++) {   //对称阵
					
					double[] vectorJ = VectorUtil.assignVector(RTMatrix, j); //RTMatrix中的第j列
					int[] nonZeroIndexJ = VectorUtil.nonZeroIndex(vectorJ);
					int nonZeroNumJ = VectorUtil.sumVector(nonZeroIndexJ);  //调用过j的用户数
					
					double averageJ = 0.0;
					if(nonZeroNumJ != 0) {
						averageJ = VectorUtil.sumVector(vectorJ)/nonZeroNumJ;
					}
					
					int[] nonZeroIndexIJ = VectorUtil.nonZeroIndexBoth(nonZeroIndexI, nonZeroIndexJ);
					int nonZeroBothNum = VectorUtil.sumVector(nonZeroIndexIJ);  //i,j共同被nonZeroBothNum个用户调用过
					
					if(nonZeroBothNum == 0) {
						IPCCMatrix[i][j] = 0.0;;
					} else {
						
						double nominator = 0.0;
						double denominatorL = 0.0;
						double denominatorR = 0.0;
						for(int u = 0; u < UserSize; u++) {
							
							if(nonZeroIndexIJ[u] == 1) {                 //被用户u共同调用过的服务
								nominator = nominator + (RTMatrix[u][i] - averageI) * (RTMatrix[u][j] - averageJ);
								denominatorL = denominatorL + (RTMatrix[u][i] - averageI) * (RTMatrix[u][i] - averageI);
								denominatorR = denominatorR + (RTMatrix[u][j] - averageJ) * (RTMatrix[u][j] - averageJ);
							}
						}//for(u...)
						
						if(denominatorL == 0 || denominatorR == 0) {
							IPCCMatrix[i][j] = 0.0;
						} else {
							IPCCMatrix[i][j] = nominator/(Math.sqrt(denominatorL) * Math.sqrt(denominatorR)); //基本的UICC
						}			
						
						if(IPCCMatrix[i][j] < 0 ) {
							IPCCMatrix[i][j] = 0.0;
						} else {
							if(nonZeroNumI != 0 && nonZeroNumJ != 0) {
								IPCCMatrix[i][j] = (2 * nonZeroBothNum * IPCCMatrix[i][j])/(nonZeroNumI + nonZeroNumJ);//enhanced UICC
							} else {
								IPCCMatrix[i][j] = 0.0;
							}
							
						}
						
					}//if...else...
					
				}//for(j...)
				System.out.println(i);
			}//for(i...)

			//对称阵，赋值
			for(int i = 0; i < ServiceSize; i++) {
				for(int j = 0; j < i; j++) {
					if(IPCCMatrix[i][j] == 0) {   //必定为0
						IPCCMatrix[i][j] = IPCCMatrix[j][i];
					}
				}//for(j...)
			}//for(i...)
			
			//service-self
			for(int i = 0; i < ServiceSize; i++) {
				IPCCMatrix[i][i] = 0.0;
			}
			
			System.out.println("IPCCMatrix Computation Done~");
	   }
	
	   //compute predicted matrix
	   private void computePreMatrix() {
		   
			//计算待预测的矩阵PreMatrix[][]
			for(int u = 0; u < UserSize; u++) {
				
				for(int i = 0; i < ServiceSize; i++) {
					
					if(RTMatrix[u][i] == 0) {         //已经有值就用不着预测了
						
						double averageI = AverageService[i];
						double nominator = 0.0;
						double denominator = 0.0;
						
						double[] simVectorI = IPCCMatrix[i];
						double[] topKSimVector = VectorUtil.filterTopK(simVectorI, TopK);
						
						for(int k = 0; k < ServiceSize; k++) {
							if((RTMatrix[u][k] != 0) && (topKSimVector[k] != 0)){ //相似服务，且被同一个用户调用过
								double averageK = AverageService[k];
								
								nominator = nominator + IPCCMatrix[i][k] * (RTMatrix[u][k] - averageK);
								denominator = denominator + IPCCMatrix[i][k];
								
							}//if()
								
						}//for(k...)
						
						if(denominator != 0) {
							PreMatrix[u][i] = averageI + nominator/denominator;
						} else {
							PreMatrix[u][i] = averageI;
						}//
						
					}//if(...)
					
				}//for(i...)
				
				System.out.println(u);
			}//for(u...)
			
			System.out.println("PreMatrix Computation Done~");
			
		}//computeIPCC()
		
		
		//计算RMSE与MAE
		public void computeRMSE_MAE() throws IOException {
			
			double[] RMSE_MAE = VectorUtil.computeMAE_RMSE(CutMatrix, PreMatrix);
			
			String outputContent = Sparsity + " " + TopK + " " + RMSE_MAE[0] + " " + RMSE_MAE[1];
			
			String fileName = FolderName + spiltMarker + ResultFileName;
			VectorUtil.outputFileRMSE_MAE(fileName, outputContent);
			
			System.out.print("MAE : " + RMSE_MAE[0]);
			System.out.println(" ;RMSE : " + RMSE_MAE[1]);
			
			fileName = FolderName + spiltMarker + PrefixIPCCSimFile + Sparsity + Suffix;
			VectorUtil.outputMatrix(IPCCMatrix, fileName);      //输出IPCC的相似度矩阵
			
			fileName = FolderName + "/" + IPCCPreMatrixFile + Sparsity + Suffix;
			VectorUtil.outputMatrix(PreMatrix, fileName);
		}
		
		//main function
		public static void main(String[] args) throws IOException {
			
			C4IPCC c4IPCC = new C4IPCC();
			
			c4IPCC.loadMatrix();
			
			c4IPCC.computeIPCC();
			
			c4IPCC.computePreMatrix();
			
			c4IPCC.computeRMSE_MAE();
			
			System.out.println("IPCC Computation Done~. Program End~");
		}
		
	}

