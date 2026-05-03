package top.sssd.ddns.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuyang13
 */
public class AmisPageUtils<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private int total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 返回的数据实体
     */
    private List<T> items;

    /**
     * 分页
     */
    public AmisPageUtils(IPage<T> page) {
        this.items = page.getRecords();
        this.total = (int)page.getTotal();
        this.page = (int)page.getCurrent();
    }

    public AmisPageUtils(){}

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
