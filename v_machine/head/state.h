#pragma once
typedef int size;
#define true 1
#define false 0
#define unknow 3
struct state
{
	int *Stack, *Arg, *Local, *Static, *Heap;
	char **codes;
	short flag;
	size Stack_size, Arg_size, Local_size, Static_size, Heap_size;
};