<template>
  <div class="login-view">
    <div class="login-card">
      <div class="login-brand">
        <h1 class="brand-name">知我</h1>
        <p class="brand-slogan">目标驱动的个人效率工作台</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">服务地址</label>
          <input
            v-model="serverUrl"
            type="text"
            class="form-input"
            placeholder="http://localhost:9999"
          />
        </div>

        <div class="form-group">
          <label class="form-label">用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
          />
        </div>

        <div v-if="errorMsg" class="form-error">
          {{ errorMsg }}
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? "登录中..." : "登录" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAppStore } from "@/stores/app";
import { login } from "@/api/auth";

const router = useRouter();
const appStore = useAppStore();

const serverUrl = ref(appStore.serverUrl);
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
  background: var(--z-bg-primary);
}

.login-card {
  width: 380px;
  padding: var(--z-space-2xl);
  background: var(--z-bg-secondary);
  border-radius: var(--z-radius-xl);
  box-shadow: var(--z-shadow-lg);
}

.login-brand {
  text-align: center;
  margin-bottom: var(--z-space-2xl);
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--z-text-primary);
  margin-bottom: var(--z-space-sm);
}

.brand-slogan {
  font-size: 14px;
  color: var(--z-text-secondary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--z-space-lg);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--z-space-sm);
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--z-text-secondary);
}

.form-input {
  height: 40px;
  padding: 0 var(--z-space-md);
  border: 1px solid var(--z-border-strong);
  border-radius: var(--z-radius-md);
  background: var(--z-bg-primary);
  font-size: 14px;
  color: var(--z-text-primary);
  outline: none;
  transition: border-color var(--z-transition-fast);
}

.form-input:focus {
  border-color: var(--z-accent);
}

.form-error {
  font-size: 13px;
  color: var(--z-danger);
  text-align: center;
}

.login-button {
  height: 44px;
  margin-top: var(--z-space-sm);
  border: none;
  border-radius: var(--z-radius-md);
  background: var(--z-accent);
  color: var(--z-text-inverse);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--z-transition-fast);
}

.login-button:hover:not(:disabled) {
  background: var(--z-accent-hover);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>