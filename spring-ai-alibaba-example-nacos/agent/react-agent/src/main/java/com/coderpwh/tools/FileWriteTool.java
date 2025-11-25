package com.coderpwh.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * @author coderpwh
 */
public class FileWriteTool implements Tool<FileWriteTool.Request, String> {


    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("file_write", this)
                .description("Tool for write files")
                .inputType(Request.class)
                .build();

    }


    @Override
    public String apply(FileWriteTool.Request request, ToolContext toolContext) {
        return null;
    }


    public record Request(@JsonProperty(value = "file_path", required = true)
                          @JsonPropertyDescription("The path of the file to write") String filePath,

                          @JsonProperty(value = "content", required = true)
                          @JsonPropertyDescription("The content to write to the file")
                          String content) {

    }


}
