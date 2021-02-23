package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a session saved to disk.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedSession {
	private UserType type;
	private String uuid;
	private String username;
	private String accessToken;
	private String refreshToken;
	private byte[] avatarImage;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		SavedSession that = (SavedSession) o;

		return getUuid().equals(that.getUuid());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(uuid)
				.toHashCode();
	}
}
