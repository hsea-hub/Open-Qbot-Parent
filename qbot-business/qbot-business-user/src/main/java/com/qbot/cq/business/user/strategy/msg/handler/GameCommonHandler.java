package com.qbot.cq.business.user.strategy.msg.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.hook.SendTextMsgDTO;
import com.qbot.cq.business.common.enums.RoomStatusEnum;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.common.utils.MahjongDaqueUtils;
import com.qbot.cq.business.user.entity.contants.TextContant;
import com.qbot.cq.business.user.entity.po.GameMahJong;
import com.qbot.cq.business.user.entity.po.GameMahJongDetail;
import com.qbot.cq.business.user.service.IGamaMahJongDetailService;
import com.qbot.cq.business.user.service.IGameMahJongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GameCommonHandler {
    @Autowired
    private IGameMahJongService gameMahJongService;
    @Autowired
    private IGamaMahJongDetailService gamaMahJongDetailService;
    private static final String command = "打麻将";
    private static final String touchTheBarHu = "碰杠胡";
@Transactional(rollbackFor = Exception.class)
    public void commandHandler(MsgBO msgBO) {
        // 群聊
        if (msgBO.getRoomId().contains("room")) {
            return;
        }
        // 没开始不接收消息
        GameMahJongDetail mahJongDetail = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getWxid, msgBO.getFromUser()).eq(GameMahJongDetail::getRoomStatus, RoomStatusEnum.STARTED.getValue()).orderByDesc(GameMahJongDetail::getId).last("limit 1").one();
        if (Objects.isNull(mahJongDetail)) {
            return;
        }
        // 获取开启的人数
        List<GameMahJongDetail> gameMahJongDetails = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getBaseId, mahJongDetail.getBaseId()).eq(GameMahJongDetail::getRoomStatus, RoomStatusEnum.STARTED.getValue()).ne(GameMahJongDetail::getWxid, msgBO.getFromUser()).orderByDesc(GameMahJongDetail::getId).list();
        GameMahJong gameMahJong = gameMahJongService.lambdaQuery().eq(GameMahJong::getId, mahJongDetail.getBaseId()).one();
        // 等待缺的人数
        List<String> waitQueList = gameMahJongDetails.stream().filter(item -> {
            return StrUtil.isEmpty(item.getQue());
        }).map(GameMahJongDetail::getNickName).collect(Collectors.toList());
        // 如果没有定缺，就需要定缺
        if (StrUtil.isEmpty(mahJongDetail.getQue())) {
            MahjongDaqueUtils.Suit suit = MahjongDaqueUtils.parseMissingSuit(msgBO.getContent());
            if (Objects.isNull(suit)) {
                return;
            }
            mahJongDetail.setQue(suit.toChinese());
            gamaMahJongDetailService.updateById(mahJongDetail);
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.SUCCESSFUL_VACANCY_DETERMINATION, mahJongDetail.getQue(), waitQueList)).build());

            for (GameMahJongDetail gameMahJongDetail : gameMahJongDetails) {
                HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.SUCCESSFUL_VACANCY_DETERMINATION_OTHER, mahJongDetail.getNickName(), mahJongDetail.getQue(), waitQueList)).build());
            }
            if (CollectionUtil.isEmpty(waitQueList)){
                GameMahJongDetail mahJongDetailFirst = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getWxid, msgBO.getFromUser()).eq(GameMahJongDetail::getRoomStatus, RoomStatusEnum.STARTED.getValue()).eq(GameMahJongDetail::getCurIndex,1).orderByDesc(GameMahJongDetail::getId).last("limit 1").one();
                HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetailFirst.getWxid()).msg(TextContant.IT_S_YOUR_TURN_TO_PLAY).build());
            }
        }
        if (StrUtil.isEmpty(mahJongDetail.getQue()) || CollectionUtil.isNotEmpty(waitQueList)) {
            return;
        }
        Deque<MahjongDaqueUtils.Tile> wall = MahjongDaqueUtils.wallFromStrings(gameMahJong.getCurCardWall());
        MahjongDaqueUtils.Tile tile = null;
        if (StrUtil.isNotBlank(gameMahJong.getCurCard())) {
            tile = MahjongDaqueUtils.parseTile(gameMahJong.getCurCard());
        }

        List<MahjongDaqueUtils.Tile> tiles = MahjongDaqueUtils.jsonToTiles(mahJongDetail.getCurCardWall());
        MahjongDaqueUtils.Suit que = MahjongDaqueUtils.parseMissingSuit(mahJongDetail.getQue());
        if (touchTheBarHu.contains(msgBO.getContent())) {

            List<MahjongDaqueUtils.Tile> newCard = null;
            try {
                newCard = MahjongDaqueUtils.handleCommand(msgBO.getContent(), tiles, wall, que, tile, true);
            } catch (Exception e) {
                HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.SYS_BASE_MESSAGE,e.getMessage())).build());
                return;
            }
            // 胡
            if (newCard.size()==tiles.size()){
                gameMahJong.setRoomStatus(RoomStatusEnum.FINISHED.getValue());
                boolean update = gameMahJongService.updateById(gameMahJong);
                HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJong.getWxid()).msg(StrFormatter.format(TextContant.CONGRATULATIONS_ON_YOUR_SUCCESSFUL_HU_CARD,msgBO.getContent(), newCard)).build());

                if (!update) {
                    HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command)).build());
                    throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command));
                }
                for (GameMahJongDetail gameMahJongDetail : gameMahJongDetails) {
                    gameMahJongDetail.setRoomStatus(RoomStatusEnum.FINISHED.getValue());
                    gamaMahJongDetailService.updateById(gameMahJongDetail);
                    HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.HU_GANG_PENG, mahJongDetail.getNickName(), msgBO.getContent(), newCard)).build());
                }
            } else {
                if ("碰".equals(msgBO.getContent())) {
                    List<MahjongDaqueUtils.Tile> diffed = MahjongDaqueUtils.diffRemovedTiles(tiles, newCard);
                    gameMahJong.setCurIndex(mahJongDetail.getCurIndex());
                    boolean update = gameMahJongService.updateById(gameMahJong);
                    if (!update) {
                        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command)).build());
                        throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command));
                    }
                    HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJong.getWxid()).msg(StrFormatter.format(TextContant.CONGRATULATIONS_ON_YOUR_SUCCESSFUL_HU_CARD,msgBO.getContent(), newCard)).build());
                    mahJongDetail.setCurCardWall(MahjongDaqueUtils.tilesToJson(newCard));
                    gamaMahJongDetailService.updateById(mahJongDetail);
                    for (GameMahJongDetail gameMahJongDetail : gameMahJongDetails) {
                        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.HU_GANG_PENG, mahJongDetail.getNickName(), msgBO.getContent(), diffed)).build());
                    }
                }
                if ("杠".equals(msgBO.getContent())) {
                    List<MahjongDaqueUtils.Tile> diffed = MahjongDaqueUtils.diffRemovedTiles(tiles, newCard);
                    MahjongDaqueUtils.draw(newCard,wall);
                    gameMahJong.setCurIndex(mahJongDetail.getCurIndex()-1<=0?4:mahJongDetail.getCurIndex()-1);
                    gameMahJong.setCurCardWall(MahjongDaqueUtils.wallToStrings(wall));
                    boolean update = gameMahJongService.updateById(gameMahJong);
                    if (!update) {
                        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command)).build());
                        throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command));
                    }
                    HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJong.getWxid()).msg(StrFormatter.format(TextContant.CONGRATULATIONS_ON_YOUR_SUCCESSFUL_HU_CARD,msgBO.getContent(), newCard)).build());
                    mahJongDetail.setCurCardWall(MahjongDaqueUtils.tilesToJson(newCard));
                    gamaMahJongDetailService.updateById(mahJongDetail);
                    for (GameMahJongDetail gameMahJongDetail : gameMahJongDetails) {
                        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.HU_GANG_PENG, mahJongDetail.getNickName(), msgBO.getContent(), diffed)).build());
                    }
                }
            }
            return;
        }
        int player = MahjongDaqueUtils.nextPlayer(gameMahJong.getCurIndex(), 4);
        System.out.println(player);
        if (player != mahJongDetail.getCurIndex()) {
            GameMahJongDetail jongDetail = gameMahJongDetails.stream().filter(item -> {
                return item.getCurIndex() == player;
            }).findFirst().get();
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.WAITING_FOR_THE_CARD_TO_BE_PLAYED, jongDetail.getNickName())).build());
            return;
        }
        List<MahjongDaqueUtils.Tile> newCard = null;
        try {
            newCard = MahjongDaqueUtils.handleCommand(msgBO.getContent(), tiles, wall, que, tile, true);
        } catch (Exception e) {
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.SYS_BASE_MESSAGE,e.getMessage())).build());
            return;
        }
        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.SUCCESSFUL_CARD_PLAYING,msgBO.getContent(), newCard)).build());

        mahJongDetail.setCurCardWall(MahjongDaqueUtils.tilesToJson(newCard));
        gamaMahJongDetailService.updateById(mahJongDetail);
        gameMahJong.setCurIndex(mahJongDetail.getCurIndex());
        gameMahJong.setCurCard(msgBO.getContent());
        boolean update = gameMahJongService.updateById(gameMahJong);
        if (!update) {
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command)).build());
            throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command));
        }
        for (GameMahJongDetail gameMahJongDetail : gameMahJongDetails) {
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(gameMahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.OTHER_PRODUCE, mahJongDetail.getNickName(), msgBO.getContent())).build());
        }
        int playerNext = MahjongDaqueUtils.nextPlayer(mahJongDetail.getCurIndex(), 4);
        GameMahJongDetail jongDetail = gameMahJongDetails.stream().filter(item -> {
            return item.getCurIndex() == playerNext;
        }).findFirst().get();
        List<MahjongDaqueUtils.Tile> cards = MahjongDaqueUtils.jsonToTiles(jongDetail.getCurCardWall());
        MahjongDaqueUtils.Tile draw = MahjongDaqueUtils.draw(cards, wall);

        mahJongDetail.setCurCardWall(MahjongDaqueUtils.tilesToJson(cards));
        gamaMahJongDetailService.updateById(mahJongDetail);

        gameMahJong.setCurCardWall(MahjongDaqueUtils.wallToStrings(wall));
        boolean update2 = gameMahJongService.updateById(gameMahJong);

        if (!update2) {
            HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(mahJongDetail.getWxid()).msg(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command)).build());
            throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, command));
        }
        HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(jongDetail.getWxid()).msg(StrFormatter.format(TextContant.PLAYING_CARDS, draw, cards)).build());

    }
}
