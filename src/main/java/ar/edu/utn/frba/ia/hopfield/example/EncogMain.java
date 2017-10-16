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
	//alto de la entrada
	private static final int HEIGHT = 61;
	//ancho de la entrada
	private static final int WIDTH = 79;
	//máximo de iteraciones para verificación
	private static final int MAX_ITERATIONS = 100;
	//carpeta de entrenamiento
	private static final String TRAINING_FILES_FOLDER = "bwletters/";
	//carpeta de pruebas
	private static final String TESTING_FILES_FOLDER = "test/";
	//carpeta principal
	private static final String NETWORK_FILE_FOLDER = "networks/";
	//cantidad de veces a aprender un patrón
	private static final int STORE_COUNT = 1;
	//archivo para guardar los patrones como texto
	private static final String NETWORK_PATTERNS_FILE = "patterns.properties";
	//Properties para patrones como texto
	private static Properties patterns = new Properties();
	
	/**
	 * 
	 * Ejecución principal de la aplicación
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Calendar startTime = Calendar.getInstance();
		int neuronCount = WIDTH*HEIGHT;
		HopfieldNetwork network = null;
		URL networkFolder = ClassLoader.getSystemResource(NETWORK_FILE_FOLDER); 
		File trainingFolder = new File(ClassLoader.getSystemResource(TRAINING_FILES_FOLDER).getFile());
		File testFolder = new File(ClassLoader.getSystemResource(TESTING_FILES_FOLDER).getFile());
		File patternsFile = new File(networkFolder.getFile(), NETWORK_PATTERNS_FILE);
		
		//cargo los patrones de entrenamiento en una instancia de Properties para poder comparar con el resultado luego
		collectPatterns(trainingFolder, patternsFile);
		

		//inicializo la red neuronal
		System.out.println("Generando red neuronal de " + neuronCount + " neuronas...");
		System.out.println("A continuación se entrenará la red:");
		network = new HopfieldNetwork((int) (neuronCount));
		//entreno la red con los patrones de la carpeta de entrenamiento
		int imgCargadas = 1;
		int imgTotales = trainingFolder.listFiles().length;

		for (File img : trainingFolder.listFiles()) {
			System.out.println("->cargando imagen " + img.getName() + " ("+ imgCargadas + "/" + imgTotales +")");
			BiPolarNeuralData data = new BiPolarNeuralData(toBooleanData(img));
			for(int i = 0; i < STORE_COUNT; i++){
				network.addPattern(data);
			}
			imgCargadas++;
		}
		System.out.println("Fin del entrenamiento.");
		System.out.println("A continuación se realizarán las validaciones:");
		//evalúo elementos
		int imgProcesadas = 1;
		int imgPruebaTotales = testFolder.listFiles().length;
		for (File img : testFolder.listFiles()) {
			System.out.println("->evaluando imagen de prueba " + img.getName() + " ("+ imgProcesadas + "/" + imgPruebaTotales +")");
			//convierto imagen a datos de entrada
			BiPolarNeuralData data = new BiPolarNeuralData(toBooleanData(img));
			//evalúo la entrada con la red
			BiPolarNeuralData result = evaluatePattern(network, data);
			//comparo el resultado con los patrones almacenados para ver si hay coincidencia
			checkMatch(result);
			//además persisto el resultado como imagen para poder visualizarlo
			try {
				ImageIO.write(toImage(result), "png", new File(networkFolder.getFile(), img.getName() +"_result.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			imgProcesadas++;
		}
		System.out.println("Fin de la ejecución");
		long diffInMillis = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();
		System.out.println("Tiempo de ejecución en segundos: " + diffInMillis / 1000);
	}
	
	/**
	 * 
	 * Compara un {@link BiPolarNeuralData} pasándolo a texto con un conjunto de {@link Properties} para encontrar coincidencia
	 * 
	 * @param result
	 * @return
	 */
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

	/**
	 * 
	 * Convierte un conjunto de imágenes en texto y los almacena en una instancia de {@link Properties}
	 * 
	 * @param trainingFolder
	 * @param resultFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void collectPatterns(File trainingFolder, File resultFile) throws FileNotFoundException, IOException {
		BiPolarNeuralData data;
		patterns = new Properties();
		for (File img : trainingFolder.listFiles()) {
			data = new BiPolarNeuralData(toBooleanData(img));
			patterns.put(img.getName(), toText(data));
		}
		patterns.store(new FileOutputStream(resultFile),null);
	}
	
	/**
	 * 
	 * Convierte un archivo de imagen a un array de boolean
	 * 
	 * @param imgFile
	 * @return
	 */
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
	/**
	 * Determina si un color está más cerca del negro (true) o del blanco (false) en base a un valor de barrera
	 * 
	 * @param rgbColor
	 * @return
	 */
	private static boolean isBlack(int rgbColor){
		int threshold = 100;
		int r = (rgbColor >> 16) & 0xff;
		int g = (rgbColor >> 8) & 0xff;
		int b = rgbColor & 0xff;
		double whiteRedDiff = 255 - r;
		double whiteGreenDiff = 255 - g;
		double whiteBlueDiff = 255 - b;
		double whiteDistance = whiteRedDiff * whiteRedDiff + whiteGreenDiff * whiteGreenDiff + whiteBlueDiff * whiteBlueDiff;
		
		return whiteDistance > threshold ? true : false;
	}
	
	/**
	 * 
	 * Convierte un {@link BiPolarNeuralData} a una {@link BufferedImage} para almacenar en disco
	 * 
	 * @param data
	 * @return
	 */
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
	
	/**
	 * 
	 * En base a una red neuronal y a un set de prubas realiza la validación y retorna el resultado
	 * 
	 * @param network
	 * @param testData
	 * @return
	 */
	private static BiPolarNeuralData evaluatePattern(HopfieldNetwork network,
			BiPolarNeuralData testData) {
		
		network.setCurrentState(testData);
		
		int iterations = network.runUntilStable(MAX_ITERATIONS);
		
		BiPolarNeuralData result = network.getCurrentState();
		
		System.out.println("Finalizado. Nro. de iteraciones: " + iterations);
		
		return result;
	}

	/**
	 * 
	 * Convierte un {@link BiPolarNeuralData} a una cadena de caracteres donde el true se representa como un caracter 'O' y el false como un ' '
	 * 
	 * @param data
	 * @return
	 */
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
