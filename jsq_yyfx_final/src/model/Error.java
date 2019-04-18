package model;

public class Error {
	
	private int lineNum;
	private String errorDes;
	
	public Error(String errorDes, int lineNum) {
		super();
		this.setErrorDes(errorDes);
		this.lineNum = lineNum;
	}
	
	public int getLineNum() {
		return lineNum;
	}
	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}
	@Override
	public String toString() {
		return "Error [" + "lineNum=" + lineNum + "\tDescription=" + errorDes + "]";
	}

	public String getErrorDes() {
		return errorDes;
	}

	public void setErrorDes(String errorDes) {
		this.errorDes = errorDes;
	}
}
