package de.prozesskraft.commons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

public class AutoCropBorder
{

	public AutoCropBorder(String pathToImageFile) throws IOException
	{
		File imageFile = new File(pathToImageFile);
		boolean success = false;
		int versuche = 0;
		while(!(success) && versuche<3)
		{
			try
			{
				BufferedImage in = ImageIO.read(imageFile);
				BufferedImage out = getCroppedImage(in, 0);
				ImageIO.write(out, "png", imageFile);
				success = true;
			}
			catch (IndexOutOfBoundsException e)
			{
				versuche++;
				System.err.println("problems with reading image. (IndexOutOfBoundsException)");
				try
				{
					Thread.sleep(300);
				} catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch (IIOException e)
			{
				versuche++;
				System.err.println("problems with reading image. (IIOException)");
				try
				{
					Thread.sleep(300);
				} catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	public BufferedImage getCroppedImage(BufferedImage source, double tolerance) {
		   // Get our top-left pixel color as our "baseline" for cropping
		   int baseColor = source.getRGB(0, 0);

		   int width = source.getWidth();
		   int height = source.getHeight();

		   int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
		   int bottomY = -1, bottomX = -1;
		   for(int y=0; y<height; y++) {
		      for(int x=0; x<width; x++) {
		         if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
		            if (x < topX) topX = x;
		            if (y < topY) topY = y;
		            if (x > bottomX) bottomX = x;
		            if (y > bottomY) bottomY = y;
		         }
		      }
		   }

		   BufferedImage destination = new BufferedImage( (bottomX-topX+1), 
		                 (bottomY-topY+1), BufferedImage.TYPE_INT_ARGB);

		   destination.getGraphics().drawImage(source, 0, 0, 
		               destination.getWidth(), destination.getHeight(), 
		               topX, topY, bottomX, bottomY, null);

		   return destination;
		}

		private boolean colorWithinTolerance(int a, int b, double tolerance) {
		    int aAlpha  = (int)((a & 0xFF000000) >>> 24);   // Alpha level
		    int aRed    = (int)((a & 0x00FF0000) >>> 16);   // Red level
		    int aGreen  = (int)((a & 0x0000FF00) >>> 8);    // Green level
		    int aBlue   = (int)(a & 0x000000FF);            // Blue level

		    int bAlpha  = (int)((b & 0xFF000000) >>> 24);   // Alpha level
		    int bRed    = (int)((b & 0x00FF0000) >>> 16);   // Red level
		    int bGreen  = (int)((b & 0x0000FF00) >>> 8);    // Green level
		    int bBlue   = (int)(b & 0x000000FF);            // Blue level

		    double distance = Math.sqrt((aAlpha-bAlpha)*(aAlpha-bAlpha) +
		                                (aRed-bRed)*(aRed-bRed) +
		                                (aGreen-bGreen)*(aGreen-bGreen) +
		                                (aBlue-bBlue)*(aBlue-bBlue));

		    // 510.0 is the maximum distance between two colors 
		    // (0,0,0,0 -> 255,255,255,255)
		    double percentAway = distance / 510.0d;     

		    return (percentAway > tolerance);
		}
	
}
