package ar.edu.utn.frba.ia.hopfield.example;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.encog.ml.data.specific.BiPolarNeuralData;
import org.encog.neural.thermal.HopfieldNetwork;

public class EncogMain {
	
	private static final int HEIGHT = 61;
	private static final int WIDTH = 79;
	private static final int MAX_ITERATIONS = 100;
	private static final String TRAINING_FILES_FOLDER = "bwletters/";
	private static final String NETWORK_FILE_FOLDER = "networks/";
	private static final String NETWORK_FILE = "hopfield.eg";
	private static final String NETWORK_PATTERNS_FILE = "patterns.properties";
	private static final int STORE_COUNT = 1;
	
	private static Properties patterns = new Properties();
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Calendar startTime = Calendar.getInstance();
		int neuronCount = WIDTH*HEIGHT;
		HopfieldNetwork network = null;
		URL networkFolder = ClassLoader.getSystemResource(NETWORK_FILE_FOLDER); 
		File networkFile = new File(networkFolder.getFile(), NETWORK_FILE);
		File trainingFolder = new File(ClassLoader.getSystemResource(TRAINING_FILES_FOLDER).getFile());
		File patternsFile = new File(networkFolder.getFile(), NETWORK_PATTERNS_FILE);
		
		collectPatterns(trainingFolder, patternsFile);

		//if(networkFile.exists()) network = (HopfieldNetwork) EncogDirectoryPersistence.loadObject(networkFile);

		if(null == network) {
			network = new HopfieldNetwork((int) (neuronCount));
			
			int imgCargadas = 1;
			int imgTotales = trainingFolder.listFiles().length;
			BiPolarNeuralData data;
			for (File img : trainingFolder.listFiles()) {
				System.out.println("cargando imagen " + img.getName() + " ("+ imgCargadas + "/" + imgTotales +")");
				data = new BiPolarNeuralData(toBooleanData(img));
				for(int i = 0; i < STORE_COUNT; i++){
					network.addPattern(data);
				}
				imgCargadas++;
			}
			//EncogDirectoryPersistence.saveObject(networkFile, network);
			System.out.println("Red guardada: " + networkFile.getAbsolutePath());
		}
		//imagen a testear
		BiPolarNeuralData test = new BiPolarNeuralData(toBooleanData(new File("F:/Workspaces/tengwar-rna/src/main/resources/bwletters/result18.png")));

		BiPolarNeuralData result = evaluatePattern(network, test);
		
		checkMatch(result);
		
		try {
			ImageIO.write(toImage(result), "png", new File(networkFolder.getFile(), "result.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long diffInMillis = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();
		System.out.println("Tiempo de ejecución en segundos: " + diffInMillis / 1000);
		System.out.println(toText(result));
	}
	
	private static boolean checkMatch(BiPolarNeuralData result) {
		String resultText = toText(result);
		String match = null;
		for(Entry<Object, Object> pattern : patterns.entrySet()) {
			if(resultText.equals((String) pattern.getValue())){
				match = (String) pattern.getKey();
				break;
			}
		}
		
		if(null != match){
			System.out.println("Coincidencia con pattern: " + match);
			return true;
		}else{
			System.out.println("No se encontró coincidencia :(");
			return false;
		}
	}

	private static void collectPatterns(File trainingFolder, File resultFile) throws FileNotFoundException, IOException {
		BiPolarNeuralData data;
		patterns = new Properties();
		for (File img : trainingFolder.listFiles()) {
			data = new BiPolarNeuralData(toBooleanData(img));
			patterns.put(img.getName(), toText(data));
		}
		patterns.store(new FileOutputStream(resultFile),null);
	}
	
	private static boolean[] toBooleanData(File imgFile) {
		try {
			boolean[] imgData = new boolean[HEIGHT*WIDTH];
			BufferedImage image = ImageIO.read(imgFile);
			int i = 0;
			for(int x = 0; x < image.getWidth(); x++) {
				for(int y = 0; y < image.getHeight(); y++) {
					imgData[i++] = isBlack(image.getRGB(x, y));
				}
			}
			return imgData;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean isBlack(int rgbColor){
		int r = (rgbColor >> 16) & 0xff;
		int g = (rgbColor >> 8) & 0xff;
		int b = rgbColor & 0xff;
		double whiteRedDiff = 255 - r;
		double whiteGreenDiff = 255 - g;
		double whiteBlueDiff = 255 - b;
		double whiteDistance = whiteRedDiff * whiteRedDiff + whiteGreenDiff * whiteGreenDiff + whiteBlueDiff * whiteBlueDiff;
		
		return whiteDistance > 100 ? true : false;
	}
	
	private static BufferedImage toImage(BiPolarNeuralData data) {
		int i = 0;
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				if(data.getBoolean(i++)) {
					image.setRGB(x, y, Color.BLACK.getRGB());
				}else{
					image.setRGB(x, y, Color.WHITE.getRGB());
				}		
			}
		}
		
		return image;
	}
	
	private static BiPolarNeuralData evaluatePattern(HopfieldNetwork network,
			BiPolarNeuralData testData) {
		
		network.setCurrentState(testData);
		
		int iterations = network.runUntilStable(MAX_ITERATIONS);
		
		BiPolarNeuralData result = network.getCurrentState();
		
		System.out.println("Finalizado. Nro. de iteraciones: " + iterations);
		
		return result;
	}

	public static String toText(BiPolarNeuralData data) {
		int index = 0;
		StringBuilder sb = new StringBuilder();
		for(int row = 0;row<HEIGHT;row++)
		{
			for(int col = 0;col<WIDTH;col++) {
				if(data.getBoolean(index++)){
					sb.append('O');
				}else{
					sb.append(' ');
				}
			}
//			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
