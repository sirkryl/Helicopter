import java.io.InputStream;
import java.util.Random;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

/**
 * draws and handles whole game and it's logic.
 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
 *
 */
public class HelicopterCanvas extends GameCanvas implements Runnable {

	/** responsible for the overall speed of the game **/
	private int sleep = 20; 

	/** Random generator used to calculate obstacle-positions **/
	private static Random random;
	
	/** all the images needed **/
	private Image helicopterImg;
	private Image bgImg_Top;
	private Image bgImg_Bot;
	private Image obstacleImg;
	
	/** font **/
	private Font font;
	
	/** booleans for the different states of the game **/
	private boolean first = true;
	private boolean paused = false;
	private boolean over = false;
	private boolean obstacle = false;
	private boolean sound = false;
	private boolean mute = false;
	private boolean down = false;
	private boolean getRecords = true;
	
	/** time-variable for helicopter-exploding **/
	private int exploding = 20;
	
	/** our layerManager **/
	private LayerManager layerManager;
	
	/** Sprites **/
	private Sprite helicopterSprite;
	private Sprite bgSprite_Top;
	private Sprite bgSprite_Bot;
	private Sprite fObstacleSprite;
	private Sprite sObstacleSprite;
	
	/** initial helicopter - and obstacle - positions **/
	private int helicopterX = (getWidth() / 2 )-67;
	private int helicopterY = (getHeight() / 2);
	private int fObstacleX = getWidth()+50;
	private int fObstacleY = getHeight()/2;
	private int sObstacleX = getWidth()+50;
	private int sObstacleY = (getHeight()/2)-50;
	
	/** velocity of the helicopter **/
	private double helicopterYVel = 3.0;
	private double helicopterYUpVel = 0.1;
	private double obstacleVel = 2.0;
	
	/** used to update the 'Best'-Label **/
	private int[] records = new int[10];
	
	/** for helicopteranimation **/
	private  int[] frameSequence = {0,1,2,3,4,5,6};
	
	/** midlet **/
	private static Helicopter mid;
	
	/** two different inputstreams for explosion and coptersounds **/
	private static InputStream is;
	private static InputStream is_;
	
	/** two different players for explosion and coptersounds **/
	public static Player heliPlayer = null;
	public static Player explPlayer = null;

	/** user's current score **/
	private int score = 0;
	
	/** user's record **/
	private int record = 0;
	
	/** user's current level **/
	private int level = 1;

	/**
	 * normal constructor (nothing special here)
	 */
	public HelicopterCanvas(Helicopter mid) {
		super(true);
		HelicopterCanvas.mid = mid;
		this.setFullScreenMode(true);
	}

	/**
	 * initialize the game
	 */
	public void start() 
	{

		layerManager = new LayerManager();
		
		try {
			helicopterImg = Image.createImage("/helicopter.png");
			obstacleImg = Image.createImage("/obstacle.png");
			bgImg_Top = Image.createImage("/rect.png");
			bgImg_Bot = Image.createImage("/rect.png");
		} catch (Exception e) {
			System.out.println("Fehler beim Einlesen der Bilder");
			e.printStackTrace();
		}

		//create all sprites and append them to our layermanager
		helicopterSprite = new Sprite(helicopterImg, 67, 39);
		helicopterSprite.defineReferencePixel(0, 0);
		helicopterSprite.setRefPixelPosition(helicopterX, helicopterY);
		helicopterSprite.setFrameSequence(frameSequence);
		
		fObstacleSprite = new Sprite(obstacleImg, 32, 64);
		fObstacleSprite.defineReferencePixel(0,0);
		fObstacleSprite.setRefPixelPosition(fObstacleX,fObstacleY);
		
		sObstacleSprite = new Sprite(obstacleImg, 32, 64);
		sObstacleSprite.defineReferencePixel(0,0);
		sObstacleSprite.setRefPixelPosition(sObstacleX,sObstacleY);
		
		bgSprite_Top = new Sprite(bgImg_Top, 320, 32);
		bgSprite_Bot = new Sprite(bgImg_Bot, 320, 32);
		bgSprite_Top.defineReferencePixel(0,0);
		bgSprite_Bot.defineReferencePixel(bgImg_Bot.getWidth(),bgImg_Bot.getHeight());
		bgSprite_Top.setRefPixelPosition(0,0);
		bgSprite_Bot.setRefPixelPosition(getWidth(),getHeight());
		
		layerManager.append(fObstacleSprite);
		layerManager.append(sObstacleSprite);
		layerManager.append(helicopterSprite);	
		layerManager.append(bgSprite_Top);
		layerManager.append(bgSprite_Bot);
		
		//set our font
		font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
		
		//get our sounds and initialise the necessary players
		is = mid.getClass().getResourceAsStream("/helicopter.wav");
		try {
			heliPlayer = Manager.createPlayer(is, "audio/x-wav");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		is_ = mid.getClass().getResourceAsStream("/explosion.wav");
		try {
			explPlayer = Manager.createPlayer(is_, "audio/x-wav");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//start the gameloop
		Thread runner = new Thread(this);
		runner.start();
	}

	/**
	 * create the background
	 * @param g
	 */
	private void createBackground(Graphics g) 
	{
		g.setColor(0xdddfe8);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * updates the screen accordingly
	 * @param g
	 */
	private void updateScreen(Graphics g) 
	{
		//create background every time
		createBackground(g);

		//copter-"animation", switch between two specific frames
		if(helicopterSprite.getFrame() == 1 || first || paused) 
		{
			helicopterSprite.setFrame(0);
		}
		else if(over && exploding > 10)
		{
			exploding--;
			helicopterSprite.setFrame(6);
		}
		else if(over && exploding > 0 && exploding <= 10)
		{
			exploding--;
			helicopterSprite.setFrame(5);
		}
		else if(over && exploding == 0)
		{
			helicopterSprite.setFrame(4);
		}
		else if(helicopterSprite.getFrame() == 0 && !over && !first && !paused)
		{
			helicopterSprite.setFrame(1);
		}
		else if(helicopterSprite.getFrame() == 2)
		{
			helicopterSprite.setFrame(3);
		}
		else if(helicopterSprite.getFrame() == 3)
		{
			helicopterSprite.setFrame(2);
		}
		
		//place copter and obstacles accordingly
		helicopterSprite.setRefPixelPosition(helicopterX, helicopterY);
		fObstacleSprite.setRefPixelPosition(fObstacleX, fObstacleY);
		sObstacleSprite.setRefPixelPosition(sObstacleX, sObstacleY);
		
		//set up the viewvindow
		layerManager.setViewWindow(0, 0, getWidth(), getHeight());
		layerManager.paint(g, 0, 0);
		
		//we need some scores too
		createScoreboard(g); 
		
		//game over-output
		if(over)
		{
			g.setColor(0xdddfe8);
			g.fillRect( 0, (getHeight()/2)-80, getWidth()-2, 50);
			g.setColor(0x000000);
			if (score == record)
			{
				g.drawString("New Record! .., but still dead.", getWidth()/2, (getHeight()/2)-66, Graphics.TOP | Graphics.HCENTER);
			}
			else
			{
				g.drawString("Game over, you are dead.", getWidth()/2, (getHeight()/2)-66, Graphics.TOP | Graphics.HCENTER);
			}
			g.drawString("BOOOOM!", getWidth()/2, (getHeight()/2)-80, Graphics.TOP | Graphics.HCENTER);
			g.drawString("Press 'Right' to start again.", getWidth()/2, (getHeight()/2)-52, Graphics.TOP | Graphics.HCENTER);

		}
		
		//before starting a new game, the user has to press 'up'
		if(first)
		{
			
			g.setColor(0x000000);
			g.drawString("Press 'Up' to start", getWidth()/2, (getHeight()/2)-80, Graphics.TOP | Graphics.HCENTER);
		}
		
		//game can be paused, too.
		else if(paused)
		{
			g.setColor(0xdddfe8);
			g.fillRect( 0, (getHeight()/2)-80, getWidth()-2, 20);
			g.setColor(0x000000);
			g.drawString("Game paused, press 'Up' to continue", getWidth()/2, (getHeight()/2)-80, Graphics.TOP | Graphics.HCENTER);
		}
		
		//if there should be sound
		if(!mute)
		{
			g.setColor(0xffffff);
			g.drawString("Sound: on", 80, getHeight()-25, Graphics.TOP | Graphics.RIGHT);		
		}
		
		//if the game is muted.
		else if(mute)
		{
			g.setColor(0xffffff);
			g.drawString("Sound: off", 80, getHeight()-25, Graphics.TOP | Graphics.RIGHT);	
		}
		
		//move the copter
		moveHelicopter();
		
		//move the obstacles
		moveObstacles();
		
		//update graphics
		flushGraphics();
	}

	/**
	 * moves the helicopter depending on user input and handles collisions
	 */
	private void moveHelicopter() {
		int keyState = getKeyStates();

		//starts the game at the beginning and then just raises the copter (delayed velocity) and plays a sound
		if ((keyState & UP_PRESSED) != 0 && helicopterY >= 19 && !paused && !over) {
			first = false;
			if (helicopterYVel != 3.0)
			{
				helicopterYVel = 3.0;
			}
			helicopterY -= helicopterYUpVel;
			if (helicopterYUpVel < 2)
			{
				helicopterYUpVel += 0.12;
			}
			if (helicopterYUpVel >= 2 && helicopterYUpVel < 3.5)
			{
				helicopterYUpVel += 0.15;
			}
			if(!sound) 
			{
				helicopterSprite.setFrame(2);
				if(!mute)
				{
					playHelicopterSound();
				}
				sound = true;
				
			}
		} 
		
		//if 'up' isn't pressed, the copter drops (delayed downVelocity) and no sound is played
		else if (helicopterY <= getHeight()-63 && !first && !paused && !over) {
			helicopterY += helicopterYVel;
			if(helicopterYVel < 4)
			{
				helicopterYVel += 0.12;
			}
			if (helicopterYUpVel != 0.1)
			{
				helicopterYUpVel = 0.1;
			}
			if(sound)
			{
				helicopterSprite.setFrame(0);
				if(!mute)
					
				{
					stopSounds();
				}
				sound = false;
				
			}
		} 
		
		//one press = one mute/unmute
		if ((keyState & LEFT_PRESSED) == 0 && down)
		{
			down = false;
		}
		
		//mute or unmute
		if ((keyState & LEFT_PRESSED) != 0 && !down)
		{
			down = true;
			if(!mute)
			{
				mute = true;
				stopSounds();
			}
			else if(mute)
			{
				mute = false;
			}
				
		}
		
		//pause game
		if ((keyState & FIRE_PRESSED) != 0 && !over)
		{
			
			if(!paused)
			{
				paused = true;
			}
		}
		
		//unpause game
		if ((keyState & UP_PRESSED) != 0 && paused)
		{
			paused = false;
		}
		
		//checks for collisions and ends the game if one is found, also stores the record if it is one
		if (((helicopterSprite.collidesWith(bgSprite_Top, true)) 
				|| (helicopterSprite.collidesWith(bgSprite_Bot, false)) 
				|| (helicopterSprite.collidesWith(fObstacleSprite, true))
				|| (helicopterSprite.collidesWith(sObstacleSprite, true))) && !over)
		{
			if(score > record) 
			{
				record = score;
			}
			mid.writeRecords(score);
			
			over = true;
			if(!mute) 
				{
					stopSounds();
					playExplosionSound();
				}
			sound = false;
			
		}
		
		//starts a new game after the copter crashed
		if ((keyState & RIGHT_PRESSED) != 0 && over)
		{
			
			first = true;
			reset();
			over = false;
			paused = false;
			obstacle = false;
		}
	}

	/**
	 * play coptersounds, while it raises
	 */
	private void playHelicopterSound()
	{	
		try {
			heliPlayer.setLoopCount(10);
			heliPlayer.realize(); 
			heliPlayer.prefetch();	
			heliPlayer.start();
		} catch (Exception e) {
			System.out.println("Fehler beim Abspielen des Coptersounds");
			e.printStackTrace();
		}
	}
	
	/**
	 * play an explosionsound, if the copter crashes
	 */
	private void playExplosionSound()
	{
		try {
			explPlayer.realize(); 
			explPlayer.prefetch();	
			explPlayer.start();
		} catch (Exception e) {
			System.out.println("Fehler beim Abspielen des Explosionssounds");
			e.printStackTrace();
		}
	}
	
	/**
	 * stop coptersounds
	 */
	private void stopSounds()
	{
		try {
			heliPlayer.deallocate();
			heliPlayer.stop();
		} catch (Exception e) {
			System.out.println("Fehler beim Anhalten des Coptersounds");
			e.printStackTrace();
		}
	}
	
	/**
	 * create the score'board' in the upper right corner and update it with every tick.
	 * it also updates the 'best'-label if there is already a record in the recordstore
	 * @param g
	 */
	private void createScoreboard(Graphics g) {
		if(!first && !paused && !over) score++;
		g.setFont(font);
		g.setColor(0xffffff);
		if(getRecords)
		{
			getRecords = false;
			try
			{
				records = mid.readOrderedRecords();
				record = records[0];
			}
			catch (Exception e) {
				System.out.println("Fehler beim Auslesen des RecordStores");
				e.printStackTrace();
			}
			
		}
		
		g.drawString("Score: "+score, getWidth()-15, 7, Graphics.TOP | Graphics.RIGHT);		
		g.drawString("Best: "+record, 13, 7, Graphics.TOP | Graphics.LEFT);	
		g.drawString("Level: "+level, getWidth()-15, getHeight()-25, Graphics.TOP | Graphics.RIGHT);		
	}
	
	/**
	 * moves the obstacles accordingly - only if game is running.
	 * obstacles are positioned randomly and the obstacleVelocity changes with the level
	 */
	private void moveObstacles() {
		if((score % 500) == 0 && score != 0)
		{
			this.level += 1;
			this.obstacleVel += 0.3;
		}
		if(!first && !paused && !over)
		{
			random = new Random();
			if(fObstacleX <= -32)
			{
				fObstacleX = getWidth();			
				fObstacleY = Math.abs(random.nextInt() % (getHeight()-128))+32;
			}
			if(sObstacleX <= -32)
			{
				sObstacleX = getWidth();				
				sObstacleY = Math.abs(random.nextInt() % (getHeight()-128))+32;
			}
			fObstacleX -= obstacleVel;
			if(fObstacleX <= (getWidth()/2)+40) 
				{
					obstacle = true;
				}
			if(obstacle) 
				{
					sObstacleX -= obstacleVel;
				}
		}
	}
	
	/**
	 * pauses the game, if it's not over - only used if the user is in the menu while the game is running
	 */
	public void pause()
	{
		if(!this.paused && !this.over)
		{
			this.paused = true;
		}
	}
	
	/**
	 * sets the sleep variable accordingly
	 * @param sleep how long?
	 */
	public void priority(int sleep)
	{
		this.sleep = sleep;
	}
	
	/**
	 * resets the game if the user decides to start a new one
	 */
	public void reset() {
		helicopterYUpVel = 1;
		obstacleVel = 2;
		helicopterX = (getWidth() / 2 )-67;
		helicopterY = (getHeight() / 2);
		score = 0;
		level = 1;
		fObstacleX = getWidth()+50;
		fObstacleY = getHeight()/2;
		sObstacleX = getWidth()+50;
		sObstacleY = (getHeight()/2)-50;
		helicopterSprite.setFrame(0);
		exploding = 20;
		first = true;
		paused = false;
		over = false;
		obstacle = false;
		sound = false;
		down = false;
		getRecords = true;
		try {
			heliPlayer.deallocate();
			heliPlayer.stop();
		} catch (MediaException e) {
			System.out.println("Fehler beim Anhalten des Coptersounds");
			e.printStackTrace();
		}
	}

	/**
	 * game loop
	 */
	public void run() {

		while (true) {
			updateScreen(getGraphics());
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {

			}
		}

	}

}