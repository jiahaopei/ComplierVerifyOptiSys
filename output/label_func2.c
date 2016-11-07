#include <stdio.h>                                // 8
#include "func2.h"                                // 9


int sub(int x, int y, int d) {                    // 10
    int z;                                        // 10.1
    z = x - y - d;                                // 10.2
    printf("sub function %d\n", z);               // 10.3
    return z;                                     // 10.4
}                                                 // 11
