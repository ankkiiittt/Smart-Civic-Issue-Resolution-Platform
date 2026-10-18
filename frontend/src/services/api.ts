import { API_BASE_URL } from '../config';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'CITIZEN' | 'OFFICER' | 'ADMIN';
  phoneNumber?: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  targetSlaHours: number;
}

export interface Department {
  id: number;
  name: string;
  description: string;
}

export interface IssuePhoto {
  id: number;
  photoUrl: string;
  uploadedAt: string;
  isBefore: boolean;
}

export interface IssueHistory {
  id: number;
  status: 'REPORTED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED';
  changedAt: string;
  comment?: string;
  changedByUsername: string;
}

export interface Issue {
  id: number;
  title: string;
  description: string;
  category: Category;
  status: 'REPORTED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  reportedAt: string;
  slaDeadline: string;
  isEscalated: boolean;
  longitude: number;
  latitude: number;
  reporterUsername: string;
  assignedOfficerUsername?: string;
  upvotesCount: number;
  hasUpvoted: boolean;
  photos: IssuePhoto[];
  histories: IssueHistory[];
}

export interface Analytics {
  totalIssues: number;
  resolvedIssues: number;
  pendingIssues: number;
  slaBreachRate: number;
  issuesByCategory: Record<string, number>;
  issuesByStatus: Record<string, number>;
  averageResolutionTimeByCategory: Record<string, number>;
  weeklyVolumeTrends: { weekStartDate: string; count: number }[];
}

function getHeaders(isMultipart = false): HeadersInit {
  const headers: Record<string, string> = {};
  if (!isMultipart) {
    headers['Content-Type'] = 'application/json';
  }
  const token = localStorage.getItem('token');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errBody = await response.json();
      errorMessage = errBody.error || errBody.message || errorMessage;
    } catch {
      errorMessage = response.statusText || errorMessage;
    }
    throw new Error(errorMessage);
  }
  
  if (response.status === 204) {
    return {} as T;
  }
  
  const body = await response.json();
  return body.data; // Wraps ApiResponse<T> where data holds the payload
}

function mapIssueResponse(data: any): Issue {
  if (!data) return data;
  return {
    ...data,
    category: data.category || {
      id: data.categoryId,
      name: data.categoryName || 'General Issue',
      description: '',
      targetSlaHours: 24,
    },
    reportedAt: data.reportedAt || data.createdAt,
  };
}

export const api = {
  // Auth
  async login(loginData: any): Promise<{ token: string; username: string; role: string }> {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(loginData),
    });
    return handleResponse(res);
  },

  async register(registerData: any): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(registerData),
    });
    return handleResponse(res);
  },

  async getMe(): Promise<User> {
    const res = await fetch(`${API_BASE_URL}/auth/me`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  // Issues
  async getIssues(params: { page?: number; size?: number; status?: string; categoryId?: number; query?: string } = {}): Promise<{ content: Issue[]; totalElements: number }> {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) queryParams.append('page', params.page.toString());
    if (params.size !== undefined) queryParams.append('size', params.size.toString());
    if (params.status) queryParams.append('status', params.status);
    if (params.categoryId) queryParams.append('categoryId', params.categoryId.toString());
    if (params.query) queryParams.append('query', params.query);

    const res = await fetch(`${API_BASE_URL}/issues?${queryParams.toString()}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await handleResponse<{ content: any[]; totalElements: number }>(res);
    return {
      content: (data.content || []).map(mapIssueResponse),
      totalElements: data.totalElements || 0,
    };
  },

  async getNearbyIssues(longitude: number, latitude: number, radiusMeters: number): Promise<Issue[]> {
    const res = await fetch(`${API_BASE_URL}/issues/nearby?longitude=${longitude}&latitude=${latitude}&radius=${radiusMeters}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await handleResponse<any[]>(res);
    return (data || []).map(mapIssueResponse);
  },

  async getIssue(id: number): Promise<Issue> {
    const res = await fetch(`${API_BASE_URL}/issues/${id}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await handleResponse<any>(res);
    return mapIssueResponse(data);
  },

  async reportIssue(issueRequest: any, photos: File[]): Promise<Issue> {
    const formData = new FormData();
    formData.append('title', issueRequest.title);
    formData.append('description', issueRequest.description);
    formData.append('categoryId', issueRequest.categoryId.toString());
    formData.append('latitude', issueRequest.latitude.toString());
    formData.append('longitude', issueRequest.longitude.toString());
    if (issueRequest.addressDescription) {
      formData.append('addressDescription', issueRequest.addressDescription);
    }
    photos.forEach((file) => {
      formData.append('photos', file);
    });

    const res = await fetch(`${API_BASE_URL}/issues`, {
      method: 'POST',
      headers: getHeaders(true),
      body: formData,
    });
    const data = await handleResponse<any>(res);
    return mapIssueResponse(data);
  },

  async updateIssueStatus(id: number, statusUpdateRequest: any, photo?: File): Promise<Issue> {
    const formData = new FormData();
    formData.append('status', statusUpdateRequest.status);
    if (statusUpdateRequest.comments) {
      formData.append('comments', statusUpdateRequest.comments);
    }
    if (photo) {
      formData.append('photo', photo);
    }

    const res = await fetch(`${API_BASE_URL}/issues/${id}/status`, {
      method: 'PUT',
      headers: getHeaders(true),
      body: formData,
    });
    const data = await handleResponse<any>(res);
    return mapIssueResponse(data);
  },

  async upvote(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/issues/${id}/upvote`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  async removeUpvote(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/issues/${id}/upvote`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  async deleteIssue(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/issues/${id}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  // Analytics
  async getAnalytics(): Promise<Analytics> {
    const res = await fetch(`${API_BASE_URL}/analytics`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  }
};
