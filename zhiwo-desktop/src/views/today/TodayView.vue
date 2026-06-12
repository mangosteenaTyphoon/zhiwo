<template>
  <div class="today-view">
    <h1 class="page-title">今日</h1>
    <p class="page-desc">今天需要推进的任务</p>
  </div>
</template>

<script setup lang="ts">
// 今日工作台 - 占位
</script>

<style scoped>
.today-view {
  max-width: 960px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
  margin-bottom: var(--z-space-sm);
}

.page-desc {
  font-size: 14px;
  color: var(--z-text-secondary);
}
</style>