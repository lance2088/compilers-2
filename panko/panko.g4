grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

statement: 
     SUCHY .*?                                         # Suchy
     | PAN TYPE NAME rvalue                            # Declare
     | NAMOTAJ NAME rvalue                             # Assign 
     | BLOCK_START statements BLOCK_END                # Block
     | IF rvalue NEWLINE tr=statement (NEWLINE ELSE NEWLINE fa=statement)?     # If
     | WHILE rvalue NEWLINE statement                          # While
     | FOR NAME rvalue NEWLINE statement                       # For
     | VYMOTAJ rvalue                                  # Vymotaj  
     |                                                 # Emp 
     ;

rvalue:
     op=EXP<assoc=right> rvalue rvalue                 # Exp
     | op=(DIV|MUL) rvalue rvalue                      # Mul
     | op=(ADD|SUB) rvalue rvalue                      # Add
     | op=MOD rvalue rvalue                            # Mod
     | op=OR rvalue rvalue                             # Or
     | op=AND rvalue rvalue                            # And
     | op=NOT rvalue                                   # Not
     | op=EQUAL rvalue rvalue                          # Equal
     | op=SMALLER rvalue rvalue                        # Smaller
     | INT                                             # Int
     | PIPKOS                                          # Pipkos
     | FAJNE                                           # Fajne
     | TISIC                                           # Tisic
     | NAME                                            # Var
     ;
     
SUCHY: 'SUCHY'; 

INT: DIGIT+;
FLOAT: DIGIT+ '.' DIGIT*;
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
EXP: '^';
MOD: '%'; 
WHITESPACE: [ \t] -> skip;
NEWLINE: '\n';
NAMOTAJ: 'NAMOTAJ'; 
PAN: 'PAN'; 
BLOCK_START: '{';
BLOCK_END: '}';
IF: 'AGE?';
ELSE: 'PIVO';
WHILE: 'MACKAJ';
FOR: 'POCHIPUJ';
AND: 'AND';
OR: 'OR';
NOT: 'NOT';
EQUAL: 'EQUAL';
SMALLER: 'SMALLER'; 

VYMOTAJ: 'VYMOTAJ'; 

PIPKOS: 'PIPKOS'; 
FAJNE: 'FAJNE';
TISIC: 'TISIC';
TROSKU: 'TROSKU'; 

TYPE: 'INT' | 'FLOAT' | 'CHAR';
NAME: [a-zA-Z][a-zA-Z0-9]*;
fragment DIGIT: [0-9];
