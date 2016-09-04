	.file	"test4.c"

	.section .rodata
	.align 3                                         # 3.4_as
.LC0:	                                            # 3.4_as
	4607182418800017408                              # 3.4_as
	4611686018427387904                              # 3.4_as
	.align 3                                         # 3.5_as
.LC1:	                                            # 3.5_as
	4607182418800017408                              # 3.5_as
	4613937818241073152                              # 3.5_as
	.align 3                                         # 3.7_as
.LC2:	                                            # 3.7_as
	1                                                # 3.7_as
	0                                                # 3.7_as

	.section ".text"
	.align 2                                         # 3_fs
	.globl main                                      # 3_fs
	.type main, @function                            # 3_fs
main:	                                            # 3_fs
	stwu 1,-32(1)                                    # 3_fs
	mflr 0                                           # 3_fs
	stw 31,28(1)                                     # 3_fs
	stw 0,36(1)                                      # 3_fs
	mr 31,1                                          # 3_fs

	lis 9,.LC0@ha                                    # 3.4_as
	lfd 0,.LC0@l(9)                                  # 3.4_as
	stfd 0,8(31)                                     # 3.4_as

	lis 9,.LC1@ha                                    # 3.5_as
	lfd 0,.LC1@l(9)                                  # 3.5_as
	stfd 0,12(31)                                    # 3.5_as

	lfd 0,8(31)                                      # 3.6_as
	stfd 0,16(31)                                    # 3.6_as

	lis 9,.LC2@ha                                    # 3.7_as
	lfd 0,.LC2@l(9)                                  # 3.7_as
	stfd 0,8(31)                                     # 3.7_as

	lfd 13,12(31)                                    # 3.8_ex
	lfd 0,16(31)                                     # 3.8_ex
	fdiv 0,13,0                                      # 3.8_ex
	stfd 0,20(31)                                    # 3.8_ex

	lfd 13,8(31)                                     # 3.8_ex
	lfd 0,20(31)                                     # 3.8_ex
	fmul 0,13,0                                      # 3.8_ex
	stfd 0,24(31)                                    # 3.8_ex

	lfd 13,16(31)                                    # 3.8_ex
	lfd 0,24(31)                                     # 3.8_ex
	fsub 0,13,0                                      # 3.8_ex
	stfd 0,28(31)                                    # 3.8_ex

	lfd 13,12(31)                                    # 3.8_ex
	lfd 0,28(31)                                     # 3.8_ex
	fadd 0,13,0                                      # 3.8_ex
	stfd 0,32(31)                                    # 3.8_ex

	lfd 0,32(31)                                     # 3.8_as
	stfd 0,8(31)                                     # 3.8_as

	li 0,0                                           # 3.9_re
	mr 3,0                                           # 3.9_re
	lwz 11,0(1)                                      # 3_fs
	lwz 0,4(11)                                      # 3_fs
	mtlr 0                                           # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size main,.-main                                # 3_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
