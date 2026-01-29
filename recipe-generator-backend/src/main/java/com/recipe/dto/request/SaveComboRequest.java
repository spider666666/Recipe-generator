package com.recipe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存食材组合请求
 */
@Data
public class SaveComboRequest {

    @NotBlank(message = "组合名称不能为空")
    private String name;

    @NotBlank(message = "食材列表不能为空")
    private String ingredients;
}
