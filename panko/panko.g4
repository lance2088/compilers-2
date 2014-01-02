grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

statement: 
     SUCHY .*?                                         # Suchy
     | MEGA funkcia                                   # Main
     | funkcia                                        # Function
     | PAN TYPE NAME rexpression                            # Declare
     | NAMOTAJ NAME rexpression                             # Assign 
     | BLOCK_START statements BLOCK_END                # Block
     | IF rexpression NEWLINE tr=statement (NEWLINE ELSE NEWLINE fa=statement)?     # If
     | WHILE rexpression NEWLINE statement                          # While
     | FOR NAME rexpression NEWLINE statement                       # For
     | VYMOTAJ rexpression                                  # Vymotaj  
     |                                                 # Emp 
     ;

funkcia: 
    MOTAC TYPE NAME (TYPE NAME)* NEWLINE 
    (statements NEWLINE)?
    VYPAPAJ rexpression NEWLINE
    ;                             

rvalue:
     op=EXP<assoc=right> rexpression rexpression                 # Exp
     | op=(DIV|MUL) rexpression rexpression                      # Mul
     | op=(ADD|SUB) rexpression rexpression                      # Add
     | op=MOD rexpression rexpression                            # Mod
     | op=OR rexpression rexpression                             # Or
     | op=AND rexpression rexpression                            # And
     | op=NOT rexpression                                   # Not
     | op=EQUAL rexpression rexpression                          # Equal
     | op=SMALLER rexpression rexpression                        # Smaller
     | INT                                             # Int
     | PIPKOS                                          # Pipkos
     | FAJNE                                           # Fajne
     | TISIC                                           # Tisic
     ;
     
rexpression:
    rvalue                                            # Value
    | NAME (rvalue)*                                  # Name
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
MEGA: 'MEGA'; 
MOTAC: 'MOTAC'; 
VYPAPAJ: 'VYPAPAJ'; 
PAN: 'PAN'; 
NAMOTAJ: 'NAMOTAJ'; 
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
