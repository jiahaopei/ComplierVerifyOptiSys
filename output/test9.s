	.file	"test9.c"

	.section .rodata
	.align 2                                         # 2.4_fc
.LC0:	                                            # 2.4_fc
	.string	"%c %c"                                  # 2.4_fc
	.align 2                                         # 2.5_fc
.LC1:	                                            # 2.5_fc
	.string	"%c = %d %c = %d\n"                      # 2.5_fc

	.section ".text"
	.align 2                                         # 2_fs
	.globl main                                      # 2_fs
	.type main, @function                            # 2_fs
main:	                                            # 2_fs
	stwu 1,-32(1)                                    # 2_fs
	mflr 0                                           # 2_fs
	stw 31,28(1)                                     # 2_fs
	stw 0,36(1)                                      # 2_fs
	mr 31,1                                          # 2_fs

	lis 0,.LC0@ha                                    # 2.4_fc
	addic 0,0,.LC0@l                                 # 2.4_fc
	mr 3,0                                           # 2.4_fc
	lbz 4,8(31)                                      # 2.4_fc
	lbz 5,12(31)                                     # 2.4_fc
	crxor 6,6,6                                      # 2.4_fc
	bl __isoc99_scanf                                # 2.4_fc

	lis 0,.LC1@ha                                    # 2.5_fc
	addic 0,0,.LC1@l                                 # 2.5_fc
	mr 3,0                                           # 2.5_fc
	lbz 4,8(31)                                      # 2.5_fc
	lbz 5,8(31)                                      # 2.5_fc
	lbz 6,12(31)                                     # 2.5_fc
	lbz 7,12(31)                                     # 2.5_fc
	crxor 6,6,6                                      # 2.5_fc
	bl printf                                        # 2.5_fc

	li 0,99                                          # 2.6_as
	stb 0,8(31)                                      # 2.6_as

	li 9,'\''                                        # 2.7_ex
	li 0,99                                          # 2.7_ex
	add 0,9,0                                        # 2.7_ex
	stw 0,20(31)                                     # 2.7_ex

	lbz 0,20(31)                                     # 2.7_as
	stb 0,12(31)                                     # 2.7_as

	li 0,0                                           # 2.8_re
	mr 3,0                                           # 2.8_re
	lwz 11,0(1)                                      # 2_fs
	lwz 0,4(11)                                      # 2_fs
	mtlr 0                                           # 2_fs
	lwz 31,-4(11)                                    # 2_fs
	mr 1,11                                          # 2_fs
	blr                                              # 2_fs
	.size main,.-main                                # 2_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
