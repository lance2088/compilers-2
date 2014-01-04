grammar panko;

init: statements;

statements: statement (NEWLINE statement)*;

/**
TODO: Check if rexpression is parsed uniquelly (NAME 
TODO: PAN TYPE NAME (rexpression)? 
TODO: VYMOTAJ ROLKA i  //is not a parser error
TODO: Mal by podporovať volanie externych funkcii (napriklad C-ckovych funkcii, staci iba tie, co pouzivaju kompatobilne typy)
TODO: refactor 
*/

statement: 
     SUCHY .*?                                        # Suchy
     | MEGA funkcia                                   # Main
     | funkcia                                        # FunctionDefine
     | WCBOOK rexpression TYPE NAME                   # ArrayDefine
     | NAMOTAJ ROLKA rvalue NAME rexpression          # ArrayAssign 
     | FREE_PRE FREE_TEPLYCH NAME                     # ArrayDelete
     | PAN TYPE NAME rexpression                      # VariableDefine
     | NAMOTAJ NAME rexpression                       # VariableAssign 
     | BLOCK_START statements BLOCK_END               # Block
     | IF rexpression NEWLINE tr=statement (NEWLINE ELSE NEWLINE fa=statement)?     # If
     | WHILE rexpression NEWLINE statement            # While
     | FOR NAME rexpression NEWLINE statement         # For
     | VMOTAJ TYPE NAME                               # Vmotaj
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
     op=EXP<assoc=right> rvalue rvalue            # Exp
     | op=(DIV|MUL) rvalue rvalue                 # Mul
     | op=(ADD|SUB) rvalue rvalue                 # Add
     | op=MOD rvalue rvalue                       # Mod
     | op=OR rvalue rvalue                        # Or
     | op=AND rvalue rvalue                       # And
     | op=NOT rvalue                              # Not
     | op=EQUAL rvalue rvalue                     # Equal
     | op=SMALLER rvalue rvalue                   # Smaller
     | INT                                        # Int
     | PIPKOS                                     # Pipkos
     | FAJNE                                      # Fajne
     | TISIC                                      # Tisic
     | ROLKA rvalue NAME                          # ArrayValue 
     | NAME                                       # VariableValue
     | BAVI rvalue                                # RandomValue
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
FREE_PRE: 'PRE';
FREE_TEPLYCH: 'TEPLYCH'; 
WHITESPACE: [ \t] -> skip;
NEWLINE: '\n';
MEGA: 'MEGA'; 
MOTAC: 'MOTAC'; 
VYPAPAJ: 'VYPAPAJ'; 
ZMOTAJ: 'ZMOTAJ'; 
PAN: 'PAN'; 
NAMOTAJ: 'NAMOTAJ'; 
WCBOOK: 'WCBOOK'; 
ROLKA: 'ROLKA'; 
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

VMOTAJ: 'VMOTAJ'; 
VYMOTAJ: 'VYMOTAJ'; 

PIPKOS: 'PIPKOS'; 
FAJNE: 'FAJNE';
TISIC: 'TISIC';
TROSKU: 'TROSKU'; 
BAVI: 'BAVI'; 

TYPE: 'INT' | 'FLOAT' | 'CHAR';
NAME: [a-zA-Z][a-zA-Z0-9]*;
fragment DIGIT: [0-9];
