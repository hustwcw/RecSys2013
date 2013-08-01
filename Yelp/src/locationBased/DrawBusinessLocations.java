package locationBased;

import java.awt.*;
import java.applet.*;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

class Mycanvas extends Canvas {
	Mycanvas() {
		getSize();//给出大小，自动调用后面的方法getPreferredSize()
	}
	public void paint(Graphics g) { //参数g会自动获得.
		g.setColor(Color.red);
		CityMapTest cityMapTest = new CityMapTest();
		cityMapTest.loadMap("test_city");
		Set<BusiCityRecTest> set = cityMapTest.getAllBusiRec();
		Iterator<BusiCityRecTest> it = set.iterator();
		BusiCityRecTest rec;
		while(it.hasNext()){
			rec = it.next();
			double scaleX = 1250 / (cityMapTest.maxLong - cityMapTest.minLong);
			double scaleY = 750 / (cityMapTest.maxLat - cityMapTest.minLat);
			int posX = (int) (Math.round((rec.longitude - cityMapTest.minLong)*scaleX)+10);
			int posY = (int) (Math.round((rec.latitude - cityMapTest.minLat)*scaleY)+10);
			if(posX >= 1300) System.err.println("error posx!");
			if(posY >= 800) System.err.println("error posy!");
			
			g.drawRect(posX, posY, 2, 2);
		}
		//g.drawString("我在画布上写字呢",100,30);
		//g.drawString("我将来还能画很多图形呢",6,50);
	}
	public Dimension getPreferredSize() {
		return new Dimension(1300, 1100);
	}
}
public class DrawBusinessLocations extends JFrame {
	Mycanvas mycanvas;
	public void init() {
		Container contentpane=getContentPane();//初始化一个容器
		contentpane.setBackground(Color.GRAY);//设置颜色
		contentpane.setLayout(null);//设置容器布局管理器为无
		
		Mycanvas canvas = new Mycanvas();
		canvas.setVisible(true);
		contentpane.add(canvas);
		setSize(1300, 800);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args){
		new DrawBusinessLocations().init();
	}
}

