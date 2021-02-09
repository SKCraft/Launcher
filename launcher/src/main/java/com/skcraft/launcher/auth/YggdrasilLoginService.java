/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.*;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Creates authenticated sessions using the Mojang Yggdrasil login protocol.
 */
@RequiredArgsConstructor
public class YggdrasilLoginService implements LoginService {

    private final URL authUrl;
    private final String clientId;

    public Session login(String id, String password)
            throws IOException, InterruptedException, AuthenticationException {
        AuthenticatePayload payload = new AuthenticatePayload(new Agent("Minecraft"), id, password, clientId);

        return call(this.authUrl, payload, null);
    }

    @Override
    public Session restore(SavedSession savedSession)
            throws IOException, InterruptedException, AuthenticationException {
        RefreshPayload payload = new RefreshPayload(savedSession.getAccessToken(), clientId);

        return call(new URL(this.authUrl, "/refresh"), payload, savedSession);
    }

    private Session call(URL url, Object payload, SavedSession previous)
            throws IOException, InterruptedException, AuthenticationException {
        HttpRequest req = HttpRequest
                .post(url)
                .bodyJson(payload)
                .execute();

        if (req.getResponseCode() != 200) {
            ErrorResponse error = req.returnContent().asJson(ErrorResponse.class);

            throw new AuthenticationException(error.getErrorMessage());
        } else {
            AuthenticateResponse response = req.returnContent().asJson(AuthenticateResponse.class);
            Profile profile = response.getSelectedProfile();

            if (previous != null && previous.getAvatarImage() != null) {
                profile.setAvatarImage(previous.getAvatarImage());
            } else {
                profile.setAvatarImage(VisageSkinService.fetchSkinHead(profile.getUuid()));
            }

            return profile;
        }
    }

    @Data
    private static class Agent {
        private final String name;
        private final int version = 1;
    }

    @Data
    private static class AuthenticatePayload {
        private final Agent agent;
        private final String username;
        private final String password;
        private final String clientToken;
    }

    @Data
    private static class RefreshPayload {
        private final String accessToken;
        private final String clientToken;
        private boolean requestUser = true;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthenticateResponse {
        private String accessToken;
        private String clientToken;
        @JsonManagedReference private Profile selectedProfile;
    }

    @Data
    private static class ErrorResponse {
        private String error;
        private String errorMessage;
        private String cause;
    }

    /**
     * Return in the list of available profiles.
     */
    @Data
    @ToString(exclude = "response")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Profile implements Session {
        @JsonProperty("id") private String uuid;
        private String name;
        private boolean legacy;
        private String avatarImage;
        @JsonIgnore private final Map<String, String> userProperties = Collections.emptyMap();
        @JsonBackReference private AuthenticateResponse response;

        @Override
        @JsonIgnore
        public String getSessionToken() {
            return String.format("token:%s:%s", getAccessToken(), getUuid());
        }

        @Override
        @JsonIgnore
        public String getAccessToken() {
            return response.getAccessToken();
        }

        @Override
        @JsonIgnore
        public UserType getUserType() {
            return legacy ? UserType.LEGACY : UserType.MOJANG;
        }

        @Override
        public boolean isOnline() {
            return true;
        }
    }

}
