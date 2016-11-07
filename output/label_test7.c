#include <stdio.h>                                // 1
#include <ctype.h>                                // 2
#include "func1.h"                                // 7
#include "func2.h"                                // 12

int inc(int x) {                                  // 13
    int z;                                        // 13.1
    z = x + 1;                                    // 13.2
    return z;                                     // 13.3
}                                                 // 14

int main() {                                      // 15
    int a, b, c;                                  // 15.1
    int d;                                        // 15.2
    int e;                                        // 15.3

    a = 1;                                        // 15.4
    b = 2;                                        // 15.5
    c = add(a, 3);                                // 15.6
    d = sub(a, c, b);                             // 15.7
    e = inc(4);                                   // 15.8

    printf("The add result is : %d\n", c);        // 15.9
    printf("The sub result is : %d\n", d);        // 15.10
    return 0;                                     // 15.11
}                                                 // 16
