#include "stdio.h"
#include "stdlib.h"
#include "state.h"
#include "commands.h"
#include "string.h"

#define EXTRA_SIZE 10 


static int theLinesOfFile(FILE *fp)     //how many lines there in target file
{
	char str[40];
	int lines = 0;
	while (fgets(str, sizeof(str), fp) != NULL)
	{
		lines++;
	}
	rewind(fp);
	return lines;
}

static char** readFileToMem(FILE *fp)     //import all the commands to memory
{
	int lines = theLinesOfFile(fp);
	char **codes = NULL;
	char temp;
	if (!(codes = (char**)calloc(lines, sizeof(char*))))
	{
		printf("codes calloc failed!\n");
		getchar();
		exit(0);
	}
	for (int i = 0; i < lines; i++)
	{
		if (!(*(codes + i) = (char*)calloc(40, sizeof(char))))
		{
			printf("codes calloc failed!\n");
			getchar();
			exit(1);
		}
	}

	for (int i = 0; i < lines; i++)
	{
		for (int j = 0; j < 40; j++)
		{
			temp = getc(fp);
			if (temp != '\n'&&temp != -1)
			{
				codes[i][j] = temp;
			}
			else
			{
				codes[i][j] = '\n';
				break;
			}
		}
	}

	return codes;
}

void vmInitialize(char *filePath, struct state *vm_state) //the initializtion of the vm
{
	FILE *fp;
	char **codes;
	fp = fopen(filePath, "r");
	if (fp == NULL)
	{
		printf("open failed\n");
		getchar();
		exit(2);
	}

	codes = readFileToMem(fp);
	fclose(fp);

	vm_state->Stack = calloc(20, sizeof(int));
	vm_state->Stack_size = 20;

	vm_state->Arg = calloc(20, sizeof(int));
	vm_state->Arg_size = 20;

	vm_state->Local = calloc(20, sizeof(int));
	vm_state->Local_size = 20;

	vm_state->Static = calloc(20, sizeof(int));
	vm_state->Static_size = 20;

	vm_state->Heap = calloc(100, sizeof(int));
	vm_state->Heap_size = 100;

	if (vm_state->Stack == NULL || vm_state->Arg == NULL || vm_state->Local == NULL || vm_state->Static == NULL || vm_state->Heap == NULL)
	{
		printf("allocate space failed\n");
		getchar();
		exit(3);
	}

	vm_state->flag = unknow;

	vm_state->codes = codes;
}


/*
separate the single line code into 3 parts
,one is operate symbol such as 'add','sub',two is the first data to
use,three is the other data to use.
*/
static void separateCode(struct singleCode *sc, char *cp)
{
	int i = 0;
	int j = 0;
	while (*(cp + i) != ' '&&*(cp + i) != '\n')
	{
		sc->operateCode[j] = *(cp + i);
		i++;
		j++;
	}
	sc->operateCode[j] = '\0';
	j = 0;
	i++;
	while (*(cp + i) != ' '&&*(cp + i) != '\n'&&*(cp + i) != 0)
	{
		sc->operateArg1[j] = *(cp + i);
		i++;
		j++;
	}
	sc->operateArg1[j] = '\0';
	j = 0;
	i++;
	while (*(cp + i) != ' '&&*(cp + i) != '\n'&&*(cp + i) != 0)
	{
		sc->operateArg2[j] = *(cp + i);
		i++;
		j++;
	}
	sc->operateArg2[j] = '\0';
}

void vmRun(struct state *vm_state)
{
	struct singleCode single;
	FILE *fp = NULL;
	char **codes;
	short type;
	int *Stack, *Arg, *Local, *Static, *Heap, *pointer;
	int arg1, arg2;
	int currentHeapPosition = 0, currentArgPosition = 0, currentLocalPosition = 0;
	int argBaseNum = 0, localBaseNum = 0;
	int historyArgBaseNum[100], historyLocalBaseNum[100];
	int historyArgPointer = 0, histroyLocalPointer = 0;
	int changeArgBase = false;

	/*
	record the initial position of all the space
	*/
	codes = vm_state->codes;
	Stack = vm_state->Stack;
	Arg = vm_state->Arg;
	Local = vm_state->Local;
	Static = vm_state->Static;
	Heap = vm_state->Heap;
	pointer = vm_state->Heap;
	/*
	the program begins at the begining of static area or at the main funcion
	*/
	separateCode(&single, *codes);
	if (strcmp(single.operateCode, "static"))
	{
		while (strcmp(single.operateArg1, "main"))
		{
			separateCode(&single, *(++codes));
		}
	}
	while (true)
	{
		separateCode(&single, *(codes++));
		if (!strcmp(single.operateCode, "endstatic"))
		{
			while (strcmp(single.operateArg1, "main"))
			{
				separateCode(&single, *(codes++));
			}
		}
		type = compareOprateCode(&single);
		switch (type)
		{
		case end:
		{
			if (fp != NULL)
			{
				fclose(fp);
			}
			printf("\nvm stoped in normal situation\n");
			getchar();
			exit(-1);
			break;
		}
		case add:
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in add,no more args\n");
				getchar();
				exit(4);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			*Stack = arg2 + arg1;
			Stack++;
			break;
		}
		case sub:
		{
			if (Stack == vm_state->Stack)
			{
				printf("wrong in sub,no more args\n");
				getchar();
				exit(5);
			}
			if (Stack == vm_state->Stack + 1)
			{
				*(Stack - 1) = - *(Stack - 1);
				break;
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			*Stack = arg2 - arg1;
			Stack++;
			break;
		}
		case mult: //multiplication
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in mult,no more args\n");
				getchar();
				exit(5);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			*Stack = arg2 * arg1;
			Stack++;
			break;
		}
		case dev: //division
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in sub,no more args\n");
				getchar();
				exit(5);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			*Stack = arg2 / arg1;
			Stack++;
			break;
		}
		case eq: //if equal
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in eq,no more args\n");
				getchar();
				exit(7);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg1 == arg2)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case uneq: // if unequal
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in uneq,no more args\n");
				getchar();
				exit(8);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg1 != arg2)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case gt_eq: 
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in gt_eq,no more args\n");
				getchar();
				exit(9);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg2 >= arg1)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case lt_eq:
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in lt_eq,no more args\n");
				getchar();
				exit(10);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg2 <= arg1)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case gt:
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in gt,no more args\n");
				getchar();
				exit(11);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg2 > arg1)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case lt:
		{
			if (Stack == vm_state->Stack || Stack == vm_state->Stack + 1)
			{
				printf("wrong in lt,no more args\n");
				getchar();
				exit(12);
			}
			arg1 = *(--Stack);
			arg2 = *(--Stack);
			if (arg2 < arg1)
			{
				*(Stack++) = 1;
			}
			else
				*(Stack++) = 0;
			break;
		}
		case goto:
		{
			arg1 = atoi(single.operateArg1);
			codes = vm_state->codes + arg1;
			break;
		}
		case ifgoto:
		{
			if (*(--Stack) == 0)
			{
				vm_state->flag = false;
			}
			else
			{
				vm_state->flag = true;
			}
			if (vm_state->flag == true)
			{
				arg1 = atoi(single.operateArg1);
				codes = vm_state->codes + arg1;
			}
			break;
		}
		case push: //push the data to stack
		{
			char *arguement1 = single.operateArg1;
			arg2 = atoi(single.operateArg2);
			if (!strcmp(arguement1, "arg"))
			{
				*(Stack++) = *(Arg + argBaseNum + arg2);
			}
			else if (!strcmp(arguement1, "local"))
			{
				*(Stack++) = *(Local + localBaseNum + arg2);
			}
			else if (!strcmp(arguement1, "static"))
			{
				*(Stack++) = *(Static + arg2);
			}
			else if (!strcmp(arguement1, "heap"))
			{
				*(Stack++) = *(pointer + arg2);
			}
			else if (!strcmp(arguement1, "constant"))
			{
				*(Stack++) = arg2;
			}
			else if (!strcmp(arguement1, "heappos"))
			{
				*(Stack++) = currentHeapPosition;
			}
			else
			{
				printf("push no match\n");
				getchar();
				exit(13);
			}

			//extend the space automatically when the stack is full
			if ((Stack - vm_state->Stack) == vm_state->Stack_size - 1)
			{
				if ((vm_state->Stack = (int *)realloc(vm_state->Stack, vm_state->Stack_size + EXTRA_SIZE)) == NULL)
				{
					printf("realloc stack failed\n");
					getchar();
					exit(14);
				}
				vm_state->Stack_size = vm_state->Stack_size + EXTRA_SIZE;
			}
			break;
		}
		case pop: //pop the data from stack
		{
			char *arguement1 = single.operateArg1;
			arg2 = atoi(single.operateArg2);
			if (!strcmp(arguement1, "arg"))
			{
				if (arg2 > vm_state->Arg_size - 1)
				{
					printf("wild pointer in pop to arg\n");
					getchar();
					exit(0);
				}
				if (arg2 == vm_state->Arg_size - 1)
				{
					if ((vm_state->Arg = (int *)realloc(vm_state->Arg, vm_state->Arg_size + EXTRA_SIZE)) == NULL)
					{
						printf("realloc arg failed\n");
						getchar();
						exit(15);
					}
					vm_state->Arg_size = vm_state->Arg_size + EXTRA_SIZE;
				}
				if (changeArgBase == true)
				{
					historyArgBaseNum[historyArgPointer++] = argBaseNum;
					argBaseNum = currentArgPosition;
					changeArgBase = false;
				}
				*(Arg + arg2 + argBaseNum) = *(--Stack);
				currentArgPosition++;
			}
			else if (!strcmp(arguement1, "local"))
			{
				if (arg2 > vm_state->Local_size - 1)
				{
					printf("wild pointer in pop to local\n");
					getchar();
					exit(0);
				}
				if (arg2 == vm_state->Local_size - 1)
				{
					if ((vm_state->Local = (int *)realloc(vm_state->Local, vm_state->Local_size + EXTRA_SIZE)) == NULL)
					{
						printf("realloc local failed\n");
						getchar();
						exit(16);
					}
					vm_state->Local_size = vm_state->Local_size + EXTRA_SIZE;
				}
				*(Local + arg2 + localBaseNum) = *(--Stack);
				currentLocalPosition++;
			}
			else if (!strcmp(arguement1, "static"))
			{
				if (arg2 > vm_state->Static_size - 1)
				{
					printf("wild pointer in pop to static\n");
					getchar();
					exit(0);
				}
				if (arg2 == vm_state->Static_size - 1)
				{
					if ((vm_state->Static = (int *)realloc(vm_state->Static, vm_state->Static_size + EXTRA_SIZE)) == NULL)
					{
						printf("realloc static failed\n");
						getchar();
						exit(17);
					}
					vm_state->Static_size = vm_state->Static_size + EXTRA_SIZE;
				}
				*(Static + arg2) = *(--Stack);
			}
			else if (!strcmp(arguement1, "pointer"))
			{
				pointer = vm_state->Heap + *(--Stack);
				if (pointer > Heap)
				{
					printf("wild pointer");
					getchar();
					exit(18);
				}
			}
			else if (!strcmp(arguement1, "heap"))
			{
				*(pointer + arg2) = *(--Stack);
			}
			// initial a empty space for array
			else if (!strcmp(arguement1, "heaparr"))
			{
				if (Heap + 1 - vm_state->Heap_size == vm_state->Heap_size - 1)
				{
					if ((vm_state->Heap = (int *)realloc(vm_state->Heap, vm_state->Heap_size + EXTRA_SIZE)) == NULL)
					{
						printf("realloc heap failed\n");
						getchar();
						exit(19);
					}
					vm_state->Heap_size = vm_state->Heap_size + EXTRA_SIZE;
				}
				*(Heap++) = 0;
				currentHeapPosition++;
			}
			else
			{
				printf("pop no match\n");
				getchar();
				exit(20);
			}
			break;
		}
		case printchar:
		{
			printf("%c", *(--Stack));
			break;
		}
		case not:
		{
			if (*(Stack - 1) == 0)
			{
				*(Stack - 1) = 1;
			}
			else
				*(Stack - 1) = 0;
			break;
		}
		case printnum:
		{
			printf("%d", *(--Stack));
			break;
		}
		case back:
		{
			codes = vm_state->codes + *(--Stack);
			currentArgPosition = argBaseNum;
			argBaseNum = historyArgBaseNum[--historyArgPointer];
			currentLocalPosition = localBaseNum;
			localBaseNum = historyLocalBaseNum[--histroyLocalPointer];
			break;
		}
		case call:
		{
			*(Stack++) = codes - vm_state->codes;
			codes = vm_state->codes + atoi(single.operateArg1);
			if (changeArgBase == false)
			{
				changeArgBase = true;
			}
			historyLocalBaseNum[histroyLocalPointer++] = localBaseNum;
			localBaseNum = currentLocalPosition;
			break;
		}
		default:
			break;
		}
	}
}

static int compareOprateCode(struct singleCode *single)
{
	if (!strcmp(single->operateCode, "end"))
	{
		return end;
	}
	else if (!strcmp(single->operateCode, "add"))
	{
		return add;
	}
	else if (!strcmp(single->operateCode, "sub"))
	{
		return sub;
	}
	else if (!strcmp(single->operateCode, "eq"))
	{
		return eq;
	}
	else if (!strcmp(single->operateCode, "uneq"))
	{
		return uneq;
	}
	else if (!strcmp(single->operateCode, "gt_eq"))
	{
		return gt_eq;
	}
	else if (!strcmp(single->operateCode, "lt_eq"))
	{
		return lt_eq;
	}
	else if (!strcmp(single->operateCode, "gt"))
	{
		return gt;
	}
	else if (!strcmp(single->operateCode, "lt"))
	{
		return lt;
	}
	else if (!strcmp(single->operateCode, "goto"))
	{
		return goto;
	}
	else if (!strcmp(single->operateCode, "ifgoto"))
	{
		return ifgoto;
	}
	else if (!strcmp(single->operateCode, "push"))
	{
		return push;
	}
	else if (!strcmp(single->operateCode, "pop"))
	{
		return pop;
	}
	else if (!strcmp(single->operateCode, "printchar"))
	{
		return printchar;
	}
	else if (!strcmp(single->operateCode, "not"))
	{
		return not;
	}
	else if (!strcmp(single->operateCode, "printnum"))
	{
		return printnum;
	}
	else if (!strcmp(single->operateCode, "back"))
	{
		return back;
	}
	else if (!strcmp(single->operateCode, "call"))
	{
		return call;
	}
	else if (!strcmp(single->operateCode, "mult"))
	{
		return mult;
	}
	else if (!strcmp(single->operateCode, "dev"))
	{
		return dev;
	}
	else
		return -3;
}
