if_else : 4.8.2_if, 4.8.3_el
目标码模式命题 :
P1 = GPR[0] = <LOG-EXP>
P2 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001
P3 = CR[7] == b100 -> PC = PC + 4 || CR[7] == b010 -> PC = PC + 4 || CR[7] == b001 -> PC = PC + @.L1
P4 = <STA-LIST_1>
P5 = PC = PC + @.L2
P6 = .L1:
P7 = <STA-LIST_2>
P8 = .L2:
推导序列 :
S1 = GPR[0] = <LOG-EXP>		P1
S2 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001		P2
S3 = <LOG-EXP> < 0 -> CR[7] = b100 || <LOG-EXP> > 0 -> CR[7] = b010 || <LOG-EXP> == 0 -> CR[7] = b001		S1,S2,MP
S4 = CR[7] == b100 -> PC = PC + 4 || CR[7] == b010 -> PC = PC + 4 || CR[7] == b001 -> PC = PC + @.L1		P3
S5 = <LOG-EXP> < 0 -> PC = PC + 4 || <LOG-EXP> > 0 -> PC = PC + 4 || <LOG-EXP> == 0 -> PC = PC + @.L1		S3,S4,MP
S6 = <STA-LIST_1>		P4
S7 = PC = PC + @.L2		P5
S8 = .L1:		P6
S9 = <STA-LIST_2>		P7
S10 = .L2:		P8
S11 = (<LOG-EXP> < 0 -> PC = PC + 4 || <LOG-EXP> > 0 -> PC = PC + 4 || <LOG-EXP> == 0 -> PC = PC + @.L1) ∧ (<STA-LIST_1>) ∧ (PC = PC + @.L2) ∧ (.L1:) ∧ (<STA-LIST_2>) ∧ (.L2:)		S5, S6, S7, S8, S9, S10, CI
S12 = (<LOG-EXP> != 0 -> <STA-LIST_1> || <LOG-EXP> == 0 -> <STA-LIST_2>)		S11, REDUCE
S13 = (<LOG-EXP> != 0 -> σ(<STA-LIST_1>) || <LOG-EXP> == 0 -> σ(<STA-LIST_2>))		S12, σ

for : 4.8_fo
目标码模式命题 :
P1 = <ASS-EXP_1>
P2 = PC = PC + @.L2
P3 = .L1:
P4 = <STA-LIST>
P5 = <ASS-EXP_2>
P6 = .L2:
P7 = GPR[0] = <LOG-EXP>
P8 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001
P9 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4
辅助前提 :
P0 = (σ(<ASS-EXP_1>)) ∧ ({<LOG-EXP> != 0 -> [σ(<STA-LIST>); σ(<ASS-EXP_2>)]} ** n || <LOG-EXP> == 0 -> skip)
推导序列 :
S1 = <ASS-EXP_1>		P1
S2 = PC = PC + @.L2		P2
S3 = .L1:		P3
S4 = <STA-LIST>		P4
S5 = <ASS-EXP_2>		P5
S6 = .L2:		P6
S7 = GPR[0] = <LOG-EXP>		P7
S8 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001		P8
S9 = <LOG-EXP> < 0 -> CR[7] = b100 || <LOG-EXP> > 0 -> CR[7] = b010 || <LOG-EXP> == 0 -> CR[7] = b001		S7,S8,MP
S10 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4		P9
S11 = <LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4		S9,S10,MP
S12 = (<ASS-EXP_1>) ∧ (PC = PC + @.L2) ∧ (.L1:) ∧ (<STA-LIST>) ∧ (<ASS-EXP_2>) ∧ (.L2:) ∧ (<LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4)		S1, S2, S3, S4, S5, S6, S11, CI
S13 = (<ASS-EXP_1>) ∧ (<LOG-EXP> != 0 -> <STA-LIST>; <ASS-EXP_2> || <LOG-EXP> == 0 -> null)		S12, REDUCE
S14 = (σ(<ASS-EXP_1>)) ∧ (<LOG-EXP> != 0 -> [σ(<STA-LIST>); σ(<ASS-EXP_2>)] || <LOG-EXP> == 0 -> skip)		S13, σ
S15 = (σ(<ASS-EXP_1>)) ∧ ({<LOG-EXP> != 0 -> [σ(<STA-LIST>); σ(<ASS-EXP_2>)]} ** N || <LOG-EXP> == 0 -> skip)		P0, n = N
S16 = (σ(<ASS-EXP_1>)) ∧ ({<LOG-EXP> != 0 -> [σ(<STA-LIST>); σ(<ASS-EXP_2>)]} ** (N + 1) || <LOG-EXP> == 0 -> skip)		S14, S15, CI

if : 6.2_if
目标码模式命题 :
P1 = GPR[0] = <LOG-EXP>
P2 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001
P3 = CR[7] == b100 -> PC = PC + 4 || CR[7] == b010 -> PC = PC + 4 || CR[7] == b001 -> PC = PC + @.L1
P4 = <STA-LIST>
P5 = .L1:
推导序列 :
S1 = GPR[0] = <LOG-EXP>		P1
S2 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001		P2
S3 = <LOG-EXP> < 0 -> CR[7] = b100 || <LOG-EXP> > 0 -> CR[7] = b010 || <LOG-EXP> == 0 -> CR[7] = b001		S1,S2,MP
S4 = CR[7] == b100 -> PC = PC + 4 || CR[7] == b010 -> PC = PC + 4 || CR[7] == b001 -> PC = PC + @.L1		P3
S5 = <LOG-EXP> < 0 -> PC = PC + 4 || <LOG-EXP> > 0 -> PC = PC + 4 || <LOG-EXP> == 0 -> PC = PC + @.L1		S3,S4,MP
S6 = <STA-LIST>		P4
S7 = .L1:		P5
S8 = (<LOG-EXP> < 0 -> PC = PC + 4 || <LOG-EXP> > 0 -> PC = PC + 4 || <LOG-EXP> == 0 -> PC = PC + @.L1) ∧ (<STA-LIST>) ∧ (.L1:)		S5, S6, S7, CI
S9 = (<LOG-EXP> != 0 -> <STA-LIST> || <LOG-EXP> == 0 -> null)		S8, REDUCE
S10 = (<LOG-EXP> != 0 -> σ(<STA-LIST>) || <LOG-EXP> == 0 -> skip)		S9, σ

