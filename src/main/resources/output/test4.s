	.file	"test4.c"

	.section .rodata
	.align 3                               # 3.4
.LC0:	                                  # 3.4
	4607182418800017408                    # 3.4
	4611686018427387904                    # 3.4
	.align 3                               # 3.5
.LC1:	                                  # 3.5
	4607182418800017408                    # 3.5
	4613937818241073152                    # 3.5
	.align 3                               # 3.7
.LC2:	                                  # 3.7
	1                                      # 3.7
	0                                      # 3.7

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

	lis 9,.LC0@ha                          # 3.4
	lfd 0,.LC0@l(9)                        # 3.4
	stfd 0,8(31)                           # 3.4

	lis 9,.LC1@ha                          # 3.5
	lfd 0,.LC1@l(9)                        # 3.5
	stfd 0,12(31)                          # 3.5

	lfd 0,8(31)                            # 3.6
	stfd 0,16(31)                          # 3.6

	lis 9,.LC2@ha                          # 3.7
	lfd 0,.LC2@l(9)                        # 3.7
	stfd 0,8(31)                           # 3.7

	lfd 13,12(31)                          # 3.8
	lfd 0,16(31)                           # 3.8
	fdiv 0,13,0                            # 3.8
	stfd 0,20(31)                          # 3.8

	lfd 13,8(31)                           # 3.8
	lfd 0,20(31)                           # 3.8
	fmul 0,13,0                            # 3.8
	stfd 0,24(31)                          # 3.8

	lfd 13,16(31)                          # 3.8
	lfd 0,24(31)                           # 3.8
	fsub 0,13,0                            # 3.8
	stfd 0,28(31)                          # 3.8

	lfd 13,12(31)                          # 3.8
	lfd 0,28(31)                           # 3.8
	fadd 0,13,0                            # 3.8
	stfd 0,32(31)                          # 3.8

	lfd 0,32(31)                           # 3.8
	stfd 0,8(31)                           # 3.8

	li 0,0                                 # 3.9
	mr 3,0                                 # 3.9
	lwz 11,0(1)                            # 3
	lwz 0,4(11)                            # 3
	mtlr 0                                 # 3
	lwz 31,-4(11)                          # 3
	mr 1,11                                # 3
	blr                                    # 3
	.size main,.-main                      # 3

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
