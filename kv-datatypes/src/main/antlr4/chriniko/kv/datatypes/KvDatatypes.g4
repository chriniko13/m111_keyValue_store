grammar KvDatatypes;


// ############ PARSER RULES ############

@parser::members {
  private boolean entered = false;
}


parse:
    entry
    | ANY {System.err.println("unknown char: " + $ANY.text);}
;


entry:  nestedEntry
         | flatEntry
         | listEntry
;

// { "_1stSemester" : { "_notPassed" : [ { "_les1_01" : "algos" } ; { "_les1_02" : "os" } ] } }


key: ID
;


listEntry: '{' key ':' list '}'
;


flatEntry: '{' key ':' value '}'
;


nestedEntry: '{' key ':' entry '}'
;



//listBody
//: {entered=true;}               listBodyStartNode
//    |               listBodyMidNode
//    | {entered=false;}           listBodyEndNode
//;

// ; { "_lessonsNeeded" : { "_3rdSemester" : [ { "_les3_01" : "compilers" } ; { "_2ndSemester" : { "_1stSemester" : {
//listBodyStartNode: list
//;
//listBodyMidNode: entry ';' listBody
//;
//listBodyEndNode: entry ']'
//;

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

StringValue: '"' [.A-Za-z0-9_\t ,-]* '"'
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