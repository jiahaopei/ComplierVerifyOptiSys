#include <stdio.h>                                // 18
#include "func2.h"                                // 19


int sub(int x, int y, int d) {                    // 20
    int z;                                        // 20.1
    z = x - y - d;                                // 20.2
    printf("sub function %d\n", z);               // 20.3
    return z;                                     // 20.4
}                                                 // 21
