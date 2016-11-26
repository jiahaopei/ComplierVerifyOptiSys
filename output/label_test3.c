#include <stdio.h>                                // 1

int main() {                                      // 2
    short a;                                      // 2.1
    short b;                                      // 2.2
    short c;                                      // 2.3
    short d;                                      // 2.4


    scanf("%hd %hd %hd %hd", &a, &b, &c, &d);     // 2.5


    a = b + c - d + a;                            // 2.6
    printf("%d", a);                              // 2.7


    return 0;                                     // 2.8
}                                                 // 3
