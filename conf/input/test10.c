#include <stdio.h>
#include <stdlib.h>

float add(float a, float b);
float fun(float a, float b, float c);

int main() {
    float a, b, c;
    float d;

    scanf("%f %f %f %f", &a, &b, &c, &d);
    printf("%f %f %f %f", a, b, c, d);

    a = 1.2f;
    b = 1.3F;
    
    if (a < b) {
        c = a * b + a;
    } else {
        while (a >= b) {
            c = a * a;
            a = a / 2;
        }
    }
    printf("%f", c);

    return 0;
}

float add(float a, float b) {
    return a + b;
}

float fun(float a, float b, float c) {
    do {
        b = a * 0.318;
        a = b * c;
    } while (a < c);
    return b;
}