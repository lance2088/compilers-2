#! /bin/bash
echo "=========== ANTLR4 ===========" 
antlr4 grammar.g4 -visitor
echo "=========== JAVKA ===========" 
javac *java
echo "=========== SKOMPILUJ do LLVM ===========" 
java Compiler < $1.panko > $1.ll
echo "=========== ZOPTIMALIZUJ ===========" 
opt-2.9 -S -std-compile-opts $1.ll > $1.optimized.ll
echo "=========== MOTAJ ===========" 
lli-2.9 -load=./library.so $1.optimized.ll
