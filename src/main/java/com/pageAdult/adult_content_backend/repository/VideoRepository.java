package com.pageAdult.adult_content_backend.repository;

import com.pageAdult.adult_content_backend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
