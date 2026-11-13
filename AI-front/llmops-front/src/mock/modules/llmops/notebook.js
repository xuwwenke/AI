import Mock from "mockjs";

const notebookList = [
  {
    id: 1,
    name: 'dev-zhangsan-01',
    ideType: 'VSCode',
    project: 'AI研发组',
    projectGroup: 'AI Lab',
    status: 'running',
    description: '用于PyTorch模型开发',
    creator: '张三',
    resource: ['4xV100', '32核CPU', '256G'],
    thread: '32',
    image: 'PyTorch 2.0',
    memory: '256G',
    cpu: '32核',
    gpu: '4卡V100',
    mountPath: '/data'
  },
  {
    id: 2,
    name: 'baseline-env',
    ideType: 'Jupyter',
    project: 'AI研发组',
    projectGroup: 'AI Lab',
    status: 'running',
    description: '基线实验环境',
    creator: '李四',
    resource: ['1xT4', '8核CPU', '64G'],
    thread: '8',
    image: 'PyTorch 2.0',
    memory: '64G',
    cpu: '8核',
    gpu: '1卡T4',
    mountPath: '/data'
  },
  {
    id: 3,
    name: 'dev-lisi-temp',
    ideType: 'VSCode',
    project: '数据科学组',
    projectGroup: '数据科学部',
    status: 'stopped',
    description: '临时开发环境',
    creator: '李四',
    resource: ['2xA100', '16核CPU', '128G'],
    thread: '16',
    image: 'TensorFlow 2.12',
    memory: '128G',
    cpu: '16核',
    gpu: '2卡A100',
    mountPath: '/data'
  },
  {
    id: 4,
    name: 'test-env-v2',
    ideType: 'Jupyter',
    project: '平台组',
    projectGroup: '研发部',
    status: 'stopped',
    description: '测试环境v2',
    creator: '王五',
    resource: ['1xT4', '8核CPU', '64G'],
    thread: '8',
    image: 'Jupyter Lab',
    memory: '64G',
    cpu: '8核',
    gpu: '1卡T4',
    mountPath: '/data'
  },
  {
    id: 5,
    name: 'llm-finetune',
    ideType: 'VSCode',
    project: 'AI研发组',
    projectGroup: 'AI Lab',
    status: 'running',
    description: 'LLM微调环境',
    creator: '张三',
    resource: ['8xA100', '64核CPU', '512G'],
    thread: '64',
    image: 'PyTorch 2.0',
    memory: '512G',
    cpu: '64核',
    gpu: '8卡A100',
    mountPath: '/data'
  }
];

Mock.mock(/\/notebook_modelview\/api/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: notebookList
  };
});

Mock.mock(/\/llmops\/notebook\/list/, "get", (options) => {
  return {
    code: 200,
    message: "成功",
    data: notebookList
  };
});

Mock.mock(/\/llmops\/notebook$/, "post", (options) => {
  const data = JSON.parse(options.body);
  const newNotebook = {
    id: Mock.Random.natural(10, 100),
    ...data,
    status: 'stopped',
    creator: '当前用户',
    thread: data.cpu ? data.cpu.replace('核', '') : '8',
    resource: [data.gpu || '1xT4', data.cpu || '8核CPU', data.memory || '64G']
  };
  notebookList.unshift(newNotebook);
  return {
    code: 200,
    message: "创建成功",
    data: newNotebook
  };
});

Mock.mock(/\/llmops\/notebook$/, "put", (options) => {
  const data = JSON.parse(options.body);
  const index = notebookList.findIndex(item => item.id === data.id);
  if (index !== -1) {
    notebookList[index] = { 
      ...notebookList[index], 
      ...data,
      thread: data.cpu ? data.cpu.replace('核', '') : notebookList[index].thread,
      resource: [data.gpu || notebookList[index].resource[0], data.cpu || notebookList[index].resource[1], data.memory || notebookList[index].resource[2]]
    };
  }
  return {
    code: 200,
    message: "更新成功"
  };
});

Mock.mock(/\/llmops\/notebook\/\d+/, "delete", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 1]);
  const index = notebookList.findIndex(item => item.id === id);
  if (index !== -1) {
    notebookList.splice(index, 1);
  }
  return {
    code: 200,
    message: "删除成功"
  };
});

Mock.mock(/\/llmops\/notebook\/\d+\/start/, "post", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 2]);
  const index = notebookList.findIndex(item => item.id === id);
  if (index !== -1) {
    notebookList[index].status = 'running';
  }
  return {
    code: 200,
    message: "启动成功"
  };
});

Mock.mock(/\/llmops\/notebook\/\d+\/stop/, "post", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 2]);
  const index = notebookList.findIndex(item => item.id === id);
  if (index !== -1) {
    notebookList[index].status = 'stopped';
  }
  return {
    code: 200,
    message: "停止成功"
  };
});
