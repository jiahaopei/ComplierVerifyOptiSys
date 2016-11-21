	.file	"test9.c"

	.section .rodata
	.align 2                                         # 2.5_fc
.LC0:	                                            # 2.5_fc
	.string	"%c %c"                                  # 2.5_fc
	.align 2                                         # 2.6_fc
.LC1:	                                            # 2.6_fc
	.string	"%c = %d %c = %d\n"                      # 2.6_fc

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

	lis 0,.LC0@ha                                    # 2.5_fc
	addic 0,0,.LC0@l                                 # 2.5_fc
	mr 3,0                                           # 2.5_fc
	lbz 4,8(31)                                      # 2.5_fc
	lbz 5,12(31)                                     # 2.5_fc
	crxor 6,6,6                                      # 2.5_fc
	bl __isoc99_scanf                                # 2.5_fc

	lis 0,.LC1@ha                                    # 2.6_fc
	addic 0,0,.LC1@l                                 # 2.6_fc
	mr 3,0                                           # 2.6_fc
	lbz 4,8(31)                                      # 2.6_fc
	lbz 5,8(31)                                      # 2.6_fc
	lbz 6,12(31)                                     # 2.6_fc
	lbz 7,12(31)                                     # 2.6_fc
	crxor 6,6,6                                      # 2.6_fc
	bl printf                                        # 2.6_fc

	li 9,99                                          # 2.7_ex
	lbz 0,8(31)                                      # 2.7_ex
	rlwinm 0,0,0,0xff                                # 2.7_ex
	divw 11,0,9                                      # 2.7_ex
	mullw 9,11,9                                     # 2.7_ex
	subf 0,9,0                                       # 2.7_ex
	stb 0,24(31)                                     # 2.7_ex

	lwz 0,24(31)                                     # 2.7_as
	stw 0,20(31)                                     # 2.7_as

	lbz 0,8(31)                                      # 2.8_ex
	rlwinm 9,0,0,0xff                                # 2.8_ex
	lbz 0,12(31)                                     # 2.8_ex
	rlwinm 0,0,0,0xff                                # 2.8_ex
	divw 11,0,9                                      # 2.8_ex
	mullw 9,11,9                                     # 2.8_ex
	subf 0,9,0                                       # 2.8_ex
	stb 0,24(31)                                     # 2.8_ex

	lwz 0,24(31)                                     # 2.8_as
	stw 0,20(31)                                     # 2.8_as

	lbz 0,8(31)                                      # 2.9_ex
	rlwinm 9,0,0,0xff                                # 2.9_ex
	lbz 0,12(31)                                     # 2.9_ex
	rlwinm 0,0,0,0xff                                # 2.9_ex
	divw 11,0,9                                      # 2.9_ex
	mullw 9,11,9                                     # 2.9_ex
	subf 0,9,0                                       # 2.9_ex
	stb 0,24(31)                                     # 2.9_ex

	lbz 0,24(31)                                     # 2.9_as
	stb 0,16(31)                                     # 2.9_as

	li 0,0                                           # 2.10_re
	mr 3,0                                           # 2.10_re
	lwz 11,0(1)                                      # 2_fs
	lwz 0,4(11)                                      # 2_fs
	mtlr 0                                           # 2_fs
	lwz 31,-4(11)                                    # 2_fs
	mr 1,11                                          # 2_fs
	blr                                              # 2_fs
	.size main,.-main                                # 2_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
