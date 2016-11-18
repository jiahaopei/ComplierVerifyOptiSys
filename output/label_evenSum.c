#include <stdio.h>                                // 1

int main() {                                      // 2
    int n;                                        // 2.1
    int i;                                        // 2.2
    int sum;                                      // 2.3
    int tmp;                                      // 2.4
    double a, b;                                  // 2.5


    scanf("%d %f %f", &n, &a, &b);                // 2.6
    sum = 0;                                      // 2.7

    for(i = 1; i <= n; i++) {                     // 2.8
        tmp = i % 2;                              // 2.8.1
        if(tmp == 0) {                            // 2.8.2
            sum = sum + i;                        // 2.8.2.1
        } else {                                  // 2.8.3
            sum = sum - i * 2;                    // 2.8.3.1
        
        }                                         // 2.8.4
    }                                             // 2.9

    printf("sum is %d", sum);                     // 2.10

    return 0;                                     // 2.11
}                                                 // 3
