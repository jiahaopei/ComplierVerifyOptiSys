	.file	"test2.c"
	.section .rodata
	.align 2                               # 3.5
.LC0:	                                  # 3.5
	.string	"%d %d"                        # 3.5
	.align 2                               # 3.9
.LC4:	                                  # 3.9
	.string	"c is %d for the first time!"  # 3.9
	.align 2                               # 3.10
.LC5:	                                  # 3.10
	.string	"%d"                           # 3.10

	.align 2                               # 3.15
.LC8:	                                  # 3.15
	.string	"The biggest sqrt root of %d is %d"# 3.15

	.section ".text"
	.align 2                               # 3
	.globl main                            # 3
	.type main, @function                  # 3
main:	                                  # 3
	stwu 1,-16(1)                          # 3
	stw 31,12(1)                           # 3
	mr 31,1                                # 3

	lis 0,.LC0@ha                          # 3.5
	addic 10,0,.LC0@l                      # 3.5
	mr 3,10                                # 3.5
	addi 11,31,8                           # 3.5
	mr 4,11                                # 3.5
	addi 12,31,12                          # 3.5
	mr 5,12                                # 3.5
	crxor 6,6,6                            # 3.5
	bl __isoc99_scanf                      # 3.5

	li 0,0                                 # 3.6
	stw 0,16(31)                           # 3.6

.L1:	                                   # 3.7
	lwz 0,8(31)                            # 3.7.1
	li 9,2                                 # 3.7.1
	divw 11,0,9                            # 3.7.1
	mullw 9,11,9                           # 3.7.1
	subf 0,9,0                             # 3.7.1
	stw 0,24(31)                           # 3.7.1

	lwz 0,24(31)
	li 9,0                                 # 3.7.1
	cmp 7,0,0,9                            # 3.7.1
	li 0,0                                 # 3.7.1
	li 9,1                                 # 3.7.1
	isel 0,9,0,30                          # 3.7.1
	stw 0,28(31)                           # 3.7.1

	lwz 0,28(31)                           # 3.7.1
	cmpi 7,0,0,0                           # 3.7.1
	beq 7,.L2                              # 3.7.1

	lwz 9,8(31)                            # 3.7.1.1
	li 0,2                                 # 3.7.1.1
	mullw 0,9,0                            # 3.7.1.1
	stw 0,24(31)                           # 3.7.1.1

	lwz 9,16(31)                           # 3.7.1.1
	lwz 0,24(31)
	subf 0,9,0                             # 3.7.1.1
	stw 0,28(31)                           # 3.7.1.1

	lwz 0,28(31)                           # 3.7.1.1
	stw 0,16(31)                           # 3.7.1.1

.L2:	                                   # 3.7.1

	lwz 9,8(31)                            # 3.7.3
	li 0,2                                 # 3.7.3
	mullw 0,9,0                            # 3.7.3
	stw 0,24(31)                           # 3.7.3

	lwz 9,16(31)                           # 3.7.3
	lwz 0,24(31)
	add 0,9,0                              # 3.7.3
	stw 0,28(31)                           # 3.7.3

	lwz 0,28(31)                           # 3.7.3
	stw 0,16(31)                           # 3.7.3

	lwz 0,8(31)                            # 3.7.4
	addic 0,0,1                            # 3.7.4
	stw 0,8(31)                            # 3.7.4

	lwz 0,8(31)                            # 3.8
	lwz 9,12(31)                           # 3.8
	cmp 7,0,0,9                            # 3.8
	li 0,0                                 # 3.8
	li 9,1                                 # 3.8
	isel 0,9,0,28                          # 3.8
	stw 0,24(31)                           # 3.8

	lwz 0,24(31)                           # 3.7
	cmpi 7,0,0,0                           # 3.7
	bne 7,.L1                              # 3.7

	lis 0,.LC4@ha                          # 3.9
	addic 0,0,.LC4@l                       # 3.9
	mr 3,0                                 # 3.9
	lwz 4,16(31)                           # 3.9
	crxor 6,6,6                            # 3.9
	bl printf                              # 3.9

	lis 0,.LC5@ha                          # 3.10
	addic 10,0,.LC5@l                      # 3.10
	mr 3,10                                # 3.10
	addi 11,31,8                           # 3.10
	mr 4,11                                # 3.10
	crxor 6,6,6                            # 3.10
	bl __isoc99_scanf                      # 3.10

	li 0,1                                 # 3.11
	stw 0,12(31)                           # 3.11

	lwz 9,12(31)                           # 3.12
	lwz 0,12(31)                           # 3.12
	mullw 0,9,0                            # 3.12
	stw 0,24(31)                           # 3.12

	lwz 0,24(31)                           # 3.12
	stw 0,16(31)                           # 3.12

	b .L6                                  # 3.13
.L7:	                                   # 3.13
	lwz 0,12(31)                           # 3.13.1
	addic 0,0,1                            # 3.13.1
	stw 0,12(31)                           # 3.13.1

	lwz 9,12(31)                           # 3.13.2
	lwz 0,12(31)                           # 3.13.2
	mullw 0,9,0                            # 3.13.2
	stw 0,24(31)                           # 3.13.2

	lwz 0,24(31)                           # 3.13.2
	stw 0,16(31)                           # 3.13.2

.L6:	                                   # 3.13
	lwz 0,16(31)                           # 3.13
	lwz 9,8(31)                            # 3.13
	cmp 7,0,0,9                            # 3.13
	li 0,0                                 # 3.13
	li 9,1                                 # 3.13
	isel 0,9,0,28                          # 3.13
	stw 0,24(31)                           # 3.13

	lwz 0,24(31)                           # 3.13
	cmpi 7,0,0,0                           # 3.13
	bne 7,.L7                              # 3.13
	lis 0,.LC8@ha                          # 3.15
	addic 0,0,.LC8@l                       # 3.15
	mr 3,0                                 # 3.15
	lwz 4,8(31)                            # 3.15
	lwz 5,12(31)                           # 3.15
	crxor 6,6,6                            # 3.15
	bl printf                              # 3.15

	li 0,0                                 # 3.16
	mr 3,0                                 # 3.16
	addi 11,31,16                          # 3
	lwz 31,-4(11)                          # 3
	mr 1,11                                # 3
	blr                                    # 3
	.size main,.-main                      # 3
	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
