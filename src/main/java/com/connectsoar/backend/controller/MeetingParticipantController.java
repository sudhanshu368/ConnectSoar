package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.AddParticipantRequest;
import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.ParticipantResponse;
import com.connectsoar.backend.dto.UpdateParticipantRequest;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.MeetingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings/{meetingId}/participants")
public class MeetingParticipantController {

    private static final Logger log = LoggerFactory.getLogger(MeetingParticipantController.class);

    private final MeetingService meetingService;

    public MeetingParticipantController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ParticipantResponse>> addParticipant(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody AddParticipantRequest request) {
        ParticipantResponse response = meetingService.addParticipant(principal, meetingId, request);
        return new ResponseEntity<>(ApiResponse.ok("Participant added successfully.", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> getParticipants(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<ParticipantResponse> response = meetingService.getParticipants(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<ParticipantResponse>> updateParticipantPermission(
            @PathVariable("meetingId") String meetingId,
            @PathVariable("userId") String userId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody UpdateParticipantRequest request) {
        ParticipantResponse response = meetingService.updateParticipantPermission(principal, meetingId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Participant permission updated successfully.", response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeParticipant(
            @PathVariable("meetingId") String meetingId,
            @PathVariable("userId") String userId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        meetingService.removeParticipant(principal, meetingId, userId);
        return ResponseEntity.ok(ApiResponse.okMessage("Participant removed successfully."));
    }
}
