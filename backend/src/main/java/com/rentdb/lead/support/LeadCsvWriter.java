package com.rentdb.lead.support;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.rentdb.global.config.TimeConfig;
import com.rentdb.lead.domain.Lead;

/**
 * 리드 CSV — 엑셀에서 한글이 깨지지 않도록 UTF-8 BOM 을 붙이고,
 * 셀 값이 = + - @ 로 시작하면 수식으로 실행되지 않게 앞에 ' 를 붙인다(CSV 수식 주입 방지).
 */
public final class LeadCsvWriter {

	private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final List<String> HEADER = List.of(
			"번호", "신청일시", "유형", "상태", "이름", "연락처", "차량", "상세", "차량가", "이용조건",
			"마케팅동의", "신청페이지", "utm_source", "utm_medium", "utm_campaign");

	private LeadCsvWriter() {
	}

	public static byte[] write(List<Lead> leads) {
		StringBuilder csv = new StringBuilder();
		appendRow(csv, HEADER);
		for (Lead lead : leads) {
			Map<String, String> utm = lead.getUtm() == null ? Map.of() : lead.getUtm();
			appendRow(csv, List.of(
					String.valueOf(lead.getId()),
					TimeConfig.toKst(lead.getCreatedAt()).format(DATE_TIME),
					LeadLabels.type(lead.getType()),
					LeadLabels.status(lead.getStatus()),
					lead.getName(),
					LeadLabels.formatPhone(lead.getPhone()),
					LeadLabels.vehicleName(lead.getVehicleSnapshot()),
					LeadLabels.vehicleDetail(lead.getVehicleSnapshot()),
					lead.getTotalPrice() == null ? "" : String.valueOf(lead.getTotalPrice()),
					LeadLabels.conditionSummary(lead.getConditions()),
					lead.isAgreeMarketing() ? "동의" : "미동의",
					nullToEmpty(lead.getSourceUrl()),
					utm.getOrDefault("utm_source", ""),
					utm.getOrDefault("utm_medium", ""),
					utm.getOrDefault("utm_campaign", "")));
		}

		byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
		byte[] result = new byte[UTF8_BOM.length + body.length];
		System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
		System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
		return result;
	}

	private static void appendRow(StringBuilder csv, List<String> cells) {
		for (int i = 0; i < cells.size(); i++) {
			if (i > 0) {
				csv.append(',');
			}
			csv.append(escape(cells.get(i)));
		}
		csv.append("\r\n");
	}

	static String escape(String value) {
		String cell = nullToEmpty(value);
		if (!cell.isEmpty() && "=+-@\t\r".indexOf(cell.charAt(0)) >= 0) {
			cell = "'" + cell;
		}
		if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
			cell = "\"" + cell.replace("\"", "\"\"") + "\"";
		}
		return cell;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

}
