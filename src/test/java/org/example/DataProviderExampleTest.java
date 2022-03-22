package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataProviderExample {

    private final MyClass myClass = new MyClass();

    @ParameterizedTest
    @MethodSource("provider")
    public void methodSumTest(int expected, int a, int b) {
                assertEquals(expected, myClass.getSum(a, b));
    }

    static Stream<Arguments> provider(){
        return Stream.of(
                Arguments.arguments(10, 7, 3),
                Arguments.arguments(15, 10, 5),
                Arguments.arguments(20, 12, 8)
        );
    }
}
