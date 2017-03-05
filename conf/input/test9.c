#include <stdio.h>
#include <stdlib.h>

/** 
* 计算输入double类型数据的倒数
*/
int main() {
    int i;
    int count;
    double number;
    double x;
    double sum;

     printf("请输入5个double值：");
     for (i = 0; i < count; i++) {
     	printf("%2d>",i+1);
     	scanf("%lf",&number);
        x = 1.0 / number;
        printf("%f\n",x);

        sum = sum + number;
     }

     printf("SUM = %0.2f\n",sum);

    return 0;
}
