package com.rentdb.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.rentdb.global.config.TimeConfig;

/**
 * 로컬 폴더 저장소 — {localDir}/yyyy/MM/{uuid}.{ext} 에 저장하고 /api/files/yyyy/MM/{uuid}.{ext} 로 제공한다.
 */
@Component
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

	public static final String PUBLIC_PREFIX = "/api/files/";

	/** 서버가 만든 키 형식만 허용 — ../ 같은 경로 조작 차단 */
	private static final Pattern KEY_PATTERN =
			Pattern.compile("^\\d{4}/\\d{2}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$");

	private final Path root;
	private final Clock clock;

	public LocalFileStorage(StorageProperties properties, Clock clock) {
		this.root = Path.of(properties.localDir() == null ? "uploads" : properties.localDir()).toAbsolutePath().normalize();
		this.clock = clock;
	}

	@Override
	public String store(byte[] content, String extension) {
		LocalDate today = LocalDate.now(clock.withZone(TimeConfig.KST));
		String key = "%04d/%02d/%s.%s".formatted(today.getYear(), today.getMonthValue(), UUID.randomUUID(), extension);
		Path target = root.resolve(key).normalize();
		try {
			Files.createDirectories(target.getParent());
			Files.write(target, content);
		}
		catch (IOException e) {
			throw new UncheckedIOException("파일 저장 실패", e);
		}
		return PUBLIC_PREFIX + key;
	}

	@Override
	public Optional<Resource> load(String key) {
		if (key == null || !KEY_PATTERN.matcher(key).matches()) {
			return Optional.empty();
		}
		Path file = root.resolve(key).normalize();
		if (!file.startsWith(root) || !Files.isRegularFile(file)) {
			return Optional.empty();
		}
		return Optional.of(new PathResource(file));
	}

}
