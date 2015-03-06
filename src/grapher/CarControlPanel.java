package grapher;

import gnu.io.CommPortIdentifier;

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

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JSeparator;
import javax.swing.JList;
import javax.swing.border.TitledBorder;
import javax.swing.JFormattedTextField;

import java.awt.Color;

import javax.swing.AbstractListModel;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.ListSelectionModel;

import java.awt.TextArea;

import javax.swing.JTextArea;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;



public class CarControlPanel {
	
	private Grapher				  grapher;
	private	CarConfig			  custom;
	private final CarConfig		  defaultConfig=new CarConfig();
	private BluetoothSocket		  btSocket;
	private FunctionMapConfig 	  mapConfig;
	private RandomSignalGenerator signalGenerator;
	private Thread			  	  listener;
	private boolean				  stopListening;
	
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
//	private JScrollPane		deviceListScrollPane;
	private JLabel 			lblDeviceConnectState;
	private JScrollPane 	deviceScrollPane;
	private JList 			deviceList;
	private DefaultListModel listModel;
	private JButton btnRefresh;
	private JCheckBox chckbxEncoderCount;
	private JCheckBox chckbxMagneticSensorReading;
	private JCheckBox chckbxCarSpeed;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CarControlPanel window = new CarControlPanel();
					window.frmSmartCarControl.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CarControlPanel() {
		initialize();
		btSocket=new BluetoothSocket();
		custom	=new CarConfig();
		grapher	=new Grapher();
		grapher.setDelay(Integer.parseInt(loopIntervalField.getText()));
		graphPanel.add(grapher.getChartPanel());
		
		chckbxEncoderCount = new JCheckBox("Encoder Count");
		chckbxEncoderCount.setBounds(333, 255, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxEncoderCount);
		
		chckbxMagneticSensorReading = new JCheckBox("Magnetic Sensor reading");
		chckbxMagneticSensorReading.setBounds(333, 280, 186, 23);
		frmSmartCarControl.getContentPane().add(chckbxMagneticSensorReading);
		
		chckbxCarSpeed = new JCheckBox("Car Speed");
		chckbxCarSpeed.setBounds(333, 305, 128, 23);
		frmSmartCarControl.getContentPane().add(chckbxCarSpeed);
		
		noticeLabel = new JLabel("");
		noticeLabel.setBounds(13, 376, 166, 16);
		frmSmartCarControl.getContentPane().add(noticeLabel);
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
		frmSmartCarControl.setBounds(100, 100, 620, 420);
		frmSmartCarControl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSmartCarControl.getContentPane().setLayout(null);
		
		graphPanel = new JPanel();
		graphPanel.setBounds(323, 6, 291, 187);
		frmSmartCarControl.getContentPane().add(graphPanel);
		
		powerSlider = new JSlider();
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
				}else
				{
					haltToggle.setText("halt");
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
		loopIntervalField.setText("20");
		loopIntervalField.setBounds(172, 6, 51, 28);
		frmSmartCarControl.getContentPane().add(loopIntervalField);
		loopIntervalField.setColumns(10);
		
		lblLoopInterval = new JLabel("Loop Interval:");
		lblLoopInterval.setBounds(86, 11, 93, 16);
		frmSmartCarControl.getContentPane().add(lblLoopInterval);
		
		motorPIDToggle = new JToggleButton("Disable Motor PID");
		motorPIDToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(motorPIDToggle.isSelected())
				{
					motorPIDToggle.setText("Enable Motor PID");
				}else
				{
					motorPIDToggle.setText("Disable Motor PID");
				}
			}
		});
		motorPIDToggle.setBounds(18, 212, 156, 29);
		frmSmartCarControl.getContentPane().add(motorPIDToggle);
		
		motorKpField = new JTextField();
		motorKpField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				custom.c_motorPIDControlVariable[0]=
						Integer.parseInt(motorKpField.getText());
			}
		});
		motorKpField.setText("0");
		motorKpField.setBounds(113, 253, 51, 28);
		frmSmartCarControl.getContentPane().add(motorKpField);
		motorKpField.setColumns(10);
		
		lblKp = new JLabel("Kp:");
		lblKp.setBounds(28, 262, 61, 16);
		frmSmartCarControl.getContentPane().add(lblKp);
		
		lblKi = new JLabel("Ki:");
		lblKi.setBounds(28, 299, 61, 16);
		frmSmartCarControl.getContentPane().add(lblKi);
		
		motorKiField = new JTextField();
		motorKiField.setText("0");
		motorKiField.setColumns(10);
		motorKiField.setBounds(113, 293, 51, 28);
		motorKiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				custom.c_motorPIDControlVariable[1]=
						Integer.parseInt(motorKiField.getText());
			}
		});
		frmSmartCarControl.getContentPane().add(motorKiField);
		
		lblKd = new JLabel("Kd:");
		lblKd.setBounds(28, 336, 61, 16);
		frmSmartCarControl.getContentPane().add(lblKd);
		
		motorKdField = new JTextField();
		motorKdField.setText("0");
		motorKdField.setColumns(10);
		motorKdField.setBounds(113, 333, 51, 28);
		motorKdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				custom.c_motorPIDControlVariable[2]=
						Integer.parseInt(motorKdField.getText());
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
		kalmanFilterToggle.setBounds(172, 212, 161, 29);
		kalmanFilterToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(motorPIDToggle.isSelected())
				{
					kalmanFilterToggle.setText("Disable Kalman Filter");
				}else
				{
					kalmanFilterToggle.setText("Enable Kalman Filter");
				}
			}
		});
		frmSmartCarControl.getContentPane().add(kalmanFilterToggle);
		
		kalmanQField = new JTextField();
		kalmanQField.setText("0");
		kalmanQField.setColumns(10);
		kalmanQField.setBounds(249, 253, 51, 28);
		frmSmartCarControl.getContentPane().add(kalmanQField);
		
		kalmanRField = new JTextField();
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
					btSocket.connectToPort(selectedDeviceName);
					handleSocketState();
					stopListening=false;
					refreshListener();
				}
				//TODO The disconnect function is not available. The program crashes whenever one tries to disconnect from the port.
				//	   This may because of the bug inside rxtx library.
				else	
				{
//					btSocket.disconnect();
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
				lblDeviceConnectState.setText("Too many listener");
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
		stopListening=false;
		listener=new Thread(new Runnable(){
			public void run()
			{
				while(!stopListening)
				{
					if(btSocket.dataAvailable())
					{
						BluetoothSocket.TaggedData data
						=btSocket.extractData();
						if(data!=null)
						{
							grapher.addData(data.tag, data.data);
						}
					}
				}
			}
		});
		listener.start();
	}
}
