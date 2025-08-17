package com.chatdoc.chatwithpdf.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageText {
    private int pageNumber;
    private String text;
}
