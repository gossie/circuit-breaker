package com.github.gossie.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class StateTest {

    @Test
    public void testIsOpen_emptyState() {
        State state = new State(0.5, 100L, 5);

        assertThat(state.isOpen()).isEqualTo(false);
    }

    @Test
    public void testIsOpen_open() {
        State state = new State(0.5, 100L, 5);

        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementUnsuccessfulCalls();
        state.incrementUnsuccessfulCalls();

        assertThat(state.isOpen()).isEqualTo(true);
    }

    @Test
    public void testIsOpen_closesAfterOpenTimePeriod() {
        State state = new State(0.5, 100L, 5);

        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementUnsuccessfulCalls();
        state.incrementUnsuccessfulCalls();

        sleep(150L);

        assertThat(state.isOpen()).isEqualTo(false);
    }

    @Test
    public void testIsOpen_closed_open_halfOpen_open() {
        State state = new State(0.5, 100L, 5);

        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementUnsuccessfulCalls();
        state.incrementUnsuccessfulCalls();

        sleep(150L);

        state.incrementUnsuccessfulCalls();
        assertThat(state.isOpen()).isEqualTo(true);
    }

    @Test
    public void testIsOpen_samples() {
        State state = new State(0.2, 100L, 5);

        for(int i=0; i<10; ++i) {
            state.incrementSuccessfulCalls();
        }
        state.incrementUnsuccessfulCalls();
        state.incrementUnsuccessfulCalls();

        assertThat(state.isOpen()).isEqualTo(true);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
