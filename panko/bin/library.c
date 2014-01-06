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

int _printSlzy(){
  printf("%s\n", "Olympic - Slzy tvý mámy (1972)\n-------------\nChvilku vzpomínej, je to všechno jen pár let\nNa kytaru v duchu hrej, tvoje parta je tu hned\nZ cigaret je modrej dým, hraje magneťák\nHolka sedla na tvůj klín, nevíš ani jak,\nnevíš jak.\n\nTvý roky bláznivý chtěly křídla pro svůj let\nDneska už možná nevíš sám proč tě tenkrát pálil svět\nChtěl jsi prachy na mejdan, byl to hloupej špás\nKdyž jsi v noci vyšel ven, snad ses trochu třás,\ntrochu třás\n\nKdyž tě našel noční hlídač\nbyl by to jen příběh bláznivýho kluka\nNebejt nože ve tvejch dětskej rukách\nNebejt strachu mohlo to bejt všechno jináč\n\nR.:\nSlzy tvý mámy šedivý stékají na polštář\nKdo tě zná, se vůbec nediví, že stárne její tvář\nNečekej úsměv od ženy, který jsi všechno vzal\nJen pro tvý touhy zborcený,\nléta ztracený,\nty oči pláčou dál.\n\n\nKdyž jsi vyšel ven, ze žalářních vrat\nMožná, že jsi tenkrát chtěl znovu začínat\nPoctivejma rukama, jako správnej chlap\nsnad se někdo ušklíb jen, že jsi křivě šláp,\nkřivě šláp\n\nI když byl někdo k tobě krutej\nProč jsi znovu začal mezi svejma\nTvůj pocit křivdy se pak těžko smejvá\nKdyž hledáš vinu vždycky jenom v druhejch. \n\n\nRef.:...\nSlzy tvý mámy šedivý stékají na polštář\nKdo tě zná, se vůbec nediví, že stárne její tvář\nNečekej úsměv od ženy, který jsi všechno vzal\nVrať jí ty touhy zborcený,\nať pro léta ztracený\nnemusí plakat dál.\n-------------\nChords\nEmi Ami\nD G\nEmi E7\nAmi Emi\nHmi Emi D Emi D Emi");
}
