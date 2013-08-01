package dataMatrix;

import java.io.Serializable;

public class Pair implements Serializable{
	private static final long serialVersionUID = 8567387275222214145L;
	public int x,y;
	public Pair(int x,int y){
		this.x = x;
		this.y = y;
	}
	@Override
	public boolean equals(Object pair){
		Pair p = (Pair)pair;
		if(this.x == p.x && this.y == p.y) return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return x*100000+y;
	}
}