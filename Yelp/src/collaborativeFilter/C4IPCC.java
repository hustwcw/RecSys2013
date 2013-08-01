package collaborativeFilter;
import java.io.IOException;
import dataMatrix.MyMatrix;

public class C4IPCC {

	public int UserSize; 
	public int ServiceSize;
	///public MyMatrix matrix;
	
	public static int TopK = 100;             //Top k
	public String loadPrefixName;
	public String storePrefixName;
	
	public float[][] PreMatrix;   //待预测的值
	public float[][] IPCCMatrix;  //相似度矩阵
	private C4UPCC upcc;
	
	public void storeMatrix(){
		if(storePrefixName == null) storePrefixName = "IPCC/"+loadPrefixName;
		
		VectorUtil.storeMatrixFloat(IPCCMatrix, storePrefixName, "IPCCMatrix");
		VectorUtil.storeMatrixFloat(PreMatrix, storePrefixName, "PreMatrix");
	}
	
	//载入rtmatrix.dat与rtmatrix0.xx.dat两个文件
	public void loadMatrix(String prefixName, MyMatrix matrix) throws IOException {
		//this.matrix = matrix;
		this.loadPrefixName = prefixName;
		upcc = new C4UPCC();
		UserSize = matrix.getUserLen();
		ServiceSize = matrix.getBusinessLen();
		upcc.UserSize = ServiceSize;
		upcc.ServiceSize = UserSize;
		upcc.loadPrefixName = prefixName;
		upcc.RTMatrix = matrix.toMatrixTranspose();
		upcc.UPCCMatrix = new float[ServiceSize][ServiceSize];
		IPCCMatrix = new float[ServiceSize][ServiceSize];
		upcc.AverageUser = new float[ServiceSize];
		
		System.out.println("Files load end~");
	}//loadMatrix()
	public void loadMatrix(String prefixName) throws IOException {
		MyMatrix matrix = new MyMatrix();
		matrix.setFilePathName(prefixName);
		matrix.getMapFromFile();
		
		loadMatrix(prefixName, matrix);
	}
	
	//计算IPCC矩阵
	public void computeIPCC() throws IOException {
		upcc.computeUPCC();
		int i,j;
		for(i=0;i<ServiceSize;i++){
			for(j=0;j<ServiceSize;j++) IPCCMatrix[i][j] = upcc.UPCCMatrix[j][i];
		}
		System.out.println("IPCCMatrix Trans Done~");
   }

   //compute predicted matrix
	public void computePreMatrix() {
		upcc.computePreMatrix();
		upcc.UPCCMatrix = null;
		System.gc();
		
		PreMatrix = new float[UserSize][ServiceSize];
		int i,j;
		for(i=0;i<UserSize;i++){
			for(j=0;j<ServiceSize;j++) PreMatrix[i][j] = upcc.PreMatrix[j][i];
		}
		upcc = null;
		System.gc();
		
		System.out.println("PreMatrix Trans Done~");
		
	}//computePreMatrix()
	
	
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

