#include <stdio.h>                                // 5
#include "func1.h"                                // 6


int add(int x, int y) {                           // 7
    int z;                                        // 7.1
    z = x + y;                                    // 7.2
    printf("add function %d\n", z);               // 7.3
    printf("add x %d, y %d\n", x, y);             // 7.4
    printf("addd addd\n");                        // 7.5
    return z;                                     // 7.6
}                                                 // 8
