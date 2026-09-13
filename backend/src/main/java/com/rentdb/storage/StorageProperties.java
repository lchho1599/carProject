package com.rentdb.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.storage.* — 업로드 이미지 저장소
 *
 * @param type     local(서버 폴더) — 운영 Cloudflare R2 구현은 배포 단계에서 추가
 * @param localDir local 저장 폴더
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String type, String localDir) {
}
