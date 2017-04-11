package org.server.core.filterchain;

public abstract class AbstractFilterTask implements FilterTask {
	int status = 0;

	@Override
	public int status() {
		return status;
	}

	@Override
	public void status(int newStatus) {
		this.status = newStatus;
	}
}
