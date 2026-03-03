package com.groupservice.groupservice.service;

import com.groupservice.groupservice.dto.GroupResponse;

public interface GroupMemberService {
    GroupResponse addMember(String groupPublicId, String userPublicId);
    GroupResponse removeMember(String groupPublicId, String userPublicId);
    GroupResponse myGroup(String myUserPublicId);
}