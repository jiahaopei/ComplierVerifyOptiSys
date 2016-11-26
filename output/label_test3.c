#include <stdio.h>                                // 1

int main() {                                      // 2
    short a;                                      // 2.1
    short b;                                      // 2.2
    short c;                                      // 2.3
    short d;                                      // 2.4

    scanf("%hd %hd %hd %hd", &a, &b, &c, &d);     // 2.5


    a = 34 + b;                                   // 2.6
    c = a + b;                                    // 2.7
    a = 12 + 12;                                  // 2.8
    
    printf("%hd", a);                             // 2.9
    printf("%hd", b + 23);                        // 2.10


    return 0;                                     // 2.11
}                                                 // 3
