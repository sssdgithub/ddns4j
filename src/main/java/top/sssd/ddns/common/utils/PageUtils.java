package top.sssd.ddns.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuyang13
 */
public class PageUtils<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private int totalRecords;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 当前页数
     */
    private int curPage;
    /**
     * 列表数据
     */
    private List<T> data;

    /**
     *
     */
    private String sortDir;

    /**
     *
     */
    private String sortIndx;

    /**
     * 分页
     */
    public PageUtils(IPage<T> page) {
        this.data = page.getRecords();
        this.totalRecords = (int)page.getTotal();
        this.pageSize = (int)page.getSize();
        this.curPage = (int)page.getCurrent();
        this.totalPage = (int)page.getPages();
    }

    /**
     * 分页
     * @param data        列表数据
     * @param totalRecords  总记录数
     * @param pageSize    每页记录数
     * @param curPage    当前页数
     */
    public PageUtils(List<T> data, int totalRecords, int pageSize, int curPage) {
        this.data = data;
        this.totalRecords = totalRecords;
        this.pageSize = pageSize;
        this.curPage = curPage;
        this.totalPage = (int) Math.ceil((double)totalRecords/pageSize);
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public String getSortIndx() {
        return sortIndx;
    }

    public void setSortIndx(String sortIndx) {
        this.sortIndx = sortIndx;
    }
}
