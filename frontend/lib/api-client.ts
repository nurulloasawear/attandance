interface ApiConfig {
  baseUrl: string;
  authBaseUrl?: string;
  timeout?: number;
}

interface RequestOptions extends RequestInit {
  params?: Record<string, any>;
  _retry?: boolean;
}

class ApiClient {
  private baseUrl: string;
  private authBaseUrl: string;
  private timeout: number;
  private accessToken: string | null = null;
  private refreshToken: string | null = null;
  private sessionId: string | null = null;

  constructor(config: ApiConfig) {
    this.baseUrl = config.baseUrl.replace(/\/+$/, '');
    this.authBaseUrl = (config.authBaseUrl || config.baseUrl).replace(/\/+$/, '');
    this.timeout = config.timeout || 30000;
    this.loadTokens();
  }

  private loadTokens() {
    if (typeof window !== 'undefined') {
      this.accessToken = sessionStorage.getItem('accessToken');
      this.refreshToken = localStorage.getItem('refreshToken');
      this.sessionId = sessionStorage.getItem('sessionId');
    }
  }

  setTokens(accessToken: string, refreshToken: string, sessionId?: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    if (sessionId) this.sessionId = sessionId;

    if (typeof window !== 'undefined') {
      sessionStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      if (sessionId) sessionStorage.setItem('sessionId', sessionId);
    }
  }

  setSessionId(sessionId: string) {
    this.sessionId = sessionId;
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('sessionId', sessionId);
    }
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
    this.sessionId = null;

    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      sessionStorage.removeItem('sessionId');
    }
  }

  private buildUrl(endpoint: string, params?: Record<string, any>): string {
    const ep = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    let url = `${this.baseUrl}${ep}`;

    if (params && Object.keys(params).length > 0) {
      const queryString = new URLSearchParams(
          Object.entries(params).reduce((acc, [key, value]) => {
            if (value !== null && value !== undefined) {
              acc[key] = String(value);
            }
            return acc;
          }, {} as Record<string, string>)
      ).toString();

      url += `?${queryString}`;
    }

    return url;
  }

  private buildHeaders(options?: RequestOptions): HeadersInit {
    const headers: Record<string, string> = {
      ...(options?.headers as Record<string, string> | undefined),
    };

    const isFormData =
        typeof FormData !== 'undefined' && options?.body instanceof FormData;

    if (!isFormData && !headers['Content-Type'] && !headers['content-type']) {
      headers['Content-Type'] = 'application/json';
    }

    if (this.accessToken && !headers['Authorization']) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    return headers;
  }

  private async parseBody(response: Response) {
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) return response.json();
    return response.text();
  }

  private async refreshAccessToken(): Promise<void> {
    if (!this.refreshToken) throw new Error('No refresh token available');
    if (!this.sessionId) throw new Error('No sessionId available');

    const response = await fetch(`${this.authBaseUrl}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        refreshToken: this.refreshToken,
        sessionId: this.sessionId,
      }),
      mode: 'cors',
      credentials: 'include',
    });

    const data = await this.parseBody(response);

    if (!response.ok) {
      this.clearTokens();
      throw new Error(
          (data as any)?.message ||
          (data as any)?.error ||
          'Failed to refresh token'
      );
    }

    if ((data as any)?.accessToken && (data as any)?.refreshToken) {
      const sid = (data as any)?.sessionId || this.sessionId;
      this.setTokens(
          (data as any).accessToken,
          (data as any).refreshToken,
          sid
      );
      return;
    }

    this.clearTokens();
    throw new Error('Refresh succeeded but tokens were not returned');
  }

  async request<T = any>(
      endpoint: string,
      options?: RequestOptions
  ): Promise<T> {
    this.loadTokens();

    const url = this.buildUrl(endpoint, options?.params);
    const headers = this.buildHeaders(options);

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);

    try {
      const response = await fetch(url, {
        ...options,
        headers,
        mode: 'cors',
        credentials: 'include',
        signal: controller.signal,
      });

      const data = await this.parseBody(response);

      if (!response.ok) {
        if (response.status === 401 && this.refreshToken && !options?._retry) {
          await this.refreshAccessToken();
          return this.request<T>(endpoint, { ...options, _retry: true });
        }

        const err: any = new Error(
            (data as any)?.message ||
            (data as any)?.error ||
            data ||
            'API Error'
        );
        err.status = response.status;
        err.data = data;
        throw err;
      }

      return data as T;
    } catch (error: any) {
      if (error?.name === 'AbortError')
        throw new Error('Request timeout');
      throw error;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  get<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'GET' });
  }

  post<T = any>(
      endpoint: string,
      body?: any,
      options?: RequestOptions
  ): Promise<T> {
    const isFormData =
        typeof FormData !== 'undefined' && body instanceof FormData;

    return this.request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
    });
  }

  put<T = any>(
      endpoint: string,
      body?: any,
      options?: RequestOptions
  ): Promise<T> {
    const isFormData =
        typeof FormData !== 'undefined' && body instanceof FormData;

    return this.request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
    });
  }

  patch<T = any>(
      endpoint: string,
      body?: any,
      options?: RequestOptions
  ): Promise<T> {
    const isFormData =
        typeof FormData !== 'undefined' && body instanceof FormData;

    return this.request<T>(endpoint, {
      ...options,
      method: 'PATCH',
      body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
    });
  }

  delete<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'DELETE' });
  }
}

const AUTH_BASE =
    process.env.NEXT_PUBLIC_AUTH_API_URL || 'http://localhost:8081';

const authClient = new ApiClient({
  baseUrl: AUTH_BASE,
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});

const userClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_USER_API_URL || AUTH_BASE,
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});

const attendanceClient = new ApiClient({
  baseUrl:
      process.env.NEXT_PUBLIC_ATTENDANCE_API_URL ||
      'http://localhost:8082',
  authBaseUrl: AUTH_BASE,
  timeout: 30000,
});

export { authClient, userClient, attendanceClient, ApiClient };