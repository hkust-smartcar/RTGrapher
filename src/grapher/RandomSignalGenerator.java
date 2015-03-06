package grapher;
import java.util.Random;
public class RandomSignalGenerator {
	private Random 			random;
	public	int				range=300;
    public float 			randomValue() 
    {
        return (float) (random.nextGaussian() * range / 3);
    }

    private float[] gaussianData(int count) 
    {
        float[] a = new float[count];
        for (int i = 0; i < a.length; i++) {
            a[i] = randomValue();
        }
        return a;
    }
    RandomSignalGenerator(int range)
    {
    	random=new Random();
    	this.range=range;
    }
    
}
