package com.flash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int total;
    private int success;
    private int fail;
    private List<String> errors = new ArrayList<>();

    public void addError(int row, String reason) {
        this.fail++;
        errors.add("第 " + row + " 行: " + reason);
    }

    public void addSuccess() {
        this.success++;
    }

    public static ImportResult empty() {
        ImportResult r = new ImportResult();
        r.total = 0;
        r.success = 0;
        r.fail = 0;
        return r;
    }
}
