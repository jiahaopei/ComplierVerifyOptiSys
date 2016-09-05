	.file	"test2.c"

	.section .rodata
	.align 2                                         # 3.5_fc
.LC0:	                                            # 3.5_fc
	.string	"%d %d"                                  # 3.5_fc
	.align 2                                         # 3.9_fc
.LC4:	                                            # 3.9_fc
	.string	"c is %d for the first time!"            # 3.9_fc
	.align 2                                         # 3.10_fc
.LC5:	                                            # 3.10_fc
	.string	"%d"                                     # 3.10_fc

	.align 2                                         # 3.15_fc
.LC8:	                                            # 3.15_fc
	.string	"The biggest sqrt root of %d is %d"      # 3.15_fc

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
	crxor 6,6,6                                      # 3.5_fc
	bl __isoc99_scanf                                # 3.5_fc

	li 0,0                                           # 3.6_as
	stw 0,16(31)                                     # 3.6_as

.L1:	                                             # 3.7_dw
	lwz 0,8(31)                                      # 3.7.1_ex
	li 9,2                                           # 3.7.1_ex
	divw 11,0,9                                      # 3.7.1_ex
	mullw 9,11,9                                     # 3.7.1_ex
	subf 0,9,0                                       # 3.7.1_ex
	stw 0,24(31)                                     # 3.7.1_ex

	lwz 0,24(31)                                     # 3.7.1_ex
	li 9,0                                           # 3.7.1_ex
	cmp 7,0,0,9                                      # 3.7.1_ex
	li 0,0                                           # 3.7.1_ex
	li 9,1                                           # 3.7.1_ex
	isel 0,9,0,30                                    # 3.7.1_ex
	stw 0,28(31)                                     # 3.7.1_ex

	lwz 0,28(31)                                     # 3.7.1_if
	cmpi 7,0,0,0                                     # 3.7.1_if
	beq 7,.L2                                        # 3.7.1_if

	lwz 9,8(31)                                      # 3.7.1.1_ex
	li 0,2                                           # 3.7.1.1_ex
	mullw 0,9,0                                      # 3.7.1.1_ex
	stw 0,24(31)                                     # 3.7.1.1_ex

	lwz 9,16(31)                                     # 3.7.1.1_ex
	lwz 0,24(31)                                     # 3.7.1.1_ex
	subf 0,9,0                                       # 3.7.1.1_ex
	stw 0,28(31)                                     # 3.7.1.1_ex

	lwz 0,28(31)                                     # 3.7.1.1_as
	stw 0,16(31)                                     # 3.7.1.1_as

.L2:	                                             # 3.7.1_if

	lwz 9,8(31)                                      # 3.7.3_ex
	li 0,2                                           # 3.7.3_ex
	mullw 0,9,0                                      # 3.7.3_ex
	stw 0,24(31)                                     # 3.7.3_ex

	lwz 9,16(31)                                     # 3.7.3_ex
	lwz 0,24(31)                                     # 3.7.3_ex
	add 0,9,0                                        # 3.7.3_ex
	stw 0,28(31)                                     # 3.7.3_ex

	lwz 0,28(31)                                     # 3.7.3_as
	stw 0,16(31)                                     # 3.7.3_as

	lwz 0,8(31)                                      # 3.7.4_ex
	addic 0,0,1                                      # 3.7.4_ex
	stw 0,8(31)                                      # 3.7.4_ex

	lwz 0,8(31)                                      # 3.8_ex
	lwz 9,12(31)                                     # 3.8_ex
	cmp 7,0,0,9                                      # 3.8_ex
	li 0,0                                           # 3.8_ex
	li 9,1                                           # 3.8_ex
	isel 0,9,0,28                                    # 3.8_ex
	stw 0,24(31)                                     # 3.8_ex

	lwz 0,24(31)                                     # 3.7_dw
	cmpi 7,0,0,0                                     # 3.7_dw
	bne 7,.L1                                        # 3.7_dw

	lis 0,.LC4@ha                                    # 3.9_fc
	addic 0,0,.LC4@l                                 # 3.9_fc
	mr 3,0                                           # 3.9_fc
	lwz 4,16(31)                                     # 3.9_fc
	crxor 6,6,6                                      # 3.9_fc
	bl printf                                        # 3.9_fc

	lis 0,.LC5@ha                                    # 3.10_fc
	addic 0,0,.LC5@l                                 # 3.10_fc
	mr 3,0                                           # 3.10_fc
	lwz 4,8(31)                                      # 3.10_fc
	crxor 6,6,6                                      # 3.10_fc
	bl __isoc99_scanf                                # 3.10_fc

	li 0,1                                           # 3.11_as
	stw 0,12(31)                                     # 3.11_as

	lwz 9,12(31)                                     # 3.12_ex
	lwz 0,12(31)                                     # 3.12_ex
	mullw 0,9,0                                      # 3.12_ex
	stw 0,24(31)                                     # 3.12_ex

	lwz 0,24(31)                                     # 3.12_as
	stw 0,16(31)                                     # 3.12_as

	b .L6                                            # 3.13_wh
.L7:	                                             # 3.13_wh
	lwz 0,12(31)                                     # 3.13.1_ex
	addic 0,0,1                                      # 3.13.1_ex
	stw 0,12(31)                                     # 3.13.1_ex

	lwz 9,12(31)                                     # 3.13.2_ex
	lwz 0,12(31)                                     # 3.13.2_ex
	mullw 0,9,0                                      # 3.13.2_ex
	stw 0,24(31)                                     # 3.13.2_ex

	lwz 0,24(31)                                     # 3.13.2_as
	stw 0,16(31)                                     # 3.13.2_as

.L6:	                                             # 3.13_wh
	lwz 0,16(31)                                     # 3.13_ex
	lwz 9,8(31)                                      # 3.13_ex
	cmp 7,0,0,9                                      # 3.13_ex
	li 0,0                                           # 3.13_ex
	li 9,1                                           # 3.13_ex
	isel 0,9,0,28                                    # 3.13_ex
	stw 0,24(31)                                     # 3.13_ex

	lwz 0,24(31)                                     # 3.13_wh
	cmpi 7,0,0,0                                     # 3.13_wh
	bne 7,.L7                                        # 3.13_wh
	lis 0,.LC8@ha                                    # 3.15_fc
	addic 0,0,.LC8@l                                 # 3.15_fc
	mr 3,0                                           # 3.15_fc
	lwz 4,8(31)                                      # 3.15_fc
	lwz 5,12(31)                                     # 3.15_fc
	crxor 6,6,6                                      # 3.15_fc
	bl printf                                        # 3.15_fc

	li 0,0                                           # 3.16_as
	stw 0,8(31)                                      # 3.16_as

	b .L9                                            # 3.16_fo
.L10:	                                            # 3.16_fo
	lwz 0,12(31)                                     # 3.16.1_ex
	addic 0,0,1                                      # 3.16.1_ex
	stw 0,12(31)                                     # 3.16.1_ex

	lwz 0,8(31)                                      # 3.16_ex
	addic 0,0,1                                      # 3.16_ex
	stw 0,8(31)                                      # 3.16_ex

.L9:	                                             # 3.16_fo
	lwz 0,8(31)                                      # 3.16_ex
	li 9,10                                          # 3.16_ex
	cmp 7,0,0,9                                      # 3.16_ex
	li 0,0                                           # 3.16_ex
	li 9,1                                           # 3.16_ex
	isel 0,9,0,28                                    # 3.16_ex
	stw 0,24(31)                                     # 3.16_ex

	lwz 0,24(31)                                     # 3.16_fo
	cmpi 7,0,0,0                                     # 3.16_fo
	bne 7,.L10                                       # 3.16_fo

	li 0,0                                           # 3.18_re
	mr 3,0                                           # 3.18_re
	lwz 11,0(1)                                      # 3_fs
	lwz 0,4(11)                                      # 3_fs
	mtlr 0                                           # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size main,.-main                                # 3_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
