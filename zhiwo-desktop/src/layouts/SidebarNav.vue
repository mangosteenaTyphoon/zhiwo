<template>
  <nav class="sidebar">
    <!-- 窗口控制按钮 + 品牌 -->
    <div class="sidebar-header" data-tauri-drag-region>
      <div class="window-controls">
        <button class="window-btn close" @click="closeWindow"></button>
        <button class="window-btn minimize" @click="minimizeWindow"></button>
        <button class="window-btn maximize" @click="maximizeWindow"></button>
      </div>
      <div class="sidebar-brand">
        <span class="brand-name">知我</span>
      </div>
    </div>

    <!-- 导航卡片容器 -->
    <div class="nav-card">
      <!-- 主导航 -->
      <ul class="nav-list">
        <li v-for="item in mainNavItems" :key="item.path">
          <router-link :to="item.path" class="nav-link" active-class="active">
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </li>
      </ul>

      <!-- 分组：工作区 -->
      <div class="nav-section">
        <h3 class="section-title">工作区</h3>
        <ul class="nav-list">
          <li v-for="item in workspaceItems" :key="item.path">
            <router-link :to="item.path" class="nav-link" active-class="active">
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </div>

    <!-- 底部用户信息 -->
    <div class="user-profile">
      <div class="user-avatar">
        <span>{{ userInitial }}</span>
      </div>
      <span class="user-name">{{ userName }}</span>
      <button class="logout-btn" @click="handleLogout" title="退出登录">⎋</button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { getCurrentWindow } from "@tauri-apps/api/window";
import { useAppStore } from "@/stores/app";
import { logout } from "@/api/auth";

const appStore = useAppStore();
const router = useRouter();
const appWindow = getCurrentWindow();

function closeWindow() {
  appWindow.close();
}

function minimizeWindow() {
  appWindow.minimize();
}

function maximizeWindow() {
  appWindow.toggleMaximize();
}

const mainNavItems = [
  { path: "/today", label: "今日", icon: "☀" },
];

const workspaceItems = [
  { path: "/goals", label: "目标", icon: "🎯" },
  { path: "/tasks", label: "任务", icon: "✓" },
];

const userName = computed(() => {
  return appStore.user?.nickname || appStore.user?.username || "用户";
});

const userInitial = computed(() => {
  return userName.value.charAt(0);
});

async function handleLogout() {
  try {
    await logout();
  } catch {
    // 后端退出失败也继续清理本地状态
  } finally {
    appStore.clearAuth();
    router.push("/login");
  }
}
</script>

<style scoped>
.sidebar {
  width: var(--z-sidebar-width);
  height: 100%;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border-right: none;
  display: flex;
  flex-direction: column;
  padding: 20px 14px 12px;
  position: relative;
  border-radius: 0 20px 20px 0;
}

/* 品牌 */
.sidebar-brand {
  margin-bottom: 16px;
  padding: 0 8px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--z-text-primary);
}

/* 导航卡片 - macOS 26 风格圆角容器 */
.nav-card {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 10px 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* 导航分区 */
.nav-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.section-title {
  padding: 4px 10px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--z-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 10px;
  border-radius: 10px;
  color: var(--z-text-secondary);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s ease;
  position: relative;
}

.nav-link:hover {
  background: rgba(0, 0, 0, 0.04);
  color: var(--z-text-primary);
}

/* 选中状态 - pill 形状 */
.nav-link.active {
  background: var(--z-accent);
  color: white;
  box-shadow: 0 1px 3px rgba(0, 113, 227, 0.3);
}

.nav-link.active:hover {
  background: var(--z-accent);
  color: white;
}

.nav-icon {
  font-size: 15px;
  width: 20px;
  text-align: center;
  opacity: 0.8;
}

.nav-link.active .nav-icon {
  opacity: 1;
}

/* 底部用户 */
.user-profile {
  margin-top: auto;
  padding: 10px 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--z-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--z-text-secondary);
  flex: 1;
}

.logout-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--z-text-tertiary);
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.logout-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--z-danger);
}
</style>
