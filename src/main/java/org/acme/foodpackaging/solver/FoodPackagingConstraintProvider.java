package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.ProductType;

public class FoodPackagingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                maxEndDateTime(factory),
                plushMustBeOnLine1(factory),
                rodOnlyOnLines456(factory),
                cactusOnlyOnLines123(factory),
                classicOnlyOnLines1236(factory),
                // Medium constraints
                idealEndDateTime(factory),
                // Soft constraints
                minimizeMakespan(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint maxEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getMaxEndTime().isBefore(job.getEndDateTime()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD,
                        job -> Duration.between(job.getMaxEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Max end date time");
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint idealEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getIdealEndTime().isBefore(job.getEndDateTime()))
                .penalizeLong(HardMediumSoftLongScore.ONE_MEDIUM,
                        job -> Duration.between(job.getIdealEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Ideal end date time");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    // TODO Currently dwarfed by minimizeAndLoadBalanceMakeSpan in the same score level, because that squares
    protected Constraint operatorCleaningConflict(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job ->job.getLine() != null)
                .join(factory.forEach(Job.class).filter(job ->job.getLine() != null),
                        Joiners.equal(job -> job.getLine().getOperator()),
                        Joiners.overlapping(Job::getStartCleaningDateTime, Job::getStartProductionDateTime),
                        Joiners.lessThan(Job::getId))
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, (job1, job2) -> Duration.between(
                        (job1.getStartCleaningDateTime().compareTo(job2.getStartCleaningDateTime()) > 0)
                               ? job1.getStartCleaningDateTime() : job2.getStartCleaningDateTime(),
                        (job1.getStartProductionDateTime().compareTo(job2.getStartProductionDateTime()) < 0)
                                ? job1.getStartProductionDateTime() : job2.getStartProductionDateTime()
                ).toMinutes())
                .asConstraint("Operator cleaning conflict");
    }

    protected Constraint minimizeMakespan(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getNextJob() == null)
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, job -> {
                    long minutes = Duration.between(job.getLine().getStartDateTime(), job.getEndDateTime()).toMinutes();
                    return minutes * minutes;
                })
                .asConstraint("Minimize make span");
    }

    // TODO Currently dwarfed by minimizeAndLoadBalanceMakeSpan in the same score level, because that squares
    protected Constraint minimizeCleaningDuration(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getStartProductionDateTime() != null)
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, job -> job.getPriority()
                        * Duration.between(job.getStartCleaningDateTime(), job.getStartProductionDateTime()).toMinutes())
                .asConstraint("Minimize cleaning duration");
    }

    protected Constraint plushMustBeOnLine1(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getProduct() != null)
                .filter(job -> job.getProduct().getType() == ProductType.PLUSH)
                .filter(job -> !"1".equals(job.getLine().getId()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD, job -> 1000L)
                .asConstraint("PLUSH must be on line 1");
    }

    protected Constraint rodOnlyOnLines456(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getProduct() != null)
                .filter(job -> job.getProduct().getType() == ProductType.ROD)
                .filter(job -> !Set.of("4", "5", "6").contains(job.getLine().getId()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD, job -> 1000L)
                .asConstraint("ROD must be on lines 4, 5, 6");
    }

    protected Constraint cactusOnlyOnLines123(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getProduct() != null)
                .filter(job -> job.getProduct().getType() == ProductType.CACTUS)
                .filter(job -> !Set.of("1", "2", "3").contains(job.getLine().getId()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD, job -> 1000L)
                .asConstraint("CACTUS must be on lines 1, 2, 3");
    }

    protected Constraint classicOnlyOnLines1236(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getProduct() != null)
                .filter(job -> job.getProduct().getType() == ProductType.CLASSIC)
                .filter(job -> !Set.of("1", "2", "3", "6").contains(job.getLine().getId()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD, job -> 1000L)
                .asConstraint("CLASSIC must be on lines 1, 2, 3,6");
    }
}
