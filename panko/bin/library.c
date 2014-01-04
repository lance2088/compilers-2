#include <stdlib.h>
#include <stdio.h>
#include <time.h> 

int printInt(int a) {
        printf("%d\n", a);
        return 0;
}

int printFloat(float a) {
        printf("%f\n", a);
        return 0;
}

int printChar(char c) {
  putc(c, stdout); 
  return 0; 
}

int iexp(int a, int b) {
        int ret = 1;
        for (int i = 0; i < b; i++) {
                ret *= a;
        }
        return ret;
}

int scanInt(){
  int result;
  scanf("%d", &result);
  return result; 
}

int scanChar(){
  char result;
  scanf("%c", &result);
  return result; 
}

float scanFloat(){
  float result;
  scanf("%f", &result);
  return result; 
}


int* MALLOC(int a){
  return malloc(a); 
}

int FREE(int* a){
  free(a); 
}

int SET_RANDOM(){
  srand(time(NULL));
}

int RANDOM(int r){
  return rand();
}
