<template>
  <nav class="sidebar">
    <div class="sidebar-brand">
      <span class="brand-name">知我</span>
    </div>
    <ul class="nav-list">
      <li v-for="item in navItems" :key="item.path">
        <router-link :to="item.path" class="nav-link" active-class="active">
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";

const navItems = computed(() => [
  { path: "/today", label: "今日", icon: "◎" },
  { path: "/goals", label: "目标", icon: "◉" },
  { path: "/tasks", label: "任务", icon: "▢" },
]);
</script>

<style scoped>
.sidebar {
  width: var(--z-sidebar-width);
  height: 100%;
  background: var(--z-sidebar-bg);
  backdrop-filter: blur(var(--z-sidebar-blur));
  -webkit-backdrop-filter: blur(var(--z-sidebar-blur));
  border-right: 1px solid var(--z-border);
  display: flex;
  flex-direction: column;
  padding: var(--z-space-xl) var(--z-space-lg);
}

.sidebar-brand {
  margin-bottom: var(--z-space-2xl);
  padding-left: var(--z-space-sm);
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--z-text-primary);
}

.nav-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--z-space-xs);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--z-space-md);
  padding: var(--z-space-md) var(--z-space-lg);
  border-radius: var(--z-radius-md);
  color: var(--z-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--z-transition-fast);
}

.nav-link:hover {
  background: rgba(0, 0, 0, 0.04);
  color: var(--z-text-primary);
}

.nav-link.active {
  background: var(--z-accent-light);
  color: var(--z-accent);
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}
</style>