#include <stdio.h>                                // 1
#include <stdlib.h>                               // 2

int main() {                                      // 3
    double a;                                     // 3.1
    double b;                                     // 3.2
    double c;                                     // 3.3

    a = 1.2;                                      // 3.4
    b = 1.3;                                      // 3.5
    c = a;                                        // 3.6

    a = 1;                                        // 3.7

    a = b + c - a * b / c;                        // 3.8


    return 0;                                     // 3.9
}                                                 // 4
