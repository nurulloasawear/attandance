// lib/auth.api.ts
// @ts-ignore
import { authClient } from "./api-client";

export type LoginRequest = { username: string; password: string };

export type LoginResponse = {
    accessToken: string;
    refreshToken: string;
    sessionId: string; // важно!
};

export const AuthApi = {
    login: async (body: LoginRequest) => {
        const res = await authClient.post<LoginResponse>("/api/auth/login", body);
        authClient.setTokens(res.accessToken, res.refreshToken, res.sessionId);
        return res;
    },
};