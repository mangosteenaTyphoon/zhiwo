import { defineStore } from "pinia";
import { ref } from "vue";
import { getUserInfo } from "@/api/user";
import type { CurrentUser } from "@/api/user";

export const useAppStore = defineStore("app", () => {
  const serverUrl = ref<string>(localStorage.getItem("zhiwo_server_url") || "http://localhost:8080");
  const token = ref<string>(localStorage.getItem("zhiwo_token") || "");
  const user = ref<CurrentUser | null>(null);

  function setServerUrl(url: string) {
    serverUrl.value = url;
    localStorage.setItem("zhiwo_server_url", url);
  }

  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem("zhiwo_token", newToken);
  }

  function setUser(userInfo: CurrentUser) {
    user.value = userInfo;
  }

  async function loadUserInfo() {
    if (!token.value) return;
    try {
      const authInfo = await getUserInfo();
      user.value = authInfo.userInfo;
    } catch {
      // 获取失败不阻断，保持 null
    }
  }

  function clearAuth() {
    token.value = "";
    user.value = null;
    localStorage.removeItem("zhiwo_token");
  }

  return {
    serverUrl,
    token,
    user,
    setServerUrl,
    setToken,
    setUser,
    loadUserInfo,
    clearAuth,
  };
});