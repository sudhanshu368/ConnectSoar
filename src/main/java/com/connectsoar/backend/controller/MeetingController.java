package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.CreateMeetingRequest;
import com.connectsoar.backend.dto.MeetingJoinResponse;
import com.connectsoar.backend.dto.MeetingResponse;
import com.connectsoar.backend.dto.UpdateMeetingRequest;
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
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody CreateMeetingRequest request) {
        MeetingResponse response = meetingService.createMeeting(principal, request);
        return new ResponseEntity<>(ApiResponse.ok("Meeting created successfully.", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> listMeetings(
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        List<MeetingResponse> response = meetingService.getMeetingsForUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        MeetingResponse response = meetingService.getMeetingById(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody UpdateMeetingRequest request) {
        MeetingResponse response = meetingService.updateMeeting(principal, meetingId, request);
        return ResponseEntity.ok(ApiResponse.ok("Meeting updated successfully.", response));
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        meetingService.deleteMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.okMessage("Meeting cancelled successfully."));
    }

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<ApiResponse<MeetingJoinResponse>> joinMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        MeetingJoinResponse response = meetingService.joinMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok("Meeting joined successfully.", response));
    }
}
