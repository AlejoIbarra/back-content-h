package com.pageAdult.adult_content_backend.service;

import com.pageAdult.adult_content_backend.entity.Categoria;
import com.pageAdult.adult_content_backend.entity.Video;
import com.pageAdult.adult_content_backend.repository.CategoriaRepository;
import com.pageAdult.adult_content_backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Video guardarVideo(Video video) {
        if (video.getCategoria() != null && video.getCategoria().getNombre() != null) {
            Categoria categoriaExistente = categoriaRepository.findByNombre(video.getCategoria().getNombre());

            if (categoriaExistente == null) {
                throw new RuntimeException("La categoría '" + video.getCategoria().getNombre() + "' no existe.");
            }

            video.setCategoria(categoriaExistente);
        }

        return videoRepository.save(video);
    }

    public Video actualizarVideo(Long id, Map<String, Object> updates) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado"));

        updates.forEach((campo, valor) -> {
            try {
                Field field = Video.class.getDeclaredField(campo);
                field.setAccessible(true);

                if (field.getType().equals(Long.class) && valor instanceof Integer) {
                    field.set(video, ((Integer) valor).longValue());
                } else if (field.getType().equals(Double.class) && valor instanceof Integer) {
                    field.set(video, ((Integer) valor).doubleValue());
                } else if (field.getType().equals(LocalDateTime.class) && valor instanceof String) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    field.set(video, LocalDateTime.parse((String) valor, formatter));
                } else {
                    field.set(video, valor);
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error al actualizar el campo: " + campo, e);
            }
        });

        return videoRepository.save(video);
    }

    public List<Video> listarVideos() {
        return videoRepository.findAll();
    }

    public Optional<Video> obtenerVideoPorId(Long id) {
        return videoRepository.findById(id);
    }

    public void eliminarVideo(Long id) {
        videoRepository.deleteById(id);
    }
}
