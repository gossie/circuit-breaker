package de.gmcs.circuitbreaker;

import java.util.Date;

class State {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private double maxErrorRatio;
    private long openTimePeriod;

    private Status status = Status.CLOSED;
    private long successfulCalls;
    private long unsuccessfulCalls;
    private long openTimestamp;

    State(double maxErrorRatio, long openTimePeriod) {
        this.maxErrorRatio = maxErrorRatio;
        this.openTimePeriod = openTimePeriod;
    }

    public void incrementSuccessfulCalls() {
        ++successfulCalls;
        if(status == Status.OPEN) {
            status = Status.HALF_OPEN;
        } else if(status == Status.HALF_OPEN) {
            if(calculateCurrentRatio() <= maxErrorRatio) {
                status = Status.CLOSED;
            }
        }
    }

    public void incrementUnsuccessfulCalls() {
        ++unsuccessfulCalls;

        if((status == Status.CLOSED && calculateCurrentRatio() > maxErrorRatio) || status == Status.HALF_OPEN || status == Status.OPEN) {
            openUp();
        }
    }

    public boolean isOpen() {
        long currentOpenTime = new Date().getTime() - openTimestamp;
        return status == Status.OPEN && currentOpenTime <= openTimePeriod;
    }

    private void openUp() {
        status = Status.OPEN;
        openTimestamp = new Date().getTime();
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
