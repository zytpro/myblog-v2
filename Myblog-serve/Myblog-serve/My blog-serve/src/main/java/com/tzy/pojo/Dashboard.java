package com.tzy.pojo;


public class Dashboard {
    private Integer commentNumber; // 评论数量
    private Integer articleNumber; // 文章数量
    private Integer visitor; // 访问人数

    public Integer getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(Integer commentNumber) {
        this.commentNumber = commentNumber;
    }

    public Integer getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(Integer articleNumber) {
        this.articleNumber = articleNumber;
    }

    public Integer getVisitor() {
        return visitor;
    }

    public void setVisitor(Integer visitor) {
        this.visitor = visitor;
    }

    @Override
    public String toString() {
        return "Dashboard{" +
            "commentNumber=" + commentNumber +
            ", articleNumber=" + articleNumber +
            ", visitor=" + visitor +
            '}';
    }
}

