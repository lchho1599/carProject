package com.rentdb.storage;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 이미지 업로드 — 파일 이름·브라우저가 보낸 형식은 믿지 않고 파일 앞부분(시그니처)으로 JPG/PNG/WEBP 만 받는다.
 */
@Service
@RequiredArgsConstructor
public class ImageUploadService {

	public static final long MAX_BYTES = 5L * 1024 * 1024;

	private final FileStorage fileStorage;

	public String upload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw invalid("업로드할 이미지 파일을 선택해 주세요.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw invalid("이미지는 5MB 이하만 올릴 수 있습니다.");
		}
		byte[] content;
		try {
			content = file.getBytes();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		String extension = detectImageExtension(content);
		if (extension == null) {
			throw invalid("JPG, PNG, WEBP 이미지만 올릴 수 있습니다.");
		}
		return fileStorage.store(content, extension);
	}

	/** 파일 시그니처로 형식 판별, 허용 형식이 아니면 null */
	static String detectImageExtension(byte[] bytes) {
		if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) {
			return "jpg";
		}
		if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
			return "png";
		}
		if (bytes.length >= 12 && startsWith(bytes, 'R', 'I', 'F', 'F')
				&& bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
			return "webp";
		}
		return null;
	}

	private static boolean startsWith(byte[] bytes, int... signature) {
		if (bytes.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if ((bytes[i] & 0xFF) != signature[i]) {
				return false;
			}
		}
		return true;
	}

	private static BusinessException invalid(String message) {
		return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
	}

}
