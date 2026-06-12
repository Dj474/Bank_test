package com.example.bankcards.repository.card;

import com.example.bankcards.entity.card.BlockRequest;
import com.example.bankcards.util.enums.card.BlockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    Page<BlockRequest> findByStatus(BlockRequestStatus status, Pageable pageable);

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);

    Page<BlockRequest> findByUserId(Long userId, Pageable pageable);

}