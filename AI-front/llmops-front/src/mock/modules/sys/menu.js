import Mock from "mockjs";

/**
 * id 编号
 * parent 父级
 *  id
 *  name
 * name 名称
 * url 路由地址（菜单） 或 请求地址（权限）
 * httpMethod 请求方式
 * layout （可选） 将采用布局容器进行页面渲染
 *  i 编号
 *  x 左上角横向位置
 *  y 左上角纵向位置
 *  w 横向占用格子数
 *  h 纵向占用格子数
 *  title 卡片标题
 *  component 图表组件
 *  dataUrl 数据地址
 * permissionKeyword 所需权限
 * type 类型 0 目录 1 菜单 2 权限
 * icon 图标
 * seq 同级排序
 */
let menuList = [
  {
    id: 2,
    parent: {
      id: 0,
      name: "一级菜单"
    },
    name: "带查询表格",
    url: "qtable",
    httpMethod: null,
    layout: [
      {
        i: "0",
        x: 0,
        y: 0,
        w: 8,
        h: 20,
        title: "带查询带操作的表格",
        component: "qtable",
        dataUrl: "/qtableData/list"
      },
      {
        i: "1",
        x: 8,
        y: 0,
        w: 4,
        h: 20,
        title: "带查询的表格",
        component: "qtable",
        dataUrl: "/qtableData/2list"
      }
    ],
    permissionKeyword: null,
    type: 1,
    icon: "tubiao",
    seq: 0
  },
  {
    id: 3,
    parent: {
      id: 0,
      name: "一级菜单"
    },
    name: "带查询表格2",
    url: "qtable2",
    httpMethod: null,
    layout: [
      {
        i: "0",
        x: 0,
        y: 0,
        w: 6,
        h: 20,
        title: "带查询的表格",
        component: "qtable",
        dataUrl: "/qtableData/2list"
      },
      {
        i: "1",
        x: 6,
        y: 0,
        w: 6,
        h: 20,
        title: "带查询的表格",
        component: "qtable",
        dataUrl: "/qtableData/2list"
      }
    ],
    permissionKeyword: null,
    type: 1,
    icon: "tubiao",
    seq: 0
  },
  {
    id: 4,
    parent: {
      id: 0,
      name: "一级菜单"
    },
    name: "仪表盘",
    url: "dashboard",
    httpMethod: null,
    permissionKeyword: null,
    type: 1,
    icon: "zonghe",
    seq: 0
  },
  {
    id: 1,
    parent: {
      id: 0,
      name: "一级菜单"
    },
    name: "系统管理",
    url: null,
    httpMethod: null,
    permissionKeyword: null,
    type: 0,
    icon: "system",
    seq: 0
  },
  {
    id: 11,
    parent: {
      id: 1,
      name: "系统管理"
    },
    name: "菜单管理",
    url: "sys/menu",
    httpMethod: null,
    permissionKeyword: null,
    type: 1,
    icon: "menu",
    seq: 1
  },
  {
    id: 12,
    parent: {
      id: 1,
      name: "系统管理"
    },
    name: "角色管理",
    url: "sys/role",
    httpMethod: null,
    permissionKeyword: null,
    type: 1,
    icon: "role",
    seq: 2
  },
  {
    id: 13,
    parent: {
      id: 1,
      name: "系统管理"
    },
    name: "用户列表",
    url: "sys/user",
    httpMethod: null,
    permissionKeyword: null,
    type: 1,
    icon: "admin",
    seq: 3
  },
  {
    id: 111,
    parent: {
      id: 11,
      name: "菜单管理"
    },
    name: "查看",
    url: null,
    httpMethod: "GET",
    permissionKeyword: "sys:menu:list,sys:menu:info",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 112,
    parent: {
      id: 11,
      name: "菜单管理"
    },
    name: "新增",
    url: null,
    httpMethod: "POST",
    permissionKeyword: "sys:menu:insert,sys:menu:select",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 113,
    parent: {
      id: 11,
      name: "菜单管理"
    },
    name: "修改",
    url: null,
    httpMethod: "PUT",
    permissionKeyword: "sys:menu:update,sys:menu:select",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 114,
    parent: {
      id: 11,
      name: "菜单管理"
    },
    name: "删除",
    url: null,
    httpMethod: "DELETE",
    permissionKeyword: "sys:menu:delete",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 121,
    parent: {
      id: 12,
      name: "角色管理"
    },
    name: "查看",
    url: null,
    httpMethod: "GET",
    permissionKeyword: "sys:role:list,sys:role:info",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 122,
    parent: {
      id: 12,
      name: "角色管理"
    },
    name: "新增",
    url: null,
    httpMethod: "POST",
    permissionKeyword: "sys:role:insert,sys:menu:list",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 123,
    parent: {
      id: 12,
      name: "角色管理"
    },
    name: "修改",
    url: null,
    httpMethod: "PUT",
    permissionKeyword: "sys:role:update,sys:menu:list",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 124,
    parent: {
      id: 12,
      name: "角色管理"
    },
    name: "删除",
    url: null,
    httpMethod: "DELETE",
    permissionKeyword: "sys:role:delete",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 131,
    parent: {
      id: 13,
      name: "用户列表"
    },
    name: "查看",
    url: null,
    httpMethod: "GET",
    permissionKeyword: "sys:user:list,sys:user:info",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 132,
    parent: {
      id: 13,
      name: "用户列表"
    },
    name: "新增",
    url: null,
    httpMethod: "POST",
    permissionKeyword: "sys:user:insert,sys:role:select",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 133,
    parent: {
      id: 13,
      name: "用户列表"
    },
    name: "修改",
    url: null,
    httpMethod: "PUT",
    permissionKeyword: "sys:user:update,sys:role:select",
    type: 2,
    icon: null,
    seq: 0
  },
  {
    id: 134,
    parent: {
      id: 13,
      name: "用户列表"
    },
    name: "删除",
    url: null,
    httpMethod: "DELETE",
    permissionKeyword: "sys:user:delete",
    type: 2,
    icon: null,
    seq: 0
  }
];

let navMenuList = [];

menuList.map(item => {
  if (item.parent.id == 0) {
    item.children = menuList.filter(it => it.parent.id == item.id);
    navMenuList.push(item);
  }
});

// 完整菜单
Mock.mock(/\/sys\/menu\/list/, "get", () => {
  return {
    code: 200,
    message: "成功",
    body: menuList
  };
});

// 菜单选项
Mock.mock(/\/sys\/menu\/select/, "get", () => {
  let tmp = menuList.filter(item => item.type == 0 || item.type == 1);
  tmp.push({
    id: 0,
    parent: {
      id: -1,
      name: null
    },
    name: "一级菜单",
    url: null,
    permissionKeyword: null,
    type: null,
    icon: null,
    seq: null
  });
  return {
    code: 200,
    message: "成功",
    body: tmp
  };
});

// 菜单和权限
Mock.mock(/\/sys\/menu\/nav/, "get", () => {
  return {
    code: 200,
    message: "成功",
    body: {
      menuList: navMenuList,
      permissions: [
        "sys:menu:update",
        "sys:menu:delete",
        "sys:menu:list",
        "sys:user:delete",
        "sys:user:update",
        "sys:role:list",
        "sys:menu:info",
        "sys:menu:select",
        "sys:role:select",
        "sys:user:list",
        "sys:menu:insert",
        "sys:role:insert",
        "sys:role:info",
        "sys:role:update",
        "sys:user:info",
        "sys:role:delete",
        "sys:user:insert"
      ]
    },
    timestamp: 1537438216447
  };
});

// 菜单信息
/* Mock.mock(/\/sys\/menu\/info/, "get", o => {
  const tmp = o.url.split("?")[0].split("/");
  const id = tmp[tmp.length - 1];
  const menu = menuList.find(item => item.id == id);
  return {
    code: 200,
    message: "success",
    body: menu
  };
}); */

// 菜单新增
Mock.mock(/\/sys\/menu\/insert/, "post", o => {
  const tmp = JSON.parse(o.body);
  tmp.id = Mock.Random.natural(5, 100);
  menuList.push(tmp);
  return {
    code: 200,
    message: "success"
  };
});

// 菜单修改
Mock.mock(/\/sys\/menu\/update/, "put", o => {
  const tmp = JSON.parse(o.body);
  const index = menuList.findIndex(item => item.id == tmp.id);
  menuList[index] = { ...menuList[index], ...tmp };
  return {
    code: 200,
    message: "success"
  };
});

// 菜单删除
Mock.mock(/\/sys\/menu\/delete/, "get", o => {
  const tmp = o.url.split("?")[0].split("/");
  const id = tmp[tmp.length - 1];
  menuList = menuList.filter(item => item.id != id);
  return {
    code: 200,
    message: "success"
  };
});
