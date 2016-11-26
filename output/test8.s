	.file	"test8.c"

	.section .rodata
	.align 2                                         # 4.6_fc
.LC0:	                                            # 4.6_fc
	.string	"%d %f %f"                               # 4.6_fc
	.align 2                                         # 4.11_fc
.LC5:	                                            # 4.11_fc
	.string	"sum is %d"                              # 4.11_fc

	.section ".text"
	.align 2                                         # 4_fs
	.globl main                                      # 4_fs
	.type main, @function                            # 4_fs
main:	                                            # 4_fs
	stwu 1,-32(1)                                    # 4_fs
	mflr 0                                           # 4_fs
	stw 31,28(1)                                     # 4_fs
	stw 0,36(1)                                      # 4_fs
	mr 31,1                                          # 4_fs

	lis 0,.LC0@ha                                    # 4.6_fc
	addic 0,0,.LC0@l                                 # 4.6_fc
	mr 3,0                                           # 4.6_fc
	lwz 4,8(31)                                      # 4.6_fc
	lfd 5,24(31)                                     # 4.6_fc
	lfd 6,28(31)                                     # 4.6_fc
	crxor 6,6,6                                      # 4.6_fc
	bl __isoc99_scanf                                # 4.6_fc

	li 0,0                                           # 4.7_as
	stw 0,16(31)                                     # 4.7_as

	li 0,1                                           # 4.8_as
	stw 0,12(31)                                     # 4.8_as

	b .L1                                            # 4.8_fo
.L2:	                                             # 4.8_fo
	lwz 0,12(31)                                     # 4.8.1_ex
	li 9,2                                           # 4.8.1_ex
	divw 11,0,9                                      # 4.8.1_ex
	mullw 9,11,9                                     # 4.8.1_ex
	subf 0,9,0                                       # 4.8.1_ex
	stw 0,32(31)                                     # 4.8.1_ex

	lwz 0,32(31)                                     # 4.8.1_as
	stw 0,20(31)                                     # 4.8.1_as

	lwz 0,20(31)                                     # 4.8.2_ex
	li 9,0                                           # 4.8.2_ex
	cmp 7,0,0,9                                      # 4.8.2_ex
	li 0,0                                           # 4.8.2_ex
	li 9,1                                           # 4.8.2_ex
	isel 0,9,0,30                                    # 4.8.2_ex
	stw 0,32(31)                                     # 4.8.2_ex

	lwz 0,32(31)                                     # 4.8.2_if
	cmpi 7,0,0,0                                     # 4.8.2_if
	beq 7,.L3                                        # 4.8.2_if

	lwz 9,16(31)                                     # 4.8.2.1_ex
	lwz 0,12(31)                                     # 4.8.2.1_ex
	add 0,9,0                                        # 4.8.2.1_ex
	stw 0,32(31)                                     # 4.8.2.1_ex

	lwz 0,32(31)                                     # 4.8.2.1_as
	stw 0,16(31)                                     # 4.8.2.1_as

	b .L4                                            # 4.8.3_el
.L3:	                                             # 4.8.2_if

	lwz 9,12(31)                                     # 4.8.3.1_ex
	li 0,2                                           # 4.8.3.1_ex
	mullw 0,9,0                                      # 4.8.3.1_ex
	stw 0,32(31)                                     # 4.8.3.1_ex

	lwz 9,16(31)                                     # 4.8.3.1_ex
	lwz 0,32(31)                                     # 4.8.3.1_ex
	subf 0,9,0                                       # 4.8.3.1_ex
	stw 0,36(31)                                     # 4.8.3.1_ex

	lwz 0,36(31)                                     # 4.8.3.1_as
	stw 0,16(31)                                     # 4.8.3.1_as

.L4:	                                             # 4.8.3_el

	lwz 0,12(31)                                     # 4.8_ex
	addic 0,0,1                                      # 4.8_ex
	stw 0,12(31)                                     # 4.8_ex

.L1:	                                             # 4.8_fo
	lwz 0,12(31)                                     # 4.8_ex
	lwz 9,8(31)                                      # 4.8_ex
	cmp 7,0,0,9                                      # 4.8_ex
	li 0,1                                           # 4.8_ex
	isel 0,0,0,29                                    # 4.8_ex
	stw 0,32(31)                                     # 4.8_ex

	lwz 0,32(31)                                     # 4.8_fo
	cmpi 7,0,0,0                                     # 4.8_fo
	bne 7,.L2                                        # 4.8_fo

	lwz 3,8(31)                                      # 4.10_fc
	bl f                                             # 4.10_fc

	stw 3,20(31)                                     # 4.10_as

	lis 0,.LC5@ha                                    # 4.11_fc
	addic 0,0,.LC5@l                                 # 4.11_fc
	mr 3,0                                           # 4.11_fc
	lwz 4,16(31)                                     # 4.11_fc
	crxor 6,6,6                                      # 4.11_fc
	bl printf                                        # 4.11_fc

	li 0,0                                           # 4.12_re
	mr 3,0                                           # 4.12_re
	lwz 11,0(1)                                      # 4_fs
	lwz 0,4(11)                                      # 4_fs
	mtlr 0                                           # 4_fs
	lwz 31,-4(11)                                    # 4_fs
	mr 1,11                                          # 4_fs
	blr                                              # 4_fs
	.size main,.-main                                # 4_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 6_fs
	.globl f                                         # 6_fs
	.type f, @function                               # 6_fs
f:	                                               # 6_fs
	stwu 1,-32(1)                                    # 6_fs
	stw 31,28(1)                                     # 6_fs
	mr 31,1                                          # 6_fs

	lwz 0,32(31)                                     # 6.2_ex
	li 9,1                                           # 6.2_ex
	cmp 7,0,0,9                                      # 6.2_ex
	li 0,1                                           # 6.2_ex
	isel 0,0,0,29                                    # 6.2_ex
	stw 0,40(31)                                     # 6.2_ex

	lwz 0,40(31)                                     # 6.2_if
	cmpi 7,0,0,0                                     # 6.2_if
	beq 7,.L6                                        # 6.2_if

	li 0,1                                           # 6.2.1_re
	mr 3,0                                           # 6.2.1_re
.L6:	                                             # 6.2_if

	lwz 9,32(31)                                     # 6.4_ex
	li 0,2                                           # 6.4_ex
	mullw 0,9,0                                      # 6.4_ex
	stw 0,40(31)                                     # 6.4_ex

	lwz 3,32(31)                                     # 6.4_fc
	lwz 4,40(31)                                     # 6.4_fc
	bl g                                             # 6.4_fc

	stw 3,36(31)                                     # 6.4_as

	lwz 9,32(31)                                     # 6.5_ex
	lwz 0,36(31)                                     # 6.5_ex
	mullw 0,9,0                                      # 6.5_ex
	stw 0,40(31)                                     # 6.5_ex

	lwz 0,40(31)                                     # 6.5_re
	mr 3,0                                           # 6.5_re
	lwz 11,0(1)                                      # 6_fs
	lwz 31,-4(11)                                    # 6_fs
	mr 1,11                                          # 6_fs
	blr                                              # 6_fs
	.size f,.-f                                      # 6_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 8_fs
	.globl g                                         # 8_fs
	.type g, @function                               # 8_fs
g:	                                               # 8_fs
	stwu 1,-32(1)                                    # 8_fs
	stw 31,28(1)                                     # 8_fs
	mr 31,1                                          # 8_fs

	lwz 9,44(31)                                     # 8.1_ex
	lwz 0,40(31)                                     # 8.1_ex
	add 0,9,0                                        # 8.1_ex
	stw 0,48(31)                                     # 8.1_ex

	lwz 0,48(31)                                     # 8.1_re
	mr 3,0                                           # 8.1_re
	lwz 11,0(1)                                      # 8_fs
	lwz 31,-4(11)                                    # 8_fs
	mr 1,11                                          # 8_fs
	blr                                              # 8_fs
	.size g,.-g                                      # 8_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
