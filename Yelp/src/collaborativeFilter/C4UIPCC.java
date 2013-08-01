package collaborativeFilter;
import java.io.IOException;

import dataMatrix.MyMatrix;

public class C4UIPCC {

		private int UserSize;
		private int ServiceSize;
		private MyMatrix matrix;
		private String filePrefixName;
		
		private static final float Lambda = (float)0.5;//
		//private static final float Sparsity = 05;//稀疏度
		private static final int TopK = 10;   //
		
		private float[][] UPCCSimMatrix;
		private float[][] IPCCSimMatrix;
		
		private float[][] UPCCPreMatrix;
		private float[][] IPCCPreMatrix;
		
		private static float[] CONU;
		private static float[] CONI;
		
		//private static float[][] RTMatrix;      //稀疏化后的QoS
		private static float[][] PreMatrix;
		
		
		//载入UPCC与IPCC已计算好的矩阵
		private void loadMatrix(String prefixName) throws IOException {
			filePrefixName = prefixName;
			matrix = new MyMatrix();
			matrix.setFilePathName(prefixName);
			matrix.getMapFromFile();
			UserSize = matrix.getUserLen();
			ServiceSize = matrix.getBusinessLen();
			
			C4UPCC upcc;
			C4IPCC ipcc;
			
			upcc = new C4UPCC();
			upcc.loadMatrix(prefixName, matrix);
			upcc.computeUPCC();
			upcc.computePreMatrix();
			UPCCSimMatrix = upcc.UPCCMatrix;
			UPCCPreMatrix = upcc.PreMatrix;
			upcc = null;
			System.gc();
			
			ipcc = new C4IPCC();
			ipcc.loadMatrix(prefixName, matrix);
			ipcc.computeIPCC();
			ipcc.computePreMatrix();
			IPCCSimMatrix = ipcc.IPCCMatrix;
			IPCCPreMatrix = ipcc.PreMatrix;
			ipcc = null;
			System.gc();
			
			CONU = new float[UserSize];
			CONI = new float[ServiceSize];
			
			PreMatrix = new float[UserSize][ServiceSize];
			
			System.out.println("Files load end~");
		}//loadMatrix()
	 	
		//计算CONU与CONI
		private void computeConUI() {
			
			//计算CONU--->UPCCSimFile--->UPCCMatrix
			for(int u = 0; u < UserSize; u++) {
				
				float denominator = 0;
				//计算denominator
				float[] simVector = UPCCSimMatrix[u];
				float[] topKSimVector = VectorUtil.filterTopK(simVector, TopK);
				
				for(int a = 0; a < UserSize; a++) {
					if(topKSimVector[a] != 0 ) {
						denominator = denominator + UPCCSimMatrix[u][a];
					}
				}//for(a...)
				
				//计算CONU
				CONU[u] = 0;
				
				for(int a = 0; a < UserSize; a++) {    //denominator不可能为０
					if(topKSimVector[a] != 0) {     //先不管Top_k
						CONU[u] = CONU[u] + (UPCCSimMatrix[u][a] * UPCCSimMatrix[u][a])/denominator;
				    }
				}//for(a...)
				
			}//for(i...)
			
			//计算CONI
			for(int i = 0; i < ServiceSize; i++) {
				
				float denominator = 0;
				float[] simVector = IPCCSimMatrix[i];
				float[] topKSimVector = VectorUtil.filterTopK(simVector, TopK);
				
				//计算denominator
				for(int k = 0; k < ServiceSize; k++) {
					if(topKSimVector[k] != 0) {
						denominator = denominator + IPCCSimMatrix[i][k];
					}
				}//for(k...)
				
				//计算CONI
				CONI[i] = 0;
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
					if(Float.isNaN(CONU[u]) || Float.isNaN(CONI[i])){
						System.err.println("conu coni NaN!");
					}
					float wu = CONU[u] * Lambda/(CONU[u] * Lambda + CONI[i] * (1 - Lambda));
					float wi = CONI[i] * (1 - Lambda)/(CONU[u] * Lambda + CONI[i] * (1 - Lambda));
					if(Float.isNaN(wi) || Float.isNaN(wu)){
						//System.err.println("wu wi NaN!");
						wu = Lambda;
						wi = 1-Lambda;
					}
					PreMatrix[u][i] = wu * UPCCPreMatrix[u][i] + wi * IPCCPreMatrix[u][i];
					if(PreMatrix[u][i] < 1) PreMatrix[u][i] = 1;
					else if(PreMatrix[u][i] > 5) PreMatrix[u][i] = 5;
				}//for(i...)
			}//for(u...)
		}//computePreMatrix[][]
		
		//计算RMSE与MAE
//		private void computeRMSE_MAE() throws IOException{
//			
//			float[] MAE_RMSE = VectorUtil.computeMAE_RMSE(CutMatrix, PreMatrix);
//			
//			String outputContent = Sparsity + " " + Lambda + " " + TopK + " " + MAE_RMSE[0] + " " + MAE_RMSE[1];
//			VectorUtil.outputFileRMSE_MAE(ResultFileName, outputContent);
//			
//			System.out.print("MAE : " + MAE_RMSE[0]);
//			System.out.println(" ;RMSE : " + MAE_RMSE[1]);
//			
//		}//computeRMSE_MAE()
		
		public void storeToMyMatrix(MyFloatMatrix myFloatMatrix){
			int i,j;
			String uID,bID;
			for(i=0;i<UserSize;i++){
				for(j=0;j<ServiceSize;j++){
					if(PreMatrix[i][j] != 0){
						uID = matrix.getUserID(i);
						bID = matrix.getBusinessID(j);
						myFloatMatrix.putById(uID, bID, PreMatrix[i][j]);
					}
				}
			}
		}
		
		public void loadMatrixFromFile(String setName){
			filePrefixName = setName;
			matrix = new MyMatrix();
			matrix.setFilePathName(setName);
			matrix.getMapFromFile();
			
			UPCCSimMatrix = VectorUtil.loadMatrixFloat("UPCC/"+setName, "UPCCMatrix");
			UPCCPreMatrix = VectorUtil.loadMatrixFloat("UPCC/"+setName, "PreMatrix");
			IPCCSimMatrix = VectorUtil.loadMatrixFloat("IPCC/"+setName, "IPCCMatrix");
			IPCCPreMatrix = VectorUtil.loadMatrixFloat("IPCC/"+setName, "PreMatrix");
			UserSize = UPCCPreMatrix.length;
			ServiceSize = UPCCPreMatrix[0].length;
			
			CONU = new float[UserSize];
			CONI = new float[ServiceSize];
			
			PreMatrix = new float[UserSize][ServiceSize];
			
			System.out.println("Files load end~");
		}
		private void storeMatrix(){
			VectorUtil.storeMatrixFloat(PreMatrix, "UIPCC/"+filePrefixName, "PreMatrix");
		}

		//主函数
		public static void main(String[] args) throws IOException {
			C4UIPCC c4UIPCC;
			int i,k=5;
//			
//			C4IPCC ipcc;
//			for(i=1;i<k;i++){
//				ipcc = new C4IPCC();
//				ipcc.loadMatrix("Sets/"+i);
//				ipcc.computeIPCC();
//				ipcc.computePreMatrix();
//				ipcc.storeMatrix();
//				ipcc = null;
//				System.gc();
//			}
			
//			C4UPCC upcc;
//			for(i=1;i<k;i++){
//				upcc = new C4UPCC();
//				upcc.loadMatrix("Sets/"+i);
//				upcc.computeUPCC();
//				upcc.computePreMatrix();
//				upcc.storeMatrix();
//				upcc = null;
//				
//				System.gc();
//			}
			
			for(i=4;i<5;i++){
				c4UIPCC = new C4UIPCC();
				c4UIPCC.loadMatrixFromFile("Sets/"+i);
				c4UIPCC.computeConUI();
				System.out.println("CONUI Computation Done~");
				
				c4UIPCC.computePreMatrix();
				System.out.println("PreMatrix Computation Done~");
				
				c4UIPCC.storeMatrix();
				System.out.println("Store PreMatrix Done~");
				
				c4UIPCC = null;
				System.gc();
			}

			
		}
	}

