package com.pageAdult.adult_content_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pageAdult.adult_content_backend.entity.Video;
import com.pageAdult.adult_content_backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Video crearVideo(
            @RequestPart("video") MultipartFile archivo,
            @RequestPart("datos") String datosJson) throws IOException {

        if (archivo.isEmpty()) {
            throw new RuntimeException("No se ha proporcionado un archivo de video.");
        }

        String nombreArchivo = archivo.getOriginalFilename();
        Path rutaArchivo = Paths.get("uploads/" + nombreArchivo);
        Files.createDirectories(rutaArchivo.getParent());
        Files.write(rutaArchivo, archivo.getBytes());

        ObjectMapper objectMapper = new ObjectMapper();
        Video video = objectMapper.readValue(datosJson, Video.class);
        video.setUrl(nombreArchivo);

        return videoService.guardarVideo(video);
    }

    @PatchMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Video actualizarVideo(
            @PathVariable Long id,
            @RequestPart(value = "video", required = false) MultipartFile archivo,
            @RequestPart("datos") String datosJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> updates = objectMapper.readValue(datosJson, Map.class);

        if (archivo != null && !archivo.isEmpty()) {
            String nombreArchivo = archivo.getOriginalFilename();
            Path rutaArchivo = Paths.get("uploads/" + nombreArchivo);
            Files.createDirectories(rutaArchivo.getParent());
            Files.write(rutaArchivo, archivo.getBytes());

            updates.put("url", nombreArchivo);
        }

        return videoService.actualizarVideo(id, updates);
    }

    @GetMapping
    public List<Video> obtenerVideos() {
        return videoService.listarVideos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> obtenerVideoPorId(@PathVariable Long id) {
        return videoService.obtenerVideoPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void eliminarVideo(@PathVariable Long id) {
        videoService.eliminarVideo(id);
    }

    @GetMapping("/stream/{nombreArchivo}")
    public ResponseEntity<Resource> obtenerVideo(@PathVariable String nombreArchivo) throws IOException {
        Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo);

        if (!Files.exists(rutaArchivo)) {
            return ResponseEntity.notFound().build();
        }

        Resource recurso = new UrlResource(rutaArchivo.toUri());

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(recurso).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(recurso);
    }
}