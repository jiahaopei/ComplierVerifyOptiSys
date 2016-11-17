	.file	"test7.c"

	.section .rodata
	.align 2                                         # 7.3_fc
.LC0:	                                            # 7.3_fc
	.string	"add function %d\n"                      # 7.3_fc
	.align 2                                         # 7.4_fc
.LC1:	                                            # 7.4_fc
	.string	"add x %d, y %d\n"                       # 7.4_fc
	.align 2                                         # 7.5_fc
.LC2:	                                            # 7.5_fc
	.string	"addd addd\n"                            # 7.5_fc

	.section ".text"
	.align 2                                         # 7_fs
	.globl add                                       # 7_fs
	.type add, @function                             # 7_fs
add:	                                             # 7_fs
	stwu 1,-32(1)                                    # 7_fs
	stw 31,28(1)                                     # 7_fs
	mr 31,1                                          # 7_fs

	lwz 9,8(31)                                      # 7.2_ex
	lwz 0,12(31)                                     # 7.2_ex
	add 0,9,0                                        # 7.2_ex
	stw 0,20(31)                                     # 7.2_ex

	lwz 0,20(31)                                     # 7.2_as
	stw 0,16(31)                                     # 7.2_as

	lis 0,.LC0@ha                                    # 7.3_fc
	addic 0,0,.LC0@l                                 # 7.3_fc
	mr 3,0                                           # 7.3_fc
	lwz 4,16(31)                                     # 7.3_fc
	crxor 6,6,6                                      # 7.3_fc
	bl printf                                        # 7.3_fc

	lis 0,.LC1@ha                                    # 7.4_fc
	addic 0,0,.LC1@l                                 # 7.4_fc
	mr 3,0                                           # 7.4_fc
	lwz 4,8(31)                                      # 7.4_fc
	lwz 5,12(31)                                     # 7.4_fc
	crxor 6,6,6                                      # 7.4_fc
	bl printf                                        # 7.4_fc

	lis 0,.LC2@ha                                    # 7.5_fc
	addic 0,0,.LC2@l                                 # 7.5_fc
	mr 3,0                                           # 7.5_fc
	crxor 6,6,6                                      # 7.5_fc
	bl printf                                        # 7.5_fc

	lwz 0,16(31)                                     # 7.6_re
	mr 3,0                                           # 7.6_re
	lwz 11,0(1)                                      # 7_fs
	lwz 31,-4(11)                                    # 7_fs
	mr 1,11                                          # 7_fs
	blr                                              # 7_fs
	.size add,.-add                                  # 7_fs

	.section .rodata
	.align 2                                         # 14.3_fc
.LC3:	                                            # 14.3_fc
	.string	"sub function %d\n"                      # 14.3_fc

	.section ".text"
	.align 2                                         # 14_fs
	.globl sub                                       # 14_fs
	.type sub, @function                             # 14_fs
sub:	                                             # 14_fs
	stwu 1,-32(1)                                    # 14_fs
	stw 31,28(1)                                     # 14_fs
	mr 31,1                                          # 14_fs

	lwz 9,24(31)                                     # 14.2_ex
	lwz 0,28(31)                                     # 14.2_ex
	subf 0,9,0                                       # 14.2_ex
	stw 0,36(31)                                     # 14.2_ex

	lwz 9,20(31)                                     # 14.2_ex
	lwz 0,36(31)                                     # 14.2_ex
	subf 0,9,0                                       # 14.2_ex
	stw 0,40(31)                                     # 14.2_ex

	lwz 0,40(31)                                     # 14.2_as
	stw 0,32(31)                                     # 14.2_as

	lis 0,.LC3@ha                                    # 14.3_fc
	addic 0,0,.LC3@l                                 # 14.3_fc
	mr 3,0                                           # 14.3_fc
	lwz 4,32(31)                                     # 14.3_fc
	crxor 6,6,6                                      # 14.3_fc
	bl printf                                        # 14.3_fc

	lwz 0,32(31)                                     # 14.4_re
	mr 3,0                                           # 14.4_re
	lwz 11,0(1)                                      # 14_fs
	lwz 31,-4(11)                                    # 14_fs
	mr 1,11                                          # 14_fs
	blr                                              # 14_fs
	.size sub,.-sub                                  # 14_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 17_fs
	.globl inc                                       # 17_fs
	.type inc, @function                             # 17_fs
inc:	                                             # 17_fs
	stwu 1,-32(1)                                    # 17_fs
	stw 31,28(1)                                     # 17_fs
	mr 31,1                                          # 17_fs

	lwz 9,36(31)                                     # 17.2_ex
	li 0,1                                           # 17.2_ex
	add 0,9,0                                        # 17.2_ex
	stw 0,44(31)                                     # 17.2_ex

	lwz 0,44(31)                                     # 17.2_as
	stw 0,40(31)                                     # 17.2_as

	lwz 0,40(31)                                     # 17.3_re
	mr 3,0                                           # 17.3_re
	lwz 11,0(1)                                      # 17_fs
	lwz 31,-4(11)                                    # 17_fs
	mr 1,11                                          # 17_fs
	blr                                              # 17_fs
	.size inc,.-inc                                  # 17_fs

	.section .rodata
	.align 2                                         # 19.9_fc
.LC4:	                                            # 19.9_fc
	.string	"The add result is : %d\n"               # 19.9_fc
	.align 2                                         # 19.10_fc
.LC5:	                                            # 19.10_fc
	.string	"The sub result is : %d\n"               # 19.10_fc

	.section ".text"
	.align 2                                         # 19_fs
	.globl main                                      # 19_fs
	.type main, @function                            # 19_fs
main:	                                            # 19_fs
	stwu 1,-32(1)                                    # 19_fs
	mflr 0                                           # 19_fs
	stw 31,28(1)                                     # 19_fs
	stw 0,36(1)                                      # 19_fs
	mr 31,1                                          # 19_fs

	li 0,1                                           # 19.4_as
	stw 0,44(31)                                     # 19.4_as

	li 0,2                                           # 19.5_as
	stw 0,48(31)                                     # 19.5_as

	lwz 3,44(31)                                     # 19.6_fc
	li 4,3                                           # 19.6_fc
	bl add                                           # 19.6_fc

	stw 3,52(31)                                     # 19.6_as

	lwz 3,44(31)                                     # 19.7_fc
	lwz 4,52(31)                                     # 19.7_fc
	lwz 5,48(31)                                     # 19.7_fc
	bl sub                                           # 19.7_fc

	stw 3,56(31)                                     # 19.7_as

	li 3,4                                           # 19.8_fc
	bl inc                                           # 19.8_fc

	stw 3,60(31)                                     # 19.8_as

	lis 0,.LC4@ha                                    # 19.9_fc
	addic 0,0,.LC4@l                                 # 19.9_fc
	mr 3,0                                           # 19.9_fc
	lwz 4,52(31)                                     # 19.9_fc
	crxor 6,6,6                                      # 19.9_fc
	bl printf                                        # 19.9_fc

	lis 0,.LC5@ha                                    # 19.10_fc
	addic 0,0,.LC5@l                                 # 19.10_fc
	mr 3,0                                           # 19.10_fc
	lwz 4,56(31)                                     # 19.10_fc
	crxor 6,6,6                                      # 19.10_fc
	bl printf                                        # 19.10_fc

	li 0,0                                           # 19.11_re
	mr 3,0                                           # 19.11_re
	lwz 11,0(1)                                      # 19_fs
	lwz 0,4(11)                                      # 19_fs
	mtlr 0                                           # 19_fs
	lwz 31,-4(11)                                    # 19_fs
	mr 1,11                                          # 19_fs
	blr                                              # 19_fs
	.size main,.-main                                # 19_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
