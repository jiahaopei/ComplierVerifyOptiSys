	.file	"test7.c"

	.section .rodata
	.align 2                                         # 6.9_fc
.LC0:	                                            # 6.9_fc
	.string	"The add result is : %d\n"               # 6.9_fc
	.align 2                                         # 6.10_fc
.LC1:	                                            # 6.10_fc
	.string	"The sub result is : %d\n"               # 6.10_fc

	.section ".text"
	.align 2                                         # 6_fs
	.globl main                                      # 6_fs
	.type main, @function                            # 6_fs
main:	                                            # 6_fs
	stwu 1,-32(1)                                    # 6_fs
	mflr 0                                           # 6_fs
	stw 31,28(1)                                     # 6_fs
	stw 0,36(1)                                      # 6_fs
	mr 31,1                                          # 6_fs

	li 0,1                                           # 6.4_as
	stw 0,8(31)                                      # 6.4_as

	li 0,2                                           # 6.5_as
	stw 0,12(31)                                     # 6.5_as

	lwz 3,8(31)                                      # 6.6_fc
	li 4,3                                           # 6.6_fc
	bl add                                           # 6.6_fc

	stw 3,16(31)                                     # 6.6_as

	lwz 3,8(31)                                      # 6.7_fc
	lwz 4,16(31)                                     # 6.7_fc
	lwz 5,12(31)                                     # 6.7_fc
	bl sub                                           # 6.7_fc

	stw 3,20(31)                                     # 6.7_as

	li 3,4                                           # 6.8_fc
	bl inc                                           # 6.8_fc

	stw 3,24(31)                                     # 6.8_as

	lis 0,.LC0@ha                                    # 6.9_fc
	addic 0,0,.LC0@l                                 # 6.9_fc
	mr 3,0                                           # 6.9_fc
	lwz 4,16(31)                                     # 6.9_fc
	crxor 6,6,6                                      # 6.9_fc
	bl printf                                        # 6.9_fc

	lis 0,.LC1@ha                                    # 6.10_fc
	addic 0,0,.LC1@l                                 # 6.10_fc
	mr 3,0                                           # 6.10_fc
	lwz 4,20(31)                                     # 6.10_fc
	crxor 6,6,6                                      # 6.10_fc
	bl printf                                        # 6.10_fc

	li 0,0                                           # 6.11_re
	mr 3,0                                           # 6.11_re
	lwz 11,0(1)                                      # 6_fs
	lwz 0,4(11)                                      # 6_fs
	mtlr 0                                           # 6_fs
	lwz 31,-4(11)                                    # 6_fs
	mr 1,11                                          # 6_fs
	blr                                              # 6_fs
	.size main,.-main                                # 6_fs

	.section .rodata

	.section ".text"
	.align 2                                         # 8_fs
	.globl inc                                       # 8_fs
	.type inc, @function                             # 8_fs
inc:	                                             # 8_fs
	stwu 1,-32(1)                                    # 8_fs
	stw 31,28(1)                                     # 8_fs
	mr 31,1                                          # 8_fs

	lwz 9,28(31)                                     # 8.2_ex
	li 0,1                                           # 8.2_ex
	add 0,9,0                                        # 8.2_ex
	stw 0,36(31)                                     # 8.2_ex

	lwz 0,36(31)                                     # 8.2_as
	stw 0,32(31)                                     # 8.2_as

	lwz 0,32(31)                                     # 8.3_re
	mr 3,0                                           # 8.3_re
	lwz 11,0(1)                                      # 8_fs
	lwz 31,-4(11)                                    # 8_fs
	mr 1,11                                          # 8_fs
	blr                                              # 8_fs
	.size inc,.-inc                                  # 8_fs

	.section .rodata
	.align 2                                         # 14.3_fc
.LC2:	                                            # 14.3_fc
	.string	"add function %d\n"                      # 14.3_fc
	.align 2                                         # 14.4_fc
.LC3:	                                            # 14.4_fc
	.string	"add x %d, y %d\n"                       # 14.4_fc
	.align 2                                         # 14.5_fc
.LC4:	                                            # 14.5_fc
	.string	"addd addd\n"                            # 14.5_fc

	.section ".text"
	.align 2                                         # 14_fs
	.globl add                                       # 14_fs
	.type add, @function                             # 14_fs
add:	                                             # 14_fs
	stwu 1,-32(1)                                    # 14_fs
	stw 31,28(1)                                     # 14_fs
	mr 31,1                                          # 14_fs

	lwz 9,36(31)                                     # 14.2_ex
	lwz 0,40(31)                                     # 14.2_ex
	add 0,9,0                                        # 14.2_ex
	stw 0,48(31)                                     # 14.2_ex

	lwz 0,48(31)                                     # 14.2_as
	stw 0,44(31)                                     # 14.2_as

	lis 0,.LC2@ha                                    # 14.3_fc
	addic 0,0,.LC2@l                                 # 14.3_fc
	mr 3,0                                           # 14.3_fc
	lwz 4,44(31)                                     # 14.3_fc
	crxor 6,6,6                                      # 14.3_fc
	bl printf                                        # 14.3_fc

	lis 0,.LC3@ha                                    # 14.4_fc
	addic 0,0,.LC3@l                                 # 14.4_fc
	mr 3,0                                           # 14.4_fc
	lwz 4,36(31)                                     # 14.4_fc
	lwz 5,40(31)                                     # 14.4_fc
	crxor 6,6,6                                      # 14.4_fc
	bl printf                                        # 14.4_fc

	lis 0,.LC4@ha                                    # 14.5_fc
	addic 0,0,.LC4@l                                 # 14.5_fc
	mr 3,0                                           # 14.5_fc
	crxor 6,6,6                                      # 14.5_fc
	bl printf                                        # 14.5_fc

	lwz 0,44(31)                                     # 14.6_re
	mr 3,0                                           # 14.6_re
	lwz 11,0(1)                                      # 14_fs
	lwz 31,-4(11)                                    # 14_fs
	mr 1,11                                          # 14_fs
	blr                                              # 14_fs
	.size add,.-add                                  # 14_fs

	.section .rodata
	.align 2                                         # 20.3_fc
.LC5:	                                            # 20.3_fc
	.string	"sub function %d\n"                      # 20.3_fc

	.section ".text"
	.align 2                                         # 20_fs
	.globl sub                                       # 20_fs
	.type sub, @function                             # 20_fs
sub:	                                             # 20_fs
	stwu 1,-32(1)                                    # 20_fs
	stw 31,28(1)                                     # 20_fs
	mr 31,1                                          # 20_fs

	lwz 9,52(31)                                     # 20.2_ex
	lwz 0,56(31)                                     # 20.2_ex
	subf 0,9,0                                       # 20.2_ex
	stw 0,64(31)                                     # 20.2_ex

	lwz 9,48(31)                                     # 20.2_ex
	lwz 0,64(31)                                     # 20.2_ex
	subf 0,9,0                                       # 20.2_ex
	stw 0,68(31)                                     # 20.2_ex

	lwz 0,68(31)                                     # 20.2_as
	stw 0,60(31)                                     # 20.2_as

	lis 0,.LC5@ha                                    # 20.3_fc
	addic 0,0,.LC5@l                                 # 20.3_fc
	mr 3,0                                           # 20.3_fc
	lwz 4,60(31)                                     # 20.3_fc
	crxor 6,6,6                                      # 20.3_fc
	bl printf                                        # 20.3_fc

	lwz 0,60(31)                                     # 20.4_re
	mr 3,0                                           # 20.4_re
	lwz 11,0(1)                                      # 20_fs
	lwz 31,-4(11)                                    # 20_fs
	mr 1,11                                          # 20_fs
	blr                                              # 20_fs
	.size sub,.-sub                                  # 20_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
