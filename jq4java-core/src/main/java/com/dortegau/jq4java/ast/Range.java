package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Range implements Expression {
    private final List<Expression> arguments;

    static {
        BuiltinRegistry.register("range", 1);
        BuiltinRegistry.register("range", 2);
        BuiltinRegistry.register("range", 3);
    }

    public Range(List<Expression> arguments) {
        this.arguments = arguments;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue input) {
        if (arguments.size() < 1 || arguments.size() > 3) {
            throw new RuntimeException("range/" + arguments.size() + " is not defined");
        }

        List<JqValue> args = new ArrayList<>();
        for (Expression arg : arguments) {
            List<JqValue> values = arg.evaluate(input).toList();
            if (values.size() != 1) {
                throw new RuntimeException("range arguments must produce exactly one value");
            }
            JqValue value = values.get(0);
            if (!value.isNumber()) {
                throw new RuntimeException("range argument must be a number");
            }
            args.add(value);
        }

        return generateRange(args);
    }

    private Stream<JqValue> generateRange(List<JqValue> args) {
        List<JqValue> result = new ArrayList<>();

        if (args.size() == 1) {
            // range(n) - generate [0, 1, ..., n-1]
            double to = args.get(0).asNumber();
            if (to <= 0) {
                return Stream.empty();
            }

            for (double i = 0; i < to; i += 1.0) {
                result.add(createNumberValue(i));
            }
        } else if (args.size() == 2) {
            // range(from; to) - generate [from, from+1, ..., to-1]
            double from = args.get(0).asNumber();
            double to = args.get(1).asNumber();

            if (from >= to) {
                return Stream.empty();
            }

            for (double i = from; i < to; i += 1.0) {
                result.add(createNumberValue(i));
            }
        } else if (args.size() == 3) {
            // range(from; to; step) - generate arithmetic sequence
            double from = args.get(0).asNumber();
            double to = args.get(1).asNumber();
            double step = args.get(2).asNumber();

            if (step == 0) {
                throw new RuntimeException("range step cannot be zero");
            }

            if (step > 0) {
                for (double i = from; i < to; i += step) {
                    result.add(createNumberValue(i));
                }
            } else {
                for (double i = from; i > to; i += step) {
                    result.add(createNumberValue(i));
                }
            }
        }

        return result.stream();
    }

    private JqValue createNumberValue(double value) {
        // If the value is a whole number, return it as an integer
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return JqValue.fromLong((long) value);
        } else {
            return JqValue.fromDouble(value);
        }
    }
}