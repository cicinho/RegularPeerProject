package com.cicinho.peer.transaction;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PatientMedicalRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private String from;

	private String to;

	private Map<String, List<Operation>> permissions;

	private Type type;

	private Long expiresIn;

	private String others;

	public PatientMedicalRecord(String from, String to, Map<String, List<Operation>> permissions, Type type,
			Long expiresIn, String others) {
		super();
		this.from = from;
		this.to = to;
		this.permissions = permissions;
		this.type = type;
		this.expiresIn = expiresIn;
		this.others = others;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Map<String, List<Operation>> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<String, List<Operation>> permissions) {
		this.permissions = permissions;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	@Override
	public String toString() {
		return "PatientMedicalRecord [from=" + from + ", to=" + to + ", permissions=" + permissions + ", type=" + type
				+ ", expiresIn=" + expiresIn + ", others=" + others + "]";
	}

	public enum Operation {
		GET, POST, PUT, DELETE
	}
	
	public enum Type {
		RPA, RDA, GDA
	}
}
