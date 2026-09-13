package com.rentdb.storage;

import java.util.Optional;

import org.springframework.core.io.Resource;

/**
 * 업로드 파일 저장소 — 구현을 바꿔 끼울 수 있게 분리한다 (로컬 폴더 / 추후 Cloudflare R2).
 */
public interface FileStorage {

	/**
	 * 파일을 저장하고 화면에서 쓸 공개 주소를 돌려준다.
	 *
	 * @param extension 점 없는 확장자 (jpg, png, webp)
	 */
	String store(byte[] content, String extension);

	/** 공개 주소의 파일 키(예: 2026/09/uuid.jpg)로 파일을 읽는다 — 로컬 저장소 전용 */
	Optional<Resource> load(String key);

}
