package locationBased;
import javax.swing.*; // For JPanel, etc.
import java.awt.*;           // For Graphics, etc.
import java.awt.geom.*;      // For Ellipse2D, etc.
import java.util.Iterator;
import java.util.Set;

/** An example of drawing/filling shapes with Java2D in Java 1.2.
 *
 *  From tutorial on learning Java2D at
 *  http://www.apl.jhu.edu/~hall/java/Java2D-Tutorial.html
 *
 *  1998 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 */

public class ShapeExample extends JPanel {
  private Ellipse2D.Double circle =
    new Ellipse2D.Double(10, 10, 350, 350);
  private Rectangle2D.Double square =
    new Rectangle2D.Double(100, 50, 3, 3);

  public void paintComponent(Graphics g) {
    clear(g);
    Graphics2D g2d = (Graphics2D)g;
    //g2d.fill(circle);
    CityMapTest cityMapTest = new CityMapTest();
	cityMapTest.loadMap("test_city");
	
	cityMapTest = cityMapTest.filterCity("Phoenix");
	
	Set<BusiCityRecTest> set = cityMapTest.getAllBusiRec();
	Iterator<BusiCityRecTest> it = set.iterator();
	BusiCityRecTest rec;
	Rectangle2D.Double square;
	while(it.hasNext()){
		rec = it.next();
		double scaleX = 1250 / (cityMapTest.maxLong - cityMapTest.minLong);
		double scaleY = 750 / (cityMapTest.maxLat - cityMapTest.minLat);
		int posX = (int) (Math.round((rec.longitude - cityMapTest.minLong)*scaleX)+10);
		int posY = (int) (Math.round((rec.latitude - cityMapTest.minLat)*scaleY)+10);
		if(posX >= 1300) System.err.println("error posx!");
		if(posY >= 800) System.err.println("error posy!");
		square = new Rectangle2D.Double(posX,posY,3,3);
		g2d.fill(square);
	}
    //g2d.fill(square);
  }

  // super.paintComponent clears offscreen pixmap,
  // since we're using double buffering by default.

  protected void clear(Graphics g) {
    super.paintComponent(g);
  }

  protected Ellipse2D.Double getCircle() {
    return(circle);
  }

  public static void main(String[] args) {
    WindowUtilities.openInJFrame(new ShapeExample(), 1300, 800);
  }
}