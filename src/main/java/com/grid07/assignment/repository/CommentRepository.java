package com.grid07.assignment.repository;

import com.grid07.assignment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByPostId(Long postId);


}