package com.medicare.user.domain.records;

import java.util.List;

public record ErrorResponse(
        List<ErrorDetail> erros
) {}

