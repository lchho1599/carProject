package com.rentdb.lead.event;

/** 상담 신청 저장(커밋) 후 알림 발송에 사용 */
public record LeadCreatedEvent(Long leadId) {
}
