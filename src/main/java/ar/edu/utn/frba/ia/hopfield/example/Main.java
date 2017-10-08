package ar.edu.utn.frba.ia.hopfield.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.Hopfield;

public class Main {
	
	private static int width = 0;
	private static int height = 0;
	private static String extension = "png"; 
	
	public static void main(String[] args) {
		List<DataSetRow> datasetrows = new ArrayList<DataSetRow>();
		File folder = new File("letters");
		File[] files = folder.listFiles();
		
		for(File file : files) {
			try {
				
				double[] fileContent = getImage(file);			
				DataSetRow row = new DataSetRow(fileContent);
				datasetrows.add(row);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int neurons = datasetrows.get(0).getInput().length;
		DataSet trainingSet = new DataSet(neurons);
		trainingSet.addAll(datasetrows);
		
		Hopfield network = new Hopfield(neurons);
		network.learn(trainingSet);
		
		double[] testData = null;
		try {
			testData = getImage(new File("01.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DataSetRow test = new DataSetRow(testData);
		network.setInput(test.getInput());
		network.calculate();	
		
		try {
			createImage("result", network.getOutput());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static double[] getImage(File file) throws IOException {
		int item = 0;
		BufferedImage image = ImageIO.read(file);
		double[] data = new double[image.getWidth()*image.getHeight()];
		
		width = image.getWidth();
		height = image.getHeight();
		
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				double color = (double)image.getRGB(x, y);				
				data[item++] = color;
			}
		}
			
		return data;
	}
	
	private static void createImage(String name, double[] data) throws IOException {		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int item = 0;
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, (int)data[item++]);
			}
		}
		
		ImageIO.write(image, extension, new File(name+"."+extension));
	}
}
