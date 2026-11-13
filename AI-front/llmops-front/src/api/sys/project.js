import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/project";
const url2 = "/system/sys/outer/project";

export default {
  // 项目查询
  getProjectList(params) {
    return httpRequest({
      url: url2 + "/page",
      params,
      headers: {
        userld: 1
      }
    });
  },
  // 项目更新
  updateProject(data) {
    return httpRequest({
      url: url + "/update",
      method: 'put',
      data: data,
      headers: {
        "Content-Type": "multipart/form-data"
      },
    });
  },
  // 项目新增
  addProject(data) {
    return httpRequest({
      url: url + "/create",
      method: 'post',
      data: data,
      headers: {
        "Content-Type": "multipart/form-data"
      },
    });
  },
  // 项目删除
  delProject(id) {
    return httpRequest({
      url: url + `/${id}`,
      method: 'delete',
    });
  },

  // 项目详情
  checkProject(id) {
    return httpRequest({
      url: url + `/${id}`,
      method: 'get',
    });
  },
}
