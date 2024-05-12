package jmetal.problem.tests.multiobjective.re;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.re.RE22;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class RE22Test {

  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new RE22();

    assertEquals(3, problem.numberOfVariables());
    assertEquals(2, problem.numberOfObjectives());
    assertEquals(0, problem.numberOfConstraints());
    assertEquals("RE22", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new RE22();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(3, solution.variables().size());
    assertEquals(2, solution.objectives().length);
    assertEquals(0, solution.constraints().length);
  }
}