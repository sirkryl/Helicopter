import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * our MIDlet-class, opens the Splash Screen
 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
 *
 */
public class Helicopter extends MIDlet implements CommandListener {

	/** Commands **/
	private static final Command exitCommand = new Command("Exit", Command.EXIT, 1);
	private static final Command backCommand = new Command("Back", Command.BACK, 0);
	
	/** Helicopter Canvas **/
	public static HelicopterCanvas helicopterCanvas;
	private static RecordStore rs;
	
	/** Menu Canvas **/
	private static MenuCanvas menuCanvas;
	
	/** Display **/
	public static Display display;
	
	/**
	 * MIDlet constructor, inits the canvas
	 */
	public Helicopter() 
	{
		helicopterCanvas = new HelicopterCanvas(this);
		helicopterCanvas.addCommand(exitCommand);
		helicopterCanvas.addCommand(backCommand);
		helicopterCanvas.setCommandListener(this);
		menuCanvas = new MenuCanvas(this);
	}

	/**
	 * exit app
	 */
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException 
	{	
		if(true)
		{
			notifyDestroyed();
		}
		closeRecordStore();
	}


	/**
	 * pause app
	 */
	protected void pauseApp() {

	}
	/**
	 * start app, opens the Splash Screen and the RecordStore
	 */
	protected void startApp() throws MIDletStateChangeException 
	{
		display = Display.getDisplay(this);
		new SplashScreen(display, menuCanvas );
		openRecordStore();
	}
	
	/**
	 * manages RecordStore inserts, it always stores the 10 highest records
	 * @param record
	 */
	public void writeRecords(int record)
	{
		String recStr = String.valueOf(record);
		byte[] rec = recStr.getBytes();
		try 
		{
			
			if(rs.getNumRecords() == 10)
			{
				for (int i = 1; i <= rs.getNumRecords(); i++)
				{
					if(Integer.parseInt(new String(rs.getRecord(i))) < record)
					{
						rs.setRecord(i, rec, 0, rec.length);
						System.out.println("Rekord "+record +" gespeichert");
						break;
					}
				}
				
			}
			else
			{
				rs.addRecord(rec, 0, rec.length);
				System.out.println("Rekord "+record +" gespeichert");
			}
			
		}
		catch (Exception e)
		{
			System.out.println("Fehler beim Speichern der Highscore.");
			e.printStackTrace();
		}
	}
	
	/**
	 * reads the stored highscores from the RecordStore and returns a sorted array
	 * @return sorted int-array (max > x > min)
	 * @throws RecordStoreException 
	 */
	public int[] readOrderedRecords() throws RecordStoreException
	{
		int[] records = new int[10];

        for(int i = 1; i <= rs.getNumRecords(); i++)
        {
        	records[i-1] = Integer.parseInt(new String(rs.getRecord(i)));
        }
        
        //this is in fact a simple insertionsort-algorithm from Wikipedia
        for (int i = 0; i < records.length; i++) 
        {
	        int v = records[i];
	        int j;
	        for (j = i - 1; j >= 0; j--) 
	        {
	            if ((records[j] >= v)) break;
	            records[j + 1] = records[j];
	        }
	        records[j + 1] = v;
        }
        return records;
	}
	
	/**
	 * opens a recordstore or creates one, if none exists
	 */
	private void openRecordStore()
	{
		try {
			rs = RecordStore.openRecordStore("Records", true);
		} catch (RecordStoreException e) {
			System.out.println("Fehler beim Initialisieren des RecordStore.");
			e.printStackTrace();
		}
	}

	/**
	 * closes the recordstore
	 */
	private void closeRecordStore()
	{
		try {
			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			System.out.println("Fehler beim Schlieﬂen des RecordStore.");
			e.printStackTrace();
		}
	}
	
	/**
	 * exits the application if there is an exit command and goes back to the menu
	 * if 'back' is called while the game is running
	 */
	public void commandAction(Command arg0, Displayable arg1) {
		
		
		if (arg0 == exitCommand)
		{
			try {
				destroyApp(true);
			} catch (MIDletStateChangeException e) {
				System.out.println("Fehler beim Schlieﬂen der Anwendung");
				e.printStackTrace();
			}
		}
		
		if (arg0 == backCommand)
		{
			helicopterCanvas.priority(200);
			helicopterCanvas.pause();
			menuCanvas.showSvgMenu();
		}
		
	}
}
