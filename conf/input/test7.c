#include <stdio.h>
#include "func1.h"
#include "func2.h"

int inc(int x) {
    int z;
    z = x + 1;
    return z;
}

int main() {
    int a, b, c;
    int d;
    int e;

    a = 1;
    b = 2;
    c = add(a, 3);
    d = sub(a, c, b);
    e = inc(4);

    printf("The add result is : %d\n", c);
    printf("The sub result is : %d\n", d);
    return 0;
}
