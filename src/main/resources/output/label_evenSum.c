#include <stdio.h>                                // 1

int main() {                                      // 2
    int n;                                        // 2.1
    int i;                                        // 2.2
    int sum;                                      // 2.3
    int tmp;                                      // 2.4

    scanf("%d", &n);                              // 2.5
    sum = 0;                                      // 2.6

    for(i = 1; i <= n; i++) {                     // 2.7
        tmp = i % 2;                              // 2.7.1
        if(tmp == 0) {                            // 2.7.2
            sum = sum + i;                        // 2.7.2.1
        } else {                                  // 2.7.3
            sum = sum - i * 2;                    // 2.7.3.1
        
        }                                         // 2.7.4
    }                                             // 2.8

    printf("sum is %d", sum);                     // 2.9

    return 0;                                     // 2.10
}                                                 // 3
