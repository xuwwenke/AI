import Mock from "mockjs";

const datasetList = [
  {
    id: 1,
    name: 'MNIST手写数字',
    type: 'image',
    status: 'ready',
    source: 'public',
    size: 11000000,
    samples: 70000,
    description: '经典手写数字识别数据集',
    tags: ['计算机视觉', '分类', '入门'],
    createdAt: '2024-01-15',
    updatedAt: '2024-02-20'
  },
  {
    id: 2,
    name: 'IMDB电影评论',
    type: 'text',
    status: 'ready',
    source: 'public',
    size: 84000000,
    samples: 50000,
    description: '电影情感分类数据集',
    tags: ['NLP', '情感分析', '分类'],
    createdAt: '2024-01-10',
    updatedAt: '2024-02-18'
  },
  {
    id: 3,
    name: '风控交易数据',
    type: 'tabular',
    status: 'processing',
    source: 'local',
    size: 2500000000,
    samples: 284807,
    description: '信用卡欺诈检测数据集',
    tags: ['风控', '异常检测', '表格数据'],
    createdAt: '2024-02-01',
    updatedAt: '2024-03-01'
  },
  {
    id: 4,
    name: '语音命令集',
    type: 'audio',
    status: 'ready',
    source: 'public',
    size: 2300000000,
    samples: 105829,
    description: '语音命令识别数据集',
    tags: ['语音', '分类', '入门'],
    createdAt: '2024-01-20',
    updatedAt: '2024-02-25'
  },
  {
    id: 5,
    name: '用户行为日志',
    type: 'tabular',
    status: 'ready',
    source: 'hdfs',
    size: 15000000000,
    samples: 1000000,
    description: '电商用户行为数据',
    tags: ['推荐', '序列', '大数据'],
    createdAt: '2024-01-05',
    updatedAt: '2024-02-28'
  },
  {
    id: 6,
    name: 'COCO目标检测',
    type: 'image',
    status: 'expired',
    source: 's3',
    size: 25000000000,
    samples: 330000,
    description: '通用目标检测数据集',
    tags: ['计算机视觉', '目标检测', '大模型'],
    createdAt: '2023-12-01',
    updatedAt: '2024-01-15'
  },
  {
    id: 7,
    name: 'CIFAR-10图像分类',
    type: 'image',
    status: 'ready',
    source: 'public',
    size: 170000000,
    samples: 60000,
    description: '小图像分类数据集',
    tags: ['计算机视觉', '分类', '入门'],
    createdAt: '2024-01-25',
    updatedAt: '2024-03-01'
  },
  {
    id: 8,
    name: '新闻文本分类',
    type: 'text',
    status: 'ready',
    source: 'local',
    size: 520000000,
    samples: 120000,
    description: '多分类新闻文本数据集',
    tags: ['NLP', '多分类', '文本'],
    createdAt: '2024-02-10',
    updatedAt: '2024-03-05'
  }
];

Mock.mock(/\/dataset_modelview\/api/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: datasetList
  };
});

Mock.mock(/\/llmops\/dataset/, "post", (options) => {
  const data = JSON.parse(options.body);
  const newDataset = {
    id: Mock.Random.natural(10, 100),
    ...data,
    samples: Mock.Random.natural(1000, 100000),
    size: Mock.Random.natural(1000000, 1000000000),
    status: 'processing',
    createdAt: new Date().toISOString().split('T')[0],
    updatedAt: new Date().toISOString().split('T')[0]
  };
  datasetList.unshift(newDataset);
  return {
    code: 200,
    message: "创建成功",
    data: newDataset
  };
});

Mock.mock(/\/llmops\/dataset/, "put", (options) => {
  const data = JSON.parse(options.body);
  const index = datasetList.findIndex(item => item.id === data.id);
  if (index !== -1) {
    datasetList[index] = { 
      ...datasetList[index], 
      ...data,
      updatedAt: new Date().toISOString().split('T')[0]
    };
  }
  return {
    code: 200,
    message: "更新成功"
  };
});

Mock.mock(/\/llmops\/dataset\/\d+/, "delete", (options) => {
  const urlParts = options.url.split('/');
  const id = parseInt(urlParts[urlParts.length - 1]);
  const index = datasetList.findIndex(item => item.id === id);
  if (index !== -1) {
    datasetList.splice(index, 1);
  }
  return {
    code: 200,
    message: "删除成功"
  };
});
