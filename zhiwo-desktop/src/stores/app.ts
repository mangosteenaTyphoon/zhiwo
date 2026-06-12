import { defineStore } from "pinia";
import { ref } from "vue";

export const useAppStore = defineStore("app", () => {
  const serverUrl = ref<string>(localStorage.getItem("zhiwo_server_url") || "http://localhost:9999");
  const token = ref<string>(localStorage.getItem("zhiwo_token") || "");

  function setServerUrl(url: string) {
    serverUrl.value = url;
    localStorage.setItem("zhiwo_server_url", url);
  }

  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem("zhiwo_token", newToken);
  }

  function clearAuth() {
    token.value = "";
    localStorage.removeItem("zhiwo_token");
  }

  return {
    serverUrl,
    token,
    setServerUrl,
    setToken,
    clearAuth,
  };
});