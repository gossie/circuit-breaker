[![Build Status](https://travis-ci.org/gossie/circuit-breaker.svg?branch=master)](https://travis-ci.org/gossie/circuit-breaker)
[![Coverage Status](https://coveralls.io/repos/github/gossie/circuit-breaker/badge.svg?branch=master)](https://coveralls.io/github/gossie/circuit-breaker?branch=master)

# Circuit Breaker
## Motivation and functionality
Let's suppose somewhere in your application there is a webservice call to retrieve some data from another system. If, for some reason, this webservice takes ages to respond or breaks down, you don't want your application to hang or break down as well. How does the CircuitBreaker help with that?
The CirctuitBreaker intercepts each call to the method that calls the webservice. If the CircuitBreaker has the status CLOSED, it performs the actual method call.
If a certain amount of calls were unsuccessful, that means an exception was caught or the call took to long, the CircuitBreaker will open up and stay open for a while. In that state every call to the webservice is rejected immediately, giving the webservice time to recover.
After a certain amount of time, the CircuitBreaker will switch to the status HALF_OPEN. In that state the calls are forwarded to the webservice. If there is a failure, the CircuitBreaker will open up again. If a certain number of calls succeed the CircuitBreaker will close.
You can configure the following:
* maxNumberOfSamples: The state of the CurcuitBreaker refers to the last n calls, where n is the maxNumberOfSamples
* maxErrorRation: What percentage of tracked calls has to fail until the CircuitBreaker opens up
* openTimePeriod: How long will the CircuitBreaker stay open and reject calls

## Sample code
The CircuitBreaker class is annotated with the @Aspect annotation of the AspectJ framework. It wraps around all methods that are annotated with the `com.github.gossie.circuitbreaker.IntegrationPoint` annotation.

```java
@IntegrationPointConfiguration(maxErrorRatio = 0.01, openTimePeriod = 10000, maxNumberOfSamples = 250)
public class Client {
    private WebService service;

    @IntegrationPoint(errorTimeout = 1000)
    private Result callService(Parameter p1, Parameter p2) {
        return service.performOperation(p1, p2);
    }
}
```
## Integration

```xml
<dependency>
  <groupId>com.github.gossie</groupId>
  <artifactId>circuit-breaker</artifactId>
  <version>0.1.0</version>
</dependency>
```
Add the aspectj-maven-plugin to make sure that the CircuitBreaker aspect is weaved around your IntegrationPoints.
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>aspectj-maven-plugin</artifactId>
  <version>1.9</version>
  <configuration>
    <complianceLevel>1.8</complianceLevel>
    <weaveDependencies>
      <weaveDependency>
        <groupId>com.github.gossie</groupId>
        <artifactId>circuit-breaker</artifactId>
      </weaveDependency>
    </weaveDependencies>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>compile</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```
