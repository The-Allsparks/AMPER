package org.allsparks.amper;

/**
 * Development phases for AMPER. Higher phases build on lower phases and are
 * disabled by default until acceptance criteria are met.
 *
 * <p>Phases 2+ may change motor outputs when explicitly enabled. Phase 0 and 1
 * must never command hardware.
 */
public enum AmperPhase {
    /** Measurement validation only. No intervention. */
    PHASE_0_MEASUREMENT,
    /** Passive instrumentation and warnings. No intervention. */
    PHASE_1_PASSIVE,
    /** Optional independent subsystem protections. */
    PHASE_2_LOCAL_PROTECTION,
    /** Reactive voltage state machine. */
    PHASE_3_REACTIVE,
    /** Priority-based load coordination. */
    PHASE_4_COORDINATION,
    /** Predictive voltage-sag estimation (shadow by default). */
    PHASE_5_PREDICTIVE_ESTIMATE,
    /** Predictive load shaping after validated predictions. */
    PHASE_6_PREDICTIVE_SHAPING,
    /** Optional adaptive modeling. Experimental. */
    PHASE_7_ADAPTIVE
}
