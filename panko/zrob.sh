#! /bin/bash
echo "=========== ANTLR4 ===========" 
java -jar /home/petrzlen/bin/antlr-4.1-complete.jar panko.g4 -visitor
mv *java src/
mv *tokens llvm/
echo "=========== LIBRARY =========="
cd bin
gcc -shared -fPIC -std=c99 library.c -o library.so
cd ..
echo "=========== JAVKA ===========" 
cd src
javac *java
mv *class ../bin
cd ..
echo "=========== SKOMPILUJ do LLVM ===========" 
cd bin
java Compiler < ../$1.panko > ../llvm/$1.ll
echo "  --compiler.out"
less compiler.out | sed 's/(/\n/g'
cd ..
echo "=========== ZOPTIMALIZUJ ===========" 
opt-2.9 -S -std-compile-opts llvm/$1.ll > llvm/$1.optimized.ll
echo "=========== MOTAJ ===========" 
lli-2.9 -load=bin/library.so llvm/$1.optimized.ll > llvm/$1.out
echo "=========== TEST ============" 
diff "$1.test" llvm/$1.out
