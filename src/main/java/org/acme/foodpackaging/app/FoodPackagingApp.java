package org.acme.foodpackaging.app;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.foodpackaging.bootstrap.ImportOrderData;
import org.acme.foodpackaging.bootstrap.JsonExporter;
import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.solver.FoodPackagingConstraintProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FoodPackagingApp {

    public static void main(String[] args) {

        if(args.length!=0) {
            SolverFactory<PackagingSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                    .withSolutionClass(PackagingSchedule.class)
                    .withEntityClasses(Job.class, Line.class)
                    .withConstraintProviderClass(FoodPackagingConstraintProvider.class)
                    // The solver runs only for 5 seconds on this small dataset.
                    // It's recommended to run for at least 5 minutes ("5m") otherwise.
                    .withTerminationSpentLimit(Duration.ofSeconds(5)));

            ImportOrderData importData = new ImportOrderData();

            PackagingSchedule problem = importData.scheduleInitializer(args[0]);
            // Solve the problem
            Solver<PackagingSchedule> solver = solverFactory.buildSolver();
            PackagingSchedule solution = solver.solve(problem);

            SolutionManager< PackagingSchedule, HardMediumSoftScore> solutionManager = SolutionManager.create(solverFactory);
            ScoreExplanation< PackagingSchedule, HardMediumSoftScore> scoreExplanation = solutionManager.explain(solution);
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            JsonExporter exporter = new JsonExporter(String.valueOf(importData.getDATE().format(formatter)), problem.getJobs());

            String exportFileDefaultPath = "src/main/resources/exportFiles/" + args[0] + "Export.json";
            if (args.length > 1) {
                try {
                    Path customExportPath = Paths.get(args[1]);
                    Files.createDirectories(customExportPath.getParent()); // создаёт все нужные директории
                    exporter.convertToJsonFile(String.valueOf(customExportPath));
                } catch (IOException e) {
                    System.err.println("Ошибка записи: " + e.getMessage());
                    exporter.convertToJsonFile(exportFileDefaultPath);
                }
            }

            else {
                exporter.convertToJsonFile(exportFileDefaultPath);
            }
        }
        else {
            System.out.println("No date set!");
        }

    }
}