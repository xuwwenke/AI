package com.eversec.llmops.service.controller.req;

public class PageParams {

    //默认查询第一页
    private static int PAGENUM_DEFAULT = 1;
    //默认每页查询10条
    private static int PAGESIZE_DEFAULT = 10;



    private int pageNum;


    private int pageSize;

    public int getPageNum() {
        if (pageNum <= 0) {
            //默认查询第一页
            return PAGENUM_DEFAULT;
        }
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        //默认每页显示10条
        if (pageSize <= 0) {
            return PAGESIZE_DEFAULT;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    //获取查询起止位置
    public int getStartPosition() {
        return this.getPageNum() - 1 < 0 ? 0 : (this.getPageNum() - 1) * this.getPageSize();
    }
}