#include <stdio.h>

int main() {
    short a;
    short b;
    short c;
    short d;


    scanf("%hd %hd %hd %hd", &a, &b, &c, &d);

    //a = (a | c) || (b & c) && !d;

    a = b + c - d + a;
    printf("%d", a);


    return 0;
}