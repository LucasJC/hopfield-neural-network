package ar.edu.utn.frba.ia.hopfield.example;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.encog.ml.data.specific.BiPolarNeuralData;
import org.encog.neural.thermal.HopfieldNetwork;

public class EncogMain {
	
	private static final int HEIGHT = 61;
	private static final int WIDTH = 79;
	private static final int MAX_ITERATIONS = 100;
	
	public static void main(String[] args) {
		
		Calendar startTime = Calendar.getInstance();
		
		int neuronCount = WIDTH*HEIGHT;
		
		HopfieldNetwork network = new HopfieldNetwork(neuronCount);
		//paso las img a datos
		BiPolarNeuralData img1 = new BiPolarNeuralData(toBlackAndWhite("letters/01.png", neuronCount));
		BiPolarNeuralData img31 = new BiPolarNeuralData(toBlackAndWhite("letters/31.png", neuronCount));
		System.out.println("Muestro img 1 y 31");
		display(img1, img31);
		System.out.println("--------------------------");
		BiPolarNeuralData img2 = new BiPolarNeuralData(toBlackAndWhite("letters/02.png", neuronCount));
		//entreno con la 1 y la 31 (que son muy diferentes)
		network.addPattern(img1);
		network.addPattern(img31);
		//intento evaluar la 2 (debería matchear a la 1 por ser mucho más parecida)
		evaluatePattern(network, img2);
		
		long diffInMillis = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();
		System.out.println("Tiempo de ejecución en segundos: " + diffInMillis / 1000);
	}
	
	private static boolean[] toBlackAndWhite(String imgLocation, int resultSize) {
//		int BLACK = 0;
//		int WHITE = 255;
//		int threshold = 127;
		
		try {
			boolean[] imgData = new boolean[resultSize];
			URL imgResource = ClassLoader.getSystemResource(imgLocation);
			if(null == imgResource) {
				throw new RuntimeException("error abriendo imagen");
			}
			BufferedImage image = ImageIO.read(imgResource);
			int i = 0;
			for(int x = 0; x < image.getWidth(); x++) {
				for(int y = 0; y < image.getHeight(); y++) {
					imgData[i++] = Color.BLACK.getRGB() == image.getRGB(x, y) ? true : false;				
				}
			}
			return imgData;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private static void evaluatePattern(HopfieldNetwork network,
			BiPolarNeuralData data1) {
		
		network.setCurrentState(data1);
		
		int iterations = network.runUntilStable(MAX_ITERATIONS);
		
		BiPolarNeuralData result = network.getCurrentState();
		
		System.out.println("Fin. Nro. de itearciones: " + iterations);
		System.out.println("dato evaluado VS resultado");
		display(data1, result);
	}

	public static void display(BiPolarNeuralData pattern1,BiPolarNeuralData pattern2)
	{
		int index1 = 0;
		int index2 = 0;
		
		for(int row = 0;row<HEIGHT;row++)
		{
			StringBuilder line = new StringBuilder();
			
			for(int col = 0;col<WIDTH;col++)
			{
				if(pattern1.getBoolean(index1++))
					line.append('O');
				else
					line.append(' ');
			}
			
			line.append("   ->   ");
			
			for(int col = 0;col<WIDTH;col++)
			{
				if(pattern2.getBoolean(index2++))
					line.append('O');
				else
					line.append(' ');
			}
			
			System.out.println(line.toString());
		}
}
}
