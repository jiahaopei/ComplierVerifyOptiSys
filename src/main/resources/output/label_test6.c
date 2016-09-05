#include <stdio.h>                                // 1

int add(int x, int y) {                           // 2
    int z;                                        // 2.1
    z = x + y;                                    // 2.2
    printf("add function %d\n", z);               // 2.3
    printf("add x %d, y %d\n", x, y);             // 2.4
    printf("addd addd\n");                        // 2.5
    return z;                                     // 2.6
}                                                 // 3

int sub(int x, int y, int d) {                    // 4
    int z;                                        // 4.1
    z = x - y - d;                                // 4.2
    printf("sub function %d\n", z);               // 4.3
    return z;                                     // 4.4
}                                                 // 5

int inc(int x) {                                  // 6
    int z;                                        // 6.1
    z = x + 1;                                    // 6.2
    return z;                                     // 6.3
}                                                 // 7

int main() {                                      // 8
    int a, b, c;                                  // 8.1
    int d;                                        // 8.2
    int e;                                        // 8.3

    a = 1;                                        // 8.4
    b = 2;                                        // 8.5
    c = add(a, 3);                                // 8.6
    d = sub(a, c, b);                             // 8.7
    e = inc(4);                                   // 8.8

    printf("The add result is : %d\n", c);        // 8.9
    printf("The sub result is : %d\n", d);        // 8.10
    return 0;                                     // 8.11
}                                                 // 9
