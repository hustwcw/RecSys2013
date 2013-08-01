package collaborativeFilter;
import java.io.IOException;
import dataMatrix.MyMatrix;

public class C4IPCC {

	public int UserSize; 
	public int ServiceSize;
	public MyMatrix matrix;
	
	public static int TopK = 100;             //Top k
	public static final float Sparsity = (float)0.2;      //稀疏度
	public String prefixName;
	
	public float[][] RTMatrix;       //稀疏QoS矩阵
	public float[][] PreMatrix;   //待预测的值
	public float[][] IPCCMatrix;  //相似度矩阵
	public float[] AverageService;         //每个Service被调用的QoS平均值
	
	
	//载入rtmatrix.dat与rtmatrix0.xx.dat两个文件
	public void loadMatrix(String prefixName, MyMatrix matrix) throws IOException {
		this.matrix = matrix;
		this.prefixName = prefixName;
		
		UserSize = matrix.getUserLen();
		ServiceSize = matrix.getBusinessLen();
		RTMatrix = matrix.toMatrix();
		IPCCMatrix = new float[ServiceSize][ServiceSize];
		AverageService = new float[ServiceSize];
		
		System.out.println("Files load end~");
	}//loadMatrix()
	
	//计算IPCC矩阵
	public void computeIPCC() throws IOException {
		float sumF;
		int[] nonZeroNums = new int[ServiceSize];
		int[][] nonZeroPoss = new int[ServiceSize][];
		for(int i=0;i<ServiceSize;i++){
			sumF = VectorUtil.sumVector(RTMatrix[i]);
			nonZeroPoss[i] = VectorUtil.nonZeroPos(RTMatrix[i]);
			nonZeroNums[i] = VectorUtil.nonZeroNums(RTMatrix[i]);
			AverageService[i] = sumF/(float)(nonZeroNums[i]);
		}
		
		for(int i = 0; i < ServiceSize; i++) {
			
			float[] vectorI = VectorUtil.assignVector(RTMatrix, i); //RTMatrix中第i列
			int[] nonZeroIndexI = VectorUtil.nonZeroIndex(vectorI);  //调用过i的用户下标
			float sumVectorI = VectorUtil.sumVector(vectorI);
			int nonZeroNumI = VectorUtil.sumVector(nonZeroIndexI);   //调用过i的用户数
			
			float averageI = 0;
			if(nonZeroNumI != 0) {
				averageI = sumVectorI/nonZeroNumI;
			}
			 
			AverageService[i] = averageI;            //平均值
			
			for(int j = i+1; j < ServiceSize; j++) {   //对称阵
				
				float[] vectorJ = VectorUtil.assignVector(RTMatrix, j); //RTMatrix中的第j列
				int[] nonZeroIndexJ = VectorUtil.nonZeroIndex(vectorJ);
				int nonZeroNumJ = VectorUtil.sumVector(nonZeroIndexJ);  //调用过j的用户数
				
				float averageJ = 0;
				if(nonZeroNumJ != 0) {
					averageJ = VectorUtil.sumVector(vectorJ)/nonZeroNumJ;
				}
				
				int[] nonZeroIndexIJ = VectorUtil.nonZeroIndexBoth(nonZeroIndexI, nonZeroIndexJ);
				int nonZeroBothNum = VectorUtil.sumVector(nonZeroIndexIJ);  //i,j共同被nonZeroBothNum个用户调用过
				
				if(nonZeroBothNum == 0) {
					IPCCMatrix[i][j] = 0;;
				} else {
					
					float nominator = 0;
					float denominatorL = 0;
					float denominatorR = 0;
					for(int u = 0; u < UserSize; u++) {
						
						if(nonZeroIndexIJ[u] == 1) {                 //被用户u共同调用过的服务
							nominator = nominator + (RTMatrix[u][i] - averageI) * (RTMatrix[u][j] - averageJ);
							denominatorL = denominatorL + (RTMatrix[u][i] - averageI) * (RTMatrix[u][i] - averageI);
							denominatorR = denominatorR + (RTMatrix[u][j] - averageJ) * (RTMatrix[u][j] - averageJ);
						}
					}//for(u...)
					
					if(denominatorL == 0 || denominatorR == 0) {
						IPCCMatrix[i][j] = 0;
					} else {
						IPCCMatrix[i][j] = (float)(nominator/(Math.sqrt(denominatorL) * Math.sqrt(denominatorR))); //基本的UICC
					}			
					
					if(IPCCMatrix[i][j] < 0 ) {
						IPCCMatrix[i][j] = 0;
					} else {
						if(nonZeroNumI != 0 && nonZeroNumJ != 0) {
							IPCCMatrix[i][j] = (2 * nonZeroBothNum * IPCCMatrix[i][j])/(nonZeroNumI + nonZeroNumJ);//enhanced UICC
						} else {
							IPCCMatrix[i][j] = 0;
						}
						
					}
					
				}//if...else...
				
			}//for(j...)
			//System.out.println(i);
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
			IPCCMatrix[i][i] = 0;
		}
		
		System.out.println("IPCCMatrix Computation Done~");
   }

   //compute predicted matrix
	public void computePreMatrix() {
	   PreMatrix = new float[UserSize][ServiceSize];
		//计算待预测的矩阵PreMatrix[][]
		for(int u = 0; u < UserSize; u++) {
			
			for(int i = 0; i < ServiceSize; i++) {
				
				if(RTMatrix[u][i] == 0) {         //已经有值就用不着预测了
					
					float averageI = AverageService[i];
					float nominator = 0;
					float denominator = 0;
					
					float[] simVectorI = IPCCMatrix[i];
					float[] topKSimVector = VectorUtil.filterTopK(simVectorI, TopK);
					
					for(int k = 0; k < ServiceSize; k++) {
						if((RTMatrix[u][k] != 0) && (topKSimVector[k] != 0)){ //相似服务，且被同一个用户调用过
							float averageK = AverageService[k];
							
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
			
			//System.out.println(u);
		}//for(u...)
		
		System.out.println("PreMatrix Computation Done~");
		
	}//computeIPCC()
	
	
	//计算RMSE与MAE
//	public void computeRMSE_MAE() throws IOException {
//		
//		String fileName = FolderName + spiltMarker + PrefixIPCCSimFile + Sparsity + Suffix;
//		VectorUtil.outputMatrix(IPCCMatrix, fileName);      //输出IPCC的相似度矩阵
//		
//		fileName = FolderName + "/" + IPCCPreMatrixFile + Sparsity + Suffix;
//		VectorUtil.outputMatrix(PreMatrix, fileName);
//	}
	
	public float[][] getIPCCMatrix(){
		return IPCCMatrix;
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

