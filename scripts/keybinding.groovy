/* :name=SAMPLE - Key Binding :description=Example of keybinding event
 * 
 * @author  
 * @date    
 * @version 
 */
 
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
manager.addKeyEventDispatcher(new KeyEventDispatcher(){
  public boolean dispatchKeyEvent(KeyEvent e)
  {
	try {
		System.out.println("Youhou!");
	}
	finally {
	}
    return true;
  }
});



