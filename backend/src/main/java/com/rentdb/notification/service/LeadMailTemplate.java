package com.rentdb.notification.service;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.rentdb.global.common.Masking;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.support.LeadLabels;
import com.rentdb.notification.NotificationProperties;

import lombok.RequiredArgsConstructor;

/**
 * 신규 상담 알림 메일 (설계 문서 §9.5) — 이름·연락처는 마스킹, 전체 정보는 관리자 링크에서만 확인.
 */
@Component
@RequiredArgsConstructor
public class LeadMailTemplate {

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final NotificationProperties properties;

	public record MailContent(String subject, String html) {
	}

	public MailContent render(Lead lead) {
		String maskedName = Masking.name(lead.getName());
		String typeLabel = LeadLabels.type(lead.getType());
		String vehicle = LeadLabels.vehicleName(lead.getVehicleSnapshot());
		String subject = "[신규 상담] %s · %s · %s".formatted(typeLabel, vehicle, maskedName);

		String adminUrl = trimTrailingSlash(properties.adminBaseUrl()) + "/admin/leads/" + lead.getId();
		String createdAt = TimeConfig.toKst(lead.getCreatedAt()).format(DATE_TIME);

		StringBuilder rows = new StringBuilder();
		row(rows, "신청 유형", typeLabel);
		row(rows, "차량", vehicle);
		String detail = LeadLabels.vehicleDetail(lead.getVehicleSnapshot());
		if (!detail.isEmpty()) {
			row(rows, "상세", detail);
		}
		if (lead.getTotalPrice() != null) {
			row(rows, "차량가", "%,d원".formatted(lead.getTotalPrice()));
		}
		String conditions = LeadLabels.conditionSummary(lead.getConditions());
		if (!conditions.isEmpty()) {
			row(rows, "이용조건", conditions);
		}
		row(rows, "이름", maskedName);
		row(rows, "연락처", Masking.phone(lead.getPhone()));
		row(rows, "신청일시", createdAt);

		String html = """
				<div style="font-family:'Malgun Gothic',Apple SD Gothic Neo,sans-serif;max-width:560px;color:#111827">
				  <h2 style="margin:0 0 16px;font-size:20px;color:#1e3a5f">신규 상담 신청이 접수되었습니다</h2>
				  <table style="width:100%%;border-collapse:collapse;font-size:14px">%s</table>
				  <p style="margin:24px 0 8px">
				    <a href="%s" style="display:inline-block;padding:12px 20px;background:#1e3a5f;color:#ffffff;text-decoration:none;border-radius:6px">관리자 페이지에서 상세 보기</a>
				  </p>
				  <p style="margin:16px 0 0;font-size:12px;color:#6b7280">개인정보 보호를 위해 이름·연락처 전체는 관리자 로그인 후 확인할 수 있습니다.</p>
				</div>
				""".formatted(rows, HtmlUtils.htmlEscape(adminUrl));

		return new MailContent(subject, html);
	}

	private static void row(StringBuilder rows, String label, String value) {
		rows.append("<tr><th style=\"text-align:left;padding:8px 12px;background:#f3f4f6;width:96px;"
				+ "border-bottom:1px solid #e5e7eb;font-weight:600\">")
				.append(HtmlUtils.htmlEscape(label))
				.append("</th><td style=\"padding:8px 12px;border-bottom:1px solid #e5e7eb\">")
				.append(HtmlUtils.htmlEscape(value))
				.append("</td></tr>");
	}

	private static String trimTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

}
