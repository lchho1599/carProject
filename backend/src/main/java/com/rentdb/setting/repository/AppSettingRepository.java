package com.rentdb.setting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.setting.domain.AppSetting;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
