#include <stdio.h>                                // 1

int f(int a);                                     // 2
int g(int b);                                     // 3

int main() {                                      // 4
    int n;                                        // 4.1
    int i;                                        // 4.2
    int sum;                                      // 4.3
    int tmp;                                      // 4.4
    double a, b;                                  // 4.5


    scanf("%d %f %f", &n, &a, &b);                // 4.6
    sum = 0;                                      // 4.7

    for(i = 1; i <= n; i++) {                     // 4.8
        tmp = i % 2;                              // 4.8.1
        if((tmp == 0)) {                          // 4.8.2
            sum = sum + i;                        // 4.8.2.1
        } else {                                  // 4.8.3
            sum = sum - i * 2;                    // 4.8.3.1
        
        }                                         // 4.8.4
    }                                             // 4.9

    tmp = f(n);                                   // 4.10

    printf("sum is %d", sum);                     // 4.11

    return 0;                                     // 4.12
}                                                 // 5

int f(int a) {                                    // 6
    int tmp;                                      // 6.1
    if (a <= 1) {                                 // 6.2
        return 1;                                 // 6.2.1
    }                                             // 6.3
    tmp = g(a, (a * 2));                          // 6.4

    return a * tmp;                               // 6.5
}                                                 // 7

int g(int b, int c) {                             // 8
    return c + b;                                 // 8.1
}                                                 // 9


