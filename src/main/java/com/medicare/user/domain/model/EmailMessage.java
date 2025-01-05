package com.medicare.user.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("to")
    private String to;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;
}
