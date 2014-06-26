grammar boolean;

LE 
	: LE C LE
	| N LE
	| '\w+'
	;
C
	: 'v'
	| '^'
	;
N
	: '!'
	;