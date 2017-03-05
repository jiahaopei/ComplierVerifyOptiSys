#include <stdio.h>

/**
* 测试short类型
*/
int main() {
    short a;
    short b;
    short c;
    short d;

    scanf("%hd %hd %hd %hd", &a, &b, &c, &d);

    // 复杂表达式
    a = (a | c) || (b & c) && !d;
    for (a = 1; a < b; a++) {
        if (a % 3 == 1) {
            d = d + c;
        } else {
            d = d * a;
        }
    }

    printf("%hd %hd hd", a, b, c);

    return 0;
}