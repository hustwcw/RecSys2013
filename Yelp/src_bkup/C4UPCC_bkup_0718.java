package collaborativeFilter;
import java.io.IOException;
import dataMatrix.MyMatrix;

public class C4UPCC {
	
	public int UserSize; 
	public int ServiceSize;
	//public MyMatrix matrix;
	
	public static int TopK = 100;             //Top k
	public String prefixName;
	
	public float[][] RTMatrix;       //稀疏QoS矩阵
	public float[][] PreMatrix;   //待预测的值
	public float[][] UPCCMatrix;  //相似度矩阵
	public float[] AverageUser;         //每个User调用的QoS的平均值
	
	//载入rtmatrix.dat与rtmatrix0.xx.dat两个文件
	public void loadMatrix(String prefixName, MyMatrix matrix) throws IOException {
		this.prefixName = prefixName;
		//this.matrix = matrix;
		
		UserSize = matrix.getUserLen();
		ServiceSize = matrix.getBusinessLen();
		RTMatrix = matrix.toMatrix();
		UPCCMatrix = new float[UserSize][UserSize];
		AverageUser = new float[UserSize];
		
		System.out.println("Files load end~");
	}
	
	//计算UPCC矩阵
	public void computeUPCC() throws IOException {
		float sumF;
		int[] nonZeroNums = new int[UserSize];
		int[][] nonZeroPoss = new int[UserSize][];
		for(int i=0;i<UserSize;i++){
			sumF = VectorUtil.sumVector(RTMatrix[i]);
			nonZeroPoss[i] = VectorUtil.nonZeroPos(RTMatrix[i]);
			nonZeroNums[i] = VectorUtil.nonZeroNums(RTMatrix[i]);
			AverageUser[i] = sumF/(float)(nonZeroNums[i]);
		}
		
		for(int a = 0; a < UserSize; a++) {
			
			int[] nonZeroPosA = nonZeroPoss[a];  //A调用过的服务的下标的数组
			//float sumVectorA = VectorUtil.sumVector(vectorA);       
			//int nonZeroNumA = VectorUtil.sumVector(nonZeroIndexA);//A调用过的服务个数
			int nonZeroNumA = nonZeroNums[a];
			float averageA = AverageUser[a];        //平均值
			//AverageUser[a] = averageA;

			for(int u = a + 1; u < UserSize; u++) {  //对称阵
				
				int[] nonZeroPosU = nonZeroPoss[u];  //U调用过的服务下标位置为１
				//int nonZeroNumU = VectorUtil.sumVector(nonZeroIndexU);
				int nonZeroNumU = nonZeroNums[u];
				
				float averageU = AverageUser[u];
				
				int[] nonZeroPosAU = VectorUtil.nonZeroPosBoth(nonZeroPosA, nonZeroPosU);//U/A共同调用的服务
				int nonZeroBothNum = nonZeroPosAU.length;
				
				if(nonZeroBothNum == 0) {        //未共同调用过任何服务
					UPCCMatrix[a][u] = 0;
				} else {
					
					float nominator = 0;
					float denominatorL = 0;
					float denominatorR = 0;
					for(int i = 0; i < nonZeroBothNum; i++){ //共同调用的服务
						int pos = nonZeroPosAU[i];
						nominator = nominator + (RTMatrix[a][pos] - averageA) * (RTMatrix[u][pos] - averageU);
					}//for(i...)
					if(nominator <= 0) UPCCMatrix[a][u] = 0;
					else{
						for(int i = 0; i < nonZeroBothNum; i++){ //共同调用的服务
							int pos = nonZeroPosAU[i];
							denominatorL = denominatorL + (RTMatrix[a][pos] - averageA) * (RTMatrix[a][pos] - averageA);
							denominatorR = denominatorR + (RTMatrix[u][pos] - averageU) * (RTMatrix[u][pos] - averageU); 
							
						}//for(i...)
						
						if(denominatorL == 0 || denominatorR == 0) {
							UPCCMatrix[a][u] = 0;
						} else {
							UPCCMatrix[a][u] = (float)(nominator/(Math.sqrt(denominatorL) * Math.sqrt(denominatorR)));  //基本的ＵＰＣＣ
						}
						
						if(nonZeroNumA == 0 || nonZeroNumU == 0) {
							UPCCMatrix[a][u] = 0;
						} else {
							UPCCMatrix[a][u] = 2 * nonZeroBothNum *UPCCMatrix[a][u]/(nonZeroNumA + nonZeroNumU);//enhanced UPCC
						}
					}
					
				}//if...else...
				
			}//for(u....)
				
		}//for(a...)
		
		//对称阵，赋值
		for(int i = 0; i < UserSize; i++) {
			for(int j = 0; j < i; j++) {
				if(UPCCMatrix[i][j] == 0) {    //肯定为零，仅为程序
					UPCCMatrix[i][j] = UPCCMatrix[j][i];
				}
			}//for(j...)
		}//for(i...)
		
		//user-self
		for(int i = 0; i < UserSize; i++) {
			UPCCMatrix[i][i] = 0;
		}
		
		System.out.println("upcc computes end~");
    }
	
	//
	public void computePreMatrix() {
		
    	PreMatrix = new float[UserSize][ServiceSize];
		//计算待预测的矩阵PreMatrix[][]
		for(int u = 0; u < UserSize; u++) {
			if(u%10 == 0) System.out.println("upcc pre matrix line "+u+" start");
			float averageU = AverageUser[u]; //U调用服务QoS的平均值
			   
			float[] simVectorU = UPCCMatrix[u];
			float[] topKSimVector = VectorUtil.filterTopK(simVectorU, TopK);
			
			for(int i = 0; i < ServiceSize; i++) {
				
			   if(RTMatrix[u][i] == 0) {   //不等于０，即已有的值就没必要再预测了
				   
				   float nominator = 0;
				   float denominator = 0;
				   
				   for(int a = 0; a < UserSize; a++) {  //查找邻居
					   
					   if((RTMatrix[a][i] != 0) && (topKSimVector[a] != 0)) { //相似用户，且调用过i服务
						   float averageA = AverageUser[a];
						   
						   nominator = nominator + UPCCMatrix[u][a] * (RTMatrix[a][i] - averageA);
						   denominator = denominator + UPCCMatrix[u][a];
						   
					   }//if(...)
				   }//for(a...)
				   
				   if(denominator != 0) {
					   PreMatrix[u][i] = averageU + nominator/denominator; 
				   } else {
					   PreMatrix[u][i] = averageU;
				   }
				   
			   }//if(...)
			
			}//for(i...)
		}//for(u...)
 		
	}//computeUPCC()
	
	//计算RMSE与MAE
//	private void computeRMSE_MAE() throws IOException {
//		
//		String fileName = FolderName + spiltMarker + PrefixUPCCSimFile + Sparsity + Suffix;
//		VectorUtil.outputMatrix(UPCCMatrix, fileName);      //输出UPCC的相似度矩阵
//		
//		fileName = FolderName + spiltMarker + UPCCPreMatrixFile + Sparsity + Suffix;
//		VectorUtil.outputMatrix(PreMatrix, fileName);   //输出UPCC计算出来的PreMatrix
//		
//	}//computeRMSE_MAE()
	
	public float[][] getUPCCMatrix(){
		return UPCCMatrix;
	}
	public float[][] getPreMatrix(){
		return PreMatrix;
	}
	
	public int getUserSize(){
		return UserSize;
	}
	public int getServiceSize(){
		return ServiceSize;
	}
	
}
