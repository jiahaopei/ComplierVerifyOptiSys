#include <stdio.h>                                // 1
#include "func2.h"                                // 2


int sub(int x, int y, int d) {                    // 3
    int z;                                        // 3.1
    z = x - y - d;                                // 3.2
    printf("sub function %d\n", z);               // 3.3
    return z;                                     // 3.4
}                                                 // 4
