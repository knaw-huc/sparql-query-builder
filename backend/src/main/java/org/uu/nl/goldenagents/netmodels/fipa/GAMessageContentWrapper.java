package org.uu.nl.goldenagents.netmodels.fipa;

import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

public class GAMessageContentWrapper implements FIPASendableObject {

	private static final long serialVersionUID = 1L;

	private int header;
	private FIPASendableObject content;
	
	public GAMessageContentWrapper(GAMessageHeader header) {
		this(header, null);
	}
	
	public GAMessageContentWrapper(FIPASendableObject content) {
		this(GAMessageHeader.UNKNOWN, content);
	}
	
	public GAMessageContentWrapper(GAMessageHeader header, FIPASendableObject content) {
		this.header = header.toIndex();
		this.content = content;
	}
	
	public FIPASendableObject getContent() {
		return this.content;
	}

	public GAMessageHeader getHeader() {
		return GAMessageHeader.fromIndex(this.header);
	}
	
	public void setHeader(int header) {
		this.header = header;
	}

	public void setHeader(GAMessageHeader header) {
		this.header = header.toIndex();
	}

	public void setContent(FIPASendableObject content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return this.getHeader().toString() + " | " + (content != null ? this.content.toString() : "No content");
	}
	
}
