package org.dotspace.oofp.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TestConstraintOptions {

    @NotEmpty
    private String name;

    @NotNull
    private Long number;

    @NotNull
    private Date date;

    @NotEmpty
    private List<String> items;

}
