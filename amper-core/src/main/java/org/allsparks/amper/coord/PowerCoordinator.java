package org.allsparks.amper.coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.allsparks.amper.api.PowerGrant;
import org.allsparks.amper.api.PowerRequest;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Receives subsystem power requests and returns constraints or grants.
 * Does not own robot hardware.
 *
 * <p>Phase 0–1: always returns unrestricted grants. Active allocation belongs
 * to Phase 4+ and remains disabled by default.
 */
public final class PowerCoordinator {
    private final PowerPolicy policy;
    private final List<PowerRequest> lastRequests = new ArrayList<>();
    private final List<PowerGrant> lastGrants = new ArrayList<>();

    public PowerCoordinator(PowerPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public List<PowerGrant> allocate(List<PowerRequest> requests) {
        lastRequests.clear();
        lastGrants.clear();
        if (requests != null) {
            lastRequests.addAll(requests);
        }

        boolean intervene = policy.featureFlags().isPhase4Coordination()
                || policy.featureFlags().isAnyInterventionEnabled();

        for (PowerRequest request : lastRequests) {
            if (!intervene || !policy.featureFlags().isPhase4Coordination()) {
                lastGrants.add(PowerGrant.unrestricted(request.requestedEffort()));
            } else {
                // Placeholder: real priority allocation is Phase 4 work.
                lastGrants.add(PowerGrant.unrestricted(request.requestedEffort()));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(lastGrants));
    }

    public List<PowerRequest> lastRequests() {
        return Collections.unmodifiableList(new ArrayList<>(lastRequests));
    }

    public List<PowerGrant> lastGrants() {
        return Collections.unmodifiableList(new ArrayList<>(lastGrants));
    }
}
