#include <stdio.h>

int main() {
    char a;
    char b;
    char c;


    scanf("%c %c", &a, &b);
    printf("%c = %d %c = %d\n", a, a, b, b);

    a = 'c';
    b = '\'' + a;

    return 0;
}