package de.caegroup.commons;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class Reflect extends JComponent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image;

    public Reflect() {
        try {
            image = ImageIO.read( new File( "/tmp/1376246196740_png/processTopologyFaded.png" ) );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public void paintComponent( Graphics g ) {
        Graphics2D g2d = (Graphics2D)g;
        int width = getWidth();
        int height = getHeight();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        float opacity = 0.6f;
        float fadeGeFactor = 0.0f;

        g2d.setPaint( new GradientPaint( 0, 0, Color.white, 0, height, Color.white ) );
        g2d.fillRect( 0, 0, width, height );
//        g2d.translate( (width-imageWidth)/2, height/2-imageHeight );
//        g2d.drawRenderedImage( image, null );
//        g2d.translate( 0, 2*imageHeight+gap );
//        g2d.scale( 1, -1 );

        BufferedImage reflection = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB );
		Graphics2D rg = reflection.createGraphics();
        rg.drawRenderedImage( image, null );
		rg.setComposite( AlphaComposite.getInstance( AlphaComposite.DST_IN ) );
        rg.setPaint( 
            new GradientPaint( 
                imageWidth, 0, new Color( 0.0f, 0.0f, 0.0f, 0.0f ),
                imageWidth*fadeGeFactor, 0, new Color( 0.0f, 0.0f, 0.0f, opacity )
            )
        );
        rg.fillRect( 0, 0, imageWidth, imageHeight );
        rg.dispose();
        g2d.drawRenderedImage( reflection, null );
    }
    
    public Dimension getPreferredSize() {
        return new Dimension( 500, 500 );
    }
    
    public static void main (String args[]) {
        JFrame f = new JFrame();
        Reflect r = new Reflect();
        f.getContentPane().add( r );
        f.pack();
        f.show();
    }
}