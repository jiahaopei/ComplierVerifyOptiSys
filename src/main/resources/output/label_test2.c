#include <stdio.h>                                // 1
#include <stdlib.h>                               // 2

int main() {                                      // 3
    int a;                                        // 3.1
    int b;                                        // 3.2
    int c;                                        // 3.3
    int d;                                        // 3.4

    scanf("%d %d", &a, &b);                       // 3.5
    c = 0;                                        // 3.6
    do {                                          // 3.7
        if (a % 2 == 0) {                         // 3.7.1
            c = c - a * 2;                        // 3.7.1.1
        }                                         // 3.7.2
        c = c + a * 2;                            // 3.7.3
        a++;                                      // 3.7.4
    } while (a < b);                              // 3.8
    printf("c is %d for the first time!", c);     // 3.9

    scanf("%d", &a);                              // 3.10
    b = 1;                                        // 3.11
    c = b * b;                                    // 3.12
    while (c < a) {                               // 3.13
        b++;                                      // 3.13.1
        c = b * b;                                // 3.13.2
    }                                             // 3.14
    printf("The biggest sqrt root of %d is %d", a, b);// 3.15


    
    for (a = 0; a < 10; a++) {                    // 3.16
        b++;                                      // 3.16.1

    }                                             // 3.17
    
    return 0;                                     // 3.18
}                                                 // 4
