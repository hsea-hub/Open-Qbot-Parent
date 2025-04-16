package com.qbot.cq.business.common.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.qbot.cq.business.common.contexts.UserRequestContext;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.MsgDTO;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

public class MsgUtil {
    public static MsgBO msgConvert(MsgDTO msgDTO){
        MsgBO msgBO = MsgBO.builder()
                .msgId(msgDTO.getMsgId())
                .msgSequence(msgDTO.getMsgSequence())
                .type(msgDTO.getType())
                .roomId(msgDTO.getFromUser())
                .build();
        parseAndSetMsgSource(msgDTO.getSignature(),msgBO);
        extractSetSenderAndContent(msgDTO.getContent(),msgDTO.getFromUser(),msgBO);
        String content = cleanContent(msgBO.getAtWxids(), msgBO.getContent(), UserRequestContext.get());
        msgBO.setContent(content);
        return msgBO;
    }


    public static void parseAndSetMsgSource(String xml,MsgBO msgBO)  {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 默认空值
        List<String> atWxids = Lists.newArrayList();
        Integer memberCount = null;

        // 获取 <atuserlist> 节点
        NodeList atuserlistNodes = doc.getElementsByTagName("atuserlist");
        if (atuserlistNodes.getLength() > 0 && atuserlistNodes.item(0) != null) {
            String atUserListStr = atuserlistNodes.item(0).getTextContent().trim();
            if (StrUtil.isNotBlank(atUserListStr)) {
                for (String wxid : atUserListStr.split(",")) {
                    if (StrUtil.isNotBlank(wxid)){
                        atWxids.add(wxid);
                    }
                }
            }
        }

        // 获取 <membercount> 节点
        NodeList memberCountNodes = doc.getElementsByTagName("membercount");
        if (memberCountNodes.getLength() > 0 && memberCountNodes.item(0) != null) {
            String countStr = memberCountNodes.item(0).getTextContent().trim();
            if (!countStr.isEmpty()) {
                try {
                    memberCount = Integer.parseInt(countStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        msgBO.setAtWxids(atWxids);
        msgBO.setMemberCount(memberCount);
    }

    public static void extractSetSenderAndContent(String content, String fromUser,MsgBO msgBO) {

        boolean isGroup = fromUser.endsWith("@chatroom");
        if (isGroup) {
            // 群聊：content 是 "wxid_xxx:\n消息内容"
            String[] parts = content.split(":\n", 2);
            if (parts.length == 2) {
                msgBO.setFromUser(parts[0].trim());
                msgBO.setContent(parts[1].trim());
            }
        } else {
            msgBO.setFromUser(fromUser.trim());
            msgBO.setContent(content.trim());
        }

    }
    /**
     * 去除被 @ 的部分，仅保留消息内容
     * @param atWxids 被 @ 的 wxid 列表
     * @param content 原始消息内容（如 "@文件传输助手 菜单"）
     * @param myWxid 当前用户自己的 wxid
     * @return 清理后的消息内容
     */
    public static String cleanContent(List<String> atWxids, String content, String myWxid) {
        if (atWxids != null && atWxids.contains(myWxid)) {
            // 正则去除 @xxx（包括中间的中文全角空格）
            return content.replaceFirst("^@[^\\s\u2000-\u200A\u202F\u205F\u3000]+[\\s\u2000-\u200A\u202F\u205F\u3000]*", "").trim();
        }
        return content;
    }
}
