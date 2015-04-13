import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import javax.microedition.midlet.MIDletStateChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGElement;

/**
 * EventListener for SVG objects
 * @author Christoph Huber, Stephan Herzog, Daniel Kececi
 */
public class SVGListener implements SVGEventListener 
{
		/** SVG declerations **/
        private Document doc;
        private Helicopter mid;
        
        /** game state **/
        private boolean started = true; 
        
        /** menu states **/
        private final int neuesSpiel = 0;
        private final int fortsetzen = 1;
        private final int hilfe = 2;
        private final int highscore = 3;
        private final int beenden = 4;
        
        /** current menu state **/
        private int curPosition = 0;
        private SVGElement curButton = null;
        
        /**
         * 
         * SVGListener constructor to init the listener
         * 
         * @param svgA
         * @param svgI
         * @param mid
         */
        public SVGListener(SVGAnimator svgA, SVGImage svgI, Helicopter mid) 
        {
        	this.mid = mid;
        	//svgAnimator = svgA;
        	doc = svgI.getDocument();
        	//svgElement = (SVGElement) doc.getDocumentElement();
        	curButton = (SVGElement) doc.getElementById("currentButton");
        }

        public void keyPressed(int keystate) 
        {     
        	/** Key Enter **/
        	if(keystate == -5) 
        	{
                switch(curPosition) 
                {
                case neuesSpiel:
                	if(started)
                	{
                		Helicopter.helicopterCanvas.start();
                		started = false;
                        System.out.println("Neues Spiel wird gestartet.");
                	}
                	Helicopter.helicopterCanvas.priority(20);
                	Helicopter.helicopterCanvas.reset();
                	Helicopter.display.setCurrent(Helicopter.helicopterCanvas);	
                	break;
                        
                case fortsetzen:
                    System.out.println("Spiel wird fortgesetzt.");
                    if(started)
                    {
                    	Helicopter.helicopterCanvas.start();
                		started = false;
                        System.out.println("Kein angefangenes Spiel -> Neues Spiel wird gestartet.");
                    }
                    Helicopter.helicopterCanvas.priority(20);
                    Helicopter.helicopterCanvas.pause();
                    Helicopter.display.setCurrent(Helicopter.helicopterCanvas);
                    break;

                case hilfe:
                	System.out.println("Hilfe wird gestartet.");
                	MenuCanvas.showHelpScreen();
                	break;
                        
                case highscore:
                	System.out.println("Highscore wird angezeigt.");
                	MenuCanvas.showHighScoreScreen();
                	break;
                        
                case beenden:
                	System.out.println("Spiel wird beendet.");
	                try
	                {
	                	mid.destroyApp(true);
	                }
	                catch(MIDletStateChangeException e) 
	                {
	                	e.printStackTrace();
	                }
                	break;
                }
        	}
        	/** Key Up **/	
        	if(keystate == -1) 
        	{
                switch(curPosition) {
                case fortsetzen:
                        curPosition = neuesSpiel;
                        curButton.setTrait("y", "60");
                        break;
                        
                case hilfe:
                        curPosition = fortsetzen;
                        curButton.setTrait("y", "105");
                        break;
                        
                case highscore:
                        curPosition = hilfe;
                        curButton.setTrait("y", "150");
                        break;
                        
                case beenden:
                        curPosition = highscore;
                        curButton.setTrait("y", "195");
                        break;                          
                }
        	}
        	/** Key Down **/
        	if(keystate == -2) 
        	{
                switch(curPosition) {
                case neuesSpiel:
                        curPosition = fortsetzen;
                        curButton.setTrait("y", "105");
                        break;
                
                case fortsetzen:
                        curPosition = hilfe;
                        curButton.setTrait("y", "150");
                        break;
                        
                case hilfe:
                        curPosition = highscore;
                        curButton.setTrait("y", "195");
                        break;
                        
                case highscore:
                        curPosition = beenden;
                        curButton.setTrait("y", "240");
                        break;                          
                }
                
        }

        }
        
        public void hideNotify() {
                // TODO Auto-generated method stub

        }

        public void keyReleased(int keystate) {
                // TODO Auto-generated method stub

        }

        public void pointerPressed(int arg0, int arg1) {
                // TODO Auto-generated method stub

        }

        public void pointerReleased(int arg0, int arg1) {
                // TODO Auto-generated method stub

        }

        public void showNotify() {
                // TODO Auto-generated method stub

        }

        public void sizeChanged(int arg0, int arg1) {
                // TODO Auto-generated method stub

        }
}