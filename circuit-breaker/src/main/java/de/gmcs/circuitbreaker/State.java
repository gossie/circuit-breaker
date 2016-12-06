package de.gmcs.circuitbreaker;

public class State {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private Status status = Status.CLOSED;
    private long successfulCalls;
    private long unsuccessfulCalls;


    public void incrementSuccessfulCalls() {
        ++successfulCalls;
    }

    public void incrementUnsuccessfulCalls() {
        ++unsuccessfulCalls;
    }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public void calculateStatus(double maxErrorRatio) {
        if (calculateCurrentRatio() > maxErrorRatio) {
            status = Status.OPEN;
        }
    }

    private double calculateCurrentRatio() {
        if (successfulCalls == 0L) {
            return 1.0;
        }
        return (double) unsuccessfulCalls / successfulCalls;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("status: ")
                .append(status)
                .append("\tsuccessfulCalls: ")
                .append(successfulCalls)
                .append("\tunsuccessfulCalls: ")
                .append(unsuccessfulCalls)
                .append("\tcurrentRatio: ")
                .append(calculateCurrentRatio())
                .toString();
    }
}
