package com.amerd.schoolbook.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomPage<T> {

    private List<T> items;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}
