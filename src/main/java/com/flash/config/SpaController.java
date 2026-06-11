package com.flash.config;

import com.flash.community.entity.Article;
import com.flash.community.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SpaController {

    private final ArticleService articleService;

    @GetMapping("/articles/{id:[0-9]+}")
    public Object articleDetail(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ua = request.getHeader("User-Agent");
        if (ua != null && isCrawler(ua)) {
            try {
                Article article = articleService.getArticleEntity(id, null);
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(buildArticleHtml(article, request));
                return null;
            } catch (Exception e) {
                log.debug("SEO render failed for article {}: {}", id, e.getMessage());
            }
        }
        return "forward:/index.html";
    }

    @GetMapping({
        "/questions/**", "/practice/**", "/wrong/**",
        "/statistics/**", "/profile/**",
        "/articles/create", "/articles/*/edit",
        "/notifications/**", "/series/**", "/author/**",
        "/articles", "/articles/"
    })
    public String forward() {
        return "forward:/index.html";
    }

    private boolean isCrawler(String ua) {
        String low = ua.toLowerCase();
        return low.contains("googlebot") || low.contains("bingbot") || low.contains("slurp")
            || low.contains("duckduckbot") || low.contains("baiduspider") || low.contains("yandexbot")
            || low.contains("facebookexternalhit") || low.contains("twitterbot")
            || low.contains("linkedinbot") || low.contains("slack")
            || low.contains("discord") || low.contains("telegrambot")
            || low.contains("whatsapp") || low.contains("skypeuripreview")
            || low.contains("bot") || low.contains("spider") || low.contains("crawler");
    }

    private String buildArticleHtml(Article article, HttpServletRequest request) {
        String title = article.getTitle();
        String author = article.getAuthor() != null ? article.getAuthor().getNickname() : "";
        String date = article.getCreatedAt() != null ? article.getCreatedAt().toString() : "";
        String rawContent = article.getContent() != null ? article.getContent() : "";
        String textContent = rawContent.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        String excerpt = textContent.length() > 300 ? textContent.substring(0, 300) + "..." : textContent;
        String url = request.getRequestURL().toString();
        String description = excerpt.isEmpty() ? title : excerpt;

        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - 面试刷题系统</title>
                <meta name="description" content="%s">
                <meta property="og:title" content="%s">
                <meta property="og:description" content="%s">
                <meta property="og:type" content="article">
                <meta property="og:url" content="%s">
                <meta property="og:site_name" content="面试刷题系统">
                <meta property="article:published_time" content="%s">
                <meta name="twitter:card" content="summary_large_image">
                <meta name="robots" content="index,follow">
            </head>
            <body>
                <article>
                    <h1>%s</h1>
                    <p>作者: %s | 发布于: %s</p>
                    <div>%s</div>
                </article>
            </body>
            </html>
            """.formatted(
                escape(title), escape(description),
                escape(title), escape(description),
                escape(url), escape(date),
                escape(title), escape(author), escape(date),
                escape(excerpt)
            );
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
