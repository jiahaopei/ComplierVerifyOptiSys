#include <stdio.h>                                // 1
#include <stdlib.h>                               // 2

int main() {                                      // 3
    int i;                                        // 3.1
    int count;                                    // 3.2
    double number;                                // 3.3
    double x;                                     // 3.4
    double sum;                                   // 3.5

     printf("请输入5个double值：");                     // 3.6
     for (i = 0; i < count; i++) {                // 3.7
     	printf("%2d>",i+1);                         // 3.7.1
     	scanf("%lf",&number);                       // 3.7.2
        x = 1.0 / number;                         // 3.7.3
        printf("%f\n",x);                         // 3.7.4

        sum = sum + number;                       // 3.7.5
     }                                            // 3.8

     printf("SUM = %0.2f\n",sum);                 // 3.9

    return 0;                                     // 3.10
}                                                 // 4
