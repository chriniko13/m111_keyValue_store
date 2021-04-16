grammar KvDatatypes;


// ############ PARSER RULES ############


parse:
    entry
    | ANY {System.err.println("unknown char: " + $ANY.text);}
;


entry:  nestedEntry
         | flatEntry
         | listEntry
;


key: ID
;


listEntry: '{' key ':' list '}'
;


flatEntry: '{' key ':' value '}'
;


nestedEntry: '{' key ':' entry '}'
;



list: '[' entry ']'
    | '[' entry ( ';' entry)+ ']'
;


value: StringValue
        | IntValue
        | FloatValue
        | EmptyValue
        ;

newline: NL
;


// ############ LEXER RULES ############
/*
    The character set notation can only be used in a lexer rule (rules that start with a capital letter, and produce tokens instead of parse trees).
*/

IntValue: DIGIT+
;

FloatValue:  DIGIT+ '.' DIGIT+
;


ID: '"' [_][a-z][A-Za-z0-9_-]* '"'
;

StringValue: '"' ['.A-Za-z0-9_\t ,-]* '"'
;

EmptyValue: WS* '{' WS* '}' WS*
;

// A 'skip' command tells the lexer to get another token and throw out the current text.
WHITESPACE : (' ' | '\t')+ -> skip ;

WS: (' ' | '\t');

NL: ('\r'? '\n' | '\r')+ ;

/*
NL
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;
*/

ANY : . ;


fragment DIGIT : [0-9] ;
// ######################################