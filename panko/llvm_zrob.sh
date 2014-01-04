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

