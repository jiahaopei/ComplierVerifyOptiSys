	.file	"test3.c"

	.section .rodata
	.align 2                                         # 3.5_fc
.LC0:	                                            # 3.5_fc
	.string	"%d %d %d %d"                            # 3.5_fc

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

	lis 0,.LC0@ha                                    # 3.5_fc
	addic 0,0,.LC0@l                                 # 3.5_fc
	mr 3,0                                           # 3.5_fc
	lwz 4,8(31)                                      # 3.5_fc
	lwz 5,12(31)                                     # 3.5_fc
	lwz 6,16(31)                                     # 3.5_fc
	lwz 7,20(31)                                     # 3.5_fc
	crxor 6,6,6                                      # 3.5_fc
	bl __isoc99_scanf                                # 3.5_fc

	lwz 9,20(31)                                     # 3.6_ex
	lwz 0,8(31)                                      # 3.6_ex
	add 0,9,0                                        # 3.6_ex
	stw 0,24(31)                                     # 3.6_ex

	lwz 9,16(31)                                     # 3.6_ex
	lwz 0,24(31)                                     # 3.6_ex
	subf 0,9,0                                       # 3.6_ex
	stw 0,28(31)                                     # 3.6_ex

	lwz 9,12(31)                                     # 3.6_ex
	lwz 0,28(31)                                     # 3.6_ex
	add 0,9,0                                        # 3.6_ex
	stw 0,32(31)                                     # 3.6_ex

	lwz 0,32(31)                                     # 3.6_as
	stw 0,8(31)                                      # 3.6_as

	li 0,0                                           # 3.7_re
	mr 3,0                                           # 3.7_re
	lwz 11,0(1)                                      # 3_fs
	lwz 0,4(11)                                      # 3_fs
	mtlr 0                                           # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size main,.-main                                # 3_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
