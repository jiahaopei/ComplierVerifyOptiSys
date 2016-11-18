#include <stdio.h>

int f(int a);
int g(int b);

/**
*   a simple input
*/
int main() {
    int n;
    int i;
    int sum;
    int tmp;
    double a, b;


    scanf("%d %f %f", &n, &a, &b);    // read n
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

    tmp = f(n);

    printf("sum is %d", sum);

    return 0;
}

int f(int a) {
    int tmp;
    if (a <= 1) {
        return 1;
    }
    tmp = a - 1;
    tmp = g(tmp);

    return a * tmp;
}

int g(int b) {
    int res;
    res = f(b);
    return res;
}


