	.file	"evenSum.c"
	.section .rodata
	.align 2                               # 2.5
.LC0:	                                  # 2.5
	.string	"%d"                           # 2.5
	.align 2                               # 2.9
.LC5:	                                  # 2.9
	.string	"sum is %d"                    # 2.9

	.section ".text"
	.align 2                               # 2
	.globl main                            # 2
	.type main, @function                  # 2
main:	                                  # 2
	stwu 1,-16(1)                          # 2
	stw 31,12(1)                           # 2
	mr 31,1                                # 2

	lis 0,.LC0@ha                          # 2.5
	addic 10,0,.LC0@l                      # 2.5
	mr 3,10                                # 2.5
	addi 11,31,8                           # 2.5
	mr 4,11                                # 2.5
	crxor 6,6,6                            # 2.5
	bl __isoc99_scanf                      # 2.5

	li 0,0                                 # 2.6
	stw 0,16(31)                           # 2.6

	li 0,1                                 # 2.7
	stw 0,12(31)                           # 2.7

	b .L1                                  # 2.7
.L2:	                                   # 2.7
	lwz 0,12(31)                           # 2.7.1
	li 9,2                                 # 2.7.1
	divw 11,0,9                            # 2.7.1
	mullw 9,11,9                           # 2.7.1
	subf 0,9,0                             # 2.7.1
	stw 0,24(31)                           # 2.7.1

	lwz 0,24(31)                           # 2.7.1
	stw 0,20(31)                           # 2.7.1

	lwz 0,20(31)                           # 2.7.2
	li 9,0                                 # 2.7.2
	cmp 7,0,0,9                            # 2.7.2
	li 0,0                                 # 2.7.2
	li 9,1                                 # 2.7.2
	isel 0,9,0,30                          # 2.7.2
	stw 0,24(31)                           # 2.7.2

	lwz 0,24(31)                           # 2.7.2
	cmpi 7,0,0,0                           # 2.7.2
	beq 7,.L3                              # 2.7.2

	lwz 9,16(31)                           # 2.7.2.1
	lwz 0,12(31)                           # 2.7.2.1
	add 0,9,0                              # 2.7.2.1
	stw 0,24(31)                           # 2.7.2.1

	lwz 0,24(31)                           # 2.7.2.1
	stw 0,16(31)                           # 2.7.2.1

	b .L4                                  # 2.7.3
.L3:	                                   # 2.7.2

	lwz 9,12(31)                           # 2.7.3.1
	li 0,2                                 # 2.7.3.1
	mullw 0,9,0                            # 2.7.3.1
	stw 0,24(31)                           # 2.7.3.1

	lwz 9,16(31)                           # 2.7.3.1
	lwz 0,24(31)
	subf 0,9,0                             # 2.7.3.1
	stw 0,28(31)                           # 2.7.3.1

	lwz 0,28(31)                           # 2.7.3.1
	stw 0,16(31)                           # 2.7.3.1

.L4:	                                   # 2.7.3

	lwz 0,12(31)                           # 2.7
	addic 0,0,1                            # 2.7
	stw 0,12(31)                           # 2.7

.L1:	                                   # 2.7
	lwz 0,12(31)                           # 2.7
	lwz 9,8(31)                            # 2.7
	cmp 7,0,0,9                            # 2.7
	li 0,1                                 # 2.7
	isel 0,0,0,29                          # 2.7
	stw 0,24(31)                           # 2.7

	lwz 0,24(31)                           # 2.7
	cmpi 7,0,0,0                           # 2.7
	bne 7,.L2                              # 2.7

	lis 0,.LC5@ha                          # 2.9
	addic 0,0,.LC5@l                       # 2.9
	mr 3,0                                 # 2.9
	lwz 4,16(31)                           # 2.9
	crxor 6,6,6                            # 2.9
	bl printf                              # 2.9

	li 0,0                                 # 2.10
	mr 3,0                                 # 2.10
	addi 11,31,16                          # 2
	lwz 31,-4(11)                          # 2
	mr 1,11                                # 2
	blr                                    # 2
	.size main,.-main                      # 2
	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
