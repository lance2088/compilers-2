grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

statement: 
     SUCHY .*?                                        # Suchy
     | MEGA funkcia                                   # Main
     | funkcia                                        # FunctionDefine
     | PAN TYPE NAME rexpression                      # VariableDefine
     | NAMOTAJ NAME rexpression                       # VariableAssign 
     | BLOCK_START statements BLOCK_END               # Block
     | IF rexpression NEWLINE tr=statement (NEWLINE ELSE NEWLINE fa=statement)?     # If
     | WHILE rexpression NEWLINE statement            # While
     | FOR NAME rexpression NEWLINE statement         # For
     | VYMOTAJ rexpression                            # Vymotaj  
     | rexpression                                    # Evaluate
     |                                                # Emp 
     ;

/**TODO: Disallow function definitions inside functions */
funkcia: 
    MOTAC TYPE NAME (TYPE NAME)* NEWLINE 
    (statements NEWLINE)?
    VYPAPAJ rexpression
    ;                              

rvalue:
     op=EXP<assoc=right> rvalue rexpression            # Exp
     | op=(DIV|MUL) rvalue rexpression                 # Mul
     | op=(ADD|SUB) rvalue rexpression                 # Add
     | op=MOD rvalue rexpression                       # Mod
     | op=OR rvalue rexpression                        # Or
     | op=AND rvalue rexpression                       # And
     | op=NOT rexpression                              # Not
     | op=EQUAL rvalue rexpression                     # Equal
     | op=SMALLER rvalue rexpression                   # Smaller
     | INT                                             # Int
     | PIPKOS                                          # Pipkos
     | FAJNE                                           # Fajne
     | TISIC                                           # Tisic
     | NAME                                            # VariableValue
     ;
     
rexpression: 
    rvalue                                             # RValue
    | ZMOTAJ NAME (rvalue*) (rexpression)?             # FunctionValue
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
ZMOTAJ: 'ZMOTAJ'; 
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
