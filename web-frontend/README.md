# WHUSingularity 用户前端

这是一个静态前端页面，用于用户服务的登录、注册和充值等功能。

## 目录结构

```
web-frontend/
├── css/
│   └── style.css          # 全局样式
├── js/
│   └── api.js             # API 接口封装、认证管理、工具函数
├── index.html             # 用户主页（个人中心、充值）
├── login.html             # 登录页面
├── register.html          # 注册页面
└── README.md
```

## 功能说明

### 1. 用户注册
- 访问 `register.html`
- 输入用户名、密码、确认密码、昵称（可选）
- 点击注册按钮

### 2. 用户登录
- 访问 `login.html`
- 输入用户名和密码
- 点击登录按钮
- 成功后跳转至用户主页

### 3. 用户主页
- 显示用户信息：用户名、昵称、用户ID、角色
- 显示账户余额
- 充值功能
- 退出登录功能

## 使用方法

### 前置条件
1. 确保用户服务已启动（默认端口 8082）
2. 确保数据库已初始化
3. 如果使用跨域访问，后端需要配置 CORS

### 启动方式

#### 方式一：使用浏览器直接打开
直接在浏览器中打开 `login.html` 即可使用。

#### 方式二：使用本地服务器（推荐）
使用任意 HTTP 服务器，例如：

```bash
# 使用 Python 3
cd web-frontend
python -m http.server 8080

# 或使用 Node.js (http-server)
npm install -g http-server
cd web-frontend
http-server -p 8080
```

然后在浏览器中访问 `http://localhost:8080/login.html`

## API 配置

默认 API 地址为 `http://localhost:8082`，如需修改，请编辑 `js/api.js` 中的 `API_BASE_URL` 常量。

## 技术栈

- HTML5
- CSS3
- 原生 JavaScript（ES6+）
- 无框架依赖，可直接使用

## 主要特性

1. **响应式设计** - 适配多种屏幕尺寸
2. **表单验证** - 前端输入格式校验
3. **认证管理** - 使用 localStorage 存储 token 和用户信息
4. **友好提示** - 成功/失败提示
5. **模态弹窗** - 充值功能使用弹窗实现
