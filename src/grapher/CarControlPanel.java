package grapher;

import gnu.io.CommPortIdentifier;
import grapher.BluetoothSocket.TaggedData;
import grapher.LineComponent.PositionTag;
import grapher.LineComponent.Line;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridLayout;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JList;

import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.ListSelectionModel;

import java.awt.TextArea;

import javax.swing.JTextArea;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

import java.awt.Color;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;



public class CarControlPanel {
	
	private Grapher				  grapher;
	private	CarConfig			  custom;
	private final CarConfig		  defaultConfig=new CarConfig();
	private BluetoothSocket		  btSocket;
	private FunctionMapConfig 	  mapConfig;
	private RandomSignalGenerator signalGenerator;
	private Thread			  	  listener;
	private boolean				  stopListening;
	private boolean				  useFileModule		=false;
	private String 				  PATH				="/dev/tty.SCTRAVIS-DevB";
	private FileMonitor 		  fileMonitor;
	private Thread				  fileMonitorThread;
	public static volatile Queue<TaggedData> dataQueue;
	
	private JFrame 			frmSmartCarControl;
	private JTextField 		loopIntervalField;
	private JTextField 		motorKpField;
	private JTextField 		motorKiField;
	private JTextField 		motorKdField;
	private JTextField 		kalmanQField;
	private JTextField 		kalmanRField;
	private	JButton 		btnConnect;
	private	JLabel 			lblPower;
	private JPanel 			graphPanel;
	private JSlider 		powerSlider;
	private JLabel 			lblServo;
	private JSlider 		servoSlider;
	private JToggleButton 	haltToggle;
	private JLabel 			lblLoopInterval;
	private JCheckBox 		chckbxMotorPower;
	private JCheckBox 		chckbxServoPower;
	private JToggleButton 	motorPIDToggle;
	private JLabel 			lblKd;
	private JLabel 			lblKp;
	private JLabel 			lblKi;
	private JToggleButton	kalmanFilterToggle;
	private JLabel 			lblQ;
	private JLabel 			lblR;
	private JButton 		btnReset;
	private JButton 		btnApplied;
	private JLabel 			noticeLabel;
	private JPanel			trackPanel;
	private LineComponent	lineComponent;
//	private JScrollPane		deviceListScrollPane;
	private JLabel 			lblDeviceConnectState;
	private JScrollPane 	deviceScrollPane;
	private JList 			deviceList;
	private DefaultListModel listModel;
	private JButton btnRefresh;
	private JCheckBox chckbxEncoderCount;
	private JCheckBox chckbxMagneticSensorReading;
	private JCheckBox chckbxCarSpeed;
	private JTextField xCoor;
	private JTextField yCoor;
	private JLabel lblReference;
	private JButton btnRefSet;
	private JLabel lblMotor;
	private JLabel lblServo_1;
	private JLabel lblKp_1;
	private JLabel lblKi_1;
	private JLabel lblKd_1;
	private JTextField servoKpField;
	private JTextField servoKiField;
	private JTextField servoKdField;
	private JButton btnIntervalSet;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CarControlPanel window = new CarControlPanel();
					window.frmSmartCarControl.setVisible(true);
					window.waitString();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public void waitString()
	{
//		try{
//			fileMonitor.wait();
//		}catch(InterruptedException e)
//		{
//			noticeLabel.setText(fileMonitor.data);
//		}
	}
	public void writeMessage(int cmd, float value)
	{
		if(fileMonitor!=null)
		{
			fileMonitor.income="$"+cmd+" "+value;
		}
	}
	public CarControlPanel() {
		initialize();
		btSocket=new BluetoothSocket();
		btSocket.masterPanel=this;
		custom	=new CarConfig();
		grapher	=new Grapher();
		lineComponent=new LineComponent();
		lineComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lineComponent.mouseX=e.getX();
				lineComponent.mouseY=e.getY();
				if(e.getButton()==MouseEvent.BUTTON3)
				{
					lineComponent.isGroupSelecting=true;
					lineComponent.anX=e.getX();
					lineComponent.anY=e.getY();
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				int mx=e.getX(),my=e.getY();
				int refX=lineComponent.refX;
				int refY=lineComponent.refY;
				boolean hasLineSelected=false;
				for(Line line : lineComponent.lines)
				{
					int x1=line.x1+refX,
						y1=line.y1+refY,
						x2=line.x2+refX,
						y2=line.y2+refY;
//					System.out.println(Math.abs(((mx-x1)*(y2-y1)-(my-y1)*(x2-x1)))); //for distance tracing
					if(((x1<=mx&&mx<=x2)||(x2<=mx&&mx<=x1))
							&&Math.abs(((mx-x1)*(y2-y1)-(my-y1)*(x2-x1)))<=100)
					{
						lineComponent.selectedLine.add(line);
						lineComponent.repaint();
						hasLineSelected=true;
						break;
					}
				}
				if(!hasLineSelected)
				{
					lineComponent.selectedLine.clear();
					lineComponent.repaint();
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) 
			{
				lineComponent.mouseEntered=true;
				lineComponent.repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) 
			{
				lineComponent.mouseEntered=false;
				lineComponent.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				lineComponent.isGroupSelecting=false;
			}
		});
		lineComponent.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1)
				{
					lineComponent.refX+=(e.getX()-lineComponent.mouseX);
					lineComponent.refY+=(e.getY()-lineComponent.mouseY);
				}
				lineComponent.mouseX=e.getX();
				lineComponent.mouseY=e.getY();
				if(e.getButton()==MouseEvent.BUTTON3)
				{
					for(Line line: lineComponent.lines)
					{
						int x1=line.x1;
						int y1=line.y1;
						int x2=line.x2;
						int y2=line.y2;
						int mx=lineComponent.mouseX;
						int my=lineComponent.mouseY;
						int anX=lineComponent.anX;
						int anY=lineComponent.anY;
						boolean hasLineSelected=false;
						if((mx-x1)*(y2-y1)==(my-y1)*(x2-x1))
						{
							lineComponent.selectedLine.add(line);
							lineComponent.repaint();
							hasLineSelected=true;
							break;
						}
					}

				}
				lineComponent.repaint();
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				lineComponent.mouseX=e.getX();
				lineComponent.mouseY=e.getY();
				lineComponent.repaint();
			}
		});
		grapher.setDelay(Integer.parseInt(loopIntervalField.getText()));
		graphPanel.add(grapher.getChartPanel());
		//TODO the file monitor is initialized here
		fileMonitor=new FileMonitor(PATH,this);
//		fileMonitorThread=new Thread(fileMonitor);
		
		chckbxEncoderCount = new JCheckBox("Encoder Count");
		chckbxEncoderCount.setBounds(333, 255, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxEncoderCount);
		
		chckbxMagneticSensorReading = new JCheckBox("Magnetic Sensor");
		chckbxMagneticSensorReading.setBounds(333, 280, 186, 23);
		frmSmartCarControl.getContentPane().add(chckbxMagneticSensorReading);
		
		chckbxCarSpeed = new JCheckBox("Car Speed");
		chckbxCarSpeed.setBounds(333, 305, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxCarSpeed);
		
		noticeLabel = new JLabel("");
		noticeLabel.setBounds(172, 324, 166, 16);
		frmSmartCarControl.getContentPane().add(noticeLabel);
		
		xCoor = new JTextField();
		xCoor.setBounds(191, 364, 42, 28);
		frmSmartCarControl.getContentPane().add(xCoor);
		xCoor.setColumns(10);
		
		yCoor = new JTextField();
		yCoor.setBounds(278, 364, 51, 28);
		frmSmartCarControl.getContentPane().add(yCoor);
		yCoor.setColumns(10);
		
		JLabel lblX = new JLabel("x:");
		lblX.setBounds(172, 370, 23, 16);
		frmSmartCarControl.getContentPane().add(lblX);
		
		JLabel lblY = new JLabel("y:");
		lblY.setBounds(255, 370, 23, 16);
		frmSmartCarControl.getContentPane().add(lblY);
		
		JButton btnTrace = new JButton("Trace");
		btnTrace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					btSocket.writeData("LCD_TOGGLE_PAGE", "0");
					lineComponent.addLine(Integer.parseInt(xCoor.getText()),Integer.parseInt(yCoor.getText()));
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		btnTrace.setBounds(16, 365, 75, 29);
		frmSmartCarControl.getContentPane().add(btnTrace);
		
		trackPanel = new JPanel();
		trackPanel.setBackground(Color.WHITE);
		trackPanel.add(lineComponent);
		lineComponent.setBounds(16, 404, 598, 187);
		lineComponent.setPreferredSize(new Dimension(598,187));
		trackPanel.setBounds(16, 404, 598, 187);
		trackPanel.setPreferredSize(new Dimension(598,187));
		frmSmartCarControl.getContentPane().add(trackPanel);
		
		lblReference = new JLabel("Reference:");
		lblReference.setBounds(186, 336, 82, 16);
		frmSmartCarControl.getContentPane().add(lblReference);
		
		btnRefSet = new JButton("Set");
		btnRefSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					int x=Integer.parseInt(xCoor.getText());
					int y=Integer.parseInt(yCoor.getText());
					lineComponent.setReferenceCoordinate(x, y);
				}catch(NumberFormatException e1)
				{
					e1.printStackTrace();
					return;
				}
			}
		});
		btnRefSet.setBounds(278, 334, 51, 29);
		frmSmartCarControl.getContentPane().add(btnRefSet);
		
		lblMotor = new JLabel("Motor:");
		lblMotor.setBounds(16, 240, 61, 16);
		frmSmartCarControl.getContentPane().add(lblMotor);
		
		lblServo_1 = new JLabel("Servo:");
		lblServo_1.setBounds(101, 240, 61, 16);
		frmSmartCarControl.getContentPane().add(lblServo_1);
		
		lblKp_1 = new JLabel("Kp:");
		lblKp_1.setBounds(107, 262, 23, 16);
		frmSmartCarControl.getContentPane().add(lblKp_1);
		
		lblKi_1 = new JLabel("Ki:");
		lblKi_1.setBounds(107, 299, 23, 16);
		frmSmartCarControl.getContentPane().add(lblKi_1);
		
		lblKd_1 = new JLabel("Kd:");
		lblKd_1.setBounds(108, 336, 29, 16);
		frmSmartCarControl.getContentPane().add(lblKd_1);
		
		servoKpField = new JTextField();
		servoKpField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("SERVO_PID_KP", servoKpField.getText());

			}
		});
		servoKpField.setText("0");
		servoKpField.setBounds(142, 253, 32, 28);
		frmSmartCarControl.getContentPane().add(servoKpField);
		servoKpField.setColumns(10);
		
		servoKiField = new JTextField();
		servoKiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("SERVO_PID_KI", servoKiField.getText());
			}
		});
		servoKiField.setText("0");
		servoKiField.setBounds(142, 293, 32, 28);
		frmSmartCarControl.getContentPane().add(servoKiField);
		servoKiField.setColumns(10);
		
		servoKdField = new JTextField();
		servoKdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("SERVO_PID_KD", servoKdField.getText());
			}
		});
		servoKdField.setText("0");
		servoKdField.setBounds(142, 330, 32, 28);
		frmSmartCarControl.getContentPane().add(servoKdField);
		servoKdField.setColumns(10);
		
		btnIntervalSet = new JButton("Set");
		btnIntervalSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					grapher.setDelay(Integer.parseInt(loopIntervalField.getText()));
				}catch(NumberFormatException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		btnIntervalSet.setBounds(229, 6, 51, 29);
		frmSmartCarControl.getContentPane().add(btnIntervalSet);
		
		JButton btnResetView = new JButton("Center");
		btnResetView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lineComponent.refX=lineComponent.drefX;
				lineComponent.refY=lineComponent.drefY;
				lineComponent.repaint();
			}
		});
		btnResetView.setBounds(85, 365, 75, 29);
		frmSmartCarControl.getContentPane().add(btnResetView);
		
		JButton servoPIDToggle = new JButton("Disable");
		servoPIDToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(servoPIDToggle.getText().equals("Disable"))
				{
					servoPIDToggle.setText("Enable");
				}else
				{
					servoPIDToggle.setText("Disable");
				}
				btSocket.writeData("SERVO_PID_TOGGLE", "0");
			}
		});
		servoPIDToggle.setBounds(89, 212, 75, 29);
		frmSmartCarControl.getContentPane().add(servoPIDToggle);
		
		JCheckBox motorReverse = new JCheckBox("Reverse");
		motorReverse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("MOTOR_REVERSE", "0");
			}
		});
//		motorReverse.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				
//			}
//		});
		motorReverse.setBounds(16, 184, 82, 23);
		frmSmartCarControl.getContentPane().add(motorReverse);
		grapher.start();
		
		signalGenerator = new RandomSignalGenerator(300);
//		listener =new Thread(new Runnable()
//		{
//			public void run()
//			{
////				switch(btSocket.getReadFlag())
////				{
////					case 0:
////						break;
////					case 1:
////						BluetoothSocket.TaggedData data
////						=btSocket.extractData();
////						if(data!=null)
////						{
////							grapher.addData(data.tag, data.data);
////						}
////						break;
////					case 2:
////						writeNotice("Incoming data mismatch");
////						break;
////					case 3:
////						writeNotice("Bluetooth socket queue overflow");
////						break;
////					default:
////						writeNotice("Bluetooth socket encounters unknown error");
////						break;
////				}
//				while(!stopListening)
//				{
//				if(btSocket.dataAvailable())
//				{
//					BluetoothSocket.TaggedData data
//					=btSocket.extractData();
//					if(data!=null)
//					{
//						grapher.addData(data.tag, data.data);
//					}
//				}
//			}
//			}});
		refreshDeviceList();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSmartCarControl = new JFrame();
		frmSmartCarControl.setTitle("Smart Car Control Panel");
		frmSmartCarControl.setResizable(false);
		frmSmartCarControl.setBounds(100, 100, 620, 630);
		frmSmartCarControl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSmartCarControl.getContentPane().setLayout(null);
		
		graphPanel = new JPanel();
		graphPanel.setBounds(323, 6, 291, 187);
		frmSmartCarControl.getContentPane().add(graphPanel);
		
		powerSlider = new JSlider();
		powerSlider.setValue(500);
		powerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source=(JSlider)e.getSource();
				if(btSocket!=null&&source!=null)
				btSocket.writeData("MOTOR_POWER", Integer.toString(source.getValue()));
			}
		});
		powerSlider.setPaintTicks(true);
		powerSlider.setMajorTickSpacing(100);
		powerSlider.setValue(500);
		powerSlider.setPaintLabels(true);
		powerSlider.setMaximum(1000);
		powerSlider.setOrientation(SwingConstants.VERTICAL);
		powerSlider.setBounds(16, 54, 58, 139);
		frmSmartCarControl.getContentPane().add(powerSlider);
		
		lblPower = new JLabel("Power:");
		lblPower.setBounds(18, 41, 61, 16);
		frmSmartCarControl.getContentPane().add(lblPower);
		
		lblServo = new JLabel("Servo:");
		lblServo.setBounds(69, 41, 61, 16);
		frmSmartCarControl.getContentPane().add(lblServo);
		
		servoSlider = new JSlider();
		servoSlider.setValue(900);
		servoSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source=(JSlider)e.getSource();
				if(btSocket!=null&&source!=null)
				{
					btSocket.writeData("SERVO_ANGLE", Integer.toString(source.getValue()));
				}
			}
		});
		servoSlider.setPaintTicks(true);
		servoSlider.setMajorTickSpacing(180);
		servoSlider.setOrientation(SwingConstants.VERTICAL);
		servoSlider.setPaintLabels(true);
		servoSlider.setValue(900);
		servoSlider.setMaximum(1800);
		servoSlider.setBounds(79, 54, 58, 139);
		frmSmartCarControl.getContentPane().add(servoSlider);
		
		haltToggle = new JToggleButton("halt");
		haltToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(haltToggle.isSelected())
				{
					haltToggle.setText("Unhalt");
					btSocket.writeData("HALT", "0");

				}else
				{
					haltToggle.setText("halt");
					btSocket.writeData("HALT", "0");
				}
			}
		});
		haltToggle.setBounds(6, 6, 75, 29);
		frmSmartCarControl.getContentPane().add(haltToggle);
		
		loopIntervalField = new JTextField();
		loopIntervalField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				custom.c_loop_interval=
						Integer.parseInt(loopIntervalField.getText());
			}
		});
		loopIntervalField.setText("50");
		loopIntervalField.setBounds(172, 6, 51, 28);
		frmSmartCarControl.getContentPane().add(loopIntervalField);
		loopIntervalField.setColumns(10);
		
		lblLoopInterval = new JLabel("Loop Interval:");
		lblLoopInterval.setBounds(86, 11, 93, 16);
		frmSmartCarControl.getContentPane().add(lblLoopInterval);
		
		motorPIDToggle = new JToggleButton("Disable");
		motorPIDToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(motorPIDToggle.getText().equals("Disable"))
				{
					motorPIDToggle.setText("Enable");
				}else
				{
					motorPIDToggle.setText("Disable");
				}
				btSocket.writeData("MOTOR_PID_TOGGLE", "0");
			}
		});
//		motorPIDToggle.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				if(motorPIDToggle.isSelected())
//				{
//					motorPIDToggle.setText("Enable Motor PID");
//					btSocket.writeData("MOTOR_PID_TOGGLE", "0");
//				}else
//				{
//					motorPIDToggle.setText("Disable Motor PID");
//				}
//			}
//		});
		motorPIDToggle.setBounds(6, 212, 71, 29);
		frmSmartCarControl.getContentPane().add(motorPIDToggle);
		
		motorKpField = new JTextField();
		motorKpField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				custom.c_motorPIDControlVariable[0]=
//						Integer.parseInt(motorKpField.getText());
				btSocket.writeData("MOTOR_PID_KP",motorKpField.getText());
			}
		});
		motorKpField.setText("0");
		motorKpField.setBounds(57, 253, 32, 28);
		frmSmartCarControl.getContentPane().add(motorKpField);
		motorKpField.setColumns(10);
		
		lblKp = new JLabel("Kp:");
		lblKp.setBounds(28, 262, 61, 16);
		frmSmartCarControl.getContentPane().add(lblKp);
		
		lblKi = new JLabel("Ki:");
		lblKi.setBounds(28, 299, 23, 16);
		frmSmartCarControl.getContentPane().add(lblKi);
		
		motorKiField = new JTextField();
		motorKiField.setText("0");
		motorKiField.setColumns(10);
		motorKiField.setBounds(57, 293, 32, 28);
		motorKiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				custom.c_motorPIDControlVariable[1]=
//						Integer.parseInt(motorKiField.getText());
				btSocket.writeData("MOTOR_PID_KI", motorKiField.getText());
			}
		});
		frmSmartCarControl.getContentPane().add(motorKiField);
		
		lblKd = new JLabel("Kd:");
		lblKd.setBounds(28, 336, 61, 16);
		frmSmartCarControl.getContentPane().add(lblKd);
		
		motorKdField = new JTextField();
		motorKdField.setText("0");
		motorKdField.setColumns(10);
		motorKdField.setBounds(57, 330, 32, 28);
		motorKdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				custom.c_motorPIDControlVariable[2]=
//						Integer.parseInt(motorKdField.getText());
				btSocket.writeData("MOTOR_PID_KD", motorKdField.getText());
			}
		});
		frmSmartCarControl.getContentPane().add(motorKdField);
		
		chckbxMotorPower = new JCheckBox("Motor Power");
		chckbxMotorPower.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO the following seems hard to implement...
				if(chckbxMotorPower.isSelected())
				{
					
				}
			}
		});
		chckbxMotorPower.setBounds(333, 205, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxMotorPower);
		
		chckbxServoPower = new JCheckBox("Servo Angle");
		chckbxServoPower.setBounds(333, 229, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxServoPower);
		
		kalmanFilterToggle = new JToggleButton("Disable Kalman Filter");
		kalmanFilterToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(kalmanFilterToggle.getText().equals("Disable Kalman Filter"))
				{
					kalmanFilterToggle.setText("Enable Kalman Filter");
				}else
				{
					kalmanFilterToggle.setText("Disable Kalman Filter");
				}
				btSocket.writeData("KALMAN_FILTER_TOGGLE","0");
			}
		});
		kalmanFilterToggle.setBounds(172, 212, 161, 29);
//		kalmanFilterToggle.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				if(motorPIDToggle.isSelected())
//				{
//					kalmanFilterToggle.setText("Disable Kalman Filter");
//				}else
//				{
//					kalmanFilterToggle.setText("Enable Kalman Filter");
//				}
//				btSocket.writeData("KALMAN_FILTER_TOGGLE", "0");
//			}
//		});
		frmSmartCarControl.getContentPane().add(kalmanFilterToggle);
		
		kalmanQField = new JTextField();
		kalmanQField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("KALMAN_FILTER_Q", kalmanQField.getText());
			}
		});
		kalmanQField.setText("0");
		kalmanQField.setColumns(10);
		kalmanQField.setBounds(249, 253, 51, 28);
		frmSmartCarControl.getContentPane().add(kalmanQField);
		
		kalmanRField = new JTextField();
		kalmanRField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btSocket.writeData("KALMAN_FILTER_R", kalmanRField.getText());

			}
		});
		kalmanRField.setText("0");
		kalmanRField.setColumns(10);
		kalmanRField.setBounds(249, 293, 51, 28);
		frmSmartCarControl.getContentPane().add(kalmanRField);
		
		lblQ = new JLabel("Q:");
		lblQ.setBounds(176, 259, 61, 16);
		frmSmartCarControl.getContentPane().add(lblQ);
		
		lblR = new JLabel("R:");
		lblR.setBounds(176, 299, 61, 16);
		frmSmartCarControl.getContentPane().add(lblR);
		
		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetConfig();
			}
		});
		btnReset.setBounds(380, 348, 117, 29);
		frmSmartCarControl.getContentPane().add(btnReset);
		
		btnApplied = new JButton("Apply");
		btnApplied.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateConfig();
				broadcastChanges();
				//TODO for test only
				grapher.addData(1, signalGenerator.randomValue());
//				DefaultListModel tempModel=(DefaultListModel)deviceList.getModel();
//				tempModel.addElement("Test");
//				deviceList.setModel(tempModel);
			}
		});
		btnApplied.setBounds(497, 348, 117, 29);
		frmSmartCarControl.getContentPane().add(btnApplied);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!btSocket.isConnected())
				{
					String selectedDeviceName=(String)deviceList.getSelectedValue();
					//TODO this is the place where I decide which module to use
					if(useFileModule)
					{
//						fileMonitor=new FileMonitor(PATH,this);
						if(!fileMonitor.isAlive())
						fileMonitor.start();
					}else
					{
						btSocket.connectToPort(selectedDeviceName);
						handleSocketState();
					}
					stopListening=false;
					refreshListener();
				}
				//TODO The disconnect function is not available. The program crashes whenever one tries to disconnect from the port.
				//	   This may because of the bug inside rxtx library.
				//	   
				else	
				{
					btSocket.disconnect();
					if(useFileModule)
					{
						if(fileMonitorThread.isAlive())
						{
							fileMonitorThread=null;
						}
					}
					handleSocketState();
					stopListening=true;
					btnConnect.setText("Connect");
				}
			}
		});
		btnConnect.setBounds(155, 164, 82, 29);
		frmSmartCarControl.getContentPane().add(btnConnect);
		
		lblDeviceConnectState = new JLabel("Not Connected");
		lblDeviceConnectState.setBounds(163, 38, 137, 16);
		frmSmartCarControl.getContentPane().add(lblDeviceConnectState);
		
		deviceScrollPane = new JScrollPane();
		deviceScrollPane.setBounds(164, 54, 147, 113);
		frmSmartCarControl.getContentPane().add(deviceScrollPane);
		
		listModel=new DefaultListModel<String>();
		deviceList = new JList(listModel);
		deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		deviceScrollPane.setViewportView(deviceList);
		
		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshDeviceList();
			}
		});
		btnRefresh.setBounds(229, 164, 82, 29);
		frmSmartCarControl.getContentPane().add(btnRefresh);
		
	}
	
	void refreshDeviceList()
	{
		listModel.removeAllElements();
		DefaultListModel<String> tempModel=(DefaultListModel<String>)deviceList.getModel();
		btSocket.searchForPorts();
//		Enumeration portList=btSocket.getPortList();
//		while(portList.hasMoreElements())
//		{
//			CommPortIdentifier curPort = (CommPortIdentifier)portList.nextElement();
////			listModel.addElement(curPort.getName());
//			tempModel.addElement(curPort.getName());
//		}
		HashMap<String, CommPortIdentifier> map= 
				new HashMap<String, CommPortIdentifier>(btSocket.getPortHashMap());
		java.util.Iterator<java.util.Map.Entry<String, CommPortIdentifier>> it
											   =map.entrySet().iterator();
		while(it.hasNext())
		{
			tempModel.addElement(it.next().getKey());
		}
		deviceList.setModel(tempModel);
		
	}
	
	void handleSocketState()
	{
		switch(btSocket.getState())
		{
			case 0:
				lblDeviceConnectState.setText("Not Connected");
				break;
			case 1:
				lblDeviceConnectState.setText("Connection Failed.");
				break;
			case 2:
				lblDeviceConnectState.setText("Port is in use");
				break;
			case 3:
				lblDeviceConnectState.setText("Connected");
				btnConnect.setText("Disconnect");
				break;
			case 4:
				lblDeviceConnectState.setText("Fail to create I/O");
				break;
			case 5:
				lblDeviceConnectState.setText("Too many listeners");
				break;
			case 6:
				lblDeviceConnectState.setText("Fail to close port");
				break;
			case 7:
				lblDeviceConnectState.setText("Fail to get data");
				break;
			case 8:
				lblDeviceConnectState.setText("Fail to write data");
				break;
			default:
				lblDeviceConnectState.setText("Unknown Error");
				break;		
		}
	}
	void resetConfig()
	{
		custom=defaultConfig;
	}
	void updateConfig()
	{
		
	}
	void broadcastChanges()
	{
		
	}
	public void writeNotice(String notice)
	{
		noticeLabel.setText(notice);
	}
	public void refreshListener()
	{
//		stopListening=false;
//		listener=new Thread(new Runnable(){
//			public void run()
//			{
//				while(!stopListening)
//				{
//					if(btSocket.dataAvailable())
//					{
//						BluetoothSocket.TaggedData data
//						=btSocket.extractData();
//						if(data!=null)
//						{
//							System.out.println(data.tag+" "+data.data);
//							grapher.addData(data.tag, data.data);
//						}
//					}
//				}
//			}
//		});
//		listener.start();
	}
	public void addData(TaggedData data)
	{
		grapher.addData(data.tag,data.data);
	}
	public void addData(String data)
	{
		StringTokenizer st=new StringTokenizer(data);
		int i=0;
		/*
		 * Coordinate case: when incoming data is something like (x,y)
		 */
		if(data.substring(0, 1)=="(")
		{
			data=data.substring(0, data.length()-1);
			st=new StringTokenizer(data);
			if(st.countTokens()!=2)return;
			else {
				{
					try{
						int x=Integer.parseInt(st.nextToken(","));
						int y=Integer.parseInt(st.nextToken(","));
						lineComponent.addLine(x,y);
						return;
					}catch(NumberFormatException e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
		}
		
		while(st.hasMoreTokens())
		{
			String token=st.nextToken(" ");
			float data1;
			try {
				data1=Float.parseFloat(token);
			} catch (NumberFormatException e) {
				// TODO: handle exception
				e.printStackTrace();
				i++;
				continue;
			}
			grapher.addData(i, data1);
			i++;
		}
	}
}
