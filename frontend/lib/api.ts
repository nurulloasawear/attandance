const API_BASE_URL = 'http://localhost:8081/api';

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user?: {
    publicId: string;
    username: string;
    email: string;
    role: string;
  };
}

export interface User {
  publicId: string;
  username: string;
  email: string;
  role: string;
}

class ApiClient {
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  constructor() {
    this.loadTokens();
  }

  private loadTokens() {
    if (typeof window !== 'undefined') {
      this.accessToken = localStorage.getItem('accessToken');
      this.refreshToken = localStorage.getItem('refreshToken');
    }
  }

  private saveTokens(accessToken: string, refreshToken: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    if (typeof window !== 'undefined') {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
    }
  }

  private clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
    if (typeof window !== 'undefined') {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  }

  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const error = await res.json();
      throw error;
    }

    const data = await res.json();
    this.saveTokens(data.accessToken, data.refreshToken);
    return data;
  }

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const error = await res.json();
      throw error;
    }

    const data = await res.json();
    this.saveTokens(data.accessToken, data.refreshToken);
    return data;
  }

  async checkUsername(username: string): Promise<{ available: boolean }> {
    const res = await fetch(`${API_BASE_URL}/auth/check-username?username=${encodeURIComponent(username)}`);
    if (!res.ok) throw new Error('Failed to check username');
    return res.json();
  }

  async checkEmail(email: string): Promise<{ available: boolean }> {
    const res = await fetch(`${API_BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
    if (!res.ok) throw new Error('Failed to check email');
    return res.json();
  }

  async getMe(): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/users/me`, {
      headers: { Authorization: `Bearer ${this.accessToken}` },
    });

    if (!res.ok) throw new Error('Failed to fetch user');
    return res.json();
  }

  async logout(): Promise<void> {
    if (this.refreshToken) {
      await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${this.accessToken}`,
        },
        body: JSON.stringify({ refreshToken: this.refreshToken }),
      }).catch(() => {
        // Ignore errors on logout
      });
    }
    this.clearTokens();
  }

  async refreshAccessToken(): Promise<AuthResponse> {
    if (!this.refreshToken) throw new Error('No refresh token');

    const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: this.refreshToken }),
    });

    if (!res.ok) {
      this.clearTokens();
      throw new Error('Failed to refresh token');
    }

    const data = await res.json();
    this.saveTokens(data.accessToken, data.refreshToken);
    return data;
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }
}

export const apiClient = new ApiClient();
