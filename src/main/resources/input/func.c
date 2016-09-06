#include <stdio.h>
#include "func.h"

//
// Created by destiny on 16/9/5.
//
//
//
//
//

int add(int x, int y) {
    int z;
    z = x + y;
    printf("add function %d\n", z);
    printf("add x %d, y %d\n", x, y);
    printf("addd addd\n");
    return z;
}

int sub(int x, int y, int d) {
    int z;
    z = x - y - d;
    printf("sub function %d\n", z);
    return z;
}