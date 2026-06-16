<template>
  <div class="app-layout" :style="{ '--sidebar-w': sidebarWidth + 'px' }">
    <!-- 四边调整大小手柄 -->
    <div class="resize-handle top" @mousedown.stop="startResize('n')"></div>
    <div class="resize-handle bottom" @mousedown.stop="startResize('s')"></div>
    <div class="resize-handle left" @mousedown.stop="startResize('w')"></div>
    <div class="resize-handle right" @mousedown.stop="startResize('e')"></div>
    <div class="resize-handle corner top-left" @mousedown.stop="startResize('nw')"></div>
    <div class="resize-handle corner top-right" @mousedown.stop="startResize('ne')"></div>
    <div class="resize-handle corner bottom-left" @mousedown.stop="startResize('sw')"></div>
    <div class="resize-handle corner bottom-right" @mousedown.stop="startResize('se')"></div>

    <div class="sidebar-wrapper" @mousedown="onDragStart">
      <SidebarNav :collapsed="collapsed" />
    </div>
    <!-- 分隔条：可拖拽调整宽度 + 点击收起 -->
    <div
      class="sidebar-divider"
      @mousedown.stop="onDividerDown"
      @dblclick="toggleCollapse"
    >
      <button class="collapse-btn" @click.stop="toggleCollapse" :title="collapsed ? '展开导航' : '收起导航'">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline v-if="collapsed" points="10,3 6,8 10,13" />
          <polyline v-else points="6,3 10,8 6,13" />
        </svg>
      </button>
    </div>
    <main class="main-content">
      <div class="main-drag-bar" @mousedown="onDragStart"></div>
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getCurrentWindow } from "@tauri-apps/api/window";
import { invoke } from "@tauri-apps/api/core";
import SidebarNav from "./SidebarNav.vue";

const appWindow = getCurrentWindow();

// === 侧边栏宽度调整 ===
const sidebarWidth = ref(220);
const collapsed = ref(false);
const lastWidth = ref(220);
const MIN_WIDTH = 160;
const MAX_WIDTH = 360;
const COLLAPSED_WIDTH = 56;

function toggleCollapse() {
  if (collapsed.value) {
    sidebarWidth.value = lastWidth.value;
    collapsed.value = false;
  } else {
    lastWidth.value = sidebarWidth.value;
    collapsed.value = true;
    sidebarWidth.value = COLLAPSED_WIDTH;
  }
}

// 分隔条拖拽调整宽度
let resizing = false;
function onDividerDown(e: MouseEvent) {
  resizing = true;
  const startX = e.clientX;
  const startW = sidebarWidth.value;
  document.body.style.cursor = "col-resize";
  document.body.style.userSelect = "none";
  window.addEventListener("mousemove", onDividerMove);
  window.addEventListener("mouseup", onDividerUp);

  function onDividerMove(ev: MouseEvent) {
    if (!resizing) return;
    const newW = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, startW + ev.clientX - startX));
    sidebarWidth.value = newW;
    if (newW > COLLAPSED_WIDTH + 20) collapsed.value = false;
  }
  function onDividerUp() {
    resizing = false;
    document.body.style.cursor = "";
    document.body.style.userSelect = "";
    window.removeEventListener("mousemove", onDividerMove);
    window.removeEventListener("mouseup", onDividerUp);
  }
}

// === 窗口拖拽 ===
let dragging = false;
let startScrX = 0, startScrY = 0, startWinX = 0, startWinY = 0;

async function onDragStart(e: MouseEvent) {
  const el = e.target as HTMLElement;
  if (el.closest("button, a, input, select, .sidebar-divider, .collapse-btn")) return;
  const [wx, wy] = (await invoke("get_pos")) as [number, number];
  dragging = true;
  startScrX = e.screenX; startScrY = e.screenY;
  startWinX = wx; startWinY = wy;
  document.body.style.cursor = "grabbing";
  window.addEventListener("mousemove", onMove);
  window.addEventListener("mouseup", onUp);
}

function onMove(e: MouseEvent) {
  if (!dragging) return;
  invoke("drag_move", { x: startWinX + e.screenX - startScrX, y: startWinY + e.screenY - startScrY });
}

function onUp() {
  dragging = false;
  document.body.style.cursor = "";
  window.removeEventListener("mousemove", onMove);
  window.removeEventListener("mouseup", onUp);
}

// === 调整大小 ===
function startResize(direction: string) {
  appWindow.startResizeDragging(direction as any);
}
</script>

<style scoped>
.app-layout {
  --sidebar-w: 220px;
  display: flex;
  width: 100%;
  height: 100%;
  border-radius: var(--z-window-radius);
  overflow: hidden;
  padding: 8px;
  gap: 0;
  position: relative;
  background:
    radial-gradient(ellipse at 10% 20%, rgba(120,160,255,0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 90% 80%, rgba(255,160,160,0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(160,255,200,0.05) 0%, transparent 60%),
    linear-gradient(180deg, #f2f2f5 0%, #ebebf0 100%);
}

/* 侧边栏 wrapper —— 只有这个区域可拖拽窗口 */
.sidebar-wrapper {
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  overflow: auto;
  background: #ffffff;
  border-radius: 16px;
  padding: var(--z-space-2xl);
  clip-path: inset(0 round 16px);
  position: relative;
}

/* 主内容区顶部隐形拖拽条 —— 可拖拽窗口 */
.main-drag-bar {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 20px;
  z-index: 10;
  cursor: default;
}

/* 分隔条 */
.sidebar-divider {
  width: 6px;
  flex-shrink: 0;
  cursor: col-resize;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
}
.sidebar-divider:hover,
.sidebar-divider:active {
  background: rgba(0,0,0,0.03);
}
.collapse-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid rgba(0,0,0,0.1);
  background: rgba(255,255,255,0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s ease;
  z-index: 10;
}
.sidebar-divider:hover .collapse-btn {
  opacity: 1;
}
.collapse-btn svg {
  width: 12px;
  height: 12px;
  color: var(--z-text-secondary);
}
.collapse-btn:hover {
  background: #fff;
  border-color: rgba(0,0,0,0.2);
}

.resize-handle { position: absolute; z-index: 50; }
.resize-handle.top { top: 0; left: 12px; right: 12px; height: 4px; cursor: ns-resize; }
.resize-handle.bottom { bottom: 0; left: 12px; right: 12px; height: 4px; cursor: ns-resize; }
.resize-handle.left { left: 0; top: 12px; bottom: 12px; width: 4px; cursor: ew-resize; }
.resize-handle.right { right: 0; top: 12px; bottom: 12px; width: 4px; cursor: ew-resize; }
.resize-handle.corner { width: 12px; height: 12px; }
.resize-handle.corner.top-left { top: 0; left: 0; cursor: nwse-resize; }
.resize-handle.corner.top-right { top: 0; right: 0; cursor: nesw-resize; }
.resize-handle.corner.bottom-left { bottom: 0; left: 0; cursor: nesw-resize; }
.resize-handle.corner.bottom-right { bottom: 0; right: 0; cursor: nwse-resize; }
</style>
