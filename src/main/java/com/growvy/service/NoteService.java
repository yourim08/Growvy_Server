package com.growvy.service;

import com.growvy.dto.req.NoteCreateRequest;
import com.growvy.entity.*;
import com.growvy.repository.JobPostMemberRepository;
import com.growvy.repository.LearnTagRepository;
import com.growvy.repository.NoteRepository;
import com.growvy.repository.NoteTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final LearnTagRepository learnTagRepository;
    private final JobPostMemberRepository jobPostMemberRepository;


    @Transactional
    public void createNote(
            User user,
            NoteCreateRequest req
    ) {

        JobSeekerProfile jobSeeker =
                user.getJobSeekerProfile();

        // 1. 해당 공고를 실제 완료했는지 확인
        JobPostMember member =
                jobPostMemberRepository
                        .findByJobPostIdAndJobSeekerUserIdAndStatus(
                                req.getJobPostId(),
                                jobSeeker.getUserId(),
                                JobPostMember.Status.DONE
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "완료한 일만 기록할 수 있습니다."
                                ));

        // 2. 이미 저장된 노트가 있는지 확인
        if (noteRepository.existsByJobPost_IdAndJobSeeker_User_IdAndStatus(
                req.getJobPostId(),
                user.getId(),
                Note.Status.SAVED
        )) {
            throw new IllegalArgumentException(
                    "이미 작성한 노트입니다."
            );
        }

        // 3. 노트 저장
        Note note = new Note();

        note.setJobPost(member.getJobPost());
        note.setJobSeeker(jobSeeker);

        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setOverallExperience(req.getOverallExperience());

        note.setStatus(Note.Status.SAVED);

        Note savedNote = noteRepository.save(note);

        // 4. 기존 태그 연결
        if (req.getLearnTagIds() != null) {

            for (Long tagId : req.getLearnTagIds()) {

                LearnTag tag = learnTagRepository.findById(tagId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "태그 없음"
                                ));

                NoteTag noteTag = new NoteTag();
                noteTag.setNote(savedNote);
                noteTag.setLearnTag(tag);

                noteTagRepository.save(noteTag);
            }
        }

        // 5. 사용자 직접 입력 태그
        if (req.getCustomTags() != null) {

            for (String tagName : req.getCustomTags()) {

                LearnTag tag =
                        learnTagRepository.findByName(tagName)
                                .orElseGet(() -> {

                                    LearnTag newTag =
                                            new LearnTag();

                                    newTag.setName(tagName);

                                    return learnTagRepository.save(newTag);
                                });

                NoteTag noteTag = new NoteTag();

                noteTag.setNote(savedNote);
                noteTag.setLearnTag(tag);

                noteTagRepository.save(noteTag);
            }
        }
    }
}
