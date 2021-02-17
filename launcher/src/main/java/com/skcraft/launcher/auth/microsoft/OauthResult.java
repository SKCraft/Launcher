package com.skcraft.launcher.auth.microsoft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface OauthResult {
	boolean isError();

	@RequiredArgsConstructor
	class Success implements OauthResult {
		@Getter private final String authCode;

		@Override
		public boolean isError() {
			return false;
		}
	}

	@RequiredArgsConstructor
	class Error implements OauthResult {
		@Getter private final String errorMessage;

		@Override
		public boolean isError() {
			return true;
		}
	}
}
