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
        "'{a, b: .y, c}' ; '{\"a\":1, \"y\":2, \"c\":3}' ; '{\"a\":1,\"b\":2,\"c\":3}'",
        "'{foo, \"bar\": .baz}' ; '{\"foo\":1, \"baz\":2}' ; '{\"foo\":1,\"bar\":2}'",
        "'{a, b, c: .x + .y}' ; '{\"a\":1, \"b\":2, \"x\":3, \"y\":4}' ; '{\"a\":1,\"b\":2,\"c\":7}'"
    }, delimiter = ';')
    void testObjectMixedSyntax(String program, String input, String expected) {
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
        "'map(. + 1) | map(. * 2)' ; '[1,2,3]' ; '[4,6,8]'"
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
}
