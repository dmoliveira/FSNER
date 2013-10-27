package lbd.FSNER.Evaluation.Component;

import lbd.FSNER.Utils.Annotations.Comment;

public enum EvaluationLevel {
	@Comment(message="Its looks if token belongs to an entity (binary).")
	TokenLv1(true),

	@Comment(message="Its looks if label is correct.")
	LabelLv2(true),

	@Comment(message="Its looks if the entity was correct labeled.")
	EntityLv3(true);

	private boolean mIsToEvaluate;

	EvaluationLevel(boolean pIsToEvaluate) {
		mIsToEvaluate = pIsToEvaluate;
	}

	public boolean isToEvaluate() {
		return mIsToEvaluate;
	}
}
