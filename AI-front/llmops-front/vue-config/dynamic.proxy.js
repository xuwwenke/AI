const { readFileSync } = require('fs');
const { resolve } = require('path');

const getProxyList = () => {
  try {
    const proxyList = readFileSync(resolve(__dirname, './dynamic.proxy.list.json'), 'utf-8');
    return JSON.parse(proxyList);
  } catch (error) {
    throw new Error(error);
  }
};

const getActiveProxy = () => {
  try {
    const proxyList = getProxyList();
    if (proxyList.some(i => i.active)) {
      return proxyList.find(i => i.active);
    }
  } catch (error) {
    throw new Error(error);
  }
};

module.exports = () => {
  return getActiveProxy().target;
};
