	.file	"test3.c"

	.section .rodata
	.align 2                                         # 2.5_fc
.LC0:	                                            # 2.5_fc
	.string	"%hd %hd %hd %hd"                        # 2.5_fc
	.align 2                                         # 2.9_fc
.LC1:	                                            # 2.9_fc
	.string	"%hd"                                    # 2.9_fc
	.align 2                                         # 2.10_fc
.LC2:	                                            # 2.10_fc
	.string	"%hd"                                    # 2.10_fc

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
	lhz 4,8(31)                                      # 2.5_fc
	lhz 5,10(31)                                     # 2.5_fc
	lhz 6,12(31)                                     # 2.5_fc
	lhz 7,14(31)                                     # 2.5_fc
	crxor 6,6,6                                      # 2.5_fc
	bl __isoc99_scanf                                # 2.5_fc

	li 9,34                                          # 2.6_ex
	lwz 0,10(31)                                     # 2.6_ex
	add 0,9,0                                        # 2.6_ex
	stw 0,16(31)                                     # 2.6_ex

	lbz 0,16(31)                                     # 2.6_as
	sth 0,8(31)                                      # 2.6_as

	lhz 0,8(31)                                      # 2.7_ex
	extsh 0,0                                        # 2.7_ex
	rlwinm 9,0,0,0xff                                # 2.7_ex
	lhz 0,10(31)                                     # 2.7_ex
	extsh 0,0                                        # 2.7_ex
	rlwinm 0,0,0,0xff                                # 2.7_ex
	add 0,9,0                                        # 2.7_ex
	sth 0,16(31)                                     # 2.7_ex

	lbz 0,16(31)                                     # 2.7_as
	sth 0,12(31)                                     # 2.7_as

	li 9,12                                          # 2.8_ex
	li 0,12                                          # 2.8_ex
	add 0,9,0                                        # 2.8_ex
	stw 0,16(31)                                     # 2.8_ex

	lbz 0,16(31)                                     # 2.8_as
	sth 0,8(31)                                      # 2.8_as

	lis 0,.LC1@ha                                    # 2.9_fc
	addic 0,0,.LC1@l                                 # 2.9_fc
	mr 3,0                                           # 2.9_fc
	lhz 4,8(31)                                      # 2.9_fc
	crxor 6,6,6                                      # 2.9_fc
	bl printf                                        # 2.9_fc

	lwz 9,10(31)                                     # 2.10_ex
	li 0,23                                          # 2.10_ex
	add 0,9,0                                        # 2.10_ex
	stw 0,16(31)                                     # 2.10_ex

	lis 0,.LC2@ha                                    # 2.10_fc
	addic 0,0,.LC2@l                                 # 2.10_fc
	mr 3,0                                           # 2.10_fc
	lwz 4,16(31)                                     # 2.10_fc
	crxor 6,6,6                                      # 2.10_fc
	bl printf                                        # 2.10_fc

	li 0,0                                           # 2.11_re
	mr 3,0                                           # 2.11_re
	lwz 11,0(1)                                      # 2_fs
	lwz 0,4(11)                                      # 2_fs
	mtlr 0                                           # 2_fs
	lwz 31,-4(11)                                    # 2_fs
	mr 1,11                                          # 2_fs
	blr                                              # 2_fs
	.size main,.-main                                # 2_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
