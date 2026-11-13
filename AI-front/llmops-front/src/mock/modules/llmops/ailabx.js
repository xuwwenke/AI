import Mock from "mockjs";

const categories = ['全部', '模型套件', '政府应用', '图像处理', '自然资源处理', '数据分析', '推荐系统'];

const modelApps = [
  {
    id: 1,
    name: '对话应用',
    desc: '基于GPT-4的智能对话助手',
    icon: 'el-icon-chat-dot-round',
    iconBg: '#dbeafe',
    model: 'GPT-4',
    version: 'v1.0',
    rating: '4.8/5.0',
    primaryBtn: '部署',
    category: '模型套件'
  },
  {
    id: 2,
    name: '图像识别应用',
    desc: '基于ResNet的图像分类系统',
    icon: 'el-icon-picture',
    iconBg: '#d1fae5',
    model: 'ResNet-50',
    version: 'v2.1',
    rating: '4.6/5.0',
    primaryBtn: '部署',
    category: '图像处理'
  },
  {
    id: 3,
    name: '文本分析应用',
    desc: '基于BERT的文本分析系统',
    icon: 'el-icon-document',
    iconBg: '#fef3c7',
    model: 'BERT-base',
    version: 'v1.5',
    rating: '4.7/5.0',
    primaryBtn: '部署',
    category: '数据分析'
  }
];

const longxdApps = [
  {
    id: 4,
    name: '龙虾识别',
    desc: '识别龙虾品种大小',
    icon: 'el-icon-s-tools',
    iconBg: '#fce7f3',
    type: '图像识别',
    version: 'v1.2',
    rating: '4.9/5.0',
    primaryBtn: '部署',
    category: '自然资源处理'
  },
  {
    id: 5,
    name: '龙虾市场分析',
    desc: '分析龙虾市场价格趋势',
    icon: 'el-icon-s-marketing',
    iconBg: '#fef3c7',
    type: '数据分析',
    version: 'v2.0',
    rating: '4.7/5.0',
    primaryBtn: '部署',
    category: '数据分析'
  },
  {
    id: 6,
    name: '龙虾食谱推荐',
    desc: '基于龙虾种类推荐食谱',
    icon: 'el-icon-s-goods',
    iconBg: '#d1fae5',
    type: '推荐系统',
    version: 'v1.1',
    rating: '4.8/5.0',
    primaryBtn: '部署',
    category: '推荐系统'
  }
];

const llmApps = [
  {
    id: 7,
    name: 'Qwen',
    desc: '阿里云千问大模型',
    icon: 'el-icon-s-opportunity',
    iconBg: '#dbeafe',
    type: '通用大模型',
    version: 'v1.5',
    rating: '4.8/5.0',
    primaryBtn: '对话',
    category: '模型套件'
  },
  {
    id: 8,
    name: 'DeepSeek',
    desc: '深度求索大模型',
    icon: 'el-icon-s-data',
    iconBg: '#ede9fe',
    type: '通用大模型',
    version: 'v1.0',
    rating: '4.7/5.0',
    primaryBtn: '对话',
    category: '模型套件'
  },
  {
    id: 9,
    name: 'GPT',
    desc: 'OpenAI GPT大模型',
    icon: 'el-icon-s-platform',
    iconBg: '#ecfdf5',
    type: '通用大模型',
    version: 'v4.0',
    rating: '4.9/5.0',
    primaryBtn: '对话',
    category: '模型套件'
  }
];

Mock.mock(/\/ailabx_modelview\/api\/categories/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: categories
  };
});

Mock.mock(/\/ailabx_modelview\/api\/modelApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: modelApps
  };
});

Mock.mock(/\/ailabx_modelview\/api\/longxdApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: longxdApps
  };
});

Mock.mock(/\/ailabx_modelview\/api\/llmApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: llmApps
  };
});

Mock.mock(/\/llmops\/ailabx\/categories/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: categories
  };
});

Mock.mock(/\/llmops\/ailabx\/modelApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: modelApps
  };
});

Mock.mock(/\/llmops\/ailabx\/longxdApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: longxdApps
  };
});

Mock.mock(/\/llmops\/ailabx\/llmApps/, "get", () => {
  return {
    code: 200,
    message: "成功",
    data: llmApps
  };
});
