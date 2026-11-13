import Mock from "mockjs";

const imagesList = [
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 10,
      name: "文本处理",
      describe: "文本处理相关功能",
      type: "job-template",
      expand: '{\n    "index": 5.4\n}'
    },
    images_url: "python3.9",
    creator: "admin",
    modified: "10 days ago",
    id: 29
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 18,
      name: "模型服务化",
      describe: "模型服务化部署相关的组件模板",
      type: "job-template",
      expand: '{\n    "index": 14\n}'
    },
    images_url: "docker-registry:81/public/llama.cpp:server",
    creator: "admin",
    modified: "14 days ago",
    id: 28
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 12,
      name: "机器学习算法",
      describe: "传统机器学习，lr/决策树/gbdt/xgb/fm等",
      type: "job-template",
      expand: '{\n    "index": 7\n}'
    },
    images_url: "docker-registry:81/cube-studio/decision-tree:20260312",
    creator: "admin",
    modified: "14 days ago",
    id: 27
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 2,
      name: "基础命令",
      describe: "python/bash等直接在服务器命令行中执行命令的模板",
      type: "job-template",
      expand: '{\n    "index": 1\n}'
    },
    images_url: "docker-registry:81/harbor-cube-studio/deploy-service:20240601",
    creator: "admin",
    modified: "14 days ago",
    id: 26
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 26,
      name: "大模型",
      describe: "大模型相关模板",
      type: "job-template",
      expand: '{\n    "index": 22\n}'
    },
    images_url: "docker-registry:81/harbor-cube-studio/llamafactory:new",
    creator: "admin",
    modified: "21 days ago",
    id: 22
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 4,
      name: "数据预处理",
      describe: "结构化话数据特征处理",
      type: "job-template",
      expand: '{\n    "index": 3\n}'
    },
    images_url: "docker-registry:81/harbor-cube-studio/data-clean:20260302",
    creator: "admin",
    modified: "25 days ago",
    id: 21
  },
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-06T21:51:44",
      id: 2,
      name: "基础命令",
      describe: "python/bash等直接在服务器命令行中执行命令的模板",
      type: "job-template",
      expand: '{\n    "index": 1\n}'
    },
    images_url: "hiyouga/llamafactory:latest",
    creator: "admin",
    modified: "27 days ago",
    id: 18
  }
];

Mock.mock(/\/images_modelview\/api/, "get", () => {
  return {
    result: {
      data: imagesList
    }
  };
});

Mock.mock(/\/llmops\/image\/list/, "get", (options) => {
  return {
    code: 200,
    message: "成功",
    data: imagesList
  };
});

Mock.mock(/\/llmops\/image$/, "post", (options) => {
  const data = JSON.parse(options.body);
  const newImage = {
    id: Mock.Random.natural(100, 999),
    project: {
      created_on: new Date().toISOString(),
      changed_on: new Date().toISOString(),
      id: Mock.Random.natural(100, 999),
      name: (data.project && data.project.name) || data.name || '新镜像',
      describe: (data.project && data.project.describe) || data.description || '',
      type: 'job-template',
      expand: (data.project && data.project.expand) || '{}'
    },
    images_url: data.images_url || data.name || '',
    creator: data.creator || 'admin',
    modified: '刚刚',
    category: data.category || '基础命令'
  };
  imagesList.unshift(newImage);
  return {
    code: 200,
    message: "创建成功",
    data: newImage
  };
});

Mock.mock(/\/llmops\/image$/, "put", (options) => {
  const data = JSON.parse(options.body);
  const index = imagesList.findIndex(item => item.id === data.id);
  if (index !== -1) {
    imagesList[index] = { 
      ...imagesList[index], 
      ...data,
      project: {
        ...imagesList[index].project,
        ...data.project,
        name: (data.project && data.project.name) || data.name || imagesList[index].project.name,
        describe: (data.project && data.project.describe) || data.description || imagesList[index].project.describe,
        changed_on: new Date().toISOString()
      },
      modified: '刚刚'
    };
  }
  return {
    code: 200,
    message: "更新成功"
  };
});

Mock.mock(/\/llmops\/image\/\d+/, "delete", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 1]);
  const index = imagesList.findIndex(item => item.id === id);
  if (index !== -1) {
    imagesList.splice(index, 1);
  }
  return {
    code: 200,
    message: "删除成功"
  };
});

Mock.mock(/\/llmops\/image\/\d+/, "get", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 1]);
  const image = imagesList.find(item => item.id === id);
  return {
    code: 200,
    message: "成功",
    data: image || null
  };
});

const repositoryList = [
  {
    project: {
      created_on: "2026-01-06T21:51:44",
      changed_on: "2026-01-15T14:32:00",
      id: 1,
      name: "docker-hub",
      describe: "官方 Docker Hub 镜像仓库",
      type: "repository",
      expand: '{\n    "type": "public"\n}'
    },
    url: "https://hub.docker.com",
    secret: "docker-hub-secret",
    username: "admin",
    password: "",
    creator: "admin",
    modified: "10 days ago",
    id: 1
  },
  {
    project: {
      created_on: "2026-01-10T10:20:00",
      changed_on: "2026-02-01T09:15:00",
      id: 2,
      name: "private-registry",
      describe: "企业私有镜像仓库",
      type: "repository",
      expand: '{\n    "type": "private"\n}'
    },
    url: "https://registry.company.com",
    secret: "private-registry-secret",
    username: "registry-admin",
    password: "",
    creator: "李四",
    modified: "14 days ago",
    id: 2
  },
  {
    project: {
      created_on: "2026-01-20T16:45:00",
      changed_on: "2026-03-01T11:30:00",
      id: 3,
      name: "harbor-registry",
      describe: "Harbor 企业级镜像仓库",
      type: "repository",
      expand: '{\n    "type": "harbor"\n}'
    },
    url: "https://harbor.company.com",
    secret: "harbor-registry-secret",
    username: "harbor-user",
    password: "",
    creator: "王五",
    modified: "5 days ago",
    id: 3
  },
  {
    project: {
      created_on: "2026-02-05T08:30:00",
      changed_on: "2026-03-10T15:45:00",
      id: 4,
      name: "aliyun-registry",
      describe: "阿里云容器镜像服务",
      type: "repository",
      expand: '{\n    "type": "aliyun"\n}'
    },
    url: "https://registry.cn-hangzhou.aliyuncs.com",
    secret: "aliyun-registry-secret",
    username: "aliyun-user",
    password: "",
    creator: "赵六",
    modified: "2 days ago",
    id: 4
  }
];

Mock.mock(/\/repository_modelview\/api/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: repositoryList
  };
});

Mock.mock(/\/llmops\/repository/, "post", (options) => {
  const data = JSON.parse(options.body);
  const newRepository = {
    id: Mock.Random.natural(100, 999),
    project: {
      created_on: new Date().toISOString(),
      changed_on: new Date().toISOString(),
      id: Mock.Random.natural(100, 999),
      name: (data.project && data.project.name) || data.name || '新仓库',
      describe: (data.project && data.project.describe) || data.description || '',
      type: 'repository',
      expand: (data.project && data.project.expand) || '{}'
    },
    url: data.url || '',
    secret: data.secret || '',
    username: data.username || '',
    password: data.password || '',
    creator: data.creator || 'admin',
    modified: '刚刚'
  };
  repositoryList.unshift(newRepository);
  return {
    code: 200,
    message: "创建成功",
    data: newRepository
  };
});

Mock.mock(/\/llmops\/repository/, "put", (options) => {
  const data = JSON.parse(options.body);
  const index = repositoryList.findIndex(item => item.id === data.id);
  if (index !== -1) {
    repositoryList[index] = { 
      ...repositoryList[index], 
      ...data,
      project: {
        ...repositoryList[index].project,
        ...data.project,
        name: (data.project && data.project.name) || data.name || repositoryList[index].project.name,
        describe: (data.project && data.project.describe) || data.description || repositoryList[index].project.describe,
        changed_on: new Date().toISOString()
      },
      modified: '刚刚'
    };
  }
  return {
    code: 200,
    message: "更新成功"
  };
});

Mock.mock(/\/llmops\/repository\/\d+/, "delete", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 1]);
  const index = repositoryList.findIndex(item => item.id === id);
  if (index !== -1) {
    repositoryList.splice(index, 1);
  }
  return {
    code: 200,
    message: "删除成功"
  };
});
