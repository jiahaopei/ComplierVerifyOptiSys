#include <stdio.h>
#include <stdlib.h>

int main() {
    int a;
    int b;
    int c;
    int d;

    scanf("%d %d", &a, &b);
    c = 0;
    do {
        if (a % 2 == 0) {
            c = c - a * 2;
        }
        c = c + a * 2;
        a++;
    } while (a < b);
    printf("c is %d for the first time!", c);

    scanf("%d", &a);
    b = 1;
    c = b * b;
    while (c < a) {
        b++;
        c = b * b;
    }
    printf("The biggest sqrt root of %d is %d", a, b);


    // for 
    for (a = 0; a < 10; a++) {
        b++;

    }
    
    return 0;
}