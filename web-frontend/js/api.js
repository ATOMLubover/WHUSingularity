const API_BASE_URL = 'http://localhost:8082';

const API = {
    async request(url, options = {}) {
        const token = localStorage.getItem('accessToken');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}${url}`, {
                ...options,
                headers
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error?.message || '请求失败');
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    async register(username, password, nickname) {
        return this.request('/api/user/register', {
            method: 'POST',
            body: JSON.stringify({ username, password, nickname })
        });
    },

    async login(username, password) {
        return this.request('/api/user/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
    },

    async logout() {
        return this.request('/api/user/logout', {
            method: 'POST'
        });
    },

    async getCurrentUser() {
        return this.request('/api/user/me', {
            method: 'GET'
        });
    },

    async getUserById(id) {
        return this.request(`/api/user/${id}`, {
            method: 'GET'
        });
    },

    async recharge(userId, amount) {
        return this.request(`/api/user/${userId}/recharge`, {
            method: 'POST',
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
    },

    async deduct(userId, amount) {
        return this.request(`/api/user/${userId}/deduct`, {
            method: 'POST',
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
    },

    async updateUser(id, password, nickname, role, balance) {
        const body = {};
        if (password !== undefined) body.password = password;
        if (nickname !== undefined) body.nickname = nickname;
        if (role !== undefined) body.role = role;
        if (balance !== undefined) body.balance = balance;

        return this.request(`/api/user/${id}`, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }
};

const Auth = {
    setToken(token) {
        localStorage.setItem('accessToken', token);
    },

    getToken() {
        return localStorage.getItem('accessToken');
    },

    setUser(user) {
        localStorage.setItem('user', JSON.stringify(user));
    },

    getUser() {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    clearAuth() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
    },

    isLoggedIn() {
        return !!this.getToken();
    }
};

const Utils = {
    showAlert(element, message, type = 'error') {
        element.className = `alert alert-${type}`;
        element.textContent = message;
        element.classList.remove('hidden');
        
        setTimeout(() => {
            element.classList.add('hidden');
        }, 3000);
    },

    formatMoney(amount) {
        return `¥${parseFloat(amount).toFixed(2)}`;
    },

    validateUsername(username) {
        const regex = /^[a-zA-Z0-9_]{4,32}$/;
        return regex.test(username);
    },

    validatePassword(password) {
        return password && password.length >= 8 && password.length <= 64;
    },

    validateNickname(nickname) {
        return !nickname || (nickname.length >= 1 && nickname.length <= 32);
    }
};
