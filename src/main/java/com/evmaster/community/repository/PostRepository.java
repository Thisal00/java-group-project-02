package com.evmaster.community.repository;

import com.evmaster.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Only show posts that are NOT hidden
    List<Post> findByHiddenFalse();

    List<Post> findByHiddenFalseOrderByCreatedAtDesc();
}
