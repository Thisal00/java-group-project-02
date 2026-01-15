package com.evmaster.community.repository;

import com.evmaster.community.model.Post;
import com.evmaster.community.model.PostLike;
import com.evmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);

    int countByPost(Post post);

    void deleteByPostAndUser(Post post, User user);
}
