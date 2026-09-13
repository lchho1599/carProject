package com.rentdb.storage.controller;

import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.global.error.BusinessException;
import com.rentdb.storage.FileStorage;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

/** 업로드 이미지 공개 제공 (로컬 저장소) — 파일 이름이 무작위라 내용이 바뀌지 않으므로 오래 캐시한다 */
@Hidden
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

	private final FileStorage fileStorage;

	@GetMapping("/{year}/{month}/{filename}")
	public ResponseEntity<Resource> file(@PathVariable String year, @PathVariable String month,
			@PathVariable String filename) {
		String key = year + "/" + month + "/" + filename;
		Resource resource = fileStorage.load(key).orElseThrow(() -> BusinessException.notFound("파일"));
		MediaType mediaType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok()
				.contentType(mediaType)
				.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
				.body(resource);
	}

}
