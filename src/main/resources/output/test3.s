	.file	"test3.c"
	.section .rodata
	.align 2                               # 3.5
.LC0:	                                  # 3.5
	.string	"%d %d %d %d"                  # 3.5

	.section ".text"
	.align 2                               # 3
	.globl main                            # 3
	.type main, @function                  # 3
main:	                                  # 3
	stwu 1,-32(1)                          # 3
	mflr 0                                 # 3
	stw 31,28(1)                           # 3
	stw 0,36(1)                            # 3
	mr 31,1                                # 3

	lis 0,.LC0@ha                          # 3.5
	addic 10,0,.LC0@l                      # 3.5
	mr 3,10                                # 3.5
	addi 11,31,a                           # 3.5
	mr 4,11                                # 3.5
	addi 12,31,b                           # 3.5
	mr 5,12                                # 3.5
	addi 13,31,c                           # 3.5
	mr 6,13                                # 3.5
	addi 14,31,d                           # 3.5
	mr 7,14                                # 3.5
	crxor 6,6,6                            # 3.5
	bl __isoc99_scanf                      # 3.5

	lwz 9,d(31)                            # 3.6
	lwz 0,a(31)                            # 3.6
	add 0,9,0                              # 3.6
	stw 0,bss_tmp1(31)                     # 3.6

	lwz 9,c(31)                            # 3.6
	lwz 0,bss_tmp1(31)                     # 3.6
	subf 0,9,0                             # 3.6
	stw 0,bss_tmp2(31)                     # 3.6

	lwz 9,b(31)                            # 3.6
	lwz 0,bss_tmp2(31)                     # 3.6
	add 0,9,0                              # 3.6
	stw 0,bss_tmp3(31)                     # 3.6

	lwz 0,bss_tmp3(31)                     # 3.6
	stw 0,a(31)                            # 3.6

	li 0,0                                 # 3.7
	mr 3,0                                 # 3.7
	lwz 11,0(1)                            # 3
	lwz 0,4(11)                            # 3
	mtlr 0                                 # 3
	lwz 31,-4(11)                          # 3
	mr 1,11                                # 3
	blr                                    # 3
	.size main,.-main                      # 3
	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
