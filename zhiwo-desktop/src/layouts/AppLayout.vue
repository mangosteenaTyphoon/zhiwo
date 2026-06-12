<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import SidebarNav from "./SidebarNav.vue";
</script>

<style scoped>
.app-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

.main-content {
  flex: 1;
  overflow: auto;
  padding: var(--z-space-2xl);
}
</style>