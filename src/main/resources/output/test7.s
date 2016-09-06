	.file	"test7.c"

	.section .rodata
	.align 2                                         # 4.3_fc
.LC0:	                                            # 4.3_fc
	.string	"add function %d\n"                      # 4.3_fc
	.align 2                                         # 4.4_fc
.LC1:	                                            # 4.4_fc
	.string	"add x %d, y %d\n"                       # 4.4_fc
	.align 2                                         # 4.5_fc
.LC2:	                                            # 4.5_fc
	.string	"addd addd\n"                            # 4.5_fc

	.section ".text"
	.align 2                                         # 4_fs
	.globl add                                       # 4_fs
	.type add, @function                             # 4_fs
add:	                                             # 4_fs
	stwu 1,-32(1)                                    # 4_fs
	stw 31,28(1)                                     # 4_fs
	mr 31,1                                          # 4_fs

	lwz 9,8(31)                                      # 4.2_ex
	lwz 0,12(31)                                     # 4.2_ex
	add 0,9,0                                        # 4.2_ex
	stw 0,20(31)                                     # 4.2_ex

	lwz 0,20(31)                                     # 4.2_as
	stw 0,16(31)                                     # 4.2_as

	lis 0,.LC0@ha                                    # 4.3_fc
	addic 0,0,.LC0@l                                 # 4.3_fc
	mr 3,0                                           # 4.3_fc
	lwz 4,16(31)                                     # 4.3_fc
	crxor 6,6,6                                      # 4.3_fc
	bl printf                                        # 4.3_fc

	lis 0,.LC1@ha                                    # 4.4_fc
	addic 0,0,.LC1@l                                 # 4.4_fc
	mr 3,0                                           # 4.4_fc
	lwz 4,8(31)                                      # 4.4_fc
	lwz 5,12(31)                                     # 4.4_fc
	crxor 6,6,6                                      # 4.4_fc
	bl printf                                        # 4.4_fc

	lis 0,.LC2@ha                                    # 4.5_fc
	addic 0,0,.LC2@l                                 # 4.5_fc
	mr 3,0                                           # 4.5_fc
	crxor 6,6,6                                      # 4.5_fc
	bl printf                                        # 4.5_fc

	lwz 0,16(31)                                     # 4.6_re
	mr 3,0                                           # 4.6_re
	lwz 11,0(1)                                      # 4_fs
	lwz 31,-4(11)                                    # 4_fs
	mr 1,11                                          # 4_fs
	blr                                              # 4_fs
	.size add,.-add                                  # 4_fs

	.section .rodata
	.align 2                                         # 9.3_fc
.LC3:	                                            # 9.3_fc
	.string	"sub function %d\n"                      # 9.3_fc

	.section ".text"
	.align 2                                         # 9_fs
	.globl sub                                       # 9_fs
	.type sub, @function                             # 9_fs
sub:	                                             # 9_fs
	stwu 1,-32(1)                                    # 9_fs
	stw 31,28(1)                                     # 9_fs
	mr 31,1                                          # 9_fs

	lwz 9,24(31)                                     # 9.2_ex
	lwz 0,28(31)                                     # 9.2_ex
	subf 0,9,0                                       # 9.2_ex
	stw 0,36(31)                                     # 9.2_ex

	lwz 9,20(31)                                     # 9.2_ex
	lwz 0,36(31)                                     # 9.2_ex
	subf 0,9,0                                       # 9.2_ex
	stw 0,40(31)                                     # 9.2_ex

	lwz 0,40(31)                                     # 9.2_as
	stw 0,32(31)                                     # 9.2_as

	lis 0,.LC3@ha                                    # 9.3_fc
	addic 0,0,.LC3@l                                 # 9.3_fc
	mr 3,0                                           # 9.3_fc
	lwz 4,32(31)                                     # 9.3_fc
	crxor 6,6,6                                      # 9.3_fc
	bl printf                                        # 9.3_fc

	lwz 0,32(31)                                     # 9.4_re
	mr 3,0                                           # 9.4_re
	lwz 11,0(1)                                      # 9_fs
	lwz 31,-4(11)                                    # 9_fs
	mr 1,11                                          # 9_fs
	blr                                              # 9_fs
	.size sub,.-sub                                  # 9_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 12_fs
	.globl inc                                       # 12_fs
	.type inc, @function                             # 12_fs
inc:	                                             # 12_fs
	stwu 1,-32(1)                                    # 12_fs
	stw 31,28(1)                                     # 12_fs
	mr 31,1                                          # 12_fs

	lwz 9,36(31)                                     # 12.2_ex
	li 0,1                                           # 12.2_ex
	add 0,9,0                                        # 12.2_ex
	stw 0,44(31)                                     # 12.2_ex

	lwz 0,44(31)                                     # 12.2_as
	stw 0,40(31)                                     # 12.2_as

	lwz 0,40(31)                                     # 12.3_re
	mr 3,0                                           # 12.3_re
	lwz 11,0(1)                                      # 12_fs
	lwz 31,-4(11)                                    # 12_fs
	mr 1,11                                          # 12_fs
	blr                                              # 12_fs
	.size inc,.-inc                                  # 12_fs

	.section .rodata
	.align 2                                         # 14.9_fc
.LC4:	                                            # 14.9_fc
	.string	"The add result is : %d\n"               # 14.9_fc
	.align 2                                         # 14.10_fc
.LC5:	                                            # 14.10_fc
	.string	"The sub result is : %d\n"               # 14.10_fc

	.section ".text"
	.align 2                                         # 14_fs
	.globl main                                      # 14_fs
	.type main, @function                            # 14_fs
main:	                                            # 14_fs
	stwu 1,-32(1)                                    # 14_fs
	mflr 0                                           # 14_fs
	stw 31,28(1)                                     # 14_fs
	stw 0,36(1)                                      # 14_fs
	mr 31,1                                          # 14_fs

	li 0,1                                           # 14.4_as
	stw 0,44(31)                                     # 14.4_as

	li 0,2                                           # 14.5_as
	stw 0,48(31)                                     # 14.5_as

	lwz 3,44(31)                                     # 14.6_fc
	li 4,3                                           # 14.6_fc
	bl add                                           # 14.6_fc

	stw 3,52(31)                                     # 14.6_as

	lwz 3,44(31)                                     # 14.7_fc
	lwz 4,52(31)                                     # 14.7_fc
	lwz 5,48(31)                                     # 14.7_fc
	bl sub                                           # 14.7_fc

	stw 3,56(31)                                     # 14.7_as

	li 3,4                                           # 14.8_fc
	bl inc                                           # 14.8_fc

	stw 3,60(31)                                     # 14.8_as

	lis 0,.LC4@ha                                    # 14.9_fc
	addic 0,0,.LC4@l                                 # 14.9_fc
	mr 3,0                                           # 14.9_fc
	lwz 4,52(31)                                     # 14.9_fc
	crxor 6,6,6                                      # 14.9_fc
	bl printf                                        # 14.9_fc

	lis 0,.LC5@ha                                    # 14.10_fc
	addic 0,0,.LC5@l                                 # 14.10_fc
	mr 3,0                                           # 14.10_fc
	lwz 4,56(31)                                     # 14.10_fc
	crxor 6,6,6                                      # 14.10_fc
	bl printf                                        # 14.10_fc

	li 0,0                                           # 14.11_re
	mr 3,0                                           # 14.11_re
	lwz 11,0(1)                                      # 14_fs
	lwz 0,4(11)                                      # 14_fs
	mtlr 0                                           # 14_fs
	lwz 31,-4(11)                                    # 14_fs
	mr 1,11                                          # 14_fs
	blr                                              # 14_fs
	.size main,.-main                                # 14_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
