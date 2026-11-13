export default async function queryList4paging(
  request,
  params
) {
  const res = await request(params);
  let { total, pageNum, pageSize } = res;
  if (pageNum > Math.ceil(total / pageSize) && total > 0) {
    pageNum = Math.ceil(total / pageSize);
    if (pageNum == 0) {
      pageNum = 1;
    }
    return queryList4paging(request, { ...params, pageNum });
  }
  return res;
}
