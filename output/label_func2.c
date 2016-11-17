#include <stdio.h>                                // 12
#include "func2.h"                                // 13


int sub(int x, int y, int d) {                    // 14
    int z;                                        // 14.1
    z = x - y - d;                                // 14.2
    printf("sub function %d\n", z);               // 14.3
    return z;                                     // 14.4
}                                                 // 15
