package org.uu.nl.goldenagents;

import ch.rasc.sse.eventbus.config.EnableSseEventBus;
import org.apache.jena.sys.JenaSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableSseEventBus
public class Application extends SpringBootServletInitializer {

	public static String[] ARGS;
	
    public static void main(String[] args) {
    	
    	ARGS = args;
    	
    	System.out.println("--- --- --- --- --- --- [BEGIN ARGS] --- --- --- --- --- --- --- ---");
    	for (String arg : args)
    	{
    	System.out.println("  " + arg);
    	}
    	System.out.println("--- --- --- --- --- --- [ END ARGS ] --- --- --- --- --- --- --- ---");

		//Workaround to prevent Jena from getting stuck at initialization of the system
		JenaSystem.init();

        SpringApplication.run(Application.class, args);

    }

}
