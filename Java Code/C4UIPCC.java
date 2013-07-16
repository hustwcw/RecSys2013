package com.xyshen;
import java.io.IOException;

public class C4UIPCC {

		private static final int UserSize = 339;
		private static final int ServiceSize = 5825;
		
		private static final double Lambda = 0.3;//
		private static final double Sparsity = 0.05;//稀疏度
		private static final int TopK = 10;   //
		
		private static final String UPCCSimFile = "c4upccSimFile";//UPCCMatrix[][]
		private static final String IPCCSimFile = "c4ipccSimFile";//IPCCMatrix[][]
		
		private static double[][] UPCCSimMatrix = new double[UserSize][UserSize];
		private static double[][] IPCCSimMatrix = new double[ServiceSize][ServiceSize];
		
		private static final String UPCCPreMatrixFile = "c4upccPreMatrixFile"; //PreMatrix[][]
		private static final String IPCCPreMatrixFile = "c4ipccPreMatrixFile"; //PreMatrix[][]
		
		private static double[][] UPCCPreMatrix = new double[UserSize][ServiceSize];
		private static double[][] IPCCPreMatrix = new double[UserSize][ServiceSize];
		
		private static final String PrefixRTMatrix = "rtmatrix";
		private static final String PrefixCutMatrix = "cutmatrix";
		
		private static final String Suffix = ".dat";
		private static final String FolderName = "C4UIPCC";
		private static final String IPCCFolder = "C4IPCC";
		private static final String UPCCFolder = "C4UPCC";
		private static String ResultFileName = FolderName + "/" + "UIPCCResult.dat";
		
		private static double[] CONU = new double[UserSize];
		private static double[] CONI = new double[ServiceSize];
		
		private static double[][] RTMatrix = new double[UserSize][ServiceSize];      //稀疏化后的QoS
		private static double[][] CutMatrix = new double[UserSize][ServiceSize];   //原始的QoS矩阵 - rtmatrix
		private static double[][] PreMatrix = new double[UserSize][ServiceSize];
		
		//
		C4UIPCC() {
			
			//初始化
			for(int i = 0; i < UserSize; i++) {
				for(int j = 0; j < ServiceSize; j++) {
					RTMatrix[i][j] = 0.0;
					PreMatrix[i][j] = 0.0;
				}
			}
		}//
		
		//载入UPCC与IPCC已计算好的矩阵
		private void loadMatrix() throws IOException {
			
			String fileName = PrefixRTMatrix + Sparsity + Suffix;
			RTMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);   //载入经稀疏过的矩阵
			
			fileName = PrefixCutMatrix + Sparsity + Suffix;
			CutMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
			fileName = UPCCFolder + "/" + UPCCSimFile + Sparsity + Suffix;
			UPCCSimMatrix = VectorUtil.loadMatrix(fileName, UserSize, UserSize); //载入UPCC的Sim矩阵
			
			fileName = IPCCFolder + "/" + IPCCSimFile + Sparsity + Suffix;
			IPCCSimMatrix = VectorUtil.loadMatrix(fileName, ServiceSize, ServiceSize); //载入IPCC的Sim矩阵
			
			fileName = UPCCFolder + "/" + UPCCPreMatrixFile + Sparsity + Suffix; 
			UPCCPreMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize); //载入
			
			fileName = IPCCFolder + "/" + IPCCPreMatrixFile + Sparsity + Suffix;
			IPCCPreMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
			System.out.println("Files load end~");
		}//loadMatrix()
	 	
		//计算CONU与CONI
		private void computeConUI() {
			
			//计算CONU--->UPCCSimFile--->UPCCMatrix
			for(int u = 0; u < UserSize; u++) {
				
				double denominator = 0.0;
				//计算denominator
				double[] simVector = UPCCSimMatrix[u];
				double[] topKSimVector = VectorUtil.filterTopK(simVector, TopK);
				
				for(int a = 0; a < UserSize; a++) {
					if(topKSimVector[a] != 0 ) {
						denominator = denominator + UPCCSimMatrix[u][a];
					}
				}//for(a...)
				
				//计算CONU
				CONU[u] = 0.0;
				
				for(int a = 0; a < UserSize; a++) {    //denominator不可能为０
					if(topKSimVector[a] != 0) {     //先不管Top_k
						CONU[u] = CONU[u] + (UPCCSimMatrix[u][a] * UPCCSimMatrix[u][a])/denominator;
				    }
				}//for(a...)
				
			}//for(i...)
			
			//计算CONI
			for(int i = 0; i < ServiceSize; i++) {
				
				double denominator = 0.0;
				double[] simVector = IPCCSimMatrix[i];
				double[] topKSimVector = VectorUtil.filterTopK(simVector, TopK);
				
				//计算denominator
				for(int k = 0; k < ServiceSize; k++) {
					if(topKSimVector[k] != 0) {
						denominator = denominator + IPCCSimMatrix[i][k];
					}
				}//for(k...)
				
				//计算CONI
				CONI[i] = 0.0;
				for(int k = 0; k < ServiceSize; k++) {
					if(topKSimVector[k] !=0 ) {
						CONI[i] = CONI[i] + (IPCCSimMatrix[i][k] * IPCCSimMatrix[i][k])/denominator;
					}
				}//for(k...)
				
			}//for(i...)
		}//computeConUI()
		
		//计算PreMatrix[][]
		private void computePreMatrix() {
			
			for(int u = 0; u < UserSize; u++) {
				for(int i = 0; i < ServiceSize; i++) {
					double wu = CONU[u] * Lambda/(CONU[u] * Lambda + CONI[i] * (1 - Lambda));
					double wi = CONI[i] * (1 - Lambda)/(CONU[u] * Lambda + CONI[i] * (1 - Lambda));
					
					PreMatrix[u][i] = wu * UPCCPreMatrix[u][i] + wi * IPCCPreMatrix[u][i];
				}//for(i...)
			}//for(u...)
		}//computePreMatrix[][]
		
		//计算RMSE与MAE
		private void computeRMSE_MAE() throws IOException{
			
			double[] MAE_RMSE = VectorUtil.computeMAE_RMSE(CutMatrix, PreMatrix);
			
			String outputContent = Sparsity + " " + Lambda + " " + TopK + " " + MAE_RMSE[0] + " " + MAE_RMSE[1];
			VectorUtil.outputFileRMSE_MAE(ResultFileName, outputContent);
			
			System.out.print("MAE : " + MAE_RMSE[0]);
			System.out.println(" ;RMSE : " + MAE_RMSE[1]);
			
		}//computeRMSE_MAE()
		
		//主函数
		public static void main(String[] args) throws IOException {
			C4UIPCC c4UIPCC = new C4UIPCC();
			
			c4UIPCC.loadMatrix();
			
			c4UIPCC.computeConUI();
			
			System.out.println("CONUI Computation Done~");
			
			c4UIPCC.computePreMatrix();
			
			System.out.println("PreMatrix Computation Done~");
			
			c4UIPCC.computeRMSE_MAE();
			
			System.out.println("UIPCCComputation Done~");
		}
	}

