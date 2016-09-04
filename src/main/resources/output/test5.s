	.file	"test5.c"

	.section .rodata
	.align 2                               # 3.3
.LC0:	                                  # 3.3
	.string	"%f %f %f %f"                  # 3.3
	.align 2                               # 3.4
.LC1:	                                  # 3.4
	.string	"%f %f %f %f"                  # 3.4
	.align 3                               # 3.5
.LC2:	                                  # 3.5
	4607182418800017408                    # 3.5
	4611686018427387904                    # 3.5
	.align 3                               # 3.6
.LC3:	                                  # 3.6
	4607182418800017408                    # 3.6
	4613937818241073152                    # 3.6
	.align 3                               # 3.8
.LC4:	                                  # 3.8
	1                                      # 3.8
	0                                      # 3.8

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

	lis 0,.LC0@ha                          # 3.3
	addic 10,0,.LC0@l                      # 3.3
	mr 3,10                                # 3.3
	crxor 6,6,6                            # 3.3
	bl __isoc99_scanf                      # 3.3

	lis 0,.LC1@ha                          # 3.4
	addic 0,0,.LC1@l                       # 3.4
	mr 3,0                                 # 3.4
	crxor 6,6,6                            # 3.4
	bl printf                              # 3.4

	lis 9,.LC2@ha                          # 3.5
	lfs 0,.LC2@l(9)                        # 3.5
	stfs 0,8(31)                           # 3.5

	lis 9,.LC3@ha                          # 3.6
	lfs 0,.LC3@l(9)                        # 3.6
	stfs 0,12(31)                          # 3.6

	lfs 13,8(31)                           # 3.7
	lfs 0,12(31)                           # 3.7
	fmuls 0,13,0                           # 3.7
	stfs 0,24(31)                          # 3.7

	lfs 13,24(31)                          # 3.7
	lfs 0,8(31)                            # 3.7
	fadds 0,13,0                           # 3.7
	stfs 0,28(31)                          # 3.7

	lfs 0,28(31)                           # 3.7
	stfs 0,16(31)                          # 3.7

	lfs 13,8(31)                           # 3.8
	lis 9,.LC4@ha                          # 3.8
	lfs 0,.LC4@l(9)                        # 3.8
	fcmpu 7,0,13                           # 3.8
	li 0,0                                 # 3.8
	li 9,1                                 # 3.8
	isel 0,9,0,29                          # 3.8
	stw 0,24(31)                           # 3.8

	li 0,0                                 # 3.9
	mr 3,0                                 # 3.9
	lwz 11,0(1)                            # 3
	lwz 0,4(11)                            # 3
	mtlr 0                                 # 3
	lwz 31,-4(11)                          # 3
	mr 1,11                                # 3
	blr                                    # 3
	.size main,.-main                      # 3

	.section .rodata

	.section ".text"
	.align 2                               # 5
	.globl add                             # 5
	.type add, @function                   # 5
add:	                                   # 5
	stwu 1,-32(1)                          # 5
	stw 31,28(1)                           # 5
	mr 31,1                                # 5

	lwz 11,0(1)                            # 5
	lwz 31,-4(11)                          # 5
	mr 1,11                                # 5
	blr                                    # 5
	.size add,.-add                        # 5

	.section .rodata

	.section ".text"
	.align 2                               # 7
	.globl sub                             # 7
	.type sub, @function                   # 7
sub:	                                   # 7
	stwu 1,-32(1)                          # 7
	stw 31,28(1)                           # 7
	mr 31,1                                # 7

	lwz 11,0(1)                            # 7
	lwz 31,-4(11)                          # 7
	mr 1,11                                # 7
	blr                                    # 7
	.size sub,.-sub                        # 7

	.section .rodata

	.section ".text"
	.align 2                               # 9
	.globl mul                             # 9
	.type mul, @function                   # 9
mul:	                                   # 9
	stwu 1,-32(1)                          # 9
	stw 31,28(1)                           # 9
	mr 31,1                                # 9

	lwz 11,0(1)                            # 9
	lwz 31,-4(11)                          # 9
	mr 1,11                                # 9
	blr                                    # 9
	.size mul,.-mul                        # 9

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
