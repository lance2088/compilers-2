grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

statement: 
     SUCHY .*?                                          # Suchy
     | VYMOTAJ rvalue                                   # Vymotaj   
     |                                                 # Emp 
     ;

rvalue:
     op=EXP<assoc=right> rvalue rvalue                 # Exp
     | op=(DIV|MUL) rvalue rvalue                      # Mul
     | op=(ADD|SUB) rvalue rvalue                      # Add
     | op=MOD rvalue rvalue                            # Mod
     | INT                                              # Int
     | PIPKOS                                           # Pipkos
     | FAJNE                                            # Fajne
     | TISIC                                            # Tisic
     ;
     
anything: ~('\n')*;
NAME: [a-z][a-z0-9]*; 

INT: DIGIT+;
FLOAT: DIGIT+ '.' DIGIT*;
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
EXP: '^';
MOD: '%';
NEWLINE: '\n';
WHITESPACE: [ \t] -> skip;
IF: 'AGE?';
ELSE: 'PIVO';
WHILE: 'MACKAJ';
AND: 'AND';
OR: 'OR';
NOT: 'NOT';
FOR: 'POCHIPUJ';

SUCHY: 'SUCHY'; 
VYMOTAJ: 'VYMOTAJ'; 

PIPKOS: 'PIPKOS'; 
FAJNE: 'FAJNE';
TISIC: 'TISIC';
TROSKU: 'TROSKU'; 

fragment DIGIT: [0-9];
