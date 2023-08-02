package com.evrim.mevzuat.search.api.task;

public class MevzuatDocumentConstructorTaskException extends RuntimeException {
	
	MevzuatDocumentConstructorTask task;

	public MevzuatDocumentConstructorTaskException() {}
	
	public MevzuatDocumentConstructorTaskException(MevzuatDocumentConstructorTask task, Exception e) {
		super(e);
		this.task = task;
	}

	public MevzuatDocumentConstructorTaskException(MevzuatDocumentConstructorTask task, String message) {
		super(message);
		this.task = task;
	}
	
	public MevzuatDocumentConstructorTask getTask() {
		return task;
	}

	public void setTask(MevzuatDocumentConstructorTask task) {
		this.task = task;
	}
	
}
