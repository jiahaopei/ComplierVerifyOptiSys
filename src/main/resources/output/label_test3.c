#include <stdio.h>                                // 1
#include <stdlib.h>                               // 2

int main() {                                      // 3
    int a;                                        // 3.1
    int b;                                        // 3.2
    int c;                                        // 3.3
    int d;                                        // 3.4


    scanf("%d %d %d %d", &a, &b, &c, &d);         // 3.5


    a = b + c - d + a;                            // 3.6

    return 0;                                     // 3.7
}                                                 // 4
