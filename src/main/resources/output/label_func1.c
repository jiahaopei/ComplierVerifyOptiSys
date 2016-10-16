#include <stdio.h>                                // 2
#include "func1.h"                                // 3


int add(int x, int y) {                           // 4
    int z;                                        // 4.1
    z = x + y;                                    // 4.2
    printf("add function %d\n", z);               // 4.3
    printf("add x %d, y %d\n", x, y);             // 4.4
    printf("addd addd\n");                        // 4.5
    return z;                                     // 4.6
}                                                 // 5
