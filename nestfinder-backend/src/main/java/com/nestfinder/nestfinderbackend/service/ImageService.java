package com.nestfinder.nestfinderbackend.service;

import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.Image;
import com.nestfinder.nestfinderbackend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<Image> saveImages(MultipartFile[] files, House house) {
        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = fileStorageService.storeFile(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(fileName)
                    .toUriString();

            Image image = new Image(fileName, fileDownloadUri, house);
            imageRepository.save(image);
            savedImages.add(image);
        }

        return savedImages;
    }

    public void deleteImage(Long imageId) {
        imageRepository.deleteById(imageId);
    }

    public void deleteImagesByHouse(House house) {
        List<Image> images = house.getImages();
        images.forEach(img -> imageRepository.deleteById(img.getId()));
    }
}
