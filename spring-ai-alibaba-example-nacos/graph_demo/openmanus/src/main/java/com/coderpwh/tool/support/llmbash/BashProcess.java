package com.coderpwh.tool.support.llmbash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author coderpwh
 */
public class BashProcess {

    private static final Logger log = LoggerFactory.getLogger(BashProcess.class);

    /**
     * 执行命令
     * @param commandList
     * @param workingDirectoryPath
     * @return
     */
    public static List<String> executeCommand(List<String> commandList, String workingDirectoryPath) {
        return commandList.stream().map(commandLine -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", commandLine);
                if (!StringUtils.isEmpty(workingDirectoryPath)) {
                    pb.directory(new File(workingDirectoryPath));
                }

                // 启动进程
                Process process = pb.start();

                // 获取命令输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    log.warn(line);
                    builder.append(line);
                    builder.append("\n");
                }

                // 等待命令执行完成
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    log.warn("Bash command executed successfully.");
                }
                else {
                    log.error("Failed to execute Bash command.");
                }
                return builder.toString();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

}
