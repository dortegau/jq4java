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
    : postfix (ALT postfix)*
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
    | LBRACKET (expression (COMMA expression)*)? RBRACKET  # ArrayConstructor
    | LBRACE (objectField (COMMA objectField)*)? RBRACE    # ObjectConstructor
    | LPAREN expression RPAREN                      # ParenExpr
    ;

objectField
    : IDENTIFIER COLON expression
    | STRING COLON expression
    ;

// Lexer rules
DOT         : '.' ;
PIPE        : '|' ;
COMMA       : ',' ;
COLON       : ':' ;
ALT         : '//' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
LBRACE      : '{' ;
RBRACE      : '}' ;
LPAREN      : '(' ;
RPAREN      : ')' ;

TRUE        : 'true' ;
FALSE       : 'false' ;
NULL        : 'null' ;

NUMBER      : '-'? [0-9]+ ('.' [0-9]+)? ;
IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING      : '"' (~["\\] | '\\' .)* '"' ;

WS          : [ \t\r\n]+ -> skip ;
