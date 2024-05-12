package jmetal.algorithm.multiobjective.nsgaii;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RankingAndCrowdingSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAII<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

  protected final int maxEvaluations;

  protected final SolutionListEvaluator<S> evaluator;

  protected int evaluations;
  protected Comparator<S> dominanceComparator;

  protected int matingPoolSize;
  protected int offspringPopulationSize;

  /**
   * Constructor
   */
  public NSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
      int matingPoolSize, int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    this(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
        crossoverOperator, mutationOperator, selectionOperator, new DefaultDominanceComparator<S>(),
        evaluator);
  }

  /**
   * Constructor
   */
  public NSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
      int matingPoolSize, int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
      SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    setMaxPopulationSize(populationSize);

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;
    this.dominanceComparator = dominanceComparator;

    this.matingPoolSize = matingPoolSize;
    this.offspringPopulationSize = offspringPopulationSize;
  }

  public NSGAII(NSGAIIBuilder<S> builder) {
    super(builder.getProblem()) ;
    int numberOfDivisions  ;
    List<ReferencePoint<S>> referencePoints = new Vector<>() ;
    maxEvaluations = builder.getMaxIterations();
    crossoverOperator =  builder.getCrossoverOperator() ;
    mutationOperator  =  builder.getMutationOperator() ;
    selectionOperator =  builder.getSelectionOperator() ;

    evaluator = builder.getSolutionListEvaluator();
    dominanceComparator = builder.getDominanceComparator();
    matingPoolSize = builder.getMatingPoolSize();
    offspringPopulationSize = builder.offspringPopulationSize;

    numberOfDivisions = builder.getNumberOfDivisions() ;
    (new ReferencePoint<S>()).generateReferencePoints(referencePoints,getProblem().numberOfObjectives() , numberOfDivisions);

    setMaxPopulationSize(builder.getPopulationSize());
  }

  @Override
  protected void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @Override
  protected void updateProgress() {
    evaluations += offspringPopulationSize;
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations;
  }

  @Override
  protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  /**
   * This method iteratively applies a {@link SelectionOperator} to the population to fill the
   * mating pool population.
   *
   * @param population
   * @return The mating pool population
   */
  @Override
  protected List<S> selection(List<S> population) {
    List<S> matingPopulation = new ArrayList<>(population.size());
    for (int i = 0; i < matingPoolSize; i++) {
      S solution = selectionOperator.execute(population);
      matingPopulation.add(solution);
    }

    return matingPopulation;
  }

  /**
   * This methods iteratively applies a {@link CrossoverOperator} a  {@link MutationOperator} to the
   * population to create the offspring population. The population size must be divisible by the
   * number of parents required by the {@link CrossoverOperator}; this way, the needed parents are
   * taken sequentially from the population.
   * <p>
   * The number of solutions returned by the {@link CrossoverOperator} must be equal to the
   * offspringPopulationSize state variable
   *
   * @param matingPool
   * @return The new created offspring population
   */
  @Override
  protected List<S> reproduction(List<S> matingPool) {
    int numberOfParents = crossoverOperator.numberOfRequiredParents();

    checkNumberOfParents(matingPool, numberOfParents);

    List<S> offspringPopulation = new ArrayList<>(offspringPopulationSize);
    for (int i = 0; i < matingPool.size(); i += numberOfParents) {
      List<S> parents = new ArrayList<>(numberOfParents);
      for (int j = 0; j < numberOfParents; j++) {
        parents.add(matingPool.get(i + j));
      }

      List<S> offspring = crossoverOperator.execute(parents);

      for (S s : offspring) {
        mutationOperator.execute(s);
        offspringPopulation.add(s);
        if (offspringPopulation.size() >= offspringPopulationSize) {
          break;
        }
      }
    }
    return offspringPopulation;
  }

  @Override
  protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(population);
    jointPopulation.addAll(offspringPopulation);

    RankingAndCrowdingSelection<S> rankingAndCrowdingSelection;
    rankingAndCrowdingSelection = new RankingAndCrowdingSelection<S>(getMaxPopulationSize(),
        dominanceComparator);

    return rankingAndCrowdingSelection.execute(jointPopulation);
  }

  @Override
  public List<S> result() {
    return SolutionListUtils.getNonDominatedSolutions(getPopulation());
  }

  @Override
  public String name() {
    return "NSGAII";
  }

  @Override
  public String description() {
    return "Nondominated Sorting Genetic Algorithm version II";
  }
}
