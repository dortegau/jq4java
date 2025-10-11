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
        "'.a // .b, .c // .d' ; '{\"b\":2,\"c\":3}' ; '2\n3'"
    }, delimiter = ';')
    void testCombinedOperations(String program, String input, String expected) {
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
