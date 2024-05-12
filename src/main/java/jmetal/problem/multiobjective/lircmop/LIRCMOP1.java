package jmetal.problem.multiobjective.lircmop;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem LIR-CMOP1, defined in: An Improved epsilon-constrained Method in
 * MOEA/D for CMOPs with Large Infeasible Regions Fan, Z., Li, W., Cai, X. et al. Soft Comput
 * (2019). https://doi.org/10.1007/s00500-019-03794-x
 */
@SuppressWarnings("serial")
public class LIRCMOP1 extends AbstractDoubleProblem {
  /** Constructor */
  public LIRCMOP1() {
    this(30);
  }
  /** Constructor */
  public LIRCMOP1(int numberOfVariables) {
    numberOfObjectives(2);
    numberOfConstraints(2);
    name("LIRCMOP1");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
    List<Double> upperLimit = new ArrayList<>(numberOfVariables);

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0.0);
      upperLimit.add(1.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /** Evaluate() method */
  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] x = new double[numberOfVariables()];
    for (int i = 0; i < numberOfVariables(); i++) {
      x[i] = solution.variables().get(i);
    }

    solution.objectives()[0] = x[0] + g1(x);
    solution.objectives()[1] = 1 - x[0] * x[0] + g2(x);

    evaluateConstraints(solution);
    return solution ;
  }

  /** EvaluateConstraints() method */
  public void evaluateConstraints(DoubleSolution solution) {
    double[] x = new double[numberOfVariables()];
    for (int i = 0; i < numberOfVariables(); i++) {
      x[i] = solution.variables().get(i);
    }

    final double a = 0.51;
    final double b = 0.5;

    solution.constraints()[0] = (a - g1(x)) * (g1(x) - b);
    solution.constraints()[1] = (a - g2(x)) * (g2(x) - b);
  }

  protected double g1(double[] x) {
    double result = 0.0;
    for (int i = 2; i < numberOfVariables(); i += 2) {
      result += Math.pow(x[i] - Math.sin(0.5 * Math.PI * x[0]), 2.0);
    }
    return result;
  }

  protected double g2(double[] x) {
    double result = 0.0;
    for (int i = 1; i < numberOfVariables(); i += 2) {
      result += Math.pow(x[i] - Math.cos(0.5 * Math.PI * x[0]), 2.0);
    }

    return result;
  }
}