#! /bin/bash
echo "=========== ANTLR4 ===========" 
java -jar /home/petrzlen/bin/antlr-4.1-complete.jar panko.g4 -visitor
mv *java src/
mv *tokens llvm/
echo "=========== JAVKA ===========" 
cd src
javac *java
mv *class ../bin
cd ..
echo "=========== SKOMPILUJ do LLVM ===========" 
cd bin
java Compiler < ../$1.panko > ../llvm/$1.ll
cd ..
echo "=========== ZOPTIMALIZUJ ===========" 
opt-2.9 -S -std-compile-opts llvm/$1.ll > llvm/$1.optimized.ll
echo "=========== MOTAJ ===========" 
lli-2.9 -load=bin/library.so llvm/$1.optimized.ll > llvm/$1.out
echo "=========== TEST ============" 
diff $1.test llvm/$1.out