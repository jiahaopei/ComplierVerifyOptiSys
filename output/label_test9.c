#include <stdio.h>                                // 1

int main() {                                      // 2
    char a;                                       // 2.1
    char b;                                       // 2.2
    char c;                                       // 2.3
    int ans;                                      // 2.4


    scanf("%c %c", &a, &b);                       // 2.5
    printf("%c = %d %c = %d\n", a, a, b, b);      // 2.6

    ans = 'c' % a;                                // 2.7
    ans = a % b;                                  // 2.8
    c = a % b;                                    // 2.9
   

    return 0;                                     // 2.10
}                                                 // 3
