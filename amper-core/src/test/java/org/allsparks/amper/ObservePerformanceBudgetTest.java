package org.allsparks.amper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;
import org.allsparks.amper.sim.SimulatedClock;
import org.junit.jupiter.api.Test;

/**
 * Desktop algorithmic budget for {@link AmperSession#observe()}.
 *
 * <p>Numbers are JVM desktop timings, not Control Hub loop times. The wall-clock
 * ceiling is deliberately generous so GitHub-hosted runners do not flake. The
 * test fails on unbounded growth or accidental superlinear blow-ups, not on
 * a 2 ms SLA.
 */
class ObservePerformanceBudgetTest {

    private static final int CAPACITY = 256;
    private static final int WARMUP = 200;
    private static final int TOTAL = 1200;
    private static final long MAX_ELAPSED_MS = 30_000L;
    private static final int RELATIVE_SLOWDOWN_CEILING = 80;

    @Test
    void studentPresetsCapCurrentReadsPerLoop() {
        assertEquals(1, AmperPolicies.passiveDefaults().sampling().maxCurrentReadsPerLoop());
        assertEquals(1, AmperPolicies.measurementOnly().sampling().maxCurrentReadsPerLoop());
        assertEquals(1, AmperPolicies.localProtectionAllowed().sampling().maxCurrentReadsPerLoop());
    }

    @Test
    void loggerAndCanonicalLogStayWithinCapacity() {
        Fixture fixture = fixture(CAPACITY);
        fixture.session.start();
        for (int i = 0; i < CAPACITY + 40; i++) {
            fixture.clock.set(i * 20_000_000L);
            fixture.session.observe();
        }
        assertTrue(fixture.session.logger().snapshot().size() <= CAPACITY);
        assertTrue(fixture.session.canonicalLog().size() <= CAPACITY);
        assertTrue(fixture.session.logger().droppedCount() > 0L);
        assertTrue(fixture.session.canonicalLog().droppedCount() > 0L);
    }

    @Test
    void manyObservesFinishWithinGenerousDesktopBudget() {
        Fixture fixture = fixture(CAPACITY);
        fixture.session.start();

        long warmupStart = System.nanoTime();
        for (int i = 0; i < WARMUP; i++) {
            fixture.clock.set(i * 20_000_000L);
            fixture.session.observe();
        }
        long warmupNs = System.nanoTime() - warmupStart;

        long restStart = System.nanoTime();
        for (int i = WARMUP; i < TOTAL; i++) {
            fixture.clock.set(i * 20_000_000L);
            fixture.session.observe();
        }
        long restNs = System.nanoTime() - restStart;
        long elapsedMs = (warmupNs + restNs) / 1_000_000L;

        int firstCount = WARMUP;
        int restCount = TOTAL - WARMUP;
        long warmupPer = warmupNs / Math.max(1, firstCount);
        long restPer = restNs / Math.max(1, restCount);

        System.out.println(
                "AMPER desktop observe budget (not Control Hub): "
                        + "n=" + TOTAL
                        + " capacity=" + CAPACITY
                        + " elapsedMs=" + elapsedMs
                        + " warmupNsPerObserve=" + warmupPer
                        + " laterNsPerObserve=" + restPer
                        + " later/warmup=" + (restPer / Math.max(1L, warmupPer))
                        + " loggerSize=" + fixture.session.logger().snapshot().size()
                        + " dropped=" + fixture.session.logger().droppedCount());

        assertTrue(elapsedMs < MAX_ELAPSED_MS,
                "desktop observe budget exceeded " + MAX_ELAPSED_MS + " ms: " + elapsedMs);
        assertTrue(restPer < warmupPer * (long) RELATIVE_SLOWDOWN_CEILING,
                "later observes were more than "
                        + RELATIVE_SLOWDOWN_CEILING
                        + "x slower than warmup (possible superlinear logger scan/copy). "
                        + "warmupNs=" + warmupPer + " laterNs=" + restPer);
        assertTrue(fixture.session.logger().snapshot().size() <= CAPACITY);
    }

    private static Fixture fixture(int capacity) {
        SimulatedClock clock = new SimulatedClock();
        List<MotorElectricalTelemetry> motors = new ArrayList<MotorElectricalTelemetry>();
        motors.add(motor("frontLeft"));
        motors.add(motor("frontRight"));
        motors.add(motor("backLeft"));
        motors.add(motor("lift"));
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.recommended())
                .loggerCapacity(capacity)
                .build();
        AmperSession session = new AmperSession(
                policy,
                clock,
                RevHubTelemetrySource.voltageOnly("Control Hub", () -> 12.4),
                motors);
        return new Fixture(session, clock);
    }

    private static RevMotorTelemetry motor(String id) {
        return new RevMotorTelemetry(
                id,
                () -> 1.2,
                () -> 0.4,
                () -> 200.0,
                () -> 0.0,
                true);
    }

    private static final class Fixture {
        final AmperSession session;
        final SimulatedClock clock;

        Fixture(AmperSession session, SimulatedClock clock) {
            this.session = session;
            this.clock = clock;
        }
    }
}
