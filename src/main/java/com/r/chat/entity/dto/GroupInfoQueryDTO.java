package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;

    private String groupName;

    private String groupOwnerId;
}
