package com.xyshen;
import java.io.IOException;

public class C4UPCC {
		
		private static final int UserSize = 339; 
		private static final int ServiceSize = 5825;
		
		private static final int TopK = 10;             //Top k
		private static final double Sparsity = 0.2;      //稀疏度
		
		private static final String PrefixRTMatrix = "rtmatrix";
		private static final String PrefixCutMatrix = "cutmatrix";
		private static final String Suffix = ".dat";
		
		private static final String ResultFile = "C4UPCCResult";
		
		private static final String PrefixUPCCSimFile = "c4upccSimFile";
		private static final String UPCCPreMatrixFile = "c4upccPreMatrixFile";
		private static final String FolderName = "C4UPCC";
		private static final String spiltMarker = "//";
		
		private static double[][] RTMatrix = new double[UserSize][ServiceSize];       //稀疏QoS矩阵
		private static double[][] CutMatrix= new double[UserSize][ServiceSize];
		private static double[][] PreMatrix = new double[UserSize][ServiceSize];   //待预测的值
		
//		private static double[][] SimMatrix = new double[UserSize][UserSize];
		private static double[][] UPCCMatrix = new double[UserSize][UserSize];  //相似度矩阵
		
		private static double[] AverageUser = new double[UserSize];         //每个User调用的QoS的平均值
		
		C4UPCC() {
			
			//初始化UPCCMatrix
			for(int i = 0; i < UserSize; i++) {
				for(int j = 0; j < ServiceSize; j++) {
					PreMatrix[i][j] = 0.0;
				}//
			}//
			
			for(int i = 0; i < UserSize; i++) {
				for(int j = 0; j < UserSize; j++) {
					UPCCMatrix[i][j] = 0.0;
				}
			}
			
		}//...
		
		//载入rtmatrix.dat与rtmatrix0.xx.dat两个文件
		private void loadMatrix() throws IOException {
			
			String fileName = PrefixRTMatrix + Sparsity + Suffix;
			RTMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
			fileName = PrefixCutMatrix + Sparsity + Suffix;
			CutMatrix = VectorUtil.loadMatrix(fileName, UserSize, ServiceSize);
			
			fileName = PrefixUPCCSimFile + Sparsity + Suffix;
//			UPCCMatrix = VectorUtil.loadMatrix(fileName, UserSize, UserSize);
			System.out.println("Files load end~");
		}
		
		//计算UPCC矩阵
		private void computeUPCC() throws IOException {
			
			for(int a = 0; a < UserSize; a++) {
				
				double[] vectorA  = VectorUtil.assignRowVector(RTMatrix, a);
				int[] nonZeroIndexA = VectorUtil.nonZeroIndex(vectorA);  //A调用过的服务下标位置为１
				double sumVectorA = VectorUtil.sumVector(vectorA);       
				int nonZeroNumA = VectorUtil.sumVector(nonZeroIndexA);//A调用过的服务个数
				
				double averageA = sumVectorA/(double)nonZeroNumA;        //平均值
				AverageUser[a] = averageA;

				for(int u = a + 1; u < UserSize; u++) {  //对称阵
					
					double[] vectorU = VectorUtil.assignRowVector(RTMatrix, u);
					int[] nonZeroIndexU = VectorUtil.nonZeroIndex(vectorU);  //U调用过的服务下标位置为１
					int nonZeroNumU = VectorUtil.sumVector(nonZeroIndexU);
					
					double averageU = VectorUtil.sumVector(vectorU)/VectorUtil.sumVector(nonZeroIndexU);
					
					int[] nonZeroIndexAU = VectorUtil.nonZeroIndexBoth(nonZeroIndexA, nonZeroIndexU);//U/A共同调用的服务
					int nonZeroBothNum = VectorUtil.sumVector(nonZeroIndexAU);
					
					if(nonZeroBothNum == 0) {        //未共同调用过任何服务
						UPCCMatrix[a][u] = 0.0;
					} else {
						
						double nominator = 0.0;
						double denominatorL = 0.0;
						double denominatorR = 0.0;
						for(int i = 0; i < nonZeroIndexAU.length; i++){ //nonZeroIndexAU == ServiceSize
							
							if(nonZeroIndexAU[i] == 1) {                 //共同调用的服务
								nominator = nominator + (RTMatrix[a][i] - averageA) * (RTMatrix[u][i] - averageU);
								denominatorL = denominatorL + (RTMatrix[a][i] - averageA) * (RTMatrix[a][i] - averageA);
								denominatorR = denominatorR + (RTMatrix[u][i] - averageU) * (RTMatrix[u][i] - averageU); 
							}//if(...)
							
						}//for(i...)
						
						if(denominatorL == 0 || denominatorR == 0) {
							UPCCMatrix[a][u] = 0.0;
						} else {
							UPCCMatrix[a][u] = nominator/(Math.sqrt(denominatorL) * Math.sqrt(denominatorR));  //基本的ＵＰＣＣ
						}
						
						if(nonZeroNumA == 0 || nonZeroNumU == 0 || UPCCMatrix[a][u] < 0) {   //大于零的保留
							UPCCMatrix[a][u] = 0.0;
						} else {
							UPCCMatrix[a][u] = 2 * nonZeroBothNum *UPCCMatrix[a][u]/(nonZeroNumA + nonZeroNumU);//enhanced UPCC
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
				UPCCMatrix[i][i] = 0.0;
			}
			
			System.out.println("upcc computes end~");
	    }
		
		//
	    private void computePreMatrix() {
			
			//计算待预测的矩阵PreMatrix[][]
			for(int u = 0; u < UserSize; u++) {
				double[] vectorU = RTMatrix[u];  //行向量
				double averageU = AverageUser[u]; //U调用服务QoS的平均值
				   
				double[] simVectorU = UPCCMatrix[u];
				double[] topKSimVector = VectorUtil.filterTopK(simVectorU, TopK);
				
				for(int i = 0; i < ServiceSize; i++) {
					
				   if(RTMatrix[u][i] == 0) {   //不等于０，即已有的值就没必要再预测了
					   
					   double nominator = 0.0;
					   double denominator = 0.0;
					   int topk = 0;
					   
					   for(int a = 0; a < UserSize; a++) {  //查找邻居
						   
						   if((RTMatrix[a][i] != 0) && (topKSimVector[a] != 0)) { //相似用户，且调用过i服务
							   double averageA = AverageUser[a];
							   
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
		private void computeRMSE_MAE() throws IOException {
						
			double[] RMSE_MAE = VectorUtil.computeMAE_RMSE(CutMatrix, PreMatrix);   //计算RMSE/MAE
			
			String outputContent = Sparsity + " " + TopK + " " + RMSE_MAE[0] + "  " + RMSE_MAE[1];
			
			System.out.print("MAE : "+ RMSE_MAE[0]);
			System.out.println(" ; RMSE : " + RMSE_MAE[1]);
			
			String fileName = FolderName + spiltMarker + ResultFile + Suffix;
			VectorUtil.outputFileRMSE_MAE(fileName, outputContent);   //输出计算结果
			
			fileName = FolderName + spiltMarker + PrefixUPCCSimFile + Sparsity + Suffix;
			VectorUtil.outputMatrix(UPCCMatrix, fileName);      //输出UPCC的相似度矩阵
			
			fileName = FolderName + spiltMarker + UPCCPreMatrixFile + Sparsity + Suffix;
			VectorUtil.outputMatrix(PreMatrix, fileName);   //输出UPCC计算出来的PreMatrix
			
		}//computeRMSE_MAE()
		
		//主函数
		public static void main(String[] args) throws IOException {
			
			C4UPCC c4UPCC = new C4UPCC();
			
			c4UPCC.loadMatrix();
			
			c4UPCC.computeUPCC();
			
			c4UPCC.computePreMatrix();
			
			c4UPCC.computeRMSE_MAE();  //计算并输出RMSE/MAE
			
			System.out.println("C4UPCC Computation Done~");
		}
		
	}
