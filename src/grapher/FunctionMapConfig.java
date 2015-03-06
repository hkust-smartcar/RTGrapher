package grapher;
import java.util.HashMap;
public class FunctionMapConfig {
	 private	HashMap<Integer,Integer> 	functionMap;
	 public		void 						addHash(int hash,int value)
	 {
		 functionMap.put(hash, value);
	 }
	 public		void						removeHash(int hash)
	 {
		 functionMap.remove(hash);
	 }
	 public		void						loadMap(FunctionMapConfig config)
	 {
		 functionMap=config.getMap();
	 }
	 public		void						loadMap(HashMap<Integer,Integer> map)
	 {
		 functionMap=map;
	 }
	 public 	HashMap<Integer,Integer>	getMap()
	 {
		 return	functionMap;
	 }
	 FunctionMapConfig()
	 {
		 functionMap=new HashMap<Integer,Integer>();   
	 }
	 public		void						constructMap()
	 {
		 if(functionMap!=null)
		 {
			 for(int i=0;i<10;i++)
			 {
				 addHash(i,i);
			 }
		 }
	 }
}
