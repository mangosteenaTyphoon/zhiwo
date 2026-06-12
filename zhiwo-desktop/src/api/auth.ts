import request from "./request";

export interface LoginParams {
  username: string;
  password: string;
  captchaVerification: string;
}

export function login(params: LoginParams): Promise<string> {
  return request<string>({
    url: "/system/login",
    method: "post",
    data: params,
  });
}

export function logout(): Promise<unknown> {
  return request({
    url: "/logout",
    method: "post",
  });
}