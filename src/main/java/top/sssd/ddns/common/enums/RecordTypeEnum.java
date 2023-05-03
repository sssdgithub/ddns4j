package top.sssd.ddns.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sssd
 * @careate 2023-03-20-15:27
 */
public enum RecordTypeEnum {
    IPV6(1,"AAAA"),
    IPV4(2,"A");

    public static final List<Map<String, Object>> selectResultList = new ArrayList<>();
    private static final Map<Integer,String> map = new HashMap<>();

    static {
        for (RecordTypeEnum el : RecordTypeEnum.values()) {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("no",el.getIndex());
            stringObjectHashMap.put("label",el.getName());
            map.put(el.getIndex(), el.getName());
            selectResultList.add(stringObjectHashMap);
        }
    }

    public static String getNameByIndex(Integer index){
        return map.get(index);
    }


    private Integer index;
    private String name;

    RecordTypeEnum() {
    }

    RecordTypeEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
