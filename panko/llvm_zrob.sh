echo "=========== ZOPTIMALIZUJ ===========" 
opt-2.9 -S -std-compile-opts llvm/$1.ll > llvm/$1.optimized.ll
echo "=========== MOTAJ ===========" 
lli-2.9 -load=bin/library.so llvm/$1.optimized.ll > llvm/$1.out
cat llvm/$1.out

