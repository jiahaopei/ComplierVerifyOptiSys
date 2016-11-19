#include <stdio.h>                                // 1

/**                                               // 2
*   a simple input                                // 3
*/                                                // 4
int main() {                                      // 5
    int n;                                        // 5.1
    int i;                                        // 5.2
    int sum;                                      // 5.3
    int tmp;                                      // 5.4
    double a, b;                                  // 5.5


    scanf("%d %f %f", &n, &a, &b);    // read n   // 5.6
    sum = 0;                                      // 5.7

    /**                                           // 5.8
    * if i is even, then add to sum,              // 5.9
    * otherwise substract i * 2                   // 5.10
    */                                            // 5.11
    for(i = 1; i <= n; i++) {                     // 5.12
        tmp = i % 2;                              // 5.12.1
        if(tmp == 0) {                            // 5.12.2
            sum = sum + i;                        // 5.12.2.1
        } else {                                  // 5.12.3
            sum = sum - i * 2;                    // 5.12.3.1
        
        } // end if                               // 5.12.4
    }   // end for                                // 5.13

    printf("sum is %d", sum);                     // 5.14

    return 0;                                     // 5.15
}                                                 // 6
