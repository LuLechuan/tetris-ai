package Tetris;

import java.util.*;
import java.lang.*;

import Tetris.Features.*;
import Tetris.Helper.Tuple;
import Tetris.Helper.Helper;

public class PlayerSkeleton {

	public static TFrame frame = new TFrame(new State());

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		Random rand = new Random();
		return rand.nextInt(legalMoves.length);
	}

	public static void main(String[] args) {
		geneticFunction();
	}

	// Simulates the replacement of the population by its member's descendants
	public static void geneticFunction() {
		ArrayList<Heuristic> population = Helper.getRandomHeuristics(Constants.NUMBER_OF_HEURISTICS);

		for (int i = 0; i < Constants.NUMBER_OF_GENERATIONS; i++) {
			HashMap<Heuristic, Integer> populationWithAverageScores = getPopulationScores(population);
			population = generateNextGeneration(populationWithAverageScores);
		}
	}


	// Creates NUMBER_OF_HEURISTICS new children from the current population and returns this new population.
	// The probability of two heuristics procreating is proportional to the average score they generated.
	public static ArrayList<Heuristic> generateNextGeneration(HashMap<Heuristic, Integer> populationWithScores) {
		ArrayList<Heuristic> newPopulation = new ArrayList<Heuristic>();
		Tuple<ArrayList<Heuristic>, ArrayList<Integer>> heuristicsAndIntervals = generateProbabilityIntervalList(populationWithScores);

		for (int i = 0; i < populationWithScores.size(); i++) {
			Tuple<Heuristic, Integer> mother = randomSelect(populationWithScores, heuristicsAndIntervals);
			Tuple<Heuristic, Integer> father = randomSelect(populationWithScores, heuristicsAndIntervals);
			Heuristic child = reproduce(mother, father);

			// Add lines to mimic random mutation
			newPopulation.add(child);
		}

		return newPopulation;
	}


	public static HashMap<Heuristic, Integer> getPopulationScores(ArrayList<Heuristic> population) {
		HashMap<Heuristic, Integer> averageScores = new HashMap<>();

		// Run every heuristic NUMBER_OF_GAMES times and store the average score
		for (Heuristic heuristic : population) {
			Integer averageScore = 0;

			for (int i = 0; i < Constants.NUMBER_OF_GAMES; i++) {
				averageScore += runGameWithHeuristic(heuristic);
			}

			System.out.println("done with one heuristic");

			averageScore /= Constants.NUMBER_OF_GAMES;

			averageScores.put(heuristic, averageScore);
		}

		return averageScores;
	}



	public static int runGameWithHeuristic(Heuristic heuristic) {
		State s = new State();
		frame.bindState(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		return s.getRowsCleared();
	}


	public static Tuple<ArrayList<Heuristic>, ArrayList<Integer>> generateProbabilityIntervalList(HashMap<Heuristic, Integer> populationWithScores) {

		ArrayList<Heuristic> heuristicsList = new ArrayList<Heuristic>(populationWithScores.keySet());
		ArrayList<Integer> intervalList = new ArrayList<Integer>(heuristicsList.size());

		intervalList.set(0, populationWithScores.get(heuristicsList.get(0)));
		for (int k = 1; k < heuristicsList.size(); k ++) {
			intervalList.set(k, intervalList.get(k - 1) + populationWithScores.get(heuristicsList.get(k)));
		}
		return new Tuple<>(heuristicsList, intervalList);
	}


	public static Tuple<Heuristic, Integer> randomSelect(HashMap<Heuristic, Integer> populationWithScores, Tuple<ArrayList<Heuristic>, ArrayList<Integer>> heuristicsAndIntervals) {
		double sumOfScores = Helper.sum(populationWithScores.values());

		ArrayList<Heuristic> heuristicsList= heuristicsAndIntervals.getFirst();
		ArrayList<Integer> intervalsList= heuristicsAndIntervals.getSecond();

		Random rand = new Random();
		Double randomDouble = rand.nextDouble();
		randomDouble = randomDouble * sumOfScores;

		int chosenIndex;

		for (chosenIndex = 0; chosenIndex < intervalsList.size(); chosenIndex++) {
			if (randomDouble - 1 < intervalsList.get(chosenIndex)) {
				break;
			}
		}

		Heuristic chosenHeuristic = heuristicsList.get(chosenIndex);
		Integer chosenHeuristicScore = populationWithScores.get(chosenHeuristic);

		return new Tuple<>(chosenHeuristic, chosenHeuristicScore);
	}



	public static Heuristic reproduce(Tuple<Heuristic, Integer> mother, Tuple<Heuristic, Integer> father) {
		double scoreRatio = mother.getSecond() / father.getSecond();

		int numOfWeightsFromMother = 0;
		double[] motherWeights = mother.getFirst().weights;
		double[] fatherWeights = father.getFirst().weights;

		if (scoreRatio < 1/4) {
			numOfWeightsFromMother = (Constants.NUMBER_OF_FEATURES / 4) + 1;
		} else if (scoreRatio < 3/4) {
			numOfWeightsFromMother = Constants.NUMBER_OF_FEATURES / 2;
		} else {
			numOfWeightsFromMother = (3 * Constants.NUMBER_OF_FEATURES / 4);
		}


		int[] weightIndexesFromMother = Helper.generateRandomIndices(numOfWeightsFromMother, Constants.NUMBER_OF_FEATURES);


		double[] childWeights = new double[Constants.NUMBER_OF_FEATURES];

		for (int i = 0; i < Constants.NUMBER_OF_FEATURES; i++) {

			if (Helper.contains(weightIndexesFromMother, i)) {
				childWeights[i] = motherWeights[i];
			} else {
				childWeights[i] = fatherWeights[i];
			}
		}

		return new Heuristic(childWeights);
	}
}

class SimulatedAnnealing {

	private static final int NUM_GAMES = 5;
	private static final int NUM_ITERATIONS = 100;
	private double score;
	private int iteration;
	private Random random;

	public SimulatedAnnealing() {
		score = 0;
		iteration = 0;
    	random = new Random();
	}

	public void run() {
		score = PlayerSkeleton.runGameWithHeuristic(getHeuristic());
		System.out.println(score);
	}

	public Heuristic getHeuristic() {
		double initialTemperature = calculateInitialTemperature();
		double temperature = initialTemperature;
		Heuristic heuristic  = new Heuristic(25, 25, 25, 25);
		while (true) {
			if (temperature == 0) {
				System.out.println("Cooled down! The result is obtained.");
				return heuristic;
			}

			Heuristic newHeuristic = getNeighbourHeuristic(heuristic);
			double averageScoreWithOldHeuristic = getAverageScore(heuristic, 5);
			double averageScoreWithNewHeuristic = getAverageScore(newHeuristic, 5);
			double improvementFromOlderHeuristic = averageScoreWithNewHeuristic - averageScoreWithOldHeuristic;
			if (isAccepted(temperature, improvementFromOlderHeuristic)) {
				heuristic = newHeuristic;
			}

			temperature = scheduleNewTemperature(initialTemperature, iteration);
			iteration++;
		}
	}

	public double calculateInitialTemperature() {
		return 1000;
	}

	public Heuristic getNeighbourHeuristic(Heuristic heuristic) {
		Heuristic newHeuristic = new Heuristic(heuristic.averageHeightWeight, heuristic.maxHeightWeight,
				heuristic.numOfHolesWeight, heuristic.unevennessWeight);
		double valueChange = random.nextDouble() * 100 - 50;
		double sum = newHeuristic.averageHeightWeight + newHeuristic.maxHeightWeight + newHeuristic.numOfHolesWeight
				+ newHeuristic.unevennessWeight + valueChange;
		// The index indicates which weight is changed
		int index = random.nextInt(4);
		switch(index) {
			case 0:
				newHeuristic.averageHeightWeight += sum;
				break;
			case 1:
				newHeuristic.maxHeightWeight += sum;
				break;
			case 2:
				newHeuristic.numOfHolesWeight += sum;
				break;
			case 3:
				newHeuristic.unevennessWeight += sum;
		}
		newHeuristic.averageHeightWeight = newHeuristic.averageHeightWeight * 100 / sum;
		newHeuristic.maxHeightWeight = newHeuristic.maxHeightWeight * 100 / sum;
		newHeuristic.numOfHolesWeight = newHeuristic.numOfHolesWeight * 100 / sum;
		newHeuristic.unevennessWeight = newHeuristic.unevennessWeight * 100 / sum;

		return heuristic;
	}

	public boolean isAccepted(double temperature, double improvementFromOlderHeuristic) {
		double acceptanceProbability = getAcceptanceProbability(temperature, improvementFromOlderHeuristic);
		if (acceptanceProbability >= random.nextDouble()) {
			return true;
		}
		return false;
	}

	public double getAcceptanceProbability(double temperature, double improvementFromOlderHeuristic) {
		if (improvementFromOlderHeuristic > 0) {
			return 1.0;
		} else {
			return Math.exp((- improvementFromOlderHeuristic) / temperature);
		}
	}

	public double scheduleNewTemperature(double initialTemperature, int iteration) {
		double newTemperature = initialTemperature / (1 + Math.log(1 + iteration));
		return newTemperature;
	}

	public double getAverageScore(Heuristic heuristic, int rounds) {
		double sum = 0;
		for (int i = 0; i < rounds; i++) {
			sum += PlayerSkeleton.runGameWithHeuristic(heuristic);
		}
		return sum / rounds;
	}
}
