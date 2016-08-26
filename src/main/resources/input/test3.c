#include <stdio.h>
#include <stdlib.h>

int main() {
    int a;
    int b;
    int c;
    int d;


    scanf("%d %d %d %d", &a, &b, &c, &d);

    //a = (a | c) || (b & c) && !d;

    a = b + c - d + a;

    return 0;
}