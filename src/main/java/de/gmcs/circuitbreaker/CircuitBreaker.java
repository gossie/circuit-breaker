package de.gmcs.circuitbreaker;

import java.util.function.Function;
import java.util.Optional;

public class CircuitBreaker {

  private enum Status {
    OPEN,
    HALF_OPEN,
    CLOSED
  }

  private Status status = Status.OPEN;
  private long successfulCalls;
  private long unsuccessfulCalls;
  private double threashold;
  private Function<Object, Object> function;

  public CircuitBreaker(double threashold, Function<Object, Object> function) {
    this.threashold = threashold;
    this.function = function;
  }

  /**
   *
   * @param argument The argument is passed to the encapsulated function.
   * @return Returns an optional of the return value of the encapsulated function.
   */
  public Optional<Object> call(Object argument) {
    try {
      Object result = function.apply(argument);
      ++successfulCalls;
      return Optional.of(result);
    } catch(Exception e) {
      ++unsuccessfulCalls;
      return Optional.empty();
    }
  }
}
