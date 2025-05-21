package org.acme.foodpackaging.app;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.foodpackaging.bootstrap.DemoDataGenerator;
import org.acme.foodpackaging.bootstrap.ImportOrderData;
import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.solver.FoodPackagingConstraintProvider;

import java.time.Duration;

public class FoodPackagingApp {

    public static void main(String[] args) {
        SolverFactory<PackagingSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(PackagingSchedule.class)
                .withEntityClasses(Job.class, Line.class)
                .withConstraintProviderClass(FoodPackagingConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        //   DemoDataGenerator dataGen = new DemoDataGenerator(12, 45);
        // Load the problem

        ImportOrderData importData = new ImportOrderData();

        PackagingSchedule problem = importData.scheduleInitializer("2025-05-19");
        System.out.println("Worked!");

        // Solve the problem
        // Solver<PackagingSchedule> solver = solverFactory.buildSolver();
        //PackagingSchedule solution = solver.solve(problem);
    }
}