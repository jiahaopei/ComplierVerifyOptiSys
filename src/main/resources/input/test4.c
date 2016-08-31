#include <stdio.h>
#include <stdlib.h>

int main() {
    double a;
    double b;
    double c;

    a = 1.2;
    b = 1.3;
    c = a;

    a = 1;

    a = b + c - a * b / c;


    return 0;
}