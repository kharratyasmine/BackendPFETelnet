package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface TeamMemberHistoryRepository extends JpaRepository<TeamMemberHistory, Long> {
    List<TeamMemberHistory> findByTeamMemberOrderByModifiedDateDesc(TeamMember teamMember);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TeamMemberHistory h WHERE h.teamMember.id = :memberId")
    void deleteByTeamMemberId(@Param("memberId") Long memberId);
}