grammar Expression;

expr
    : '(' expr ')'                        # parenthesis
    | left=expr op=('*' | '/') right=expr # infix
    | left=expr op=('+' | '-') right=expr # infix
    | ID '(' args ')'                     # function
    | reference=ID                        # ref
    | value=NUM                           # const
    ;

args
    : expr (',' expr)*
    |
    ;

NUM : [0-9]+;
ID  : [a-zA-Z][_a-zA-Z0-9]*;
WS  : [ \t\r\n]+ -> skip;