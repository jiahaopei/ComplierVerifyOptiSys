	.file	"test5.c"

	.section .rodata
	.align 2                                         # 3.3_fc
.LC0:	                                            # 3.3_fc
	.string	"%f %f %f %f"                            # 3.3_fc
	.align 2                                         # 3.4_fc
.LC1:	                                            # 3.4_fc
	.string	"%f %f %f %f"                            # 3.4_fc
	.align 3                                         # 3.5_as
.LC2:	                                            # 3.5_as
	4607182418800017408                              # 3.5_as
	4611686018427387904                              # 3.5_as
	.align 3                                         # 3.6_as
.LC3:	                                            # 3.6_as
	4607182418800017408                              # 3.6_as
	4613937818241073152                              # 3.6_as
	.align 3                                         # 3.8_ex
.LC4:	                                            # 3.8_ex
	1                                                # 3.8_ex
	0                                                # 3.8_ex

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

	lis 0,.LC0@ha                                    # 3.3_fc
	addic 0,0,.LC0@l                                 # 3.3_fc
	mr 3,0                                           # 3.3_fc
	lfs 4,8(31)                                      # 3.3_fc
	lfs 5,12(31)                                     # 3.3_fc
	lfs 6,16(31)                                     # 3.3_fc
	lfs 7,20(31)                                     # 3.3_fc
	crxor 6,6,6                                      # 3.3_fc
	bl __isoc99_scanf                                # 3.3_fc

	lis 0,.LC1@ha                                    # 3.4_fc
	addic 0,0,.LC1@l                                 # 3.4_fc
	mr 3,0                                           # 3.4_fc
	lfs 4,8(31)                                      # 3.4_fc
	lfs 5,12(31)                                     # 3.4_fc
	lfs 6,16(31)                                     # 3.4_fc
	lfs 7,20(31)                                     # 3.4_fc
	crxor 6,6,6                                      # 3.4_fc
	bl printf                                        # 3.4_fc

	lis 9,.LC2@ha                                    # 3.5_as
	lfs 0,.LC2@l(9)                                  # 3.5_as
	stfs 0,8(31)                                     # 3.5_as

	lis 9,.LC3@ha                                    # 3.6_as
	lfs 0,.LC3@l(9)                                  # 3.6_as
	stfs 0,12(31)                                    # 3.6_as

	lfs 13,8(31)                                     # 3.7_ex
	lfs 0,12(31)                                     # 3.7_ex
	fmuls 0,13,0                                     # 3.7_ex
	stfs 0,24(31)                                    # 3.7_ex

	lfs 13,24(31)                                    # 3.7_ex
	lfs 0,8(31)                                      # 3.7_ex
	fadds 0,13,0                                     # 3.7_ex
	stfs 0,28(31)                                    # 3.7_ex

	lfs 0,28(31)                                     # 3.7_as
	stfs 0,16(31)                                    # 3.7_as

	lfs 13,8(31)                                     # 3.8_ex
	lis 9,.LC4@ha                                    # 3.8_ex
	lfs 0,.LC4@l(9)                                  # 3.8_ex
	fcmpu 7,0,13                                     # 3.8_ex
	li 0,0                                           # 3.8_ex
	li 9,1                                           # 3.8_ex
	isel 0,9,0,29                                    # 3.8_ex
	stw 0,24(31)                                     # 3.8_ex

	li 0,0                                           # 3.9_re
	mr 3,0                                           # 3.9_re
	lwz 11,0(1)                                      # 3_fs
	lwz 0,4(11)                                      # 3_fs
	mtlr 0                                           # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size main,.-main                                # 3_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 5_fs
	.globl add                                       # 5_fs
	.type add, @function                             # 5_fs
add:	                                             # 5_fs
	stwu 1,-32(1)                                    # 5_fs
	stw 31,28(1)                                     # 5_fs
	mr 31,1                                          # 5_fs

	lwz 11,0(1)                                      # 5_fs
	lwz 31,-4(11)                                    # 5_fs
	mr 1,11                                          # 5_fs
	blr                                              # 5_fs
	.size add,.-add                                  # 5_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 7_fs
	.globl sub                                       # 7_fs
	.type sub, @function                             # 7_fs
sub:	                                             # 7_fs
	stwu 1,-32(1)                                    # 7_fs
	stw 31,28(1)                                     # 7_fs
	mr 31,1                                          # 7_fs

	lwz 11,0(1)                                      # 7_fs
	lwz 31,-4(11)                                    # 7_fs
	mr 1,11                                          # 7_fs
	blr                                              # 7_fs
	.size sub,.-sub                                  # 7_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 9_fs
	.globl mul                                       # 9_fs
	.type mul, @function                             # 9_fs
mul:	                                             # 9_fs
	stwu 1,-32(1)                                    # 9_fs
	stw 31,28(1)                                     # 9_fs
	mr 31,1                                          # 9_fs

	lwz 11,0(1)                                      # 9_fs
	lwz 31,-4(11)                                    # 9_fs
	mr 1,11                                          # 9_fs
	blr                                              # 9_fs
	.size mul,.-mul                                  # 9_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
