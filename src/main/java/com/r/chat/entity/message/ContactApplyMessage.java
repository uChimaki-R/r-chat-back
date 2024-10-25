package com.r.chat.entity.message;

import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.result.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContactApplyMessage extends Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public ContactApplyMessage(){
        super(MessageTypeEnum.CONTACT_APPLY);
    }
}
