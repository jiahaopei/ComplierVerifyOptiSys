	.file	"func.c"

	.section .rodata
	.align 2                                         # 3.3_fc
.LC0:	                                            # 3.3_fc
	.string	"add function %d\n"                      # 3.3_fc
	.align 2                                         # 3.4_fc
.LC1:	                                            # 3.4_fc
	.string	"add x %d, y %d\n"                       # 3.4_fc
	.align 2                                         # 3.5_fc
.LC2:	                                            # 3.5_fc
	.string	"addd addd\n"                            # 3.5_fc

	.section ".text"
	.align 2                                         # 3_fs
	.globl add                                       # 3_fs
	.type add, @function                             # 3_fs
add:	                                             # 3_fs
	stwu 1,-32(1)                                    # 3_fs
	stw 31,28(1)                                     # 3_fs
	mr 31,1                                          # 3_fs

	lwz 9,8(31)                                      # 3.2_ex
	lwz 0,12(31)                                     # 3.2_ex
	add 0,9,0                                        # 3.2_ex
	stw 0,20(31)                                     # 3.2_ex

	lwz 0,20(31)                                     # 3.2_as
	stw 0,16(31)                                     # 3.2_as

	lis 0,.LC0@ha                                    # 3.3_fc
	addic 0,0,.LC0@l                                 # 3.3_fc
	mr 3,0                                           # 3.3_fc
	lwz 4,16(31)                                     # 3.3_fc
	crxor 6,6,6                                      # 3.3_fc
	bl printf                                        # 3.3_fc

	lis 0,.LC1@ha                                    # 3.4_fc
	addic 0,0,.LC1@l                                 # 3.4_fc
	mr 3,0                                           # 3.4_fc
	lwz 4,8(31)                                      # 3.4_fc
	lwz 5,12(31)                                     # 3.4_fc
	crxor 6,6,6                                      # 3.4_fc
	bl printf                                        # 3.4_fc

	lis 0,.LC2@ha                                    # 3.5_fc
	addic 0,0,.LC2@l                                 # 3.5_fc
	mr 3,0                                           # 3.5_fc
	crxor 6,6,6                                      # 3.5_fc
	bl printf                                        # 3.5_fc

	lwz 0,16(31)                                     # 3.6_re
	mr 3,0                                           # 3.6_re
	lwz 11,0(1)                                      # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size add,.-add                                  # 3_fs

	.section .rodata
	.align 2                                         # 5.3_fc
.LC3:	                                            # 5.3_fc
	.string	"sub function %d\n"                      # 5.3_fc

	.section ".text"
	.align 2                                         # 5_fs
	.globl sub                                       # 5_fs
	.type sub, @function                             # 5_fs
sub:	                                             # 5_fs
	stwu 1,-32(1)                                    # 5_fs
	stw 31,28(1)                                     # 5_fs
	mr 31,1                                          # 5_fs

	lwz 9,24(31)                                     # 5.2_ex
	lwz 0,28(31)                                     # 5.2_ex
	subf 0,9,0                                       # 5.2_ex
	stw 0,36(31)                                     # 5.2_ex

	lwz 9,20(31)                                     # 5.2_ex
	lwz 0,36(31)                                     # 5.2_ex
	subf 0,9,0                                       # 5.2_ex
	stw 0,40(31)                                     # 5.2_ex

	lwz 0,40(31)                                     # 5.2_as
	stw 0,32(31)                                     # 5.2_as

	lis 0,.LC3@ha                                    # 5.3_fc
	addic 0,0,.LC3@l                                 # 5.3_fc
	mr 3,0                                           # 5.3_fc
	lwz 4,32(31)                                     # 5.3_fc
	crxor 6,6,6                                      # 5.3_fc
	bl printf                                        # 5.3_fc

	lwz 0,32(31)                                     # 5.4_re
	mr 3,0                                           # 5.4_re
	lwz 11,0(1)                                      # 5_fs
	lwz 31,-4(11)                                    # 5_fs
	mr 1,11                                          # 5_fs
	blr                                              # 5_fs
	.size sub,.-sub                                  # 5_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
