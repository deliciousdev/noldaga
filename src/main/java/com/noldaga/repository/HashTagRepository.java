package com.noldaga.repository;

import com.noldaga.domain.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    @Query("select h from HashTag h where h.tagName=:hashTagName")
    Optional<HashTag> findByHashTagName(String hashTagName);
}
