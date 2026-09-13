package com.rentdb.storage.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rentdb.storage.ImageUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 - 이미지 업로드")
@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminUploadController {

	private final ImageUploadService imageUploadService;

	public record UploadResponse(String url) {
	}

	@Operation(summary = "이미지 업로드 (JPG/PNG/WEBP, 5MB 이하) → 공개 주소")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.status(201).body(new UploadResponse(imageUploadService.upload(file)));
	}

}
