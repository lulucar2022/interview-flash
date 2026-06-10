package com.flash.config;

import com.flash.community.entity.Article;
import com.flash.community.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RobotsController {

    private final ArticleService articleService;

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        return """
            User-agent: *
            Allow: /
            Sitemap: https://example.com/sitemap.xml
            """;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        List<Article> articles = articleService.getPublishedArticlesForSitemap();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (Article a : articles) {
            xml.append("  <url>\n");
            xml.append("    <loc>https://example.com/articles/").append(a.getId()).append("</loc>\n");
            if (a.getUpdatedAt() != null) {
                xml.append("    <lastmod>").append(a.getUpdatedAt().toLocalDate()).append("</lastmod>\n");
            }
            xml.append("  </url>\n");
        }
        xml.append("</urlset>");
        return xml.toString();
    }
}
