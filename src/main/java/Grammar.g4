grammar Grammar;



conditions: condition|( condition  ('and'|'or') conditions);

condition:LQUOTE conditions RQUOTE|((ID|INT)  (GREATER|EQUAL|LESSER|GREATEREQUAL|LESSEREQUAL|NOTEQUAL)  (ID|INT));


LQUOTE:'(';
RQUOTE:')';
GREATER:'>';
GREATEREQUAL:'>=';
LESSEREQUAL:'<=';
NOTEQUAL:'!=';
EQUAL:'=';
LESSER:'<';
ID:('a'..'z'|'A'..'Z')(('a'..'z'|'A'..'Z'|'0'..'9'|'_')*);
INT:'0'..'9'+;


WS  : (' '|'\t'|'\n'|'\r'|EOF)+ ->  skip;