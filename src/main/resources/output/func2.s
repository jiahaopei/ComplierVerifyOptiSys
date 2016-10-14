	.file	"func2.c"

	.section .rodata
	.align 2                                         # 3.3_fc
.LC0:	                                            # 3.3_fc
	.string	"sub function %d\n"                      # 3.3_fc

	.section ".text"
	.align 2                                         # 3_fs
	.globl sub                                       # 3_fs
	.type sub, @function                             # 3_fs
sub:	                                             # 3_fs
	stwu 1,-32(1)                                    # 3_fs
	stw 31,28(1)                                     # 3_fs
	mr 31,1                                          # 3_fs

	lwz 9,12(31)                                     # 3.2_ex
	lwz 0,16(31)                                     # 3.2_ex
	subf 0,9,0                                       # 3.2_ex
	stw 0,24(31)                                     # 3.2_ex

	lwz 9,8(31)                                      # 3.2_ex
	lwz 0,24(31)                                     # 3.2_ex
	subf 0,9,0                                       # 3.2_ex
	stw 0,28(31)                                     # 3.2_ex

	lwz 0,28(31)                                     # 3.2_as
	stw 0,20(31)                                     # 3.2_as

	lis 0,.LC0@ha                                    # 3.3_fc
	addic 0,0,.LC0@l                                 # 3.3_fc
	mr 3,0                                           # 3.3_fc
	lwz 4,20(31)                                     # 3.3_fc
	crxor 6,6,6                                      # 3.3_fc
	bl printf                                        # 3.3_fc

	lwz 0,20(31)                                     # 3.4_re
	mr 3,0                                           # 3.4_re
	lwz 11,0(1)                                      # 3_fs
	lwz 31,-4(11)                                    # 3_fs
	mr 1,11                                          # 3_fs
	blr                                              # 3_fs
	.size sub,.-sub                                  # 3_fs

	.ident	"powerpc-e500v2-linux-gnuspe-gcc"
