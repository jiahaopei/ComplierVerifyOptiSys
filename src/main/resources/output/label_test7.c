#include <stdio.h>                                // 1
#include "func.h"                                 // 8

int inc(int x) {                                  // 9
    int z;                                        // 9.1
    z = x + 1;                                    // 9.2
    return z;                                     // 9.3
}                                                 // 10

int main() {                                      // 11
    int a, b, c;                                  // 11.1
    int d;                                        // 11.2
    int e;                                        // 11.3

    a = 1;                                        // 11.4
    b = 2;                                        // 11.5
    c = add(a, 3);                                // 11.6
    d = sub(a, c, b);                             // 11.7
    e = inc(4);                                   // 11.8

    printf("The add result is : %d\n", c);        // 11.9
    printf("The sub result is : %d\n", d);        // 11.10
    return 0;                                     // 11.11
}                                                 // 12
