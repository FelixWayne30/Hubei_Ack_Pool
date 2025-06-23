package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.CatalogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CatalogService {

    @Resource
    private CatalogMapper catalogMapper;

    public List<Map<String, Object>> getCatalogs() {
        List<Map<String, Object>> rawData = catalogMapper.getCatalogs();
        return organizeCatalogData(rawData);
    }

    private List<Map<String, Object>> organizeCatalogData(List<Map<String, Object>> rawData) {
        // 使用 LinkedHashMap 保持插入顺序
        Map<String, Map<String, List<String>>> catalogMap = new LinkedHashMap<>();

        // 将原始数据组织成三级结构
        for (Map<String, Object> row : rawData) {
            String group = (String) row.get("group");
            String subgroup = (String) row.get("subgroup");
            String map = (String) row.get("map");

            catalogMap.computeIfAbsent(group, k -> new LinkedHashMap<>())
                    .computeIfAbsent(subgroup, k -> new ArrayList<>())
                    .add(map);
        }

        // 转换为前端所需的 JSON 结构
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> groupEntry : catalogMap.entrySet()) {
            String group = groupEntry.getKey();
            Map<String, List<String>> subgroups = groupEntry.getValue();

            List<Map<String, Object>> subgroupList = new ArrayList<>();
            for (Map.Entry<String, List<String>> subgroupEntry : subgroups.entrySet()) {
                String subgroup = subgroupEntry.getKey();
                List<String> maps = subgroupEntry.getValue();

                Map<String, Object> subgroupMap = new HashMap<>();
                subgroupMap.put("subgroup", subgroup);
                subgroupMap.put("maps", maps);
                subgroupList.add(subgroupMap);
            }

            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("group", group);
            groupMap.put("subgroups", subgroupList);
            result.add(groupMap);
        }

        return result;
    }
}