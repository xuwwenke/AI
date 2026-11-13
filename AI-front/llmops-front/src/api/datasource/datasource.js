import httpRequest from "@/utils/httpRequest";

export default {
  // 获取数据源列表
  getList(o) {
    return httpRequest({
      url: "/everbi/model/datasources",
      params: o
    });
  },
  //根据数据源id返回对应表信息
  getTables(dsId, o) {
    return httpRequest({
      url: `/everbi/model/datasources/${dsId}/tables`,
      params: o
    });
  },
  //根据数据源id表名返回对应字段信息
  getColumns(dsId, tblName, schema, catlog, tblType, tblId, refresh = false) {
    schema = schema ? schema : "";
    catlog = catlog ? catlog : "";
    tblId = tblId ? tblId : "";
    return httpRequest({
      url: `/everbi/model/datasources/${dsId}/tables/${tblName}/columns?schema=${encodeURIComponent(
        schema
      )}&catlog=${encodeURIComponent(catlog)}&tblType=${encodeURIComponent(tblType)}&tblId=${encodeURIComponent(tblId)}`,
      params: refresh ? { doRefresh: 1 } : null
    });
  },
};
