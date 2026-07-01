package com.terraformersmc.modmenu.util.mod;

import org.jetbrains.annotations.Nullable;

import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.VersionUtil;

import net.ornithemc.osl.text.api.TextComponent;
import net.ornithemc.osl.text.api.TextComponents;

public class ModrinthUpdateInfo implements UpdateInfo {
	protected final String projectId;
	protected final String versionId;
	protected final String versionNumber;
	protected final UpdateChannel updateChannel;

	private static final TextComponent MODRINTH_TEXT = TextComponents.translatable("modmenu.modrinth");

	public ModrinthUpdateInfo(String projectId, String versionId, String versionNumber, UpdateChannel updateChannel) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.versionNumber = versionNumber;
		this.updateChannel = updateChannel;
	}

	@Override
	public boolean isUpdateAvailable() {
		return true;
	}

	@Override
	public @Nullable TextComponent getUpdateMessage() {
		return TextComponents.translatable("modmenu.updateText", VersionUtil.stripPrefix(this.versionNumber), MODRINTH_TEXT);
	}

	@Override
	public String getDownloadLink() {
		return String.format("https://modrinth.com/project/%s/version/%s", projectId, versionId);
	}

	public String getProjectId() {
		return projectId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	@Override
	public UpdateChannel getUpdateChannel() {
		return this.updateChannel;
	}
}
