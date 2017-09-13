package ar.edu.utn.frba.ia.hopfield.example;

import java.util.Arrays;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.Hopfield;

public class Main {
	
	public static void main(String[] args) {
		//creo un set de entrenamiento
		DataSet trainingSet = new DataSet(9);
		trainingSet.add(new DataSetRow(new double[]{1,0,1, 1, 1, 1, 1, 0, 1}));
		trainingSet.add(new DataSetRow(new double[]{1, 1, 1, 0, 1, 0, 0, 1, 0}));
		
		//creo red con 9 neuronas
		Hopfield network = new Hopfield(9);
		network.learn(trainingSet);
		
		//creo un set de pruebas
		DataSet testSet = new DataSet(9);
		testSet.add(new DataSetRow(new double[]{1, 0, 0, 1, 0, 1, 1, 0, 1}));
		testSet.add(new DataSetRow(new double[]{1, 1, 1, 0, 1, 0, 0, 1, 0}));
		testSet.add(new DataSetRow(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0}));
		
		//para cada input del set de pruebas ejecuto la red e imprimo resultados
		for(DataSetRow row : testSet.getRows()) {
			network.setInput(row.getInput());
			network.calculate();
			network.calculate();
			System.out.println("Resultado:");
			System.out.println("Input: " + Arrays.toString(row.getInput()));
			System.out.println("Output: " + Arrays.toString(network.getOutput()));
		}
	}
}
