#include <stdio.h>                                // 1
#include <ctype.h>                                // 2
#include "func1.h"                                // 9
#include "func2.h"                                // 16

int inc(int x);                                   // 17

int main() {                                      // 18
    int a, b, c;                                  // 18.1
    int d;                                        // 18.2
    int e;                                        // 18.3

    a = 1;                                        // 18.4
    b = 2;                                        // 18.5
    c = add(a, 3);                                // 18.6
    d = sub(a, c, b);                             // 18.7
    e = inc(4);                                   // 18.8

    printf("The add result is : %d\n", c);        // 18.9
    printf("The sub result is : %d\n", d);        // 18.10
    return 0;                                     // 18.11
}                                                 // 19

int inc(int x) {                                  // 20
    int z;                                        // 20.1
    z = x + 1;                                    // 20.2
    return z;                                     // 20.3
}                                                 // 21
