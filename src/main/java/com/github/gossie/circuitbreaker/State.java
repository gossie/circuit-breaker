package com.github.gossie.circuitbreaker;

import java.util.Arrays;
import java.util.Date;

class State {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private double maxErrorRatio;
    private long openTimePeriod;
    private int maxNumberOfSamples;

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

    public void incrementSuccessfulCalls() {
        samples[determinSampleIndex()] = true;
        if(status == Status.OPEN) {
            status = Status.HALF_OPEN;
        } else if(calculateCurrentRatio() <= maxErrorRatio) {
            status = Status.CLOSED;
        }
    }

    public void incrementUnsuccessfulCalls() {
        samples[determinSampleIndex()] = false;
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
        int successfulCalls = 0;
        int unsuccessfulCalls = 0;
        for(int i=0; i<samples.length; i++) {
            if(samples[i]) {
                ++successfulCalls;
                ++unsuccessfulCalls;
            }
        }

        if (successfulCalls == 0) {
            return 1.0;
        }
        return (double) unsuccessfulCalls / successfulCalls;
    }

    private int determinSampleIndex() {
        int currentIndex = sampleIndex % samples.length;
        sampleIndex = currentIndex + 1;
        return currentIndex;
    }
}
