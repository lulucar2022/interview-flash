package com.flash.community.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentTreeDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorNickname;
    private String authorAvatarUrl;
    private Long articleId;
    private Long parentId;
    private Integer likeCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private List<CommentTreeDTO> children;
}
