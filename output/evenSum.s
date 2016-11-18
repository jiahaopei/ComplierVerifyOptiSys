	.file	"evenSum.c"

	.section .rodata
	.align 2                                         # 2.6_fc
.LC0:	                                            # 2.6_fc
	.string	"%d %f %f"                               # 2.6_fc
	.align 2                                         # 2.10_fc
.LC5:	                                            # 2.10_fc
	.string	"sum is %d"                              # 2.10_fc

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

	lis 0,.LC0@ha                                    # 2.6_fc
	addic 0,0,.LC0@l                                 # 2.6_fc
	mr 3,0                                           # 2.6_fc
	lwz 4,8(31)                                      # 2.6_fc
	lfd 5,24(31)                                     # 2.6_fc
	lfd 6,28(31)                                     # 2.6_fc
	crxor 6,6,6                                      # 2.6_fc
	bl __isoc99_scanf                                # 2.6_fc

	li 0,0                                           # 2.7_as
	stw 0,16(31)                                     # 2.7_as

	li 0,1                                           # 2.8_as
	stw 0,12(31)                                     # 2.8_as

	b .L1                                            # 2.8_fo
.L2:	                                             # 2.8_fo
	lwz 0,12(31)                                     # 2.8.1_ex
	li 9,2                                           # 2.8.1_ex
	divw 11,0,9                                      # 2.8.1_ex
	mullw 9,11,9                                     # 2.8.1_ex
	subf 0,9,0                                       # 2.8.1_ex
	stw 0,32(31)                                     # 2.8.1_ex

	lwz 0,32(31)                                     # 2.8.1_as
	stw 0,20(31)                                     # 2.8.1_as

	lwz 0,20(31)                                     # 2.8.2_ex
	li 9,0                                           # 2.8.2_ex
	cmp 7,0,0,9                                      # 2.8.2_ex
	li 0,0                                           # 2.8.2_ex
	li 9,1                                           # 2.8.2_ex
	isel 0,9,0,30                                    # 2.8.2_ex
	stw 0,32(31)                                     # 2.8.2_ex

	lwz 0,32(31)                                     # 2.8.2_if
	cmpi 7,0,0,0                                     # 2.8.2_if
	beq 7,.L3                                        # 2.8.2_if

	lwz 9,16(31)                                     # 2.8.2.1_ex
	lwz 0,12(31)                                     # 2.8.2.1_ex
	add 0,9,0                                        # 2.8.2.1_ex
	stw 0,32(31)                                     # 2.8.2.1_ex

	lwz 0,32(31)                                     # 2.8.2.1_as
	stw 0,16(31)                                     # 2.8.2.1_as

	b .L4                                            # 2.8.3_el
.L3:	                                             # 2.8.2_if

	lwz 9,12(31)                                     # 2.8.3.1_ex
	li 0,2                                           # 2.8.3.1_ex
	mullw 0,9,0                                      # 2.8.3.1_ex
	stw 0,32(31)                                     # 2.8.3.1_ex

	lwz 9,16(31)                                     # 2.8.3.1_ex
	lwz 0,32(31)                                     # 2.8.3.1_ex
	subf 0,9,0                                       # 2.8.3.1_ex
	stw 0,36(31)                                     # 2.8.3.1_ex

	lwz 0,36(31)                                     # 2.8.3.1_as
	stw 0,16(31)                                     # 2.8.3.1_as

.L4:	                                             # 2.8.3_el

	lwz 0,12(31)                                     # 2.8_ex
	addic 0,0,1                                      # 2.8_ex
	stw 0,12(31)                                     # 2.8_ex

.L1:	                                             # 2.8_fo
	lwz 0,12(31)                                     # 2.8_ex
	lwz 9,8(31)                                      # 2.8_ex
	cmp 7,0,0,9                                      # 2.8_ex
	li 0,1                                           # 2.8_ex
	isel 0,0,0,29                                    # 2.8_ex
	stw 0,32(31)                                     # 2.8_ex

	lwz 0,32(31)                                     # 2.8_fo
	cmpi 7,0,0,0                                     # 2.8_fo
	bne 7,.L2                                        # 2.8_fo

	lis 0,.LC5@ha                                    # 2.10_fc
	addic 0,0,.LC5@l                                 # 2.10_fc
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
