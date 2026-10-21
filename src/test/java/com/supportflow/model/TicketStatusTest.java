package com.supportflow.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketStatusTest {

    @Test
    void closedTicketHasNoTransitions() {
        assertTrue(TicketStatus.CLOSED.allowedTransitions().isEmpty());
    }

    @Test
    void noStatusCanTransitionToItself() {
        for (TicketStatus status : TicketStatus.values()) {
            assertFalse(status.canTransitionTo(status), status + " не должен переходить сам в себя");
        }
    }

    @Test
    void newTicketCannotBeResolvedDirectly() {
        assertFalse(TicketStatus.NEW.canTransitionTo(TicketStatus.RESOLVED));
    }

    @Test
    void happyPathIsAllowed() {
        assertTrue(TicketStatus.NEW.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.RESOLVED));
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED));
    }

    @Test
    void resolvedCanBeReopenedAndWorkResumes() {
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.REOPENED));
        assertEquals(java.util.Set.of(TicketStatus.IN_PROGRESS), TicketStatus.REOPENED.allowedTransitions());
    }
}
