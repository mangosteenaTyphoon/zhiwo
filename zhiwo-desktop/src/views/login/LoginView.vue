<template>
  <div class="login-view" data-tauri-drag-region>
    <!-- 红绿灯窗口控制按钮 -->
    <WindowControls />

    <div class="login-card">
      <!-- 品牌区域 -->
      <div class="login-brand">
        <div class="brand-icon">
          <img src="/src-tauri/icons/icon.png" alt="知我" class="brand-logo" />
        </div>
        <h1 class="brand-name">知我</h1>
        <p class="brand-slogan">目标驱动的个人效率工作台</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
            />
          </div>
        </div>

        <div v-if="errorMsg" class="form-error">
          {{ errorMsg }}
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? "登录中..." : "登录" }}
        </button>
      </form>

      <!-- 服务地址（折叠显示） -->
      <div class="server-config">
        <button class="server-toggle" @click="showServerConfig = !showServerConfig">
          <span>⚙️ 服务配置</span>
          <span class="toggle-arrow" :class="{ open: showServerConfig }">▼</span>
        </button>
        <div v-if="showServerConfig" class="server-input-wrapper">
          <input
            v-model="serverUrl"
            type="text"
            class="form-input server-input"
            placeholder="http://localhost:9999"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAppStore } from "@/stores/app";
import { login } from "@/api/auth";
import WindowControls from "@/components/WindowControls.vue";

const router = useRouter();
const appStore = useAppStore();
const serverUrl = ref(appStore.serverUrl);
const showServerConfig = ref(false);
const form = reactive({
  username: "",
  password: "",
  captchaVerification: "",
});
const loading = ref(false);
const errorMsg = ref("");

watch(serverUrl, (val) => {
  appStore.setServerUrl(val);
});

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMsg.value = "请输入用户名和密码";
    return;
  }

  loading.value = true;
  errorMsg.value = "";

  try {
    const token = await login({
      username: form.username,
      password: form.password,
      captchaVerification: "loginCaptcha",
    });
    appStore.setToken(token);
    await appStore.loadUserInfo();
    router.push("/");
  } catch (error: any) {
    errorMsg.value = error.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-view {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8eef5 50%, #f5f0f8 100%);
  position: relative;
  overflow: hidden;
  border-radius: var(--z-window-radius);
}

/* 背景装饰 */
.login-view::before {
  content: "";
  position: absolute;
  top: -30%;
  right: -20%;
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.06) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  animation: float 20s ease-in-out infinite;
}

.login-view::after {
  content: "";
  position: absolute;
  bottom: -30%;
  left: -15%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.05) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  animation: float 25s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* 窗口控制按钮位置 */
.login-view :deep(.window-controls) {
  position: absolute;
  top: 16px;
  left: 16px;
}

.login-card {
  width: 420px;
  padding: 56px 44px 44px;
  background: var(--z-card-bg);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border-radius: var(--z-card-radius);
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.1),
    0 10px 30px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  position: relative;
  z-index: 1;
  animation: cardIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-brand {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 22px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 12px 32px rgba(59, 130, 246, 0.15),
    0 4px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
}

.brand-icon:hover {
  transform: scale(1.05);
  box-shadow: 
    0 16px 40px rgba(59, 130, 246, 0.2),
    0 6px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.brand-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  object-fit: contain;
}

.brand-name {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  color: var(--z-text-primary);
  margin-bottom: 10px;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 50%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.brand-slogan {
  font-size: var(--z-font-size-md);
  color: var(--z-text-secondary);
  font-weight: var(--z-font-weight-normal);
  letter-spacing: 0.3px;
  opacity: 0.8;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: var(--z-font-size-base);
  font-weight: var(--z-font-weight-semibold);
  color: var(--z-text-secondary);
  margin-left: 6px;
  letter-spacing: 0.2px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 16px;
  opacity: 0.4;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.input-wrapper:focus-within .input-icon {
  opacity: 0.7;
}

.form-input {
  width: 100%;
  height: 52px;
  padding: 0 16px 0 46px;
  border: 1.5px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.9);
  font-size: 15px;
  color: var(--z-text-primary);
  outline: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.3px;
}

.form-input:hover {
  border-color: rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.form-input:focus {
  border-color: var(--z-input-focus-border);
  background: rgba(255, 255, 255, 1);
  box-shadow: 
    0 0 0 4px rgba(59, 130, 246, 0.08),
    0 4px 16px rgba(59, 130, 246, 0.06);
}

.form-input::placeholder {
  color: var(--z-text-tertiary);
  opacity: 0.6;
}

.form-error {
  font-size: var(--z-font-size-base);
  color: var(--z-danger);
  text-align: center;
  padding: 10px 14px;
  background: rgba(239, 68, 68, 0.06);
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.1);
}

.login-button {
  height: 52px;
  margin-top: 4px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 16px rgba(59, 130, 246, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.1);
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

.login-button::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  transition: left 0.5s ease;
}

.login-button:hover:not(:disabled)::after {
  left: 100%;
}

.login-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 
    0 6px 24px rgba(59, 130, 246, 0.4),
    0 2px 6px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 
    0 2px 8px rgba(59, 130, 246, 0.3),
    0 1px 2px rgba(0, 0, 0, 0.1);
}

.login-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 服务配置折叠区域 */
.server-config {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.server-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--z-text-tertiary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
}

.server-toggle:hover {
  color: var(--z-text-secondary);
  background: rgba(0, 0, 0, 0.02);
}

.toggle-arrow {
  font-size: 10px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.5;
}

.toggle-arrow.open {
  transform: rotate(180deg);
  opacity: 0.8;
}

.server-input-wrapper {
  margin-top: 14px;
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.server-input {
  padding-left: 16px;
  font-size: var(--z-font-size-base);
  height: 44px;
  border-radius: 10px;
}
</style>