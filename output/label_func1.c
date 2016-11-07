#include <stdio.h>                                // 3
#include "func1.h"                                // 4


int add(int x, int y) {                           // 5
    int z;                                        // 5.1
    z = x + y;                                    // 5.2
    printf("add function %d\n", z);               // 5.3
    printf("add x %d, y %d\n", x, y);             // 5.4
    printf("addd addd\n");                        // 5.5
    return z;                                     // 5.6
}                                                 // 6
