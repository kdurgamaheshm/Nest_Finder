package com.nestfinder.nestfinderbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "content")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String type; // e.g., "FAQ", "POLICY", "ABOUT_US"

    @Column(columnDefinition = "TEXT")
    private String contentBody;

    public Content() {
    }

    public Content(String title, String type, String contentBody) {
        this.title = title;
        this.type = type;
        this.contentBody = contentBody;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }
}
