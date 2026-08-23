package com.smartcivic.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file);
    void delete(String fileUrl);
}
