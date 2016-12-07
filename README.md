[![Build Status](https://travis-ci.org/gossie/circuit-breaker.svg?branch=master)](https://travis-ci.org/gossie/circuit-breaker)
[![Coverage Status](https://coveralls.io/repos/github/gossie/circuit-breaker/badge.svg?branch=master)](https://coveralls.io/github/gossie/circuit-breaker?branch=master)

# Circuit Breaker

## Motivation
Software systems often have a lot of interfaces and communicate with other sofrware systems. An error in a neighbor system should not cascade into your system. A circuit breaker makes sure that your system stays stable even though a neighbor broke down.
TODO: example

## Usage

There are two different ways to integrate a CircuitBreaker...
Either way you need to add the circuitbreaker dependency to your project.

```xml
<dependency>
  <groupId>de.gmcs</groupId>
  <artifactId>circuitbreaker</artifactId>
  <version>${circuitbreaker.version}</version>
</dependency>
```

### Integraton with AspectJ
The CircuitBreaker class is annotated with the @Aspect annotation of the AspectJ framework. It wraps around all methods that are annotated with the `de.gmcs.circuitbreaker.IntegrationPoint` annotation.

```java
public class Client {
    private Service service;
    
    @IntegrationPoint(maxErrorRatio = 0.01, errorTimeout = 1000, openTimePeriod = 10000)
    private Result callService(Parameter p1, Parameter p2) {
        return service.performOperation(p1, p2);
    }
}
```

Add the aspectj-maven-plugin to make sure that the CircuitBreaker aspect is weaved around your IntegrationPoints.

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>aspectj-maven-plugin</artifactId>
  <version>1.9</version>
  <configuration>
    <weaveDependencies>
      <weaveDependency>
        <groupId>de.gmcs</groupId>
        <artifactId>circuitbreaker</artifactId>
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

### Programmatic integration
Currently there is no support to add a CircuitBreaker without AspectJ.
