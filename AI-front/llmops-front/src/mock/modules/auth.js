import Mock from "mockjs";

// 登录
Mock.mock(/\/sys\/login/, "post", () => {
  return {
    code: 200,
    message: "success",
    body: {
      user: {
        id: 1,
        userName: "admin"
      },
      token: Mock.Random.string("abcdefghijklmnopqrstuvwxyz0123456789", 32)
    }
  };
});

// 登出
Mock.mock(/\/sys\/logout/, "get", () => {
  return {
    code: 200,
    message: "success"
  };
});
