#include <stdio.h>                                // 1

int main() {                                      // 2
    char a;                                       // 2.1
    char b;                                       // 2.2
    char c;                                       // 2.3


    scanf("%c %c", &a, &b);                       // 2.4
    printf("%c = %d %c = %d\n", a, a, b, b);      // 2.5

    a = 'c';                                      // 2.6
    b = '\'' + 99;                                // 2.7

    return 0;                                     // 2.8
}                                                 // 3
