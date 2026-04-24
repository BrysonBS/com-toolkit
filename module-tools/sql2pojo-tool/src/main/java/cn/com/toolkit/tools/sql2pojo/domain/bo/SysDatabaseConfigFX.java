package cn.com.toolkit.tools.sql2pojo.domain.bo;

import cn.com.toolkit.framework.core.util.JasyptUtils;
import cn.com.toolkit.tools.sql2pojo.domain.po.SysDatabaseConfig;
import javafx.beans.property.*;

import java.time.LocalDateTime;

public class SysDatabaseConfigFX {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final BooleanProperty isDefault = new SimpleBooleanProperty(false);
    private final ObjectProperty<LocalDateTime> createTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updateTime = new SimpleObjectProperty<>();
    public SysDatabaseConfigFX() {}
    public SysDatabaseConfigFX(SysDatabaseConfig sysDatabaseConfig) {
        if(sysDatabaseConfig == null) return;
        id.set(sysDatabaseConfig.getId());
        name.set(sysDatabaseConfig.getName());
        url.set(sysDatabaseConfig.getUrl());
        username.set(sysDatabaseConfig.getUsername());
        password.set(sysDatabaseConfig.getPassword());
        isDefault.set(sysDatabaseConfig.getIsDefault());
        createTime.set(sysDatabaseConfig.getCreateTime());
        updateTime.set(sysDatabaseConfig.getUpdateTime());
    }
    public SysDatabaseConfig getSysDatabaseConfig() {
        return new SysDatabaseConfig()
                .setId(id.get())
                .setName(name.get())
                .setUrl(url.get())
                .setUsername(username.get())
                .setPassword(password.get())
                .setIsDefault(isDefault.get())
                .setCreateTime(createTime.get())
                .setUpdateTime(updateTime.get());
    }
    public void setSysDatabaseConfig(SysDatabaseConfig sysDatabaseConfig) {
        id.set(sysDatabaseConfig.getId());
        name.set(sysDatabaseConfig.getName());
        url.set(sysDatabaseConfig.getUrl());
        username.set(sysDatabaseConfig.getUsername());
        password.set(sysDatabaseConfig.getPassword());
        isDefault.set(sysDatabaseConfig.getIsDefault());
        createTime.set(sysDatabaseConfig.getCreateTime());
        updateTime.set(sysDatabaseConfig.getUpdateTime());
    }


    public LongProperty idProperty() { return id; }
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public BooleanProperty selectedProperty() { return selected; }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }

    public StringProperty urlProperty() { return url; }
    public String getUrl() { return url.get(); }
    public void setUrl(String value) { url.set(value); }

    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }

    public StringProperty passwordProperty() { return password; }
    public String getPassword() { return password.get(); }
    public void setPassword(String value) { password.set(value); }

    public boolean getIsDefault() {
        return isDefault.get();
    }

    public BooleanProperty isDefaultProperty() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault.set(isDefault);
    }

    public LocalDateTime getUpdateTime() {
        return updateTime.get();
    }

    public ObjectProperty<LocalDateTime> updateTimeProperty() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime.set(updateTime);
    }

    public LocalDateTime getCreateTime() {
        return createTime.get();
    }

    public ObjectProperty<LocalDateTime> createTimeProperty() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime.set(createTime);
    }
}
