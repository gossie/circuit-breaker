package com.github.gossie.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StateTest {

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
}
