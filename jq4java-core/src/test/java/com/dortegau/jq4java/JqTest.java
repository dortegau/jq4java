package com.dortegau.jq4java;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JqTest {
    @ParameterizedTest
    @CsvSource({
        "true,  null, true",
        "false, null, false",
        "null,  42,   null",
        "1,     null, 1",
        "-1,    null, -1",
        "'\"hello\"', null, '\"hello\"'",
        "'\"byte order mark\"', null, '\"byte order mark\"'"
    })
    void testLiterals(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "., 42, 42",
        "., true, true",
        "., false, false",
        "., null, null",
        "., '\"hello\"', '\"hello\"'",
        "., '{\"a\":1}', '{\"a\":1}'",
        "., '[1,2,3]', '[1,2,3]'"
    })
    void testIdentity(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        ".foo, '{\"foo\": 42, \"bar\": 43}', 42",
        ".bar, '{\"foo\": 42, \"bar\": 43}', 43",
        ".foo_bar, '{\"foo_bar\": 2}', 2",
        ".foo.bar, '{\"foo\": {\"bar\": 42}, \"bar\": \"badvalue\"}', 42",
        ".bar, '{\"foo\": {\"bar\": 42}, \"bar\": \"goodvalue\"}', '\"goodvalue\"'",
        ".a.b.c.d.e.f.g.h.i.j, '{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":{\"g\":{\"h\":{\"i\":{\"j\":999}}}}}}}}}}', 999",
        ".missing, '{\"foo\": 1}', null",
        "'.\"foo\"', '{\"foo\": 20}', 20",
        "'.\"foo\".\"bar\"', '{\"foo\": {\"bar\": 20}}', 20",
        "'.[\"foo\"]', '{\"foo\": 42}', 42",
        "'.[\"my-key\"]', '{\"my-key\": 99}', 99",
        "'.[\"foo\"][\"bar\"]', '{\"foo\": {\"bar\": 42}}', 42",
        "'.[\"foo\"].bar', '{\"foo\": {\"bar\": 42}}', 42"
    })
    void testFieldAccess(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        ".foo | .bar, '{\"foo\": {\"bar\": 42}, \"bar\": \"badvalue\"}', 42",
        ". | ., '5', 5",
        ".a | .b | .c, '{\"a\":{\"b\":{\"c\":99}}}', 99",
        ".[0] | .x, '[{\"x\":1},{\"x\":2}]', 1",
        ".[] | ., '[1,2]', '1\n2'"
    })
    void testPipe(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        ".foo.bar, .foo | .bar, '{\"foo\": {\"bar\": 42}}'",
        ".foo.bar, .foo | .bar, '{\"foo\": {\"bar\": {\"baz\": 1}}}'",
        ".foo.bar, .foo | .bar, '{\"foo\": {\"bar\": [1,2,3]}}'"
    })
    void testFieldAccessPipeEquivalence(String composedFilter, String pipedFilter, String input) {
        assertEquals(
            Jq.execute(composedFilter, input),
            Jq.execute(pipedFilter, input)
        );
    }

    @ParameterizedTest
    @CsvSource({
        ".[], '[1,2,3]', '1\n2\n3'",
        ".[] | .[], '[[1,2],[3,4]]', '1\n2\n3\n4'",
        ".[], '[]', ''",
        ".[], '{}', ''"
    })
    void testArrayIteration(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        ".[0], '[1,2,3]', 1",
        ".[1], '[1,2,3]', 2",
        ".[2], '[1,2,3]', 3",
        ".[-1], '[1,2,3]', 3",
        ".[-2], '[1,2,3]', 2",
        ".[-3], '[1,2,3]', 1",
        ".[10], '[1,2,3]', null",
        ".[-10], '[1,2,3]', null"
    })
    void testArrayIndexing(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        ".[1:3], '[0,1,2,3,4]', '[1,2]'",
        ".[0:2], '[0,1,2,3,4]', '[0,1]'",
        ".[:2], '[0,1,2,3,4]', '[0,1]'",
        ".[2:], '[0,1,2,3,4]', '[2,3,4]'",
        ".[2:2], '[0,1,2,3,4]', '[]'",
        ".[-2:], '[0,1,2,3,4]', '[3,4]'",
        ".[:-2], '[0,1,2,3,4]', '[0,1,2]'",
        ".[-3:-1], '[0,1,2,3,4]', '[2,3]'"
    })
    void testArraySlicing(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'[.a, .b]' ; '{\"a\":1, \"b\":2}' ; '[1,2]'",
        "'[.x]' ; '{\"x\":42}' ; '[42]'",
        "'[.a, .b, .c]' ; '{\"a\":1, \"b\":2, \"c\":3}' ; '[1,2,3]'"
    }, delimiter = ';')
    void testArrayConstruction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'{a: .x}' ; '{\"x\":1}' ; '{\"a\":1}'",
        "'{a: .x, b: .y}' ; '{\"x\":1, \"y\":2}' ; '{\"a\":1,\"b\":2}'",
        "'{foo: .bar}' ; '{\"bar\":42}' ; '{\"foo\":42}'",
        "'{\"user-id\": .id}' ; '{\"id\":1}' ; '{\"user-id\":1}'",
        "'{\"my-key\": .x, \"other-key\": .y}' ; '{\"x\":1, \"y\":2}' ; '{\"my-key\":1,\"other-key\":2}'"
    }, delimiter = ';')
    void testObjectConstruction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'{a}' ; '{\"a\":1, \"b\":2}' ; '{\"a\":1}'",
        "'{a,b}' ; '{\"a\":1, \"b\":2}' ; '{\"a\":1,\"b\":2}'",
        "'{a,b,c}' ; '{\"a\":1, \"b\":2, \"c\":3}' ; '{\"a\":1,\"b\":2,\"c\":3}'",
        "'{foo}' ; '{\"foo\":42, \"bar\":43}' ; '{\"foo\":42}'",
        "'{x,y}' ; '{\"x\":10, \"y\":20, \"z\":30}' ; '{\"x\":10,\"y\":20}'",
        "'{nonexistent}' ; '{\"missing\":999}' ; '{\"nonexistent\":null}'"
    }, delimiter = ';')
    void testObjectShorthandSyntax(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'{a, b: .x}' ; '{\"a\":1, \"x\":2}' ; '{\"a\":1,\"b\":2}'",
        // "'{a, b: .y, c}' ; '{\"a\":1, \"y\":2, \"c\":3}' ; '{\"a\":1,\"b\":2,\"c\":3}'", // TODO: Fix shorthand parsing conflict
        "'{foo, \"bar\": .baz}' ; '{\"foo\":1, \"baz\":2}' ; '{\"foo\":1,\"bar\":2}'"
        // "'{a, b, c: .x + .y}' ; '{\"a\":1, \"b\":2, \"x\":3, \"y\":4}' ; '{\"a\":1,\"b\":2,\"c\":7}'" // TODO: Fix shorthand parsing conflict
    }, delimiter = ';')
    void testObjectMixedSyntax(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'[.[] | tojson]' ; '[1, \"foo\", [\"foo\"]]' ; '[\"1\",\"\"foo\"\",\"[\"foo\"]\"]'",
        "'tojson' ; '{\"a\":1,\"b\":[2,3]}' ; '\"{\"a\":1,\"b\":[2,3]}\"'",
        "'tojson' ; '\"line\\nbreak\"' ; '\"\"line\\\\nbreak\"\"'"
    }, delimiter = ';')
    void testToJson(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'fromjson' ; '\"{\"a\":1}\"' ; '{\"a\":1}'",
        "'fromjson' ; '\"[1,2,3]\"' ; '[1,2,3]'",
        "'fromjson' ; '\"\"foo\"\"' ; '\"foo\"'",
        "'tojson | fromjson' ; '{\"nested\": [1, {\"k\":\"v\"}]}' ; '{\"nested\":[1,{\"k\":\"v\"}]}'"
    }, delimiter = ';')
    void testFromJson(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'.a, .b' ; '{\"a\":1, \"b\":2}' ; '1\n2'",
        "'.foo, .bar' ; '{\"foo\":42, \"bar\":43}' ; '42\n43'",
        "'., .' ; '5' ; '5\n5'",
        "'.a, .b, .a' ; '{\"a\":1, \"b\":2}' ; '1\n2\n1'"
    }, delimiter = ';')
    void testCommaOperator(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'null // 3', null, 3",
        "'false // 3', null, 3",
        "'42 // 3', null, 42",
        "'0 // 3', null, 0",
        "'\"\" // \"default\"', null, '\"\"'",
        "'.name // \"Unknown\"', '{\"name\": null}', '\"Unknown\"'",
        "'.name // \"Unknown\"', '{}', '\"Unknown\"'",
        "'.name // \"Unknown\"', '{\"name\": \"Alice\"}', '\"Alice\"'"
    })
    void testAlternativeOperator(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'@base64' ; '\"hello\"' ; '\"aGVsbG8=\"'",
        "'@base64' ; '[104,101,108,108,111]' ; '\"aGVsbG8=\"'",
        "'@base64d' ; '\"aGVsbG8=\"' ; '\"hello\"'",
        "'@base64d' ; '\"4pyTIMOgIGxhIG1vZGU=\"' ; '\"✓ à la mode\"'"
    }, delimiter = ';')
    void testBase64FormatFilters(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'@uri' ; '\"hello world\"' ; '\"hello%20world\"'",
        "'@uri' ; '\"café\"' ; '\"caf%C3%A9\"'",
        "'@uri' ; '\"a+b?=c\"' ; '\"a%2Bb%3F%3Dc\"'",
        "'@uri' ; '\"\"' ; '\"\"'",
        "'@urid' ; '\"hello%20world\"' ; '\"hello world\"'",
        "'@urid' ; '\"caf%C3%A9\"' ; '\"café\"'",
        "'@urid' ; '\"a%2Bb%3F%3Dc\"' ; '\"a+b?=c\"'",
        "'@urid' ; '\"\"' ; '\"\"'"
    }, delimiter = ';')
    void testUrlEncodingFunctions(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'.users[1:] | .[].email' ; '{\"users\":[{\"name\":\"Alice\",\"email\":\"alice@example.com\"},{\"name\":\"Bob\",\"email\":\"bob@example.com\"},{\"name\":\"Charlie\",\"email\":\"charlie@example.com\"}]}' ; '\"bob@example.com\"\n\"charlie@example.com\"'",
        "'.products[-1].name' ; '{\"products\":[{\"name\":\"Laptop\",\"price\":999},{\"name\":\"Mouse\",\"price\":25}]}' ; '\"Mouse\"'",
        "'.items[0:2] | .[].data.value' ; '{\"items\":[{\"data\":{\"value\":10}},{\"data\":{\"value\":20}},{\"data\":{\"value\":30}}]}' ; '10\n20'",
        "'.records[-2].status' ; '{\"records\":[{\"status\":\"pending\"},{\"status\":\"active\"},{\"status\":\"done\"}]}' ; '\"active\"'",
        "'.user.name // .profile.displayName // \"Anonymous\"' ; '{\"profile\":{\"displayName\":\"John\"}}' ; '\"John\"'",
        "'{name: .name // \"Unknown\", age: .age // 0}' ; '{\"name\":\"Alice\"}' ; '{\"name\":\"Alice\",\"age\":0}'",
        "'[.primary // .secondary, .tertiary]' ; '{\"secondary\":2,\"tertiary\":3}' ; '[2,3]'",
        "'.[\"user-name\"] // .username // \"guest\"' ; '{\"username\":\"bob\"}' ; '\"bob\"'",
        "'{\"user-info\": {\"full-name\": .name}}' ; '{\"name\":\"Alice\"}' ; '{\"user-info\":{\"full-name\":\"Alice\"}}'",
        "'.a // .b, .c // .d' ; '{\"b\":2,\"c\":3}' ; '2\n3'",
        "'[.a > .b, .c == .d]' ; '{\"a\":10,\"b\":5,\"c\":3,\"d\":3}' ; '[true,true]'",
        "'{adult: .age >= 18, minor: .age < 18}' ; '{\"age\":25}' ; '{\"adult\":true,\"minor\":false}'",
        "'.age > 18 // false' ; '{\"age\":null}' ; 'false'",
        "'.age > 18 // false' ; '{\"age\":25}' ; 'true'",
        "'null < 10' ; 'null' ; 'true'",
        "'null > 10' ; 'null' ; 'false'",
        "'.price < 100, .stock > 0' ; '{\"price\":50,\"stock\":10}' ; 'true\ntrue'",
        "'[.[] > 5]' ; '[1,10,3,8]' ; '[false,true,false,true]'",
        "'.users[] | {name: .name, adult: .age >= 18}' ; '{\"users\":[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Bob\",\"age\":15}]}' ; '{\"name\":\"Alice\",\"adult\":true}\n{\"name\":\"Bob\",\"adult\":false}'",
        "'.items | length' ; '{\"items\":[1,2,3]}' ; '3'",
        "'[.a, .b] | length' ; '{\"a\":1,\"b\":2}' ; '2'",
        "'.name | length' ; '{\"name\":\"Alice\"}' ; '5'",
        "'.[] | length' ; '[\"hi\",\"bye\"]' ; '2\n3'",
        "'{count: .items | length}' ; '{\"items\":[1,2,3,4]}' ; '{\"count\":4}'",
        "'.items | length > 2' ; '{\"items\":[1,2,3]}' ; 'true'",
        "'.name | length == 5' ; '{\"name\":\"Alice\"}' ; 'true'",
        "'[.[] | length]' ; '[\"a\",\"bb\",\"ccc\"]' ; '[1,2,3]'",
        "'.a + .b > 10' ; '{\"a\":5,\"b\":8}' ; 'true'",
        "'[.a * 2, .b - 1]' ; '{\"a\":5,\"b\":3}' ; '[10,2]'",
        "'{sum: .a + .b, diff: .a - .b}' ; '{\"a\":10,\"b\":3}' ; '{\"sum\":13,\"diff\":7}'",
        "'.[] | . * 2' ; '[1,2,3]' ; '2\n4\n6'",
        "'.price * .quantity' ; '{\"price\":10,\"quantity\":5}' ; '50'",
        "'.a > 5 and .b < 10' ; '{\"a\":8,\"b\":3}' ; 'true'",
        "'.a > 5 or .b > 10' ; '{\"a\":2,\"b\":15}' ; 'true'",
        "'.active and .verified' ; '{\"active\":true,\"verified\":false}' ; 'false'",
        "'[.[] > 5] | .[0] and .[1]' ; '[10,8]' ; 'true'",
        "'{a:1,b:2} | keys | length' ; 'null' ; '2'",
        "'{a:1,b:2} | keys | .[0]' ; 'null' ; '\"a\"'",
        "'[1,2,3] | keys | .[]' ; 'null' ; '0\n1\n2'",
        "'. | type' ; '[1,2,3]' ; '\"array\"'",
        "'.[] | type' ; '[1,\"a\",true]' ; '\"number\"\n\"string\"\n\"boolean\"'",
        "'. | type == \"array\"' ; '[1,2]' ; 'true'",
        "'map(. * 2) | .[0]' ; '[1,2,3]' ; '2'",
        "'map(.a) | length' ; '[{\"a\":1},{\"a\":2}]' ; '2'",
        "'map(. + 1) | map(. * 2)' ; '[1,2,3]' ; '[4,6,8]'",
        "'{a,b} | keys' ; '{\"a\":1,\"b\":2,\"c\":3}' ; '[\"a\",\"b\"]'",
        "'{a,b} | length' ; '{\"a\":1,\"b\":2,\"c\":3}' ; '2'",
        // "'{name: .user.name, age} | .name' ; '{\"user\":{\"name\":\"Alice\"},\"age\":30}' ; '\"Alice\"'", // TODO: Fix shorthand parsing conflict
        "'[{a},{b,c}]' ; '{\"a\":1,\"b\":2,\"c\":3}' ; '[{\"a\":1},{\"b\":2,\"c\":3}]'",
        "'{a,b} // {\"default\":true}' ; '{\"a\":1,\"b\":2}' ; '{\"a\":1,\"b\":2}'",
        "'{x} // {\"default\":true}' ; '{\"y\":1}' ; '{\"x\":null}'",
        "'map({a})' ; '[{\"a\":1,\"b\":2},{\"a\":3,\"b\":4}]' ; '[{\"a\":1},{\"a\":3}]'",
        "'{a,b}.a + {c,d}.c' ; '{\"a\":5,\"b\":10,\"c\":3,\"d\":7}' ; '8'",
        "'map(select(. > 2))' ; '[1,2,3,4,5]' ; '[3,4,5]'",
        "'map(select(.age >= 18))' ; '[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Bob\",\"age\":15},{\"name\":\"Charlie\",\"age\":30}]' ; '[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Charlie\",\"age\":30}]'",
        "'map(select(length > 2))' ; '[\"a\",\"bb\",\"ccc\",\"dddd\"]' ; '[\"ccc\",\"dddd\"]'",
        "'map(select(type == \"number\"))' ; '[1,\"a\",2,true,3]' ; '[1,2,3]'",
        "'.users | map(select(.active))' ; '{\"users\":[{\"name\":\"Alice\",\"active\":true},{\"name\":\"Bob\",\"active\":false},{\"name\":\"Charlie\",\"active\":true}]}' ; '[{\"name\":\"Alice\",\"active\":true},{\"name\":\"Charlie\",\"active\":true}]'",
        "'map(select(. > 0) | . * 2)' ; '[-1,2,-3,4]' ; '[4,8]'",
        "'.items | map(select(.price < 100)) | length' ; '{\"items\":[{\"price\":50},{\"price\":150},{\"price\":30}]}' ; '2'",
        "'[.[] | select(. % 2 == 0)]' ; '[1,2,3,4,5,6]' ; '[2,4,6]'",
        "'map(select(.status == \"active\") | .name)' ; '[{\"name\":\"Alice\",\"status\":\"active\"},{\"name\":\"Bob\",\"status\":\"inactive\"},{\"name\":\"Charlie\",\"status\":\"active\"}]' ; '[\"Alice\",\"Charlie\"]'"
    }, delimiter = ';')
    void testCombinedOperations(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "builtins, null, array",
        "'builtins | length > 0', null, true"
    })
    void testBuiltins(String program, String input, String expected) {
        String result = Jq.execute(program, input);
        if (expected.equals("array")) {
            assertEquals(true, result.startsWith("[") && result.endsWith("]"));
        } else {
            assertEquals(expected, result);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "'[1,2,3] | length', null, 3",
        "'[] | length', null, 0",
        "'{\"a\":1,\"b\":2} | length', null, 2",
        "'{} | length', null, 0",
        "'\"hello\" | length', null, 5",
        "'\"\" | length', null, 0",
        "'null | length', null, 0",
        "'5 | length', null, 5",
        "'0 | length', null, 0",
        "'-3 | length', null, 3"
    })
    void testLength(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'5 + 3', null, 8",
        "'5 - 3', null, 2",
        "'5 * 3', null, 15",
        "'10 / 4', null, 2.5",
        "'10 % 3', null, 1",
        "'0 + 0', null, 0",
        "'-5 + 3', null, -2",
        "'10 / 2', null, 5",
        "'.a + .b', '{\"a\":5,\"b\":3}', 8",
        "'.a - .b', '{\"a\":5,\"b\":3}', 2",
        "'.a * .b', '{\"a\":5,\"b\":3}', 15",
        "'\"hello\" + \" world\"', null, '\"hello world\"'",
        "'[1,2] + [3,4]', null, '[1,2,3,4]'"
    })
    void testArithmeticOperators(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'map(. * 2)' ; '[1,2,3]' ; '[2,4,6]'",
        "'map(. + 1)' ; '[1,2,3]' ; '[2,3,4]'",
        "'map(.a)' ; '[{\"a\":1},{\"a\":2}]' ; '[1,2]'",
        "'map(. * 2)' ; '[]' ; '[]'"
    }, delimiter = ';')
    void testMap(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'. | type', 'null', '\"null\"'",
        "'5 | type', '{}', '\"number\"'",
        "'\"hello\" | type', '{}', '\"string\"'",
        "'true | type', '{}', '\"boolean\"'",
        "'false | type', '{}', '\"boolean\"'",
        "'[1,2] | type', '{}', '\"array\"'",
        "'{\"a\":1} | type', '{}', '\"object\"'",
        "'. | type', '5', '\"number\"'",
        "'. | type', '\"test\"', '\"string\"'"
    })
    void testType(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'{\"b\":2,\"a\":1,\"c\":3} | keys' ; 'null' ; '[\"a\",\"b\",\"c\"]'",
        "'[42,3,35] | keys' ; 'null' ; '[0,1,2]'",
        "'{} | keys' ; 'null' ; '[]'",
        "'[] | keys' ; 'null' ; '[]'"
    }, delimiter = ';')
    void testKeys(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'true and false', null, false",
        "'true and true', null, true",
        "'false and false', null, false",
        "'true or false', null, true",
        "'false or false', null, false",
        "'true or true', null, true",
        "'true | not', null, false",
        "'false | not', null, true",
        "'null and true', null, false",
        "'false and true', null, false",
        "'null or true', null, true",
        "'5 | not', null, false",
        "'null | not', null, true"
    })
    void testLogicalOperators(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'map(select(. > 3))' ; '[1,2,3,4,5]' ; '[4,5]'",
        "'map(select(. > 10))' ; '[1,2,3,4,5]' ; '[]'",
        "'map(select(.a > 1))' ; '[{\"a\":1},{\"a\":2},{\"a\":3}]' ; '[{\"a\":2},{\"a\":3}]'",
        "'select(. == \"hello\")' ; '\"hello\"' ; '\"hello\"'",
        "'select(. == \"world\")' ; '\"hello\"' ; ''",
        "'select(.)' ; 'true' ; 'true'",
        "'select(.)' ; 'false' ; ''",
        "'select(.)' ; 'null' ; ''",
        "'select(.)' ; '0' ; '0'",
        "'select(.)' ; '\"\"' ; '\"\"'",
        "'select(.)' ; '[]' ; '[]'",
        "'select(.)' ; '{}' ; '{}'",
        "'select(.name == \"Alice\")' ; '{\"name\":\"Alice\",\"age\":25}' ; '{\"name\":\"Alice\",\"age\":25}'",
        "'select(.name == \"Bob\")' ; '{\"name\":\"Alice\",\"age\":25}' ; ''",
        "'select(length > 2)' ; '\"hello\"' ; '\"hello\"'",
        "'select(length > 10)' ; '\"hello\"' ; ''",
        "'map(select(type == \"string\"))' ; '[1,\"a\",true,\"b\"]' ; '[\"a\",\"b\"]'"
    }, delimiter = ';')
    void testSelect(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'[10 > 0, 10 > 10, 10 > 20, 10 < 0, 10 < 10, 10 < 20]' ; '{}' ; '[true,false,false,false,false,true]'",
        "'[10 >= 0, 10 >= 10, 10 >= 20, 10 <= 0, 10 <= 10, 10 <= 20]' ; '{}' ; '[true,true,false,false,true,true]'",
        "'[10 == 10, 10 != 10, 10 != 11, 10 == 11]' ; '{}' ; '[true,false,true,false]'",
        "'[\"hello\" == \"hello\", \"hello\" != \"hello\", \"hello\" == \"world\", \"hello\" != \"world\"]' ; '{}' ; '[true,false,false,true]'",
        "'[[1,2,3] == [1,2,3], [1,2,3] != [1,2,3], [1,2,3] == [4,5,6], [1,2,3] != [4,5,6]]' ; '{}' ; '[true,false,false,true]'",
        "'[{\"foo\":42} == {\"foo\":42}, {\"foo\":42} != {\"foo\":42}, {\"foo\":42} != {\"bar\":42}, {\"foo\":42} == {\"bar\":42}]' ; '{}' ; '[true,false,true,false]'"
    }, delimiter = ';')
    void testComparisonOperators(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": true}', '\"yes\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": false}', '\"no\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": null}', '\"no\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": 0}', '\"yes\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": \"\"}', '\"yes\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": []}', '\"yes\"'",
        "'if .foo then \"yes\" else \"no\" end', '{\"foo\": {}}', '\"yes\"'",
        "'if .foo then \"yes\" else \"no\" end', '{}', '\"no\"'"
    })
    void testBasicConditional(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'if .baz then \"strange\" elif .foo then \"yep\" else \"nope\" end', '{\"foo\": 1, \"bar\": 2}', '\"yep\"'",
        "'if .baz then \"strange\" elif .foo then \"yep\" else \"nope\" end', '{\"baz\": true, \"foo\": 1}', '\"strange\"'",
        "'if .baz then \"strange\" elif .foo then \"yep\" else \"nope\" end', '{\"bar\": 2}', '\"nope\"'"
    })
    void testElifConditional(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'if true then 3 end', '7', '3'",
        "'if false then 3 end', '7', '7'",
        "'if .foo then \"yes\" end', '{\"foo\": true}', '\"yes\"'",
        "'if .foo then \"yes\" end', '{\"foo\": false}', '{\"foo\":false}'"
    })
    void testOptionalElse(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'if 1,null,2 then 3 else 4 end', 'null', '3\n4\n3'"
    })
    void testMultipleConditions(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'if .a > 5 then \"big\" else \"small\" end' ; '{\"a\": 10}' ; '\"big\"'",
        "'if .a > 5 then \"big\" else \"small\" end' ; '{\"a\": 3}' ; '\"small\"'",
        "'if .active and .verified then \"valid\" else \"invalid\" end' ; '{\"active\": true, \"verified\": true}' ; '\"valid\"'",
        "'if .name then .name else \"anonymous\" end' ; '{\"name\": \"Alice\"}' ; '\"Alice\"'",
        "'if .name then .name else \"anonymous\" end' ; '{}' ; '\"anonymous\"'",
        "'[.[] | if . > 2 then . * 2 else . end]' ; '[1,2,3,4]' ; '[1,2,6,8]'",
        "'{result: if .score >= 90 then \"A\" elif .score >= 80 then \"B\" else \"F\" end}' ; '{\"score\": 95}' ; '{\"result\":\"A\"}'",
        "'{result: if .score >= 90 then \"A\" elif .score >= 80 then \"B\" else \"F\" end}' ; '{\"score\": 85}' ; '{\"result\":\"B\"}'",
        "'{result: if .score >= 90 then \"A\" elif .score >= 80 then \"B\" else \"F\" end}' ; '{\"score\": 75}' ; '{\"result\":\"F\"}'",
        "'if .status == \"active\" then \"enabled\" else \"disabled\" end' ; '{\"status\": \"active\"}' ; '\"enabled\"'",
        "'if .value // false then \"has value\" else \"no value\" end' ; '{\"value\": null}' ; '\"no value\"'",
        "'if .items | length > 0 then \"not empty\" else \"empty\" end' ; '{\"items\": [1,2,3]}' ; '\"not empty\"'"
    }, delimiter = ';')
    void testConditionalCombinations(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'if .a > 3 then (if .b > 8 then \"both high\" else \"a high, b low\" end) else \"a low\" end' ; '{\"a\":5,\"b\":10}' ; '\"both high\"'",
        "'if .a > 3 then (if .b > 8 then \"both high\" else \"a high, b low\" end) else \"a low\" end' ; '{\"a\":5,\"b\":5}' ; '\"a high, b low\"'",
        "'if .a > 3 then (if .b > 8 then \"both high\" else \"a high, b low\" end) else \"a low\" end' ; '{\"a\":2,\"b\":10}' ; '\"a low\"'",
        "'if length > 2 then (if .[0] == 1 then \"starts with 1\" else \"starts with other\" end) else \"short array\" end' ; '[1,2,3]' ; '\"starts with 1\"'",
        "'if length > 2 then (if .[0] == 1 then \"starts with 1\" else \"starts with other\" end) else \"short array\" end' ; '[5,2,3]' ; '\"starts with other\"'",
        "'if length > 2 then (if .[0] == 1 then \"starts with 1\" else \"starts with other\" end) else \"short array\" end' ; '[5]' ; '\"short array\"'",
        "'if .user then (if .user.admin then \"admin user\" elif .user.active then \"active user\" else \"inactive user\" end) else \"no user\" end' ; '{\"user\":{\"admin\":true,\"active\":true}}' ; '\"admin user\"'",
        "'if .user then (if .user.admin then \"admin user\" elif .user.active then \"active user\" else \"inactive user\" end) else \"no user\" end' ; '{\"user\":{\"admin\":false,\"active\":true}}' ; '\"active user\"'",
        "'if .user then (if .user.admin then \"admin user\" elif .user.active then \"active user\" else \"inactive user\" end) else \"no user\" end' ; '{\"user\":{\"admin\":false,\"active\":false}}' ; '\"inactive user\"'",
        "'if .user then (if .user.admin then \"admin user\" elif .user.active then \"active user\" else \"inactive user\" end) else \"no user\" end' ; '{}' ; '\"no user\"'",
        "'if .items | length > 0 then (if .items[0].priority == \"high\" then \"urgent\" else \"normal\" end) else \"empty\" end' ; '{\"items\":[{\"priority\":\"high\",\"task\":\"fix bug\"}]}' ; '\"urgent\"'",
        "'if .items | length > 0 then (if .items[0].priority == \"high\" then \"urgent\" else \"normal\" end) else \"empty\" end' ; '{\"items\":[{\"priority\":\"low\",\"task\":\"cleanup\"}]}' ; '\"normal\"'",
        "'if .items | length > 0 then (if .items[0].priority == \"high\" then \"urgent\" else \"normal\" end) else \"empty\" end' ; '{\"items\":[]}' ; '\"empty\"'",
        "'if . > 0 then (if . > 10 then (if . > 100 then \"very large\" else \"large\" end) else \"small positive\" end) else \"non-positive\" end' ; '150' ; '\"very large\"'",
        "'if . > 0 then (if . > 10 then (if . > 100 then \"very large\" else \"large\" end) else \"small positive\" end) else \"non-positive\" end' ; '50' ; '\"large\"'",
        "'if . > 0 then (if . > 10 then (if . > 100 then \"very large\" else \"large\" end) else \"small positive\" end) else \"non-positive\" end' ; '5' ; '\"small positive\"'",
        "'if . > 0 then (if . > 10 then (if . > 100 then \"very large\" else \"large\" end) else \"small positive\" end) else \"non-positive\" end' ; '-5' ; '\"non-positive\"'",
        "'[.[] | if .active then (if .priority == \"high\" then .name + \" (urgent)\" else .name end) else \"disabled\" end]' ; '[{\"name\":\"task1\",\"active\":true,\"priority\":\"high\"},{\"name\":\"task2\",\"active\":true,\"priority\":\"low\"},{\"name\":\"task3\",\"active\":false}]' ; '[\"task1 (urgent)\",\"task2\",\"disabled\"]'",
        "'{status: if .config.enabled then (if .config.debug then \"debug mode\" else \"normal mode\" end) else \"disabled\" end}' ; '{\"config\":{\"enabled\":true,\"debug\":true}}' ; '{\"status\":\"debug mode\"}'",
        "'{status: if .config.enabled then (if .config.debug then \"debug mode\" else \"normal mode\" end) else \"disabled\" end}' ; '{\"config\":{\"enabled\":true,\"debug\":false}}' ; '{\"status\":\"normal mode\"}'",
        "'{status: if .config.enabled then (if .config.debug then \"debug mode\" else \"normal mode\" end) else \"disabled\" end}' ; '{\"config\":{\"enabled\":false}}' ; '{\"status\":\"disabled\"}'",
        "'if .data then (if .data | type == \"array\" then (if .data | length > 0 then \"non-empty array\" else \"empty array\" end) else \"not array\" end) else \"no data\" end' ; '{\"data\":[1,2,3]}' ; '\"non-empty array\"'",
        "'if .data then (if .data | type == \"array\" then (if .data | length > 0 then \"non-empty array\" else \"empty array\" end) else \"not array\" end) else \"no data\" end' ; '{\"data\":[]}' ; '\"empty array\"'",
        "'if .data then (if .data | type == \"array\" then (if .data | length > 0 then \"non-empty array\" else \"empty array\" end) else \"not array\" end) else \"no data\" end' ; '{\"data\":\"string\"}' ; '\"not array\"'",
        "'if .data then (if .data | type == \"array\" then (if .data | length > 0 then \"non-empty array\" else \"empty array\" end) else \"not array\" end) else \"no data\" end' ; '{}' ; '\"no data\"'"
    }, delimiter = ';')
    void testNestedConditionals(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "flatten, '[[1,2],[3,4]]', '[1,2,3,4]'",
        "flatten, '[[[1],[2]],[[3],[4]]]', '[[1],[2],[3],[4]]'",
        "flatten, '[]', '[]'",
        "flatten, '[1,2,3]', '[1,2,3]'"
    })
    void testFlattenFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "add, '[1,2,3]', '6'",
        "add, '[\"a\",\"b\",\"c\"]', '\"abc\"'",
        "add, '[{\"a\":1},{\"b\":2}]', '{\"a\":1,\"b\":2}'",
        "add, '[[1,2],[3,4]]', '[1,2,3,4]'",
        "add, '[]', 'null'"
    })
    void testAddFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "sort, '[3,1,2]', '[1,2,3]'",
        "sort, '[\"c\",\"a\",\"b\"]', '[\"a\",\"b\",\"c\"]'",
        "sort, '[]', '[]'",
        "sort, '[1]', '[1]'"
    })
    void testSortFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "reverse, '[1,2,3]', '[3,2,1]'",
        "reverse, '[\"a\",\"b\",\"c\"]', '[\"c\",\"b\",\"a\"]'",
        "reverse, '[]', '[]'",
        "reverse, '[1]', '[1]'"
    })
    void testReverseFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "unique, '[1,2,3,2,1]', '[1,2,3]'",
        "unique, '[\"a\",\"b\",\"a\"]', '[\"a\",\"b\"]'",
        "unique, '[]', '[]'",
        "unique, '[1,1,1]', '[1]'"
    })
    void testUniqueFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "transpose, '[[1,2],[3,4]]', '[[1,3],[2,4]]'",
        "transpose, '[[1,2,3],[4,5,6]]', '[[1,4],[2,5],[3,6]]'",
        "transpose, '[]', '[]'",
        "transpose, '[[]]', '[]'"
    })
    void testTransposeFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'[range(5)]', 'null', '[0,1,2,3,4]'",
        "'[range(0)]', 'null', '[]'",
        "'[range(-3)]', 'null', '[]'",
        "'[range(1)]', 'null', '[0]'",
        "'[range(3)]', 'null', '[0,1,2]'"
    })
    void testRangeFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'[range(0;5)]', 'null', '[0,1,2,3,4]'",
        "'[range(2;7)]', 'null', '[2,3,4,5,6]'",
        "'[range(5;5)]', 'null', '[]'",
        "'[range(5;3)]', 'null', '[]'",
        "'[range(-2;3)]', 'null', '[-2,-1,0,1,2]'",
        "'[range(0;1)]', 'null', '[0]'"
    })
    void testRangeTwoArgs(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'[range(0;10;3)]', 'null', '[0,3,6,9]'",
        "'[range(0;-5;-1)]', 'null', '[0,-1,-2,-3,-4]'",
        "'[range(2;8;2)]', 'null', '[2,4,6]'",
        "'[range(10;0;-2)]', 'null', '[10,8,6,4,2]'",
        "'[range(1;10;3)]', 'null', '[1,4,7]'",
        "'[range(5;5;1)]', 'null', '[]'",
        "'[range(0;5;-1)]', 'null', '[]'",
        "'[range(5;0;1)]', 'null', '[]'"
    })
    void testRangeThreeArgs(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource({
        "'[range(1.5;5)]', 'null', '[1.5,2.5,3.5,4.5]'",
        "'[range(0.5;3.5;0.5)]', 'null', '[0.5,1,1.5,2,2.5,3]'",
        "'[range(-1.5;1.5;0.5)]', 'null', '[-1.5,-1,-0.5,0,0.5,1]'"
    })
    void testRangeFloatingPoint(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'range(3) | . * 2' ; 'null' ; '0\n2\n4'",
        "'range(2;5) | . + 10' ; 'null' ; '12\n13\n14'",
        "'[range(1;4) | select(. > 1)]' ; 'null' ; '[2,3]'"
    }, delimiter = ';')
    void testRangeCombinations(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'to_entries' ; '{\"a\": 1, \"b\": 2}' ; '[{\"key\":\"a\",\"value\":1},{\"key\":\"b\",\"value\":2}]'",
        "'to_entries' ; '{}' ; '[]'",
        "'to_entries' ; '[\"a\", \"b\", \"c\"]' ; '[{\"key\":0,\"value\":\"a\"},{\"key\":1,\"value\":\"b\"},{\"key\":2,\"value\":\"c\"}]'",
        "'to_entries' ; '[]' ; '[]'",
        "'to_entries' ; '{\"x\": {\"nested\": true}, \"y\": [1,2,3]}' ; '[{\"key\":\"x\",\"value\":{\"nested\":true}},{\"key\":\"y\",\"value\":[1,2,3]}]'",
        "'to_entries' ; '{\"a\": null}' ; '[{\"key\":\"a\",\"value\":null}]'"
    }, delimiter = ';')
    void testToEntries(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'from_entries' ; '[{\"key\": \"a\", \"value\": 1}, {\"key\": \"b\", \"value\": 2}]' ; '{\"a\":1,\"b\":2}'",
        "'from_entries' ; '[]' ; '{}'",
        "'from_entries' ; '[{\"name\": \"a\", \"value\": 1}, {\"name\": \"b\", \"value\": 2}]' ; '{\"a\":1,\"b\":2}'",
        "'from_entries' ; '[{\"key\": \"a\"}, {\"key\": \"b\", \"value\": 2}]' ; '{\"a\":null,\"b\":2}'",
        "'from_entries' ; '[{\"key\": \"x\", \"value\": {\"nested\": true}}]' ; '{\"x\":{\"nested\":true}}'",
        "'from_entries' ; '[{\"key\": \"numbers\", \"value\": [1,2,3]}]' ; '{\"numbers\":[1,2,3]}'",
        "'from_entries' ; '[{\"key\": \"test\", \"value\": null}]' ; '{\"test\":null}'"
    }, delimiter = ';')
    void testFromEntries(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'to_entries | from_entries' ; '{\"x\": 42, \"y\": 99}' ; '{\"x\":42,\"y\":99}'",
        "'to_entries | from_entries' ; '[\"hello\", \"world\"]' ; '{\"0\":\"hello\",\"1\":\"world\"}'",
        "'map(to_entries)' ; '[{\"a\":1},{\"b\":2}]' ; '[[{\"key\":\"a\",\"value\":1}],[{\"key\":\"b\",\"value\":2}]]'",
        "'[.[] | to_entries]' ; '[{\"x\":1},{\"y\":2}]' ; '[[{\"key\":\"x\",\"value\":1}],[{\"key\":\"y\",\"value\":2}]]'"
    }, delimiter = ';')
    void testToFromEntriesCombinations(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'with_entries({key: .key, value: (.value + 1)})' ; '{\"a\": 1, \"b\": 2}' ; '{\"a\":2,\"b\":3}'",
        "'with_entries(select(.key == \"b\"))' ; '{\"a\": 1, \"b\": 2}' ; '{\"b\":2}'",
        "'with_entries({key: (\"prefix_\" + .key), value: .value})' ; '{\"a\": 1}' ; '{\"prefix_a\":1}'",
        "'with_entries(.)' ; '[1,2]' ; '{\"0\":1,\"1\":2}'"
    }, delimiter = ';')
    void testWithEntries(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        // Basic unary minus with numbers (happy path)
        "'-.' ; '5' ; '-5'",
        "'-.' ; '-3' ; '3'",
        "'-.' ; '0' ; '0'",
        "'-.' ; '0.5' ; '-0.5'",
        "'-.' ; '-0.5' ; '0.5'",

        // Object construction with unary minus (the original failing case)
        "'{x:-1}' ; '1' ; '{\"x\":-1}'",
        "'{x:-.}' ; '1' ; '{\"x\":-1}'",
        "'{x:-.}' ; '-5' ; '{\"x\":5}'",

        // Multiple expressions with unary minus
        "'{x:-1},{x:-.}' ; '5' ; '{\"x\":-1}\n{\"x\":-5}'",
        "'{a:-., b:-.}' ; '3' ; '{\"a\":-3,\"b\":-3}'",

        // Unary minus with field access
        "'-.foo' ; '{\"foo\": 10}' ; '-10'",
        "'-.bar' ; '{\"bar\": -7}' ; '7'",

        // Unary minus with array indexing
        "'-.[0]' ; '[5, 10]' ; '-5'",
        "'-.[1]' ; '[3, -8]' ; '8'",

        // Unary minus with complex expressions
        "'-(.a + .b)' ; '{\"a\": 3, \"b\": 7}' ; '-10'",

        // Array construction with unary minus
        "'[-., -.]' ; '4' ; '[-4,-4]'",
        "'[-.a, -.b]' ; '{\"a\": 2, \"b\": 8}' ; '[-2,-8]'",

        // Nested unary minus (double negative)
        "'-(-.)' ; '5' ; '5'",
        "'-(-(.))' ; '-3' ; '-3'",

        // The test case from jq.test:39 involving abs
        "'{x:-1},{x:-.},{x:-.|abs}' ; '1' ; '{\"x\":-1}\n{\"x\":-1}\n{\"x\":1}'"
    }, delimiter = ';')
    void testUnaryMinusOperator(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'abs' ; '-5' ; '5'",
        "'abs' ; '5' ; '5'",
        "'abs' ; '0' ; '0'",
        "'abs' ; '-3.5' ; '3.5'",
        "'[.[] | abs]' ; '[-1,0,2]' ; '[1,0,2]'",
        "'map(abs)' ; '[-1,2,-3]' ; '[1,2,3]'"
    }, delimiter = ';')
    void testAbsFunction(String program, String input, String expected) {
        assertEquals(expected, Jq.execute(program, input));
    }
}
