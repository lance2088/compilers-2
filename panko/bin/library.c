#include <stdlib.h>
#include <stdio.h>
#include <time.h> 

int _printInt(int a) {
        printf("%d\n", a);
        return 0;
}

int _printFloat(float a) {
        printf("%f\n", a);
        return 0;
}

int _printChar(char c) {
  putc(c, stdout); 
  return 0; 
}

int _printString(char* s) {
  printf("%s\n", s); 
}

int _iexp(int a, int b) {
        int ret = 1;
        for (int i = 0; i < b; i++) {
                ret *= a;
        }
        return ret;
}

int _scanInt(){
  int result;
  scanf("%d", &result);
  return result; 
}

int _scanChar(){
  char result;
  scanf("%c", &result);
  return result; 
}

float _scanFloat(){
  float result;
  scanf("%f", &result);
  return result; 
}


int* _MALLOC(int a){
  return malloc(a); 
}

int _FREE(int* a){
  free(a); 
}

int _SET_RANDOM(){
  srand(time(NULL));
}

int _RANDOM(int r){
  return rand() % r;
}

int cudzia(int a, int b){
  //printf("%d+%d\n", a, b); 
  return a+b; 
}
