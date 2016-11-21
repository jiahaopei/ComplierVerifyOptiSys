#include <stdio.h>

int main() {
    char a;
    char b;
    char c;
    int ans;


    scanf("%c %c", &a, &b);
    printf("%c = %d %c = %d\n", a, a, b, b);

     ans = 'c' % a;
    ans = a % b;
    c = a % b;
   

    return 0;
}