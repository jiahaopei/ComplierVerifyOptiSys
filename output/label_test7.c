#include <stdio.h>                                // 1
#include <ctype.h>                                // 2
#include "func1.h"                                // 3
#include "func2.h"                                // 4

int inc(int x);                                   // 5

int main() {                                      // 6
    int a, b, c;                                  // 6.1
    int d;                                        // 6.2
    int e;                                        // 6.3

    a = 1;                                        // 6.4
    b = 2;                                        // 6.5
    c = add(a, 3);                                // 6.6
    d = sub(a, c, b);                             // 6.7
    e = inc(4);                                   // 6.8

    printf("The add result is : %d\n", c);        // 6.9
    printf("The sub result is : %d\n", d);        // 6.10
    return 0;                                     // 6.11
}                                                 // 7

int inc(int x) {                                  // 8
    int z;                                        // 8.1
    z = x + 1;                                    // 8.2
    return z;                                     // 8.3
}                                                 // 9
