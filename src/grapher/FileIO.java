package grapher;
import java.io.*;
public class FileIO {
	private static File file ;
	private FileInputStream writer;
	public static void main(){
		File file = new File("C:/robots.txt");
		FileInputStream fis = null;
 
		try {
			fis = new FileInputStream(file);
 
			System.out.println("Total file size to read (in bytes) : "
					+ fis.available());
 
			int content;
			while ((content = fis.read()) != -1) {
				// convert to char and display it
				System.out.print((char) content);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
		
}
