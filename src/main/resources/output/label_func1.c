#include <stdio.h>                                // 1
#include "func1.h"                                // 2


int add(int x, int y) {                           // 3
    int z;                                        // 3.1
    z = x + y;                                    // 3.2
    printf("add function %d\n", z);               // 3.3
    printf("add x %d, y %d\n", x, y);             // 3.4
    printf("addd addd\n");                        // 3.5
    return z;                                     // 3.6
}                                                 // 4
