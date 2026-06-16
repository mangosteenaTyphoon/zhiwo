<template>
  <div class="window-controls" :class="{ 'light': light }">
    <button class="window-btn close" @click="closeWindow" title="关闭"></button>
    <button class="window-btn minimize" @click="minimizeWindow" title="最小化"></button>
    <button class="window-btn maximize" @click="maximizeWindow" title="最大化"></button>
  </div>
</template>

<script setup lang="ts">
import { invoke } from "@tauri-apps/api/core";

defineProps<{
  light?: boolean;
}>();

function closeWindow() {
  invoke("win_close");
}

function minimizeWindow() {
  invoke("win_minimize");
}

function maximizeWindow() {
  invoke("win_toggle_maximize");
}
</script>

<style scoped>
.window-controls {
  display: flex;
  gap: 8px;
  z-index: 100;
}

.window-btn {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.window-btn::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.window-btn:hover::before {
  opacity: 1;
}

/* 关闭按钮 */
.window-btn.close {
  background: #ff5f57;
}

.window-btn.close:hover {
  background: #ff4444;
}

.window-btn.close::before {
  background: rgba(0, 0, 0, 0.2);
}

/* 最小化按钮 */
.window-btn.minimize {
  background: #febc2e;
}

.window-btn.minimize:hover {
  background: #ffaa00;
}

.window-btn.minimize::before {
  background: rgba(0, 0, 0, 0.2);
}

/* 最大化按钮 */
.window-btn.maximize {
  background: #28c840;
}

.window-btn.maximize:hover {
  background: #00d000;
}

.window-btn.maximize::before {
  background: rgba(0, 0, 0, 0.2);
}

/* 亮色模式（用于深色背景上） */
.window-controls.light .window-btn.close {
  background: rgba(255, 95, 87, 0.8);
}

.window-controls.light .window-btn.close:hover {
  background: #ff5f57;
}

.window-controls.light .window-btn.minimize {
  background: rgba(254, 188, 46, 0.8);
}

.window-controls.light .window-btn.minimize:hover {
  background: #febc2e;
}

.window-controls.light .window-btn.maximize {
  background: rgba(40, 200, 64, 0.8);
}

.window-controls.light .window-btn.maximize:hover {
  background: #28c840;
}
</style>
