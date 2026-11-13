package com.eversec.llmops;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 自动生成 Entity、Mapper、Service、Controller 等代码
 */
public class CodeGenerator {

    // 数据库配置
    private static final String URL = "jdbc:mysql://192.168.75.239:30511/kubeflow?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin";
    
    // 包名配置
    private static final String PARENT_PACKAGE = "com.eversec.llmops";
    private static final String MODULE_NAME = "service"; // 模块名
    
    // 路径配置
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/llmops-service/src/main/java";
    private static final String MAPPER_XML_PATH = System.getProperty("user.dir") + "/llmops-service/src/main/resources/mapper/" + MODULE_NAME;

    public static void main(String[] args) {
        generateCode();
    }

    /**
     * 生成代码
     */
    public static void generateCode() {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
            .globalConfig(builder -> {
                builder.author("NiYongZhe") // 设置作者
                    .outputDir(OUTPUT_DIR) // 指定输出目录
                    .disableOpenDir() // 生成后不打开目录
                    .commentDate("yyyy-MM-dd"); // 注释日期格式
            })
            .packageConfig(builder -> {
                builder.parent(PARENT_PACKAGE) // 设置父包名
                    .moduleName(MODULE_NAME) // 设置模块名
                    .entity("entity")
                    .mapper("mapper")
                    .service("service")
                    .serviceImpl("service.impl")
                    .controller("controller")
                    .pathInfo(Collections.singletonMap(OutputFile.xml, MAPPER_XML_PATH)); // 设置mapperXml生成路径
            })
            .strategyConfig(builder -> {
                builder.addInclude("ide_service_task") // 设置需要生成的表名
                    .addTablePrefix("sys_", "t_") // 设置过滤表前缀
                    
                    // Entity 策略配置
                    .entityBuilder()
                    .enableLombok() // 开启 Lombok 模式
                    .logicDeleteColumnName("deleted") // 逻辑删除字段名
                    .enableTableFieldAnnotation() // 开启生成实体时生成字段注解
                    
                    // Controller 策略配置
                    .controllerBuilder()
                    .enableRestStyle() // 开启 RestController 风格
                    .enableHyphenStyle() // 开启驼峰转连字符
                    
                    // Service 策略配置
                    .serviceBuilder()
                    .formatServiceFileName("%sService")
                    .formatServiceImplFileName("%sServiceImpl")
                    
                    // Mapper 策略配置
                    .mapperBuilder()
                    .enableBaseResultMap() // 启用 BaseResultMap 生成
                    .enableBaseColumnList() // 启用 BaseColumnList
                    .formatMapperFileName("%sMapper")
                    .formatXmlFileName("%sMapper");
            })
            .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎
            .execute();
    }
}