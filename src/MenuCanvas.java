import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;
import javax.microedition.rms.RecordStoreException;

/**
 * contains intro and organizes proper menudisplay
 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
 *
 */
public class MenuCanvas extends GameCanvas implements Runnable {



	/** gamestate for displaying the SVG menu */
	private int sleep = 200; 
	
	/** command **/
	private static Command back;




	/** midlet */
	private static Helicopter mid;

	/** different states **/
	private static boolean first = true;
	private static boolean intro = false;
	
	/** fire key state **/
	private static boolean fire = false;
	
	/** determines if the thread is running or not **/
	private static boolean quit = false;

	/** intro player **/
	private static Player p;

	/** svg for menu **/
	private static SVGImage svgMenuImage;
	private static SVGAnimator svgMenuAnimator;
	private static Canvas svgMenuCanvas;
	
	/** custom CommandListener **/
	private static Listener listener;
	
	/** highscore and help forms **/
	private static Form helpForm;
	private static Form highScoreForm;

	/**
	 * constructor to init command and sets width and height
	 * @param mid midlet
	 */

	public MenuCanvas(Helicopter mid) {
		super(false);
		back = new Command("back", Command.BACK, 0);
		this.setFullScreenMode(true);
		MenuCanvas.mid = mid;
	}

	/** 
	 * creates svgs, listener and starts the thread
	 */
	public void init()
	{
		listener = new Listener();
		createSvgMenu();
		createHelpForm();
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	/**
	 * displays the highscorescreen, which has to be updated everytime (highscores change)
	 */
	public static void showHighScoreScreen()
	{
		highScoreForm = new Form("Highscore");
		highScoreForm.setTitle("Highscore");
		highScoreForm.addCommand(back);

		highScoreForm.setCommandListener(listener); 
		int[] records = new int[10];
		try {
			records = mid.readOrderedRecords();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		}
		if (records[0] == 0)
		{
			highScoreForm.append("Noch keine Highscores vorhanden.");
		}
		else
		{
			highScoreForm.append("Top 10: \n");
		}
		for(int i=0; i < records.length; i++)
		{
			if(records[i] != 0)
			{
				highScoreForm.append("   # "+(i+1)+" : "+String.valueOf(records[i])+"\n");
			}
		}
		Helicopter.display.setCurrent(highScoreForm);
	}

	/**
	 * create helpform (only once)
	 */
	private static void createHelpForm()
	{
		helpForm = new Form("Help");
		helpForm.append("Steuere den Helikopter an Hindernissen vorbei und halte so lange wie möglich durch.\n");
		helpForm.append("-- UP: Helikopter steigt\n");
		helpForm.append("-- NICHTS: Helikopter sinkt\n");
		helpForm.append("-- FIRE: Pause (Unpause mit UP)\n");
		helpForm.append("-- LEFT: Sound an/aus\n");
		helpForm.append("-- RIGHT: Startet Neues Spiel bei Game Over\n");
		helpForm.append("-- BACK: Zurück ins Menü\n\n");
		helpForm.append("Alle 500 Punkte erhöht sich der Schwierigkeitsgrad.\n");
		helpForm.addCommand(back);     
		helpForm.setCommandListener(listener); 
	}
	
	/**
	 * display help
	 */
	public static void showHelpScreen(){
	    Helicopter.display.setCurrent(helpForm);
	}

	/**
	 * press a key
	 * 
	 * @param key keyid
	 */
	protected void keyPressed(int key) {
		switch (mergeKey(key)) {
		case KEY_NUM5:
			fire = true;
			break;
		}
	}

	/**
	 * release a key
	 * 
	 * @param key keyid
	 */
	protected void keyReleased(int key) {
		switch (mergeKey(key)) {
		case KEY_NUM5:
			fire = false;
			break;
		}
	}

	/**
	 * merge game keys and normal keys
	 * 
	 * @param key
	 *            keyid
	 * @return mergedkey
	 */
	private byte mergeKey(int key) {
		switch (getGameAction(key)) {
		case FIRE:
			key = KEY_NUM5;
			break;
		}
		
		return (byte) key;
	}

	/**
	 * used by SplashScreen to start intro
	 */
	public void goIntro()
	{
		intro = true;
	}

	/**
	 * change priority of this thread
	 * @param sleep how long sleep?
	 */
	public void priority(int sleep)
	{
		this.sleep = sleep;
	}

	/**
	 * play intro as described in the lecture slides
	 */
	private void playIntro()
	{
		try {

			System.out.println("Playing intro");
			InputStream is = Helicopter.class.getResourceAsStream("/intro.mpeg");
			p = Manager.createPlayer(is, "video/mpeg");
			p.realize();
			VideoControl vc = (VideoControl)p.getControl("VideoControl");
			if (vc != null) 
			{
				vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
				int halfCanvasHeight = this.getHeight() / 2;
				vc.setDisplayFullScreen(false);
				vc.setDisplaySize(this.getWidth(), halfCanvasHeight+70);
				vc.setDisplayLocation(0,
						(halfCanvasHeight / 2)-35);
				vc.setVisible(true); 
			}
			else throw new RuntimeException("Video can not be played");
			p.prefetch();
			p.start();

			p.addPlayerListener(new PlayerListener() 
			{
				public void playerUpdate(Player player, String event,Object eventData) 
				{
					if (event == END_OF_MEDIA)
					{
						intro = false;
						try {
							p.stop();
						} catch (MediaException e) {
							e.printStackTrace();
						}
						p.close();
						System.out.println("Ende");
					}	
				}
			});
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * skip intro if 'fire' is pressed
	 */
	private void checkIntro()
	{
		if(fire)
		{
			try {
				p.stop();
				p.close();
			} catch (MediaException e) {
				e.printStackTrace();
			}
			intro = false;
		}
	}

	/**
	 * create svg menu (only once)
	 */
	private void createSvgMenu()
	{
		InputStream is = getClass().getResourceAsStream("/menu.svg");
		try {
			svgMenuImage = (SVGImage) SVGImage.createImage(is, null);
		} catch (IOException e) {
			System.out.println("Couldn't load svg ressource");
			e.printStackTrace();
		}
		svgMenuAnimator = SVGAnimator.createAnimator(svgMenuImage);
		svgMenuCanvas = (Canvas) svgMenuAnimator.getTargetComponent();
		svgMenuAnimator.setSVGEventListener(new SVGListener(svgMenuAnimator, svgMenuImage, mid));
		svgMenuAnimator.play();             


	}

	/**
	 * show svg menu
	 */
	public void showSvgMenu()
	{
		Display.getDisplay(mid).setCurrent(svgMenuCanvas);
	}
	
	/**
	 * game loop (thread)
	 */
	public void run() {
		while (!quit) 
		{
			try
			{
				try 
				{
					Thread.sleep(sleep);
				} catch (Exception e) {

				}
				if(first && intro)
				{
					priority(20);
					first = false;
					playIntro();
				}
				else if(intro)
				{
					checkIntro();
				}
				else if(!intro && !first)
				{
					showSvgMenu();
					quit = true;
				}
				Thread.yield();
			}
			catch (Exception e)
			{
			}
		}
	}
	
	/**
	 * custom CommandListener
	 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
	 *
	 */
	static class Listener implements CommandListener
	{
		public void commandAction(Command c, Displayable d){
			if(c == back)
			{
				Helicopter.display.setCurrent(svgMenuCanvas);        	
			}
		}
	}



}
