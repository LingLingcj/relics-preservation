package com.ling.types.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.parser.ParserEmulationProfile;

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

        // 预处理：将单个换行符转换为双换行符（Markdown段落分隔）
        String processedMarkdown = preprocessMarkdown(markdown);

        return renderer.render(parser.parse(processedMarkdown));
    }

    /**
     * 预处理Markdown文本
     * @param markdown 原始Markdown文本
     * @return 处理后的Markdown文本
     */
    private static String preprocessMarkdown(String markdown) {
        // 将单个\n替换为两个空格+\n（Markdown的硬换行语法）
        return markdown.replaceAll("(?<!\\n)\\n(?!\\n)", "  \n");
    }

    /**
     * 简单的文本换行处理（如果不需要完整的Markdown功能）
     * @param text 普通文本
     * @return HTML文本
     */
    public static String convertTextToHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 转义HTML特殊字符
        String escaped = text.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#39;");

        // 将换行符转换为<br>标签
        return escaped.replace("\n", "<br>");
    }
}
