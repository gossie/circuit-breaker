package com.github.gossie.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StateTest {

    @Test
    public void testIsOpen_emptyState() {
        State state = new State(0.5, 100L);

        assertThat(state.isOpen()).isEqualTo(false);
    }

    @Test
    public void testIsOpen_open() {
        State state = new State(0.5, 100L);

        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementSuccessfulCalls();
        state.incrementUnsuccessfulCalls();
        state.incrementUnsuccessfulCalls();

        assertThat(state.isOpen()).isEqualTo(true);
    }

    @Test
    public void testIsOpen_closesAfterOpenTimePeriod() {
        State state = new State(0.5, 100L);

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
        State state = new State(0.5, 100L);

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
    public void testToString() {
        String expected = new StringBuilder().append("status: ")
                .append("CLOSED")
                .append(" successfulCalls: ")
                .append(0)
                .append(" unsuccessfulCalls: ")
                .append(0)
                .append(" currentErrorRatio: ")
                .append(1.0)
                .append(" maxErrorRatio: ")
                .append(0.05)
                .toString();

        State state = new State(0.05, 5000);
        assertThat(state.toString()).isEqualTo(expected);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
