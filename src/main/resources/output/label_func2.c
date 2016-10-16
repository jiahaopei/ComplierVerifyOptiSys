#include <stdio.h>                                // 7
#include "func2.h"                                // 8


int sub(int x, int y, int d) {                    // 9
    int z;                                        // 9.1
    z = x - y - d;                                // 9.2
    printf("sub function %d\n", z);               // 9.3
    return z;                                     // 9.4
}                                                 // 10
