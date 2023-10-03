package com.terraformersmc.modmenu.util.mod;

import java.util.Objects;

public class ModrinthData {
	private final String projectId;
	private final String versionId;
	private final String versionNumber;

	public ModrinthData(String projectId, String versionId, String versionNumber) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.versionNumber = versionNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModrinthData)) {
			return false;
		}
		ModrinthData other = (ModrinthData)o;
		return Objects.equals(projectId, other.projectId) && Objects.equals(versionId, other.versionId) && Objects.equals(versionNumber, other.versionNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, versionId, versionNumber);
	}

	@Override
	public String toString() {
		return String.format("ModrinthData[projectId: %s, versionId: %s, versionNumber: %s]", projectId, versionId, versionNumber);
	}

	public String projectId() {
		return projectId;
	}

	public String versionId() {
		return versionId;
	}

	public String versionNumber() {
		return versionNumber;
	}
}
