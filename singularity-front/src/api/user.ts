import { request } from './client'
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, User } from './types'

export const userApi = {
  login: (data: LoginRequest) =>
    request<LoginResponse>({ method: 'POST', url: '/api/user/login', data }),

  register: (data: RegisterRequest) =>
    request<RegisterResponse>({ method: 'POST', url: '/api/user/register', data }),

  logout: () =>
    request({ method: 'POST', url: '/api/user/logout' }),

  me: () =>
    request<User>({ method: 'GET', url: '/api/user/me' }),
}
