package grapher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class FileMonitor extends Thread
{
	CarControlPanel masterPanel;
	private File file;
	private BufferedReader reader;
	private PrintWriter	 writer;
	public String	 income;
	public String 	 data;
	private final String filePath;
	private byte[]   buffer;
	private boolean hasDataAvailable;
	private boolean isRunning=false;
	FileMonitor(String filePath,CarControlPanel panel)
	{
		this.filePath=filePath;
		masterPanel=panel;
		hasDataAvailable=false;
		isRunning=true;
		buffer=new byte[100];
		data=new String();
		
	}
	public synchronized float[] getData()
	{
		StringTokenizer st=new StringTokenizer(data);
		float result[]=new float[st.countTokens()];
		if(hasDataAvailable)
		{
			try{
				for(int i=0;i<st.countTokens();i++)
				{
					result[i]=Float.parseFloat(st.nextToken());
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}
		return null;
	}
	@Override
	public void run()
	{
		try
		{
			file=new File(filePath);
			writer=new PrintWriter(file);
			reader = new BufferedReader( 
				    new InputStreamReader(new FileInputStream( file ) ));
		}catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		String temp=null;
		System.out.println("i am running:D");
		while(isRunning)
		{
			try
			{
				if( (temp=reader.readLine())!=null ) 
				{
			    	//TODO needed to modify the parameters?
			    	data=temp;
					synchronized(data)
				    {
				    	masterPanel.addData(data);
				    }
					
				}else
				{
					continue;
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			if(income!=null)
			{
				try {
					String[] cmd={"echo","'"+income+"'",">",filePath};
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				writer.println(income);
				income=null;
			}
		}
	}
}
