package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.exception.ResourceNotFoundException;
import com.nestfinder.nestfinderbackend.model.Content;
import com.nestfinder.nestfinderbackend.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @GetMapping
    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(@PathVariable Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id " + id));
        return ResponseEntity.ok(content);
    }

    @GetMapping("/type/{type}")
    public List<Content> getContentByType(@PathVariable String type) {
        return contentRepository.findByType(type);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Content> createContent(@Valid @RequestBody Content content) {
        Content savedContent = contentRepository.save(content);
        return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Content> updateContent(@PathVariable Long id, @Valid @RequestBody Content contentDetails) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id " + id));

        content.setTitle(contentDetails.getTitle());
        content.setType(contentDetails.getType());
        content.setContentBody(contentDetails.getContentBody());

        Content updatedContent = contentRepository.save(content);
        return ResponseEntity.ok(updatedContent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteContent(@PathVariable Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id " + id));

        contentRepository.delete(content);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
