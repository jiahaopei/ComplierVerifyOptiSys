	.file	"test6.c"

	.section .rodata
	.align 2                                         # 2.3_fc
.LC0:	                                            # 2.3_fc
	.string	"add function %d\n"                      # 2.3_fc
	.align 2                                         # 2.4_fc
.LC1:	                                            # 2.4_fc
	.string	"add x %d, y %d\n"                       # 2.4_fc
	.align 2                                         # 2.5_fc
.LC2:	                                            # 2.5_fc
	.string	"addd addd\n"                            # 2.5_fc

	.section ".text"
	.align 2                                         # 2_fs
	.globl add                                       # 2_fs
	.type add, @function                             # 2_fs
add:	                                             # 2_fs
	stwu 1,-32(1)                                    # 2_fs
	stw 31,28(1)                                     # 2_fs
	mr 31,1                                          # 2_fs

	lwz 9,8(31)                                      # 2.2_ex
	lwz 0,12(31)                                     # 2.2_ex
	add 0,9,0                                        # 2.2_ex
	stw 0,20(31)                                     # 2.2_ex

	lwz 0,20(31)                                     # 2.2_as
	stw 0,16(31)                                     # 2.2_as

	lis 0,.LC0@ha                                    # 2.3_fc
	addic 0,0,.LC0@l                                 # 2.3_fc
	mr 3,0                                           # 2.3_fc
	lwz 4,16(31)                                     # 2.3_fc
	crxor 6,6,6                                      # 2.3_fc
	bl printf                                        # 2.3_fc

	lis 0,.LC1@ha                                    # 2.4_fc
	addic 0,0,.LC1@l                                 # 2.4_fc
	mr 3,0                                           # 2.4_fc
	lwz 4,8(31)                                      # 2.4_fc
	lwz 5,12(31)                                     # 2.4_fc
	crxor 6,6,6                                      # 2.4_fc
	bl printf                                        # 2.4_fc

	lis 0,.LC2@ha                                    # 2.5_fc
	addic 0,0,.LC2@l                                 # 2.5_fc
	mr 3,0                                           # 2.5_fc
	crxor 6,6,6                                      # 2.5_fc
	bl printf                                        # 2.5_fc

	lwz 0,16(31)                                     # 2.6_re
	mr 3,0                                           # 2.6_re
	lwz 11,0(1)                                      # 2_fs
	lwz 31,-4(11)                                    # 2_fs
	mr 1,11                                          # 2_fs
	blr                                              # 2_fs
	.size add,.-add                                  # 2_fs

	.section .rodata
	.align 2                                         # 4.3_fc
.LC3:	                                            # 4.3_fc
	.string	"sub function %d\n"                      # 4.3_fc

	.section ".text"
	.align 2                                         # 4_fs
	.globl sub                                       # 4_fs
	.type sub, @function                             # 4_fs
sub:	                                             # 4_fs
	stwu 1,-32(1)                                    # 4_fs
	stw 31,28(1)                                     # 4_fs
	mr 31,1                                          # 4_fs

	lwz 9,24(31)                                     # 4.2_ex
	lwz 0,28(31)                                     # 4.2_ex
	subf 0,9,0                                       # 4.2_ex
	stw 0,36(31)                                     # 4.2_ex

	lwz 9,20(31)                                     # 4.2_ex
	lwz 0,36(31)                                     # 4.2_ex
	subf 0,9,0                                       # 4.2_ex
	stw 0,40(31)                                     # 4.2_ex

	lwz 0,40(31)                                     # 4.2_as
	stw 0,32(31)                                     # 4.2_as

	lis 0,.LC3@ha                                    # 4.3_fc
	addic 0,0,.LC3@l                                 # 4.3_fc
	mr 3,0                                           # 4.3_fc
	lwz 4,32(31)                                     # 4.3_fc
	crxor 6,6,6                                      # 4.3_fc
	bl printf                                        # 4.3_fc

	lwz 0,32(31)                                     # 4.4_re
	mr 3,0                                           # 4.4_re
	lwz 11,0(1)                                      # 4_fs
	lwz 31,-4(11)                                    # 4_fs
	mr 1,11                                          # 4_fs
	blr                                              # 4_fs
	.size sub,.-sub                                  # 4_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 6_fs
	.globl inc                                       # 6_fs
	.type inc, @function                             # 6_fs
inc:	                                             # 6_fs
	stwu 1,-32(1)                                    # 6_fs
	stw 31,28(1)                                     # 6_fs
	mr 31,1                                          # 6_fs

	lwz 9,36(31)                                     # 6.2_ex
	li 0,1                                           # 6.2_ex
	add 0,9,0                                        # 6.2_ex
	stw 0,44(31)                                     # 6.2_ex

	lwz 0,44(31)                                     # 6.2_as
	stw 0,40(31)                                     # 6.2_as

	lwz 0,40(31)                                     # 6.3_re
	mr 3,0                                           # 6.3_re
	lwz 11,0(1)                                      # 6_fs
	lwz 31,-4(11)                                    # 6_fs
	mr 1,11                                          # 6_fs
	blr                                              # 6_fs
	.size inc,.-inc                                  # 6_fs

	.section .rodata
	.align 2                                         # 8.9_fc
.LC4:	                                            # 8.9_fc
	.string	"The add result is : %d\n"               # 8.9_fc
	.align 2                                         # 8.10_fc
.LC5:	                                            # 8.10_fc
	.string	"The sub result is : %d\n"               # 8.10_fc

	.section ".text"
	.align 2                                         # 8_fs
	.globl main                                      # 8_fs
	.type main, @function                            # 8_fs
main:	                                            # 8_fs
	stwu 1,-32(1)                                    # 8_fs
	mflr 0                                           # 8_fs
	stw 31,28(1)                                     # 8_fs
	stw 0,36(1)                                      # 8_fs
	mr 31,1                                          # 8_fs

	li 0,1                                           # 8.4_as
	stw 0,44(31)                                     # 8.4_as

	li 0,2                                           # 8.5_as
	stw 0,48(31)                                     # 8.5_as

	lwz 3,44(31)                                     # 8.6_fc
	li 4,3                                           # 8.6_fc
	bl add                                           # 8.6_fc

	stw 3,52(31)                                     # 8.6_as

	lwz 3,44(31)                                     # 8.7_fc
	lwz 4,52(31)                                     # 8.7_fc
	lwz 5,48(31)                                     # 8.7_fc
	bl sub                                           # 8.7_fc

	stw 3,56(31)                                     # 8.7_as

	li 3,4                                           # 8.8_fc
	bl inc                                           # 8.8_fc

	stw 3,60(31)                                     # 8.8_as

	lis 0,.LC4@ha                                    # 8.9_fc
	addic 0,0,.LC4@l                                 # 8.9_fc
	mr 3,0                                           # 8.9_fc
	lwz 4,52(31)                                     # 8.9_fc
	crxor 6,6,6                                      # 8.9_fc
	bl printf                                        # 8.9_fc

	lis 0,.LC5@ha                                    # 8.10_fc
	addic 0,0,.LC5@l                                 # 8.10_fc
	mr 3,0                                           # 8.10_fc
	lwz 4,56(31)                                     # 8.10_fc
	crxor 6,6,6                                      # 8.10_fc
	bl printf                                        # 8.10_fc

	li 0,0                                           # 8.11_re
	mr 3,0                                           # 8.11_re
	lwz 11,0(1)                                      # 8_fs
	lwz 0,4(11)                                      # 8_fs
	mtlr 0                                           # 8_fs
	lwz 31,-4(11)                                    # 8_fs
	mr 1,11                                          # 8_fs
	blr                                              # 8_fs
	.size main,.-main                                # 8_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
