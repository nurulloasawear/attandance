// lib/api.ts

export interface ApiConfig {
  baseUrl: string;
  authBaseUrl?: string;
  timeout?: number;
}

export interface RequestOptions extends RequestInit {
  params?: Record<string, any>;
  _retry?: boolean;
}

function isBrowser() {
  return typeof window !== "undefined";
}

function safeGetSession(key: string) {
  if (!isBrowser()) return null;
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeSetSession(key: string, value: string) {
  if (!isBrowser()) return;
  try {
    sessionStorage.setItem(key, value);
  } catch {}
}

function safeRemoveSession(key: string) {
  if (!isBrowser()) return;
  try {
    sessionStorage.removeItem(key);
  } catch {}
}

function safeGetLocal(key: string) {
  if (!isBrowser()) return null;
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeSetLocal(key: string, value: string) {
  if (!isBrowser()) return;
  try {
    localStorage.setItem(key, value);
  } catch {}
}

function safeRemoveLocal(key: string) {
  if (!isBrowser()) return;
  try {
    localStorage.removeItem(key);
  } catch {}
}

function genId() {
  // crypto.randomUUID() works in modern browsers
  if (isBrowser() && typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return (crypto as any).randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function getOrCreateLocalId(key: string) {
  const existing = safeGetLocal(key);
  if (existing) return existing;
  const v = genId();
  safeSetLocal(key, v);
  return v;
}

function normalizeBaseUrl(url: string) {
  return (url || "").replace(/\/+$/, "");
}

function buildQuery(params?: Record<string, any>) {
  if (!params) return "";
  const entries = Object.entries(params).filter(
      ([, v]) => v !== null && v !== undefined
  );
  if (entries.length === 0) return "";
  const qs = new URLSearchParams(
      entries.reduce((acc, [k, v]) => {
        acc[k] = String(v);
        return acc;
      }, {} as Record<string, string>)
  ).toString();
  return qs ? `?${qs}` : "";
}

function isFormDataBody(body: any) {
  return typeof FormData !== "undefined" && body instanceof FormData;
}

export class ApiClient {
  private baseUrl: string;
  private authBaseUrl: string;
  private timeout: number;

  private accessToken: string | null = null;
  private refreshToken: string | null = null;
  private sessionId: string | null = null;

  // ✅ stable device id (Swagger header: X-Device)
  private deviceId: string | null = null;

  constructor(config: ApiConfig) {
    this.baseUrl = normalizeBaseUrl(config.baseUrl);
    this.authBaseUrl = normalizeBaseUrl(config.authBaseUrl || config.baseUrl);
    this.timeout = config.timeout ?? 30000;
    this.loadState();
  }

  private loadState() {
    // Always safe: in SSR this does nothing
    this.accessToken = safeGetSession("accessToken");
    this.refreshToken = safeGetLocal("refreshToken");
    this.sessionId = safeGetSession("sessionId");
    this.deviceId = safeGetLocal("deviceId") || getOrCreateLocalId("deviceId");
  }

  // -----------------------
  // Public helpers
  // -----------------------

  getSessionId() {
    this.loadState();
    return this.sessionId;
  }

  getDeviceId() {
    this.loadState();
    return this.deviceId;
  }

  setDeviceId(deviceId: string) {
    this.deviceId = deviceId;
    safeSetLocal("deviceId", deviceId);
  }

  setTokens(accessToken: string, refreshToken: string, sessionId?: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    if (sessionId) this.sessionId = sessionId;

    safeSetSession("accessToken", accessToken);
    safeSetLocal("refreshToken", refreshToken);
    if (sessionId) safeSetSession("sessionId", sessionId);
  }

  setSessionId(sessionId: string) {
    this.sessionId = sessionId;
    safeSetSession("sessionId", sessionId);
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
    this.sessionId = null;

    safeRemoveSession("accessToken");
    safeRemoveLocal("refreshToken");
    safeRemoveSession("sessionId");
  }

  // -----------------------
  // Internals
  // -----------------------

  private buildUrl(endpoint: string, params?: Record<string, any>) {
    const ep = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
    return `${this.baseUrl}${ep}${buildQuery(params)}`;
  }

  private buildHeaders(options?: RequestOptions): HeadersInit {
    this.loadState();

    const headers: Record<string, string> = {
      ...(options?.headers as Record<string, string> | undefined),
    };

    const body = options?.body as any;
    const isForm = isFormDataBody(body);

    // Content-Type
    if (!isForm && !headers["Content-Type"] && !headers["content-type"]) {
      headers["Content-Type"] = "application/json";
    }

    // ✅ X-Device
    if (!headers["X-Device"]) {
      const did = this.deviceId || getOrCreateLocalId("deviceId") || "web";
      headers["X-Device"] = did;
      this.deviceId = did;
    }

    // Authorization
    if (this.accessToken && !headers["Authorization"]) {
      headers["Authorization"] = `Bearer ${this.accessToken}`;
    }

    return headers;
  }

  private async parseBody(response: Response) {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) return response.json();
    return response.text();
  }

  private errorMessageFrom(data: any): string {
    if (!data) return "API Error";
    if (typeof data === "string") return data;

    if (typeof data === "object") {
      const msg = (data as any)?.message || (data as any)?.error;
      if (typeof msg === "string" && msg.trim()) return msg;
      try {
        return JSON.stringify(data);
      } catch {
        return "API Error";
      }
    }

    return String(data);
  }

  private async refreshAccessToken(): Promise<void> {
    this.loadState();

    if (!this.refreshToken) throw new Error("No refresh token available");
    if (!this.sessionId) throw new Error("No sessionId available (login again)");

    const response = await fetch(`${this.authBaseUrl}/api/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device": this.deviceId || getOrCreateLocalId("deviceId") || "web",
      },
      body: JSON.stringify({
        refreshToken: this.refreshToken,
        sessionId: this.sessionId,
      }),
      mode: "cors",
      credentials: "include",
    });

    const data = await this.parseBody(response);

    if (!response.ok) {
      this.clearTokens();
      throw new Error(this.errorMessageFrom(data) || "Failed to refresh token");
    }

    if ((data as any)?.accessToken && (data as any)?.refreshToken) {
      const sid = (data as any)?.sessionId || this.sessionId;
      this.setTokens((data as any).accessToken, (data as any).refreshToken, sid);
      return;
    }

    this.clearTokens();
    throw new Error("Refresh succeeded but tokens were not returned");
  }

  async request<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const headers = this.buildHeaders(options);

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);

    try {
      const response = await fetch(url, {
        ...options,
        headers,
        mode: "cors",
        credentials: "include",
        signal: controller.signal,
      });

      const data = await this.parseBody(response);

      if (!response.ok) {
        // ✅ refresh only when we have both refreshToken + sessionId
        if (
            response.status === 401 &&
            this.refreshToken &&
            this.sessionId &&
            !options?._retry
        ) {
          await this.refreshAccessToken();
          return this.request<T>(endpoint, { ...options, _retry: true });
        }

        const err: any = new Error(this.errorMessageFrom(data));
        err.status = response.status;
        err.data = data;
        throw err;
      }

      return data as T;
    } catch (error: any) {
      if (error?.name === "AbortError") throw new Error("Request timeout");
      throw error;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  get<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: "GET" });
  }

  post<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    const isForm = isFormDataBody(body);
    return this.request<T>(endpoint, {
      ...options,
      method: "POST",
      body: body === undefined || body === null ? undefined : isForm ? body : JSON.stringify(body),
    });
  }

  put<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    const isForm = isFormDataBody(body);
    return this.request<T>(endpoint, {
      ...options,
      method: "PUT",
      body: body === undefined || body === null ? undefined : isForm ? body : JSON.stringify(body),
    });
  }

  patch<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    const isForm = isFormDataBody(body);
    return this.request<T>(endpoint, {
      ...options,
      method: "PATCH",
      body: body === undefined || body === null ? undefined : isForm ? body : JSON.stringify(body),
    });
  }

  delete<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: "DELETE" });
  }
}

const AUTH_BASE = process.env.NEXT_PUBLIC_AUTH_API_URL || "http://localhost:8081";

export const authClient = new ApiClient({
  baseUrl: AUTH_BASE,
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});

export const userClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_USER_API_URL || AUTH_BASE,
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});

export const attendanceClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_ATTENDANCE_API_URL || "http://localhost:8082",
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});