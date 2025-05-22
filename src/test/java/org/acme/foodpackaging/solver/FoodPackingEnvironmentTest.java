package org.acme.foodpackaging.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.foodpackaging.bootstrap.DemoDataGenerator;
import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FoodPackingEnvironmentTest {

    @Test
    void solveFullAssert() {
        solve();
    }

    void solve() {
        SolverFactory<PackagingSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(PackagingSchedule.class)
                .withEntityClasses(Job.class, Line.class)
                .withConstraintProviderClass(FoodPackagingConstraintProvider.class)
                .withEnvironmentMode(EnvironmentMode.FULL_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30)));

        // Load the problem
        DemoDataGenerator demo_data = new DemoDataGenerator(10, 32);
        PackagingSchedule problem = demo_data.generateDemoData();

        // Solve the problem
        Solver<PackagingSchedule> solver = solverFactory.buildSolver();
        PackagingSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
        System.out.println("point");
    }
}