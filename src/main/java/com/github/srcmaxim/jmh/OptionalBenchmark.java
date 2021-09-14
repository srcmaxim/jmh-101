package com.github.srcmaxim.jmh;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Fork(1)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
public class OptionalBenchmark {

    private static final int ARRAY_LENGTH = 10_000;

    @State(Scope.Benchmark)
    public static class TestData {

        List<String> someInput = new ArrayList<>(ARRAY_LENGTH);

        @Setup
        public void setup() {
            for (int i = 0; i < ARRAY_LENGTH; i++) {
                if (RandomUtils.nextDouble() < 0.001) {
                    someInput.add(null);
                } else if (RandomUtils.nextDouble() < 0.002) {
                    someInput.add("");
                } else {
                    someInput.add(RandomStringUtils.randomAlphabetic(10));
                }
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(ARRAY_LENGTH)
    public void useOptionalForProcessing(Blackhole bh, TestData td) {
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            bh.consume(theFirstWithOptional(td.someInput.get(i)));
        }
    }

    String theFirstWithOptional(String param) {
        return Optional.ofNullable(param)
                .filter(not(String::isEmpty))
                .filter(p -> p.length() > 5)
                .filter(p -> p.charAt(0) < 'M')
                .map(p -> "Present")
                .orElse("NOT_PRESENT");
    }

    @Benchmark
    @OperationsPerInvocation(ARRAY_LENGTH)
    public void useIfStatementForProcessing(Blackhole bh, TestData td) {
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            bh.consume(theFirstWithIf(td.someInput.get(i)));
        }
    }

    private String theFirstWithIf(String param) {
        return param != null && !param.isEmpty() && param.length() > 5 && (param.charAt(0) < 'M') ? "Present" : "NOT_PRESENT";
    }

}
