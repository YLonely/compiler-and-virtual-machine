#include "stdio.h"
#include "state.h"
#include "V_M.h"
#include "stdlib.h"


void main(int argc, char* argv[])
{
	struct state vm_state;
	vmInitialize(argv[1], &vm_state);
	vmRun(&vm_state);

}