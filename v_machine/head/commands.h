#pragma once

#define end -1
#define add 0
#define sub 1
#define eq 3
#define uneq 4
#define gt_eq 5
#define lt_eq 6
#define gt 7
#define lt 8
#define goto 9
#define ifgoto 10
#define push 11
#define pop 12
#define printchar 13
#define not 14
#define printnum 15
#define back 16
#define call 17
#define mult 18
#define dev 19

struct singleCode
{
	char operateCode[15];
	char operateArg1[15];
	char operateArg2[15];
};
