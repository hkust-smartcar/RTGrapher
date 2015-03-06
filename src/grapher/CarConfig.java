package grapher;

public class CarConfig {
	CarConfig(){
		c_voltmeterVoltageRatio	=	1;
		c_servoAngleLowerBound	=	0;
		c_servoAngleUpperBound	=	1800;
		c_gearRatio				=	2.625f;
		c_wheelDiameter			=	0.0535f;
		c_magneticSensorCount	=	2;
		c_encoderCountPerRevolution	=	500;
	};
	static final int	MAX_LED_COUNT				=		4;
	static final int	MAX_MAGNETIC_SENSOR_COUNT	=		4;
	
	boolean				c_halt;
	
	volatile	boolean	c_mode;
	
	volatile	int		c_ledCount;
	
	final		float	c_voltmeterVoltageRatio;
	
	volatile	int		c_loop_interval;
	
	volatile	int		c_servoAngleMultiplier;
	volatile 	int 	c_servoAngle;
	final 	 	int		c_servoAngleLowerBound;
	final	 	int		c_servoAngleUpperBound;
	volatile 	int 	c_motorPower;
	volatile 	boolean c_motorRotateClockwise;
	
	final 	 int 		c_magneticSensorCount;
	volatile int 	  	c_magneticSensorLowerBound;
	volatile int 	  	c_magneticSensorUpperBound;

	volatile boolean 	c_motorPIDEnabled;
	volatile float[] 	c_motorPIDControlVariable;
	volatile float 	  	c_motorPIDSp;
	volatile boolean	c_motorPIDUseAutomat;

	volatile int		c_bluetoothRXThreshold;
	volatile boolean 	c_broadcastMotorPower;
	volatile boolean	c_broadcastServoAngle;
	volatile boolean	c_broadcastSensorReading;
	volatile boolean	c_broadcastPIDControlVariable;
	volatile boolean	c_broadcastPIDSp;
	volatile boolean	c_broadcastEncoderReading;
	volatile boolean	c_broadcastSpeed;

	volatile boolean	c_useKalmanFilter;
	volatile float[]	c_kalmanFilterControlVariable;
	volatile float		c_signalTriggerThreshold;

	final	 float		c_wheelDiameter;	//in m
	final	 int		c_encoderCountPerRevolution;
	final	 float		c_gearRatio;		//Tire gear/motor
}
