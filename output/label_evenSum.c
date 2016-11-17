#include <stdio.h>                                // 1

int sum;                                          // 2
int main() {                                      // 3
    int n;                                        // 3.1
    int i;                                        // 3.2
    int sum;                                      // 3.3
    int tmp;                                      // 3.4

    scanf("%d", &n);                              // 3.5
    sum = 0;                                      // 3.6

    for(i = 1; i <= n; i++) {                     // 3.7
        tmp = i % 2;                              // 3.7.1
        if(tmp == 0) {                            // 3.7.2
            sum = sum + i;                        // 3.7.2.1
        } else {                                  // 3.7.3
            sum = sum - i * 2;                    // 3.7.3.1
        
        }                                         // 3.7.4
    }                                             // 3.8

    i = !n;                                       // 3.9
    i = ~n;                                       // 3.10


    printf("sum is %d", sum);                     // 3.11

    return 0;                                     // 3.12
}                                                 // 4
