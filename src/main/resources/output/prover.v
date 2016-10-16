do_while : 9.4
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

