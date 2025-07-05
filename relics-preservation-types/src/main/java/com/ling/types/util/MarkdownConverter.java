package com.ling.types.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.ext.tables.TablesExtension;

/**
 * @Author: LingRJ
 * @Description: Markdown格式渲染工具
 * @DateTime: 2025/7/5 0:14
 **/
public class MarkdownConverter {
    private static final Parser parser;
    private static final HtmlRenderer renderer;

    static {
        MutableDataSet options = new MutableDataSet();

        // 设置解析器配置，支持换行符转换
        options.setFrom(ParserEmulationProfile.GITHUB);

        // 启用表格扩展
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(TablesExtension.create()));

        // 启用软换行转换为<br>标签
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        // 启用硬换行
        options.set(Parser.HARD_LINE_BREAK_LIMIT, true);

        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * 将Markdown文本转换为HTML
     * @param markdown Markdown文本
     * @return HTML文本
     */
    public static String convertToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        // 预处理Markdown文本，处理换行符
        String processedMarkdown = preprocessMarkdown(markdown);

        // 转换为HTML并后处理
        String html = renderer.render(parser.parse(processedMarkdown));

        // 移除多余的换行符
        return html.replaceAll("\\n+", "");
    }

    /**
     * 预处理Markdown文本
     * @param markdown 原始Markdown文本
     * @return 处理后的Markdown文本
     */
    private static String preprocessMarkdown(String markdown) {
        // 将单个换行符转换为Markdown的硬换行语法（两个空格+换行）
        // 这样可以确保换行符被正确转换为<br>标签而不是保留为\n
        return markdown.replaceAll("(?<!\\s)\\n(?!\\n)", "  \n");
    }

}
