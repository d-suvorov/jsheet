grammar Expression;

expr
    : '(' expr ')'                                      # parenthesisExpr
    | left=expr op=('*' | '/')               right=expr # infixExpr
    | left=expr op=('+' | '-')               right=expr # infixExpr
    | left=expr op=('<' | '<=' | '>' | '>=') right=expr # infixExpr
    | left=expr op=('==' | '!=')             right=expr # infixExpr
    | left=expr op='&&'                      right=expr # infixExpr
    | left=expr op='||'                      right=expr # infixExpr
    | ID '(' args ')'                                   # functionExpr
    | reference=ID                                      # referenceExpr
    | literal                                           # literalExpr
    ;

args
    : expr (',' expr)*
    |
    ;

literal
    : BOOL # boolean
    | NUM  # number
    | STR  # string
    ;

BOOL : 'false' | 'true';
NUM  : INT | FLT;
INT  : [0-9]+;
FLT  : ([0-9]+)? '.' [0-9]+;
STR  : '"' (~'"'|'\\"')* '"';

ID  : [a-zA-Z][_a-zA-Z0-9]*;

WS  : [ \t\r\n]+ -> skip;