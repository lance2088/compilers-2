grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

statement: 
     SUCHY anything                                     # Suchy
     | VYMOTAJ rvalue                                   # Vymotaj    
     ;

rvalue:
     op=EXP<assoc=right> rvalue rvalue                # Exp
     | rvalue op=(DIV|MUL) rvalue rvalue                # Mul
     | rvalue op=(ADD|SUB) rvalue rvalue                # Add
     | rvalue op=MOD rvalue rvalue                      # Mod
     | INT                                              # Int
     | PIPKOS                                           # Pipkos
     | FAJNE                                            # Fajne
     | TISIC                                            # Tisic 
     ;
     
anything: .*?;
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