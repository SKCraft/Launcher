/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.*;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Creates authenticated sessions using the Mojang Yggdrasil login protocol.
 */
@Log
public class YggdrasilLoginService implements LoginService {

    private final URL authUrl;
    private final String clientId;

    /**
     * Create a new login service with the given authentication URL.
     *
     * @param authUrl the authentication URL
     * @param clientId
     */
    public YggdrasilLoginService(@NonNull URL authUrl, String clientId) {
        this.authUrl = authUrl;
        this.clientId = clientId;
    }

    public Session login(String agent, String id, String password)
            throws IOException, InterruptedException, AuthenticationException {
        AuthenticatePayload payload = new AuthenticatePayload(new Agent(agent), id, password, clientId);

        return call(this.authUrl, payload);
    }

    @Override
    public Session restore(SavedSession savedSession)
            throws IOException, InterruptedException, AuthenticationException {
        RefreshPayload payload = new RefreshPayload(savedSession.getAccessToken(), clientId);

        return call(new URL(this.authUrl, "/refresh"), payload);
    }

    private Session call(URL url, Object payload)
            throws IOException, InterruptedException, AuthenticationException {
        HttpRequest req = HttpRequest
                .post(url)
                .bodyJson(payload)
                .execute();

        if (req.getResponseCode() != 200) {
            ErrorResponse error = req.returnContent().asJson(ErrorResponse.class);
            log.warning(error.toString());
            throw new AuthenticationException(error.getErrorMessage(), error.getErrorMessage());
        } else {
            AuthenticateResponse response = req.returnContent().asJson(AuthenticateResponse.class);

            return response.getSelectedProfile();
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
        @JsonIgnore private final Map<String, String> userProperties = Collections.emptyMap();
        @JsonBackReference private AuthenticateResponse response;

        @Override
        @JsonIgnore
        public String getSessionToken() {
            return String.format("token:%s:%s", getAccessToken(), getUuid());
        }

        @Override
        @JsonIgnore
        public String getClientToken() {
            return response.getClientToken();
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
