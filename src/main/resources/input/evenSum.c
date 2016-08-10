#include <stdio.h>

/**
*   a simple input
*/
int main() {
    int n;
    int i;
    int sum;
    int tmp;

    scanf("%d", &n);    // read n
    sum = 0;

    /**
    * if i is even, then add to sum,
    * otherwise substract i * 2
    */
    for(i = 1; i <= n; i++) { 
        tmp = i % 2;
        if(tmp == 0) {
            sum = sum + i;
        } else {
            sum = sum - i * 2;
        
        } // end if
    }   // end for

    printf("sum is %d", sum);

    return 0;
}