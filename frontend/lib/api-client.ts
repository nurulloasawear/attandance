interface ApiConfig {
  baseUrl: string;
  timeout?: number;
}

interface RequestOptions extends RequestInit {
  params?: Record<string, any>;
}

class ApiClient {
  private baseUrl: string;
  private timeout: number;
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  constructor(config: ApiConfig) {
    this.baseUrl = config.baseUrl;
    this.timeout = config.timeout || 30000;
    this.loadTokens();
  }

  private loadTokens() {
    if (typeof window !== 'undefined') {
      this.accessToken = sessionStorage.getItem('accessToken');
      this.refreshToken = localStorage.getItem('refreshToken');
    }
  }

  setTokens(accessToken: string, refreshToken: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
    }
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  }

  private buildUrl(endpoint: string, params?: Record<string, any>): string {
    let url = `${this.baseUrl}${endpoint}`;
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
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(options?.headers || {}),
    };

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    return headers;
  }

  private async handleResponse(response: Response) {
    const contentType = response.headers.get('content-type');
    let data: any = null;

    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      if (response.status === 401 && this.refreshToken) {
        await this.refreshAccessToken();
        throw new Error('Token refreshed - retry request');
      }
      const error = new Error(data?.message || data || 'API Error');
      (error as any).status = response.status;
      (error as any).data = data;
      throw error;
    }

    return data;
  }

  private async refreshAccessToken(): Promise<void> {
    if (!this.refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/auth/refresh`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken: this.refreshToken }),
        }
      );

      const data = await response.json();
      if (response.ok) {
        this.setTokens(data.accessToken, data.refreshToken);
      } else {
        this.clearTokens();
        throw new Error('Failed to refresh token');
      }
    } catch (error) {
      this.clearTokens();
      throw error;
    }
  }

  async request<T = any>(
    endpoint: string,
    options?: RequestOptions
  ): Promise<T> {
    const url = this.buildUrl(endpoint, options?.params);
    const headers = this.buildHeaders(options);

    const fetchOptions: RequestInit = {
      ...options,
      headers,
      mode: 'cors',
      credentials: 'include',
    };

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.timeout);

      const response = await fetch(url, {
        ...fetchOptions,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);
      return this.handleResponse(response);
    } catch (error: any) {
      if (error.name === 'AbortError') {
        throw new Error('Request timeout');
      }
      throw error;
    }
  }

  get<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'GET' });
  }

  post<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  put<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  patch<T = any>(endpoint: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  delete<T = any>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'DELETE' });
  }
}

// Initialize clients for different services
// @ts-ignore
const authClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_AUTH_API_URL || 'http://localhost:8081',
  timeout: 30000,
});

const userClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_USER_API_URL || 'http://localhost:8081',
  timeout: 30000,
});

const attendanceClient = new ApiClient({
  baseUrl: process.env.NEXT_PUBLIC_ATTENDANCE_API_URL || 'http://localhost:8082',
  timeout: 30000,
});

export { authClient, userClient, attendanceClient, ApiClient };
