function getApiBaseUrl() {
    if (window.location.port === '3000' || window.location.hostname === 'localhost') {
        return '';
    }
    return 'http://localhost:3000';
}

function isValidEmail(email) {
    const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return emailPattern.test(email);
}

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const togglePassword = document.getElementById('togglePassword');
    const rememberMe = document.getElementById('rememberMe');
    const errorMessage = document.getElementById('errorMessage');
    const btnLogin = document.getElementById('btnLogin');

    togglePassword.addEventListener('click', () => {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        
        const icon = togglePassword.querySelector('i');
        icon.classList.toggle('fa-eye');
        icon.classList.toggle('fa-eye-slash');
    });

    const token = localStorage.getItem('adminToken');
    if (token) {
        verifyTokenAndRedirect(token);
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        
        if (!email || !password) {
            showError('Vui lòng nhập đầy đủ email và mật khẩu');
            return;
        }

        if (!isValidEmail(email)) {
            showError('Email không đúng định dạng');
            return;
        }

        await login(email, password);
    });

    async function login(email, password) {
        btnLogin.disabled = true;
        btnLogin.classList.add('loading');
        hideError();

        try {
            const apiBaseUrl = getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/users/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                console.error('Server response is not JSON:', text);
                throw new Error('Không thể kết nối đến server. Vui lòng đảm bảo server đang chạy trên port 3000.');
            }

            if (!response.body) {
                throw new Error('Server không phản hồi. Vui lòng kiểm tra server có đang chạy không.');
            }

            const data = await response.json();

            if (!response.ok) {
                if (response.status === 403 && data.message && data.message.includes('khóa')) {
                    throw new Error('Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.');
                }
                throw new Error(data.message || 'Đăng nhập thất bại');
            }

            if (data.user.role !== 'admin') {
                throw new Error('Bạn không có quyền truy cập. Chỉ admin mới có thể đăng nhập.');
            }

            if (rememberMe.checked) {
                localStorage.setItem('adminToken', data.token);
                localStorage.setItem('adminUser', JSON.stringify(data.user));
            } else {
                sessionStorage.setItem('adminToken', data.token);
                sessionStorage.setItem('adminUser', JSON.stringify(data.user));
            }

            window.location.href = `${apiBaseUrl}/admin.html`;

        } catch (error) {
            console.error('Login error:', error);
            if (error.message.includes('fetch')) {
                showError('Không thể kết nối đến server. Vui lòng đảm bảo server Node.js đang chạy trên port 3000.');
            } else {
                showError(error.message || 'Đăng nhập thất bại. Vui lòng thử lại.');
            }
        } finally {
            btnLogin.disabled = false;
            btnLogin.classList.remove('loading');
        }
    }

    async function verifyTokenAndRedirect(token) {
        try {
            const apiBaseUrl = getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/users/profile`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    if (data.user && data.user.role === 'admin') {
                        window.location.href = `${apiBaseUrl}/admin.html`;
                    }
                }
            }
        } catch (error) {
            localStorage.removeItem('adminToken');
            sessionStorage.removeItem('adminToken');
        }
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.classList.add('show');
        setTimeout(() => {
            hideError();
        }, 5000);
    }

    function hideError() {
        errorMessage.classList.remove('show');
    }

    emailInput.focus();
});

