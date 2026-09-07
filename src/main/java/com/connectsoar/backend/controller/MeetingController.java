package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.CreateMeetingRequest;
import com.connectsoar.backend.dto.JoinCodeRequest;
import com.connectsoar.backend.dto.JoinMeetingRequest;
import com.connectsoar.backend.dto.MeetingJoinResponse;
import com.connectsoar.backend.dto.MeetingPageData;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/meetings", "/api/v1/meetings"})
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
        return new ResponseEntity<>(ApiResponse.ok("Meeting created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MeetingPageData<MeetingResponse>>> listMeetings(
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestParam(value = "tab", required = false) String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        MeetingPageData<MeetingResponse> response = meetingService.getMeetingsPaged(principal, tab, search, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        MeetingResponse response = meetingService.getMeetingById(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody UpdateMeetingRequest request) {
        MeetingResponse response = meetingService.updateMeeting(principal, meetingId, request);
        return ResponseEntity.ok(ApiResponse.ok("Meeting updated successfully", response));
    }

    @PatchMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> patchMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody UpdateMeetingRequest request) {
        MeetingResponse response = meetingService.updateMeeting(principal, meetingId, request);
        return ResponseEntity.ok(ApiResponse.ok("Meeting updated successfully", response));
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        meetingService.deleteMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.okMessage("Meeting cancelled successfully"));
    }

    @PostMapping("/{meetingId}/start")
    public ResponseEntity<ApiResponse<MeetingResponse>> startMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        MeetingResponse response = meetingService.startMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok("Meeting started successfully", response));
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<ApiResponse<MeetingResponse>> endMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        MeetingResponse response = meetingService.endMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.ok("Meeting ended successfully", response));
    }

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<ApiResponse<MeetingJoinResponse>> joinMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody(required = false) JoinMeetingRequest request) {
        String password = request != null ? request.getPassword() : null;
        MeetingJoinResponse response = meetingService.joinMeeting(principal, meetingId, password);
        return ResponseEntity.ok(ApiResponse.ok("Meeting joined successfully", response));
    }

    @PostMapping("/join-code")
    public ResponseEntity<ApiResponse<MeetingJoinResponse>> joinByCode(
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody JoinCodeRequest request) {
        MeetingJoinResponse response = meetingService.joinByCode(principal, request.getMeetingCode(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Meeting joined successfully", response));
    }

    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveMeeting(
            @PathVariable("meetingId") String meetingId,
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        meetingService.leaveMeeting(principal, meetingId);
        return ResponseEntity.ok(ApiResponse.okMessage("Left meeting successfully"));
    }
}
