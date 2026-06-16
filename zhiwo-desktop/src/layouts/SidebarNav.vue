<template>
  <nav class="sidebar" :class="{ collapsed: props.collapsed }">
    <!-- 窗口控制按钮 -->
    <WindowControls />

    <!-- 搜索框 -->
    <div class="search-box">
      <svg class="search-icon" viewBox="0 0 16 16" fill="none">
        <circle cx="7" cy="7" r="4.5" stroke="currentColor" stroke-width="1.2"/>
        <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
      </svg>
      <input type="text" placeholder="搜索" />
    </div>

    <!-- 主导航 -->
    <ul class="nav-list">
      <li v-for="item in mainNavItems" :key="item.path">
        <router-link :to="item.path" class="nav-link" active-class="active">
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </li>
    </ul>

    <!-- 工作区分组 -->
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

    <!-- 底部用户信息 -->
    <div class="user-profile">
      <div class="user-avatar">
        <span>{{ userInitial }}</span>
      </div>
      <span class="user-name">{{ userName }}</span>
      <button class="logout-btn" @click="handleLogout" title="退出登录">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M6 14H3a2 2 0 01-2-2V4a2 2 0 012-2h3"/>
          <polyline points="11,12 15,8 11,4"/>
          <line x1="15" y1="8" x2="5" y2="8"/>
        </svg>
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { invoke } from "@tauri-apps/api/core";
import WindowControls from "@/components/WindowControls.vue";
import { useAppStore } from "@/stores/app";

const props = defineProps<{ collapsed: boolean }>();
import { logout } from "@/api/auth";

const appStore = useAppStore();
const router = useRouter();


const mainNavItems = [
  { path: "/today", label: "今日", icon: "☀" },
];

const workspaceItems = [
  { path: "/inbox", label: "收集箱", icon: "📥" },
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
  try { await logout(); } catch {} finally {
    appStore.clearAuth();
    router.push("/login");
  }
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-w, 220px);
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px 10px 12px;
  position: relative;
  overflow: hidden;
  border-radius: var(--z-radius-lg);
  transition: width 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  /* 液态玻璃核心：极通透 + 强力模糊 */
  background:
    linear-gradient(
      160deg,
      rgba(255,255,255,0.22) 0%,
      rgba(255,255,255,0.06) 50%,
      rgba(255,255,255,0.14) 100%
    );
  backdrop-filter: blur(120px) saturate(300%) brightness(1.04) contrast(1.02);
  -webkit-backdrop-filter: blur(120px) saturate(300%) brightness(1.04) contrast(1.02);
  /* 多层阴影营造深度 */
  box-shadow:
    /* 内层：玻璃上边缘强反光 */
    inset 0 0.5px 0 0 rgba(255,255,255,0.9),
    /* 内层：左上角高光区 */
    inset 1px 1px 0 0 rgba(255,255,255,0.3),
    /* 内层：右下角暗区（玻璃厚度感） */
    inset -0.5px -0.5px 0 0 rgba(0,0,0,0.06),
    /* 内层：底部微暗 */
    inset 0 -1px 0 0 rgba(0,0,0,0.03),
    /* 外层：紧贴阴影（玻璃边缘） */
    0 0 0 0.5px rgba(0,0,0,0.04),
    /* 外层：近距离弥散阴影（浮起感） */
    0 2px 16px rgba(0,0,0,0.05),
    /* 外层：中距离阴影 */
    0 8px 40px rgba(0,0,0,0.06),
    /* 外层：远距离大阴影（深度） */
    0 20px 80px rgba(0,0,0,0.04);
}
/* 玻璃纹理覆盖：强光斑 + 边缘光晕 */
.sidebar::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: var(--z-radius-lg);
  pointer-events: none;
  background:
    /* 左上主光源高光 */
    radial-gradient(ellipse at 25% 8%, rgba(255,255,255,0.2) 0%, transparent 55%),
    /* 右下二次反射光 */
    radial-gradient(ellipse at 85% 92%, rgba(255,255,255,0.08) 0%, transparent 45%),
    /* 顶部环境光 */
    radial-gradient(ellipse at 60% 5%, rgba(255,255,255,0.12) 0%, transparent 40%);
}
/* 窗口控制按钮位置 */
.sidebar :deep(.window-controls) {
  margin-bottom: 20px;
  padding-left: 2px;
  padding-top: 4px;
  margin-top: -4px;
}















.search-box {
  display: flex; align-items: center; gap: 6px; padding: 6px 10px;
  background: var(--z-search-box-bg); border-radius: var(--z-nav-link-radius); margin-bottom: 16px;
  transition: all 0.2s ease; border: 1px solid transparent;
}
.search-box:focus-within { background: var(--z-search-box-bg); border-color: rgba(0,0,0,0.06); }
.search-icon { width: 14px; height: 14px; color: var(--z-section-title-color); opacity: 0.6; flex-shrink: 0; }
.search-box input { border: none; outline: none; background: transparent; font-size: var(--z-nav-link-font-size); color: var(--z-text-primary); width: 100%; font-family: inherit; }
.search-box input::placeholder { color: var(--z-section-title-color); }

.nav-section { margin-top: 8px; padding-top: 8px; border-top: 1px solid rgba(0,0,0,0.05); }
.section-title { padding: 4px 10px 6px; font-size: var(--z-section-title-size); font-weight: 600; color: var(--z-section-title-color); text-transform: uppercase; letter-spacing: 0.5px; }
.nav-list { list-style: none; display: flex; flex-direction: column; gap: 2px; }

.nav-link {
  display: flex; align-items: center; gap: 10px; padding: 7px 10px; border-radius: var(--z-nav-link-radius);
  color: var(--z-text-secondary); text-decoration: none; font-size: var(--z-nav-link-font-size); font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1); position: relative;
}
.nav-link:hover { background: var(--z-search-box-bg); color: var(--z-text-primary); transform: translateX(1px); }
.nav-link.active { background: var(--z-nav-link-active-bg); color: var(--z-accent); font-weight: 600; }
.nav-link.active::before { content:""; position:absolute; left:0; top:50%; transform:translateY(-50%); width:3px; height:16px; background:var(--z-avatar-bg); border-radius:0 2px 2px 0; animation: slideIn 0.25s cubic-bezier(0.4,0,0.2,1); }
@keyframes slideIn { from { transform:translateY(-50%) scaleY(0); opacity:0; } to { transform:translateY(-50%) scaleY(1); opacity:1; } }
.nav-icon { font-size:var(--z-nav-icon-size); width:var(--z-nav-icon-width); text-align:center; opacity:0.8; transition:transform 0.2s ease; }
.nav-link:hover .nav-icon { transform: scale(1.1); }
.nav-link.active .nav-icon { opacity:1; }

.user-profile { margin-top:auto; padding:10px 8px; display:flex; align-items:center; gap:10px; }
.user-avatar { width:28px; height:28px; border-radius:50%; background:var(--z-avatar-bg); display:flex; align-items:center; justify-content:center; color:white; font-size:var(--z-avatar-font-size); font-weight:600; flex-shrink:0; transition:transform 0.2s ease; }
.user-avatar:hover { transform:scale(1.05); }
.user-name { font-size:13px; font-weight:500; color: var(--z-text-secondary); flex:1; }
.logout-btn { width:24px; height:24px; border:none; border-radius:8px; background:transparent; color:var(--z-text-tertiary); cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all 0.15s ease; padding:0; }
.logout-btn svg { width:14px; height:14px; }
.logout-btn:hover { background:rgba(0,0,0,0.06); color:var(--z-danger); transform:scale(1.05); }

/* 收起状态 */
.sidebar.collapsed { padding: 16px 8px 12px; }
.sidebar.collapsed .nav-label,
.sidebar.collapsed .section-title,
.sidebar.collapsed .search-box,
.sidebar.collapsed .user-name,
.sidebar.collapsed .logout-btn,
.sidebar.collapsed .window-controls,

.sidebar.collapsed .user-profile { justify-content: center; }
.sidebar.collapsed .nav-section { border-top: none; margin-top: 0; padding-top: 0; }
</style>
