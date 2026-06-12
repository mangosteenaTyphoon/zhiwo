import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { useAppStore } from "@/stores/app";

const service = axios.create({
  timeout: 50000,
  headers: {
    "Content-Type": "application/json;charset=utf-8",
    "Client-Type": "desktop",
  },
});

service.interceptors.request.use((config) => {
  const appStore = useAppStore();
  config.baseURL = appStore.serverUrl;
  if (appStore.token) {
    config.headers["Authorization"] = "Bearer " + appStore.token;
  }
  return config;
});

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const data = response.data;
    if (data.code !== 0) {
      if (data.code === 401) {
        const appStore = useAppStore();
        appStore.clearAuth();
        window.location.href = "/login";
      }
      return Promise.reject(new Error(data.msg || "请求失败"));
    }
    return data.data;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>;
}