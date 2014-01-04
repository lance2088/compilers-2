#! /bin/bash
echo "=========== ANTLR4 panko.g4===========" 
java -jar /home/petrzlen/bin/antlr-4.1-complete.jar panko.g4 -visitor
mv *java src/
mv *tokens llvm/
echo "=========== LIBRARY library.c=========="
cd bin
gcc -shared -fPIC -std=c99 library.c -o library.so
cd ..
echo "=========== JAVKA src/===========" 
cd src
javac *java
mv *class ../bin
cd ..
echo "=========== SKOMPILUJ do LLVM $1===========" 
cd bin
java Compiler < ../$1.panko > ../llvm/$1.ll
echo "  --compiler.out"
less compiler.out | sed 's/(/\n/g'
cd ..
echo "=========== ZOPTIMALIZUJ $1===========" 
opt-2.9 -S -std-compile-opts llvm/$1.ll > llvm/$1.optimized.ll
echo "=========== MOTAJ $1===========" 
lli-2.9 -load=bin/library.so llvm/$1.optimized.ll < $1.in > llvm/$1.out
echo "=========== TEST $1============" 
diff "$1.test" llvm/$1.out
