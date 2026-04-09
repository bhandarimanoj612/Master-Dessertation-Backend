package com.example.sajilo_tayaar.dto.response;

import com.example.sajilo_tayaar.entity.enums.RepairStatus;

import java.time.Instant;

public record TimelineItemResponse(
        RepairStatus status,
        String changedBy,
        String remarks,
        Instant changedAt
) {}