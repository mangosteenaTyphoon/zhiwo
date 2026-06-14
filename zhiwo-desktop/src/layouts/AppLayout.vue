<template>
  <div class="app-layout" @mousedown="onDragStart">
    <!-- 四边调整大小手柄 -->
    <div class="resize-handle top" @mousedown.stop="startResize('n')"></div>
    <div class="resize-handle bottom" @mousedown.stop="startResize('s')"></div>
    <div class="resize-handle left" @mousedown.stop="startResize('w')"></div>
    <div class="resize-handle right" @mousedown.stop="startResize('e')"></div>
    <div class="resize-handle corner top-left" @mousedown.stop="startResize('nw')"></div>
    <div class="resize-handle corner top-right" @mousedown.stop="startResize('ne')"></div>
    <div class="resize-handle corner bottom-left" @mousedown.stop="startResize('sw')"></div>
    <div class="resize-handle corner bottom-right" @mousedown.stop="startResize('se')"></div>

    <SidebarNav />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { getCurrentWindow } from "@tauri-apps/api/window";
import { invoke } from "@tauri-apps/api/core";
import SidebarNav from "./SidebarNav.vue";

const appWindow = getCurrentWindow();

// === 窗口拖拽（通过 Rust drag_move 命令） ===
let dragging = false;
let startScrX = 0;
let startScrY = 0;
let startWinX = 0;
let startWinY = 0;

async function onDragStart(e: MouseEvent) {
  const el = e.target as HTMLElement;
  if (el.closest("button, a, input, textarea, .resize-handle")) return;

  const [wx, wy] = (await invoke("get_pos")) as [number, number];
  dragging = true;
  startScrX = e.screenX;
  startScrY = e.screenY;
  startWinX = wx;
  startWinY = wy;
  document.body.style.cursor = "grabbing";
  window.addEventListener("mousemove", onMove);
  window.addEventListener("mouseup", onUp);
}

function onMove(e: MouseEvent) {
  if (!dragging) return;
  const dx = e.screenX - startScrX;
  const dy = e.screenY - startScrY;
  invoke("drag_move", { x: startWinX + dx, y: startWinY + dy });
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
  display: flex;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  overflow: hidden;
  background: #f0f0f2;
  padding: 8px;
  gap: 4px;
  position: relative;
}

.main-content {
  flex: 1;
  overflow: auto;
  background: #ffffff;
  border-radius: 16px;
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
