#include <stdio.h>                                // 12
#include "func1.h"                                // 13


int add(int x, int y) {                           // 14
    int z;                                        // 14.1
    z = x + y;                                    // 14.2
    printf("add function %d\n", z);               // 14.3
    printf("add x %d, y %d\n", x, y);             // 14.4
    printf("addd addd\n");                        // 14.5
    return z;                                     // 14.6
}                                                 // 15
