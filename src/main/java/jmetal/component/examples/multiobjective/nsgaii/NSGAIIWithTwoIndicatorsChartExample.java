package jmetal.component.examples.multiobjective.nsgaii;

import static jmetal.core.util.VectorUtils.readVectors;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistancePlus;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.IndicatorPlotObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the NSGA-II algorithm showing the population while the algorithm is
 * running
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIWithTwoIndicatorsChartExample {

  public static void main(String[] args) throws JMetalException, IOException {
    String problemName = "jmetal.problem.multiobjective.zdt.ZDT4";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT4.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    Termination termination = new TerminationByEvaluations(25000);

    EvolutionaryAlgorithm<DoubleSolution> nsgaii = new NSGAIIBuilder<>(
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .build();

    var indicatorObserver1 =
        new IndicatorPlotObserver<>("NSGA-II: " + problem.name(), new PISAHypervolume(),
            referenceParetoFront, 800);
    var indicatorObserver2 =
        new IndicatorPlotObserver<>("NSGA-II: " + problem.name(),
            new InvertedGenerationalDistancePlus(), referenceParetoFront, 800);

    nsgaii.observable().register(indicatorObserver1);
    nsgaii.observable().register(indicatorObserver2);

    nsgaii.run();

    List<DoubleSolution> population = nsgaii.result();
    JMetalLogger.logger.info("Total execution time : " + nsgaii.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + nsgaii.numberOfEvaluations());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        readVectors(referenceParetoFront, ","));
  }
}
