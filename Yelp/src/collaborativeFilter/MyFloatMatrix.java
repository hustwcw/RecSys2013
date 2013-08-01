package collaborativeFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyFloatMatrix{
	private MyHash matrix = new MyHash();
	private float[][] fMatrix = null;
	private MyMapper userIdToIndex = new MyMapper();
	private MyMapper businessIdToIndex = new MyMapper();
	private List<String> indexToUserId = new ArrayList<String>();
	private List<String> indexToBusinessId = new ArrayList<String>();
	private String filePathName = "origin/";
	private String dataFileName = "dataMap.hashmap";
	private String userToIndexFileName = "userToIndex.hashmap";
	private String businessToIndexFileName = "businessToIndex.hashmap";
	private String indexToUserFileName = "indexToUser.hashmap";
	private String indexToBusinessFileName = "indexToBusiness.hashmap";
	
	public void clear(){
		matrix.clear();
		userIdToIndex.clear();
		businessIdToIndex.clear();
		indexToUserId.clear();
		indexToBusinessId.clear();
	}
	
	public void setFilePathName(String name){
		filePathName = name + "/";
	}
	public void setDataFileName(String name){
		dataFileName = name;
	}
	public void setUserToIndexFileName(String name){
		userToIndexFileName = name;
	}
	public void setBusinessToIndexFileName(String name){
		businessToIndexFileName = name;
	}
	public void setIndexToUserFileName(String name){
		indexToUserFileName = name;
	}
	public void setIndexToBusinessFileName(String name){
		indexToBusinessFileName = name;
	}
	
	public void putByIndex(int x, int y, float value){
		matrix.put(new Pair(x,y), value);
	}
	public void putById(String user_id, String business_id, float value){
		if(! userIdToIndex.containsKey(user_id)){
			userIdToIndex.put(user_id, matrix.currentUserLen);
			indexToUserId.add(user_id);
			matrix.currentUserLen ++;
		}
		if(! businessIdToIndex.containsKey(business_id)){
			businessIdToIndex.put(business_id, matrix.currentBussinessLen);
			indexToBusinessId.add(business_id);
			matrix.currentBussinessLen ++;
		}
		putByIndex(userIdToIndex.get(user_id),businessIdToIndex.get(business_id), value);
	}
	
	public boolean containsKey(int x, int y){
		return matrix.containsKey(new Pair(x, y));
	}
	public boolean containsValue(int value){
		return matrix.containsValue(value);
	}
	public boolean containsUserId(String userId){
		return userIdToIndex.containsKey(userId);
	}
	public boolean containsBusinessId(String businessId){
		return businessIdToIndex.containsKey(businessId);
	}
	
	public float get(int x, int y){
		Pair pair = new Pair(x,y);
		if(matrix.containsKey(pair)) return matrix.get(pair);
		else return 0;
	}
	public int getUserIndex(String userId){
		if(!containsUserId(userId)) return -1;
		else return userIdToIndex.get(userId);
	}
	public String getUserID(int index){
		if(index >= indexToUserId.size()) return null;
		else return indexToUserId.get(index);
	}
	public int getBusinessIndex(String businessId){
		if(!containsBusinessId(businessId)) return -1;
		else return businessIdToIndex.get(businessId);
	}
	public String getBusinessID(int index){
		if(index >= indexToBusinessId.size()) return null;
		else return indexToBusinessId.get(index);
	}
	public int getUserLen(){
		return matrix.currentUserLen;
	}
	public int getBusinessLen(){
		return matrix.currentBussinessLen;
	}
	public int getSize(){
		return matrix.size();
	}
	public Set<Pair> getKeySets(){
		return matrix.keySet();
	}
	
	public float[][] toMatrix(){
		if(fMatrix != null) return fMatrix;
		
		int uLen,bLen;
		uLen = matrix.currentUserLen;
		bLen = matrix.currentBussinessLen;
		float[][] retM = new float[uLen][bLen];
		int i,j;
		for(i=0;i<uLen;i++){
			for(j=0;j<bLen;j++) retM[i][j] = get(i, j);
		}
		fMatrix = retM;
		return retM;
	}
	
	public void refreshFMatrix(){
		fMatrix = null;
		toMatrix();
	}
	
	public void storeMapToFile(){
		FileOutputStream fo;
		ObjectOutputStream oo;
		try {
			File file = new File(filePathName);
			if(! file.exists()) file.mkdirs();
			
			fo = new FileOutputStream(filePathName+dataFileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(matrix);
			oo.close();
			
			fo = new FileOutputStream(filePathName+userToIndexFileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(userIdToIndex);
			oo.close();
			
			fo = new FileOutputStream(filePathName+businessToIndexFileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(businessIdToIndex);
			oo.close();
			
			fo = new FileOutputStream(filePathName+indexToUserFileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(indexToUserId);
			oo.close();
			
			fo = new FileOutputStream(filePathName+indexToBusinessFileName);
			oo = new ObjectOutputStream(fo);
			oo.writeObject(indexToBusinessId);
			oo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getMapFromFile(){
		clear();
		FileInputStream fi;
		ObjectInputStream oi;
		try {
			fi = new FileInputStream(filePathName+dataFileName);
			oi = new ObjectInputStream(fi);
			matrix = (MyHash)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+userToIndexFileName);
			oi = new ObjectInputStream(fi);
			userIdToIndex = (MyMapper)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+businessToIndexFileName);
			oi = new ObjectInputStream(fi);
			businessIdToIndex = (MyMapper)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+indexToUserFileName);
			oi = new ObjectInputStream(fi);
			indexToUserId = (ArrayList<String>)oi.readObject();
			fi.close();
			oi.close();
			
			fi = new FileInputStream(filePathName+indexToBusinessFileName);
			oi = new ObjectInputStream(fi);
			indexToBusinessId = (ArrayList<String>)oi.readObject();
			fi.close();
			oi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

class MyMapper extends HashMap<String, Integer> implements Serializable{
	private static final long serialVersionUID = 7809280289164419479L;
}

class MyHash extends HashMap<Pair, Float> implements Serializable{
	private static final long serialVersionUID = 4275646789894585015L;
	public int currentUserLen;
	public int currentBussinessLen;
	public MyHash(){
		currentUserLen=0;
		currentBussinessLen=0;
	}
}
