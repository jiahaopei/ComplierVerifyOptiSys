#include <stdio.h>

int main() {
    short a;
    short b;
    short c;
    short d;

    scanf("%hd %hd %hd %hd", &a, &b, &c, &d);

    //a = (a | c) || (b & c) && !d;

    a = 34 + b;
    c = a + b;
    a = 12 + 12;
    
    printf("%hd", a);
    printf("%hd", b + 23);


    return 0;
}