package com.ling.domain.interaction.model.valobj;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

/**
 * 变更跟踪器
 * @Author: LingRJ
 * @Description: 跟踪聚合根中的数据变更，实现增量保存
 * @DateTime: 2025/7/13
 */
@Getter
public class ChangeTracker {
    
    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /** 新增 */
        ADDED,
        /** 修改 */
        MODIFIED,
        /** 删除 */
        DELETED
    }
    
    /**
     * 变更记录
     */
    public static class ChangeRecord {
        private final ChangeType type;
        private final String entityType;
        private final Object entityId;
        private final Object entity;
        
        public ChangeRecord(ChangeType type, String entityType, Object entityId, Object entity) {
            this.type = type;
            this.entityType = entityType;
            this.entityId = entityId;
            this.entity = entity;
        }
        
        public ChangeType getType() { return type; }
        public String getEntityType() { return entityType; }
        public Object getEntityId() { return entityId; }
        public Object getEntity() { return entity; }
        
        @Override
        public String toString() {
            return String.format("ChangeRecord{type=%s, entityType=%s, entityId=%s}", 
                    type, entityType, entityId);
        }
    }
    
    // 使用线程安全的集合存储变更记录
    private final Set<ChangeRecord> changes = Collections.synchronizedSet(new HashSet<>());
    
    // 标记是否有变更
    private volatile boolean hasChanges = false;
    
    /**
     * 记录新增操作
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param entity 实体对象
     */
    public void recordAdd(String entityType, Object entityId, Object entity) {
        ChangeRecord record = new ChangeRecord(ChangeType.ADDED, entityType, entityId, entity);
        changes.add(record);
        hasChanges = true;
    }
    
    /**
     * 记录修改操作
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param entity 实体对象
     */
    public void recordModify(String entityType, Object entityId, Object entity) {
        ChangeRecord record = new ChangeRecord(ChangeType.MODIFIED, entityType, entityId, entity);
        changes.add(record);
        hasChanges = true;
    }
    
    /**
     * 记录删除操作
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param entity 实体对象
     */
    public void recordDelete(String entityType, Object entityId, Object entity) {
        ChangeRecord record = new ChangeRecord(ChangeType.DELETED, entityType, entityId, entity);
        changes.add(record);
        hasChanges = true;
    }
    
    /**
     * 获取指定类型的变更记录
     * @param entityType 实体类型
     * @return 变更记录集合
     */
    public Set<ChangeRecord> getChangesByType(String entityType) {
        return changes.stream()
                .filter(record -> entityType.equals(record.getEntityType()))
                .collect(HashSet::new, Set::add, Set::addAll);
    }
    
    /**
     * 获取指定变更类型的记录
     * @param changeType 变更类型
     * @return 变更记录集合
     */
    public Set<ChangeRecord> getChangesByChangeType(ChangeType changeType) {
        return changes.stream()
                .filter(record -> changeType == record.getType())
                .collect(HashSet::new, Set::add, Set::addAll);
    }
    
    /**
     * 获取指定实体类型和变更类型的记录
     * @param entityType 实体类型
     * @param changeType 变更类型
     * @return 变更记录集合
     */
    public Set<ChangeRecord> getChanges(String entityType, ChangeType changeType) {
        return changes.stream()
                .filter(record -> entityType.equals(record.getEntityType()) && 
                                changeType == record.getType())
                .collect(HashSet::new, Set::add, Set::addAll);
    }
    
    /**
     * 清空变更记录
     */
    public void clearChanges() {
        changes.clear();
        hasChanges = false;
    }
    
    /**
     * 获取变更数量
     * @return 变更数量
     */
    public int getChangeCount() {
        return changes.size();
    }
    
    /**
     * 获取指定类型的变更数量
     * @param entityType 实体类型
     * @return 变更数量
     */
    public int getChangeCount(String entityType) {
        return (int) changes.stream()
                .filter(record -> entityType.equals(record.getEntityType()))
                .count();
    }
    
    /**
     * 检查是否有指定类型的变更
     * @param entityType 实体类型
     * @return 是否有变更
     */
    public boolean hasChanges(String entityType) {
        return changes.stream()
                .anyMatch(record -> entityType.equals(record.getEntityType()));
    }
    
    /**
     * 检查是否有指定变更类型的记录
     * @param changeType 变更类型
     * @return 是否有变更
     */
    public boolean hasChanges(ChangeType changeType) {
        return changes.stream()
                .anyMatch(record -> changeType == record.getType());
    }
    
    /**
     * 获取所有变更记录
     * @return 变更记录集合
     */
    public Set<ChangeRecord> getAllChanges() {
        return new HashSet<>(changes);
    }
    
    @Override
    public String toString() {
        return String.format("ChangeTracker{hasChanges=%s, changeCount=%d}", 
                hasChanges, changes.size());
    }
}
