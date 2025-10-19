package com.dortegau.jq4java.parser;

import com.dortegau.jq4java.ast.Expression;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parser for jq expressions using ANTLR.
 */
public class JqParser {
  /**
   * Parses a jq expression string into an AST using ANTLR.
   *
   * @param program the jq expression to parse
   * @return the parsed expression
   */
  public static Expression parse(String program) {
    JqGrammarLexer lexer = new JqGrammarLexer(CharStreams.fromString(program));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JqGrammarParser grammarParser = new JqGrammarParser(tokens);
    grammarParser.removeErrorListeners();
    grammarParser.addErrorListener(new org.antlr.v4.runtime.BaseErrorListener() {
      @Override
      public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer,
                              Object offendingSymbol,
                              int line, int charPositionInLine,
                              String msg, org.antlr.v4.runtime.RecognitionException e) {
        throw new RuntimeException(
            "Parse error at " + line + ":" + charPositionInLine + ": " + msg);
      }
    });
    JqGrammarParser.ProgramContext tree = grammarParser.program();
    JqAstBuilder builder = new JqAstBuilder();
    return builder.visit(tree);
  }
}