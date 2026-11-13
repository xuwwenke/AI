import Mock from "mockjs";

/**
 * menuIds 勾选的菜单或权限项，-1 作为分隔符，其右边的 id 都为半选状态
 */
let roleList = [
  {
    id: 0,
    name: "管理员",
    description: "有系统管理权限",
    insertTime: "2017-01-01 01:11:11",
    menuIds: [
      2,
      3,
      4,
      1,
      11,
      111,
      112,
      113,
      114,
      12,
      121,
      122,
      123,
      124,
      13,
      131,
      132,
      133,
      134,
      -1
    ]
  },
  {
    id: 1,
    name: "操作员",
    description: "有操作权限",
    insertTime: "2017-02-02 02:22:22",
    menuIds: [2, 3, 111, 113, 121, 123, 131, 133, -1, 1, 11, 12, 13]
  },
  {
    id: 2,
    name: "观察员",
    description: "有查看监测权限",
    insertTime: "2017-03-03 03:33:33",
    menuIds: [2, 3, 111, 121, 131, -1, 1, 11, 12, 13]
  }
];

// 角色列表
Mock.mock(/\/sys\/role\/list/, "get", () => {
  return {
    code: 200,
    message: "success",
    body: {
      totalCount: 3,
      limit: 10,
      totalPage: 1,
      currPage: 1,
      list: roleList.map(item => {
        let tmp = { ...item };
        delete tmp.menuIds;
        return tmp;
      })
    }
  };
});

// 角色选项
Mock.mock(/\/sys\/role\/select/, "get", () => {
  return {
    code: 200,
    message: "success",
    body: roleList.map(item => {
      let tmp = { ...item };
      delete tmp.description;
      delete tmp.insertTime;
      delete tmp.menuIds;
      return tmp;
    })
  };
});

// 角色信息
Mock.mock(/\/sys\/role\/info/, "get", o => {
  const tmp = o.url.split("?")[0].split("/");
  const id = tmp[tmp.length - 1];
  const role = roleList.find(item => item.id == id);
  return {
    code: 200,
    message: "success",
    body: role
  };
});

// 角色新增
Mock.mock(/\/sys\/role\/insert/, "post", o => {
  const tmp = JSON.parse(o.body);
  tmp.id = Mock.Random.natural(5, 100);
  tmp.insertTime = Mock.Random.datetime("yyyy-MM-dd HH:mm:ss");
  roleList.push(tmp);
  return {
    code: 200,
    message: "success"
  };
});

// 角色修改
Mock.mock(/\/sys\/role\/update/, "put", o => {
  const tmp = JSON.parse(o.body);
  const index = roleList.findIndex(item => item.id == tmp.id);
  roleList[index] = { ...roleList[index], ...tmp };
  return {
    code: 200,
    message: "success"
  };
});

// 角色删除
Mock.mock(/\/sys\/role\/delete/, "delete", o => {
  const ids = o.body;
  roleList = roleList.filter(item => ids.indexOf(item.id) == -1);
  return {
    code: 200,
    message: "success"
  };
});
