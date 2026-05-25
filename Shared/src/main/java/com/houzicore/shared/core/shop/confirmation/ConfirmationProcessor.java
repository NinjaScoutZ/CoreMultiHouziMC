package com.houzicore.shared.core.shop.confirmation;

public interface ConfirmationProcessor {
	void init(ConfirmationPage<?, ?> page);
	void process(ConfirmationCallback callback);
}
