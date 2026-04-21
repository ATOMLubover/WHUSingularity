export interface ApiResponse<T = unknown> {
  success: boolean
  data?: T
  error?: ApiError
  message?: string
}

export interface ApiError {
  code: string
  message: string
}

export interface User {
  id: number
  username: string
  nickname: string | null
  role: 'normal' | 'admin'
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  tokenType: string
  accessToken: string
  expiresIn: number
  user: User
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

export type RegisterResponse = User
