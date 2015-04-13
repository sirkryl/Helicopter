import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;

/**
 * 
 * SplashScreen class for creating the splash screen
 * before the intro starts
 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
 *
 */
public class SplashScreen extends GameCanvas
{
	/** Display **/
	private static Display display;
	private static MenuCanvas next;
	private static Canvas svgCanvas;
	private static InputStream is;
	
	/** Timer for delay **/
	private static Timer timer = new Timer();
	
	/** SVG declerations **/
	private static SVGImage svgImage;
    private static SVGAnimator svgAnimator;
    
    /**
     * 
     * Constructor for SplashScreen class
     * 
     * @param display
     * @param next
     */
	public SplashScreen (Display display, MenuCanvas next) 
	{
		super(true);
		SplashScreen.display = display;
		SplashScreen.next = next;
		display.setCurrent(this);
	}
	
	/**
	 * show us that svg splashscreen
	 */
	public void paint(Graphics g)
	{
		is = Helicopter.class.getResourceAsStream("/splashscreen.svg");
        
        try {
                svgImage = (SVGImage) SVGImage.createImage(is, null);
        } catch (IOException e) {
                System.out.println("Couldn't load svg ressource");
                e.printStackTrace();
        }

        svgAnimator = SVGAnimator.createAnimator(svgImage);
        svgCanvas = (Canvas) svgAnimator.getTargetComponent();
        svgAnimator.play();
        display.setCurrent(svgCanvas);
        try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * automatically run a 5s countdown
	 */
	public void showNotify()
	{
		timer.schedule(new CountDown(), 5000);
	}
	
	/**
	 * stop splashscreen, start intro!
	 */
	private void dismiss()
	{
		timer.cancel();
		next.init();
		next.goIntro();
		
		display.setCurrent(next);
		svgAnimator.stop();
		svgAnimator = null;
		svgCanvas = null;
		svgImage = null;
	}
	
	/**
	 * countdown until intro is played
	 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
	 *
	 */
	private class CountDown extends TimerTask
	{
		public void run(){
			dismiss();
		}
	}
}
