package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StateTest {

    @Test
    public void testToString() {
        String expected = new StringBuilder().append("status: ")
                .append("CLOSED")
                .append("\tsuccessfulCalls: ")
                .append(0)
                .append("\tunsuccessfulCalls: ")
                .append(0)
                .append("\tcurrentRatio: ")
                .append(1.0)
                .toString();

        State state = new State(0.05, 5000);
        assertThat(state.toString()).isEqualTo(expected);
    }
}
