#include <stdio.h>                                // 1
#include "func1.h"                                // 6
#include "func2.h"                                // 11

int inc(int x) {                                  // 12
    int z;                                        // 12.1
    z = x + 1;                                    // 12.2
    return z;                                     // 12.3
}                                                 // 13

int main() {                                      // 14
    int a, b, c;                                  // 14.1
    int d;                                        // 14.2
    int e;                                        // 14.3

    a = 1;                                        // 14.4
    b = 2;                                        // 14.5
    c = add(a, 3);                                // 14.6
    d = sub(a, c, b);                             // 14.7
    e = inc(4);                                   // 14.8

    printf("The add result is : %d\n", c);        // 14.9
    printf("The sub result is : %d\n", d);        // 14.10
    return 0;                                     // 14.11
}                                                 // 15
