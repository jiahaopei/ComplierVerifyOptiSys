functionCall : 3.5_fc
	Semantic verify correct

assignment : 3.6_as
	Semantic verify correct

expression : 3.7.1_ex
	Semantic verify correct

expression : 3.7.1.1_ex
	Semantic verify correct

assignment : 3.7.1.1_as
	Semantic verify correct

if : 3.7.1_if
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

expression : 3.7.3_ex
	Semantic verify correct

assignment : 3.7.3_as
	Semantic verify correct

expression : 3.7.4_ex
	Semantic verify correct

expression : 3.8_ex
	Semantic verify correct

do_while : 3.7_dw
目标码模式命题 :
P1 = .L1:
P2 = <STA-LIST>
P3 = GPR[0] = <LOG-EXP>
P4 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001
P5 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4
辅助前提 :
P0 = (σ(<STA-LIST>)) ∧ ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** n || <LOG-EXP> == 0 -> skip)
推导序列 :
S1 = .L1:		P1
S2 = <STA-LIST>		P2
S3 = GPR[0] = <LOG-EXP>		P3
S4 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001		P4
S5 = <LOG-EXP> < 0 -> CR[7] = b100 || <LOG-EXP> > 0 -> CR[7] = b010 || <LOG-EXP> == 0 -> CR[7] = b001		S3,S4,MP
S6 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4		P5
S7 = <LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4		S5,S6,MP
S8 = (.L1:) ∧ (<STA-LIST>) ∧ (<LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4)		S1, S2, S7, CI
S9 = (<STA-LIST>) ∧ (<LOG-EXP> != 0 -> <STA-LIST> || <LOG-EXP> == 0 -> null)		S8, REDUCE
S10 = (σ(<STA-LIST>)) ∧ (<LOG-EXP> != 0 -> σ(<STA-LIST>) || <LOG-EXP> == 0 -> skip)		S9, σ
S11 = (σ(<STA-LIST>)) ∧ ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** N || <LOG-EXP> == 0 -> skip)		P0, n = N
S12 = (σ(<STA-LIST>)) ∧ ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** (N + 1) || <LOG-EXP> == 0 -> skip)		S10, S11, CI

functionCall : 3.9_fc
	Semantic verify correct

functionCall : 3.10_fc
	Semantic verify correct

assignment : 3.11_as
	Semantic verify correct

expression : 3.12_ex
	Semantic verify correct

assignment : 3.12_as
	Semantic verify correct

expression : 3.13.1_ex
	Semantic verify correct

expression : 3.13.2_ex
	Semantic verify correct

assignment : 3.13.2_as
	Semantic verify correct

expression : 3.13_ex
	Semantic verify correct

while : 3.13_wh
目标码模式命题 :
P1 = PC = PC + @.L2
P2 = .L1:
P3 = <STA-LIST>
P4 = .L2:
P5 = GPR[0] = <LOG-EXP>
P6 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001
P7 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4
辅助前提 :
P0 = ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** n || <LOG-EXP> == 0 -> skip)
推导序列 :
S1 = PC = PC + @.L2		P1
S2 = .L1:		P2
S3 = <STA-LIST>		P3
S4 = .L2:		P4
S5 = GPR[0] = <LOG-EXP>		P5
S6 = GPR[0] < 0 -> CR[7] = b100 || GPR[0] > 0 -> CR[7] = b010 || GPR[0] == 0 -> CR[7] = b001		P6
S7 = <LOG-EXP> < 0 -> CR[7] = b100 || <LOG-EXP> > 0 -> CR[7] = b010 || <LOG-EXP> == 0 -> CR[7] = b001		S5,S6,MP
S8 = CR[7] == b100 -> PC = PC + @.L1 || CR[7] == b010 -> PC = PC + @.L1 || CR[7] == b001 -> PC = PC + 4		P7
S9 = <LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4		S7,S8,MP
S10 = (PC = PC + @.L2) ∧ (.L1:) ∧ (<STA-LIST>) ∧ (.L2:) ∧ (<LOG-EXP> < 0 -> PC = PC + @.L1 || <LOG-EXP> > 0 -> PC = PC + @.L1 || <LOG-EXP> == 0 -> PC = PC + 4)		S1, S2, S3, S4, S9, CI
S11 = (<LOG-EXP> != 0 -> <STA-LIST> || <LOG-EXP> == 0 -> null)		S10, REDUCE
S12 = (<LOG-EXP> != 0 -> σ(<STA-LIST>) || <LOG-EXP> == 0 -> skip)		S11, σ
S13 = ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** N || <LOG-EXP> == 0 -> skip)		P0, n = N
S14 = ({<LOG-EXP> != 0 -> σ(<STA-LIST>)} ** (N + 1) || <LOG-EXP> == 0 -> skip)		S12, S13, CI

functionCall : 3.15_fc
	Semantic verify correct

assignment : 3.16_as
	Semantic verify correct

expression : 3.16.1_ex
	Semantic verify correct

expression : 3.16_ex
	Semantic verify correct

expression : 3.16_ex
	Semantic verify correct

for : 3.16_fo
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

return : 3.18_re
	Semantic verify correct

functionStatement : 3_fs
	Semantic verify correct

