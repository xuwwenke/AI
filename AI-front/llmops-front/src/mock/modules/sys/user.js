import Mock from "mockjs";

/**
 * roleIds 用户扮演的多个角色
 */
let userList = [
  {
    id: 1,
    userName: "admin",
    email: "admin@admin.com",
    tel: "13512345678",
    isValid: 0,
    roleIds: [0],
    insertTime: "2018-01-01 01:11:11"
  },
  {
    id: 2,
    userName: "cary",
    email: "cary@123.com",
    tel: "13012345678",
    isValid: 1,
    roleIds: [1, 2],
    insertTime: "2018-02-02 22:22:22"
  }
];

// 用户列表
Mock.mock(/\/sys\/user\/list/, "get", () => {
  return {
    code: 200,
    message: "success",
    body: {
      totalCount: 2,
      limit: 10,
      totalPage: 1,
      currPage: 1,
      list: userList
    }
  };
});

// 用户信息
/* Mock.mock(/\/sys\/user\/info/, "get", o => {
  const tmp = o.url.split("?")[0].split("/");
  const id = tmp[tmp.length - 1];
  const user = userList.find(item => item.id == id);
  return {
    code: 200,
    message: "success",
    body: user
  };
}); */

// 用户新增
Mock.mock(/\/sys\/user\/insert/, "post", o => {
  const tmp = JSON.parse(o.body);
  tmp.id = Mock.Random.natural(5, 100);
  tmp.insertTime = Mock.Random.datetime("yyyy-MM-dd HH:mm:ss");
  userList.push(tmp);
  return {
    code: 200,
    message: "success"
  };
});

// 用户修改
Mock.mock(/\/sys\/user\/update/, "put", o => {
  const tmp = JSON.parse(o.body);
  const index = userList.findIndex(item => item.id == tmp.id);
  userList[index] = { ...userList[index], ...tmp };
  return {
    code: 200,
    message: "success"
  };
});

// 用户删除
Mock.mock(/\/sys\/user\/delete/, "delete", o => {
  const ids = o.body;
  userList = userList.filter(item => ids.indexOf(item.id) == -1);
  return {
    code: 200,
    message: "success"
  };
});
