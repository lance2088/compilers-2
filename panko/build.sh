#! /bin/bash
echo "=========== ANTLR4 panko.g4 TODO:specify whereis antlr===========" 
#java -jar /home/petrzlen/bin/antlr-4.1-complete.jar panko.g4 -visitor
#mv *java src/
#mv *tokens llvm/
echo "=========== LIBRARY library.c=========="
cd panko/bin
gcc -shared -fPIC -std=c99 library.c -o library.so
cd ../..
echo "=========== JAVKA src/===========" 
cd panko/src
javac *java
mv *class ../bin
cd ../..
