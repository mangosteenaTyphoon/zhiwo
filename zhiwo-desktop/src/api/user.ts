import request from "./request";

export interface CurrentUser {
  id: string;
  username: string;
  nickname: string;
  avatar?: string;
}

export interface AuthInfo {
  userInfo: CurrentUser;
}

export function getUserInfo(): Promise<AuthInfo> {
  return request<AuthInfo>({
    url: "/system/info",
    method: "get",
  });
}
