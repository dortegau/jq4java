grammar JqGrammar;

// Entry point
program
    : expression EOF
    ;

// Expression hierarchy (precedence from lowest to highest)
expression
    : commaExpr (PIPE commaExpr)*
    ;

commaExpr
    : alternativeExpr (COMMA alternativeExpr)*
    ;

alternativeExpr
    : conditionalExpr (ALT conditionalExpr)*
    ;

conditionalExpr
    : IF expression THEN expression (ELIF expression THEN expression)* (ELSE expression)? END  # ConditionalExpression
    | logicalExpr                                                                               # NonConditionalExpr
    ;

logicalExpr
    : comparisonExpr ((AND | OR) comparisonExpr)*
    ;

comparisonExpr
    : arithmeticExpr ((EQ | NE | LT | LE | GT | GE) arithmeticExpr)*
    ;

arithmeticExpr
    : postfix ((PLUS | MINUS | MULT | DIV | MOD) postfix)*
    ;

postfix
    : postfix LBRACKET RBRACKET                     # ArrayIterationExpr
    | postfix LBRACKET slice RBRACKET               # ArraySliceExpr
    | postfix LBRACKET expression RBRACKET          # ArrayIndexExpr
    | postfix DOT IDENTIFIER                        # FieldAccessExpr
    | postfix DOT STRING                            # FieldAccessStringExpr
    | DOT IDENTIFIER                                # RootFieldAccess
    | DOT STRING                                    # RootFieldAccessString
    | DOT LBRACKET RBRACKET                         # RootArrayIteration
    | DOT LBRACKET slice RBRACKET                   # RootArraySlice
    | DOT LBRACKET expression RBRACKET              # RootArrayIndex
    | primary                                       # PrimaryExpr
    ;

slice
    : start=expression? COLON end=expression?
    ;

primary
    : DOT                                           # IdentityExpr
    | TRUE                                          # TrueLiteral
    | FALSE                                         # FalseLiteral
    | NULL                                          # NullLiteral
    | NUMBER                                        # NumberLiteral
    | STRING                                        # StringLiteral
    | LENGTH                                        # LengthExpr
    | BUILTINS                                      # BuiltinsExpr
    | KEYS                                          # KeysExpr
    | TYPE                                          # TypeExpr
    | NOT                                           # NotExpr
    | RANGE                                         # RangeNoArgsExpr
    | RANGE LPAREN expression (SEMICOLON expression)* RPAREN  # RangeCall
    | IDENTIFIER LPAREN expression (SEMICOLON expression)* RPAREN  # FunctionCall
    | IDENTIFIER                                    # ZeroArgFunction
    | LBRACKET (expression (COMMA expression)*)? RBRACKET  # ArrayConstructor
    | LBRACE (objectField (COMMA objectField)*)? RBRACE    # ObjectConstructor
    | LPAREN expression RPAREN                      # ParenExpr
    ;

objectField
    : IDENTIFIER COLON expression    # ExplicitField
    | STRING COLON expression        # StringField
    | IDENTIFIER                     # ShorthandField
    ;

// Lexer rules
DOT         : '.' ;
PIPE        : '|' ;
COMMA       : ',' ;
COLON       : ':' ;
SEMICOLON   : ';' ;
ALT         : '//' ;
PLUS        : '+' ;
MINUS       : '-' ;
MULT        : '*' ;
DIV         : '/' ;
MOD         : '%' ;
EQ          : '==' ;
NE          : '!=' ;
LT          : '<' ;
LE          : '<=' ;
GT          : '>' ;
GE          : '>=' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
LBRACE      : '{' ;
RBRACE      : '}' ;
LPAREN      : '(' ;
RPAREN      : ')' ;

TRUE        : 'true' ;
FALSE       : 'false' ;
NULL        : 'null' ;
LENGTH      : 'length' ;
BUILTINS    : 'builtins' ;
KEYS        : 'keys' ;
TYPE        : 'type' ;
NOT         : 'not' ;
RANGE       : 'range' ;
AND         : 'and' ;
OR          : 'or' ;
IF          : 'if' ;
THEN        : 'then' ;
ELSE        : 'else' ;
ELIF        : 'elif' ;
END         : 'end' ;

NUMBER      : '-'? [0-9]+ ('.' [0-9]+)? ;
IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING      : '"' (~["\\] | '\\' .)* '"' ;

WS          : [ \t\r\n]+ -> skip ;
