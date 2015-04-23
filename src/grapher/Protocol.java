package grapher;

import java.util.HashMap;

public class Protocol {
		Protocol()
		{
			/*
			 * Command mapping
			 */
			commandMap=new HashMap<String,Integer>();
			commandMap.put("LCD_TOGGLE_PAGE", 1);
			commandMap.put("LCD_SUSPEND"	, 2);
			commandMap.put("MOTOR_PID_KP"	, 3);
			commandMap.put("MOTOR_PID_KI"	, 4);
			commandMap.put("MOTOR_PID_KD"	, 5);
			commandMap.put("MOTOR_POWER"	, 6);
			commandMap.put("SERVO_PID_KP"	, 7);
			commandMap.put("SERVO_PID_KI"	, 8);
			commandMap.put("SERVO_PID_KD"	, 9);
			commandMap.put("SERVO_ANGLE"	, 10);
			commandMap.put("KALMAN_FILTER_Q", 11);
			commandMap.put("KALMAN_FILTER_R", 12);
			commandMap.put("MOTOR_PID_TOGGLE", 13);
			commandMap.put("SERVO_PID_TOGGLE", 14);
			commandMap.put("KALMAN_FILTER_TOGGLE", 15);
			commandMap.put("TRACE"			, 16);
			commandMap.put("HALT"			,-2);
			commandMap.put("MOTOR_REVERSE"	,-3);
			/*
			 * end of mapping
			 */
		}
	 	final static char 		  SPACE_ASCII 		= ' ';
	    final static int 		  DASH_ASCII 		= 45;
	    final static int 		  NEW_LINE_ASCII 	= 10;
	    final static char		  SIGNAL			= '$';
	    final static char		  DELIM				= 't';
	    HashMap<String,Integer>	  commandMap;
	    
}
