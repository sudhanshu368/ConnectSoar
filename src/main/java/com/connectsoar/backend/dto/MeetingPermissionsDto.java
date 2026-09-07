package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingPermissionsDto {
    @Builder.Default
    private boolean allowParticipantChat = true;

    @Builder.Default
    private boolean allowScreenSharing = true;

    @Builder.Default
    private boolean muteParticipantsOnEntry = false;

    @Builder.Default
    private boolean allowParticipantVideo = true;

    @Builder.Default
    private boolean allowParticipantAudio = true;

    @Builder.Default
    private boolean isOpenRoom = false;

    // Direct permission flags for participant join
    private Boolean canChat;
    private Boolean canShareScreen;
    private Boolean canUseCamera;
    private Boolean canUseMicrophone;
    private Boolean mutedOnEntry;
}
