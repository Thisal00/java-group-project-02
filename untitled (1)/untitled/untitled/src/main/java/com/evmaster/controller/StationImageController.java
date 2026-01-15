package com.evmaster.controller;

import com.evmaster.model.ChargingStation;
import com.evmaster.model.StationImage;
import com.evmaster.model.User;
import com.evmaster.repository.StationImageRepository;
import com.evmaster.repository.StationRepository;
import com.evmaster.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StationImageController {

    private final StationImageRepository imageRepo;
    private final StationRepository stationRepo;
    private final UserService userService;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    public StationImageController(
            StationImageRepository imageRepo,
            StationRepository stationRepo,
            UserService userService
    ) {
        this.imageRepo = imageRepo;
        this.stationRepo = stationRepo;
        this.userService = userService;
    }

    // HELPER CURRENT USER
    private User getCurrentUser(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return userService.findUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // UPLOAD STATION IMAGES
    @PostMapping("/manager/stations/{stationId}/images")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long stationId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserDetails principal
    ) throws Exception {

        User manager = getCurrentUser(principal);

        ChargingStation station = stationRepo.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        if (!station.getManager().getId().equals(manager.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your station");
        }

        File dir = new File(uploadDir + "/stations/" + stationId);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String filename = UUID.randomUUID() + "_" +
                    file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");

            File dest = new File(dir, filename);
            file.transferTo(dest);

            StationImage img = new StationImage();
            img.setStation(station);
            img.setImageUrl("/uploads/stations/" + stationId + "/" + filename);

            imageRepo.save(img);
        }

        return ResponseEntity.ok("Images uploaded successfully");
    }

    // GET STATION IMAGES (PUBLIC)
    @GetMapping("/stations/{stationId}/images")
    public List<StationImage> getImages(@PathVariable Long stationId) {
        return imageRepo.findByStation_Id(stationId);
    }

    // DELETE IMAGE
    @DeleteMapping("/manager/stations/images/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails principal
    ) {

        User manager = getCurrentUser(principal);

        StationImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        ChargingStation station = img.getStation();

        if (!station.getManager().getId().equals(manager.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your station");
        }

        // delete file from disk
        try {
            File f = new File(img.getImageUrl().replace("/uploads/", uploadDir + "/"));
            if (f.exists()) f.delete();
        } catch (Exception ignored) {}

        imageRepo.delete(img);
        return ResponseEntity.ok("Image deleted");
    }
}
