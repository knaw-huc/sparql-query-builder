package org.uu.nl.goldenagents;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.uu.nl.net2apl.core.logging.Loggable;

/**
 * Translator class that converts logging output from NET2APL to the java.util.logging used in Golden Agents
 * @author Jurian Baas
 *
 */
public class GoldenLogger extends Loggable {
	
	/**
	 * Alternative to using log() method with Level.WARNING 
	 * @param c The class that generated the message
	 * @param message The object to log. <code>toString()</code> will be called
	 */
	public void warning(Class<?> c, Object message) {
		log(c, Level.WARNING, message);
	}
	
	/**
	 * Alternative to using log() method with Level.INFO 
	 * @param c The class that generated the message
	 * @param message The object to log. <code>toString()</code> will be called
	 */
	public void info(Class<?> c, Object message) {
		log(c, Level.INFO, message);
	}
	
	/**
	 * Alternative to using log() method with Level.SEVERE 
	 * @param c The class that generated the message
	 * @param message The object to log. <code>toString()</code> will be called
	 */
	public void severe(Class<?> c, Object message) {
		log(c, Level.SEVERE, message);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public Logger getLogger(Class<?> c) {
		return Logger.getLogger(c.getName());
	}
	
	@Override
	public void log(Class<?> c, Level level, Object message) {
		
		/**
		 * Each class has a logger in the java.util.logging scheme
		 */
		Logger logger = Logger.getLogger(c.getName());
		logger.log(level, message.toString());
	}

}
