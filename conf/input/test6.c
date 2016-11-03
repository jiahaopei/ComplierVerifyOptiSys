#include <stdio.h>

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
