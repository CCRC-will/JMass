package JMass2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Mousexmp {


	public static void main ( String[] args ) {
		JFrame myP = new JFrame ();

		myP.add ( new JComponent () {
			private Rectangle theBox = null;

			{
				MouseAdapter mouseAdapter = new MouseAdapter () {
					Point p1 = null;
					Point p2 = null;

					public void mousePressed ( MouseEvent e ) {
						p1 = e.getPoint();

						theBox = new Rectangle (p1 );
						repaint ();
					}

					public void mouseDragged ( MouseEvent e ) {
						// draw rectangle when mouse is down
						p2 = e.getPoint();
						Dimension d = new Dimension(p2.x - p1.x,  p2.y - p1.y);
						theBox.setSize ( d );
						repaint ();
					}

					public void mouseReleased ( MouseEvent e ) {
						//  erase the rectangle
						theBox = null;
						repaint ();
					}
				};
				addMouseListener ( mouseAdapter );
				addMouseMotionListener ( mouseAdapter );
			}

			protected void paintComponent ( Graphics g ) {
				Graphics2D g2d = ( Graphics2D ) g;
				g2d.setPaint ( Color.GREEN.darker().darker() );
				
				if (theBox != null ) g2d.draw ( theBox );

			}
		} );

		myP.setSize ( 500, 500 );
		myP.setLocationRelativeTo ( null );
		myP.setVisible ( true );
	}
}