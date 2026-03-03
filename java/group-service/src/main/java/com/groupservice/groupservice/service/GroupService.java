package com.groupservice.groupservice.service;

import com.groupservice.groupservice.dto.GroupCreateRequest;
import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.dto.GroupUpdateRequest;

import java.util.List;

public interface GroupService {
    GroupResponse create(GroupCreateRequest req);
    GroupResponse get(String groupPublicId);
    List<GroupResponse> list();
    GroupResponse update(String groupPublicId, GroupUpdateRequest req);
    void delete(String groupPublicId);
}