package com.github.gossie.circuitbreaker;

import java.util.Arrays;
import java.util.Date;

/**
 * The class holds the state of a {CircuitBreaker}.
 */
class State {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private double maxErrorRatio;
    private long openTimePeriod;

    private Status status = Status.CLOSED;
    private boolean[] samples;
    private int sampleIndex;
    private long openTimestamp;

    State(double maxErrorRatio, long openTimePeriod, int maxNumberOfSamples) {
        this.maxErrorRatio = maxErrorRatio;
        this.openTimePeriod = openTimePeriod;
        this.samples = new boolean[maxNumberOfSamples];
        Arrays.fill(samples, true);
    }

    /**
     * The method adds a possitive sample and changes the status if necessary.
     */
    public void incrementSuccessfulCalls() {
        samples[determinSampleIndex()] = true;
        if(status == Status.OPEN) {
            status = Status.HALF_OPEN;
        } else if(calculateCurrentRatio() <= maxErrorRatio) {
            status = Status.CLOSED;
        }
    }

    /**
     * The method adds a negative sample and changes the status if necessary.
     */
    public void incrementUnsuccessfulCalls() {
        samples[determinSampleIndex()] = false;
        if((status == Status.CLOSED && calculateCurrentRatio() > maxErrorRatio) || status == Status.HALF_OPEN || status == Status.OPEN) {
            openUp();
        }
    }

    /**
     * The method returns true if the status is OPEN. If the status is HALF_OPEN or CLOSED, the method will
     * will return false.
     *
     * @return Returns true if the status is OPEN and false otherwise.
     */
    public boolean isOpen() {
        long currentOpenTime = new Date().getTime() - openTimestamp;
        return status == Status.OPEN && currentOpenTime <= openTimePeriod;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("status: ")
                .append(status)
                .append(" samples: ")
                .append(samplesAsString())
                .append(" currentErrorRatio: ")
                .append(calculateCurrentRatio())
                .append(" maxErrorRatio: ")
                .append(maxErrorRatio)
                .toString();
    }

    private String samplesAsString() {
        StringBuilder result = new StringBuilder("[");
        for(int i=0; i<samples.length-1; i++) {
            result.append(samples[i]).append(", ");
        }
        return result.append(samples[samples.length-1]).append("]").toString();
    }

    private void openUp() {
        status = Status.OPEN;
        openTimestamp = new Date().getTime();
    }

    private double calculateCurrentRatio() {
        int unsuccessfulCalls = 0;
        for(int i=0; i<samples.length; i++) {
            if(!samples[i]) {
                ++unsuccessfulCalls;
            }
        }
        return (double) unsuccessfulCalls / samples.length;
    }

    private int determinSampleIndex() {
        int currentIndex = sampleIndex % samples.length;
        sampleIndex = currentIndex + 1;
        return currentIndex;
    }
}
