package cn.edu.buaa.prover;

import java.util.List;

import cn.edu.buaa.pojo.Proposition;

public class DerivationDTO {
	
	private List<Proposition> semantemeSet;		// 推导出的命题结果
	private List<String> proves;				// 证明序列整体
	private List<String> proofs;				// 证据
 	private Integer step; 						// 步数
	
	public List<Proposition> getSemantemeSet() {
		return semantemeSet;
	}

	public void setSemantemeSet(List<Proposition> semantemeSet) {
		this.semantemeSet = semantemeSet;
	}

	public List<String> getProves() {
		return proves;
	}
	
	public void setProves(List<String> proves) {
		this.proves = proves;
	}
	
	public List<String> getProofs() {
		return proofs;
	}
	
	public void setProofs(List<String> proofs) {
		this.proofs = proofs;
	}
	
	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}
	
}
