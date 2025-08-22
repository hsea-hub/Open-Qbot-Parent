package com.qbot.cq.business.user.strategy.command.handler;
import java.util.*;

import cn.hutool.core.text.StrFormatter;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.hook.SendAtTextDTO;
import com.qbot.cq.business.common.entity.dto.hook.SendTextMsgDTO;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.enums.RoomStatusEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.common.utils.MahjongDaqueUtils;
import com.qbot.cq.business.user.entity.contants.TextContant;
import com.qbot.cq.business.user.entity.po.GameMahJongDetail;
import com.qbot.cq.business.user.entity.po.GameMahJong;
import com.qbot.cq.business.user.service.IGamaMahJongDetailService;
import com.qbot.cq.business.user.service.IGameMahJongService;
import com.qbot.cq.business.user.strategy.command.CommandChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PlayMahjongHandler implements CommandChannel {

    @Autowired
    private IGameMahJongService gameMahJongService;
    @Autowired
    private IGamaMahJongDetailService gamaMahJongDetailService;

    private String CREATE = "创建房间";
    private String UPDATE = "加入房间";

    @Override
    public String getGuideType() {
        return CommandEnum.PLAY_MAHJONG.getName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean commandHandler(CommandUtil.CommandRequest commandRequest, MsgBO msgBO) {
        System.out.println(commandRequest.getCommand());
        // 创建房间
        if (CREATE.equals(commandRequest.getValue())) {
            // 校验
            if (checkRoom(msgBO)) return false;

            GameMahJong gameMahJong = new GameMahJong();
            gameMahJong.setBotId(msgBO.getToUser());
            gameMahJong.setRoomId(msgBO.getRoomId());
            gameMahJong.setWxid(msgBO.getFromUser());
            gameMahJong.setNickName(msgBO.getFromNickname());
            gameMahJong.setCurIndex(0);
            gameMahJong.setRoomStatus(RoomStatusEnum.WAITING.getValue());
            gameMahJong.setCreateId(msgBO.getFromUser());
            gameMahJongService.save(gameMahJong);

            GameMahJongDetail gameMahJongDetail = new GameMahJongDetail();
            gameMahJongDetail.setBaseId(gameMahJong.getId());
            gameMahJongDetail.setBotId(msgBO.getToUser());
            gameMahJongDetail.setRoomId(msgBO.getRoomId());
            gameMahJongDetail.setWxid(msgBO.getFromUser());
            gameMahJongDetail.setNickName(msgBO.getFromNickname());
            gamaMahJongDetailService.save(gameMahJongDetail);

            HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.CREATE_SUCCESS, commandRequest.getCommand(), gameMahJong.getId(), gameMahJong.getId())).build());
        }
        // 加入房间
        Integer roomId = extractRoomId(commandRequest.getValue());
        if (Objects.nonNull(roomId)){
            // 校验
            if (checkRoom(msgBO)) return false;
            // 查询房间是否已满
            Long count = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getBotId, msgBO.getToUser()).eq(GameMahJongDetail::getBaseId, roomId).count();
            if (count>=4){
                HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.THE_ROOM_IS_FULL_OF_PEOPLE, commandRequest.getCommand(),roomId)).build());
                return false;
            }
            // 查询房间是否存在
            GameMahJong one = gameMahJongService.lambdaQuery().eq(GameMahJong::getId, roomId).one();
            if (Objects.isNull(one)) return false;
            // 查询是否加入房间
            GameMahJongDetail mahJongDetail = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getBotId, msgBO.getToUser()).eq(GameMahJongDetail::getBaseId, one.getId()).eq(GameMahJongDetail::getWxid, msgBO.getFromUser()).one();
            if (Objects.nonNull(mahJongDetail)){
                HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.YOU_HAVE_ALREADY_JOINED_THE_ROOM, commandRequest.getCommand(),one.getId())).build());
                return false;
            }
            // 加入房间
            GameMahJongDetail gameMahJongDetail = new GameMahJongDetail();
            gameMahJongDetail.setBaseId(one.getId());
            gameMahJongDetail.setBotId(msgBO.getToUser());
            gameMahJongDetail.setRoomId(msgBO.getRoomId());
            gameMahJongDetail.setWxid(msgBO.getFromUser());
            gameMahJongDetail.setNickName(msgBO.getFromNickname());
            gamaMahJongDetailService.save(gameMahJongDetail);

            boolean update = gameMahJongService.updateById(one);
            if (!update){
                throw new IllegalStateException(StrFormatter.format(TextContant.TOO_MANY_PARTICIPANTS, commandRequest.getCommand()));
            }
            // 加入成功通知
            long sy = 4 - count - 1;
            if (sy>0) {
                HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.ROOM_ADDED_SUCCESSFULLY, commandRequest.getCommand(), roomId, sy)).build());
            } else {
                // 自动开始
                Deque<MahjongDaqueUtils.Tile> wall = new ArrayDeque<>(MahjongDaqueUtils.shuffle(MahjongDaqueUtils.buildWall()));
                one.setTotalCardWall(MahjongDaqueUtils.wallToStrings(wall));
                List<GameMahJongDetail> gameMahJongDetails = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getBotId, msgBO.getToUser()).eq(GameMahJongDetail::getBaseId, roomId).list();
                Collections.shuffle(gameMahJongDetails);
                List<List<MahjongDaqueUtils.Tile>> hands = MahjongDaqueUtils.deal(wall, 4, 0);
                for (int i = 0; i < gameMahJongDetails.size(); i++) {
                    GameMahJongDetail jongDetail = gameMahJongDetails.get(i);
                    jongDetail.setCurIndex(i+1);
                    List<MahjongDaqueUtils.Tile> tiles = hands.get(i);
                    jongDetail.setTotalCardWall(MahjongDaqueUtils.tilesToJson(tiles));
                    jongDetail.setCurCardWall(MahjongDaqueUtils.tilesToJson(tiles));
                    jongDetail.setRoomStatus(RoomStatusEnum.STARTED.getValue());
                    gamaMahJongDetailService.updateById(jongDetail);
                }
                one.setCurCardWall(MahjongDaqueUtils.wallToStrings(wall));
                one.setRoomStatus(RoomStatusEnum.STARTED.getValue());
                gameMahJongService.updateById(one);
                HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.THE_GAME_HAS_STARTED, commandRequest.getCommand(), roomId, gameMahJongDetails.get(0).getNickName(), gameMahJongDetails.get(1).getNickName(), gameMahJongDetails.get(2).getNickName(), gameMahJongDetails.get(3).getNickName())).build());
                // 私发手牌
                for (GameMahJongDetail jongDetail : gameMahJongDetails) {
                    List<MahjongDaqueUtils.Tile> tiles = MahjongDaqueUtils.jsonToTiles(jongDetail.getCurCardWall());
                    HookRequestUtil.sendTextMsg(msgBO.getToUser(), SendTextMsgDTO.builder().wxid(jongDetail.getWxid()).msg(StrFormatter.format(TextContant.START_YOUR_HAND_IS_AS_FOLLOWS, commandRequest.getCommand(), roomId,tiles ,MahjongDaqueUtils.suggestMissingSuit(tiles).toChinese())).build());
                }
            }
        }
        return true;
    }

    private boolean checkRoom(MsgBO msgBO) {
        Long roomIng = gamaMahJongDetailService.lambdaQuery().eq(GameMahJongDetail::getWxid, msgBO.getFromUser()).eq(GameMahJongDetail::getRoomStatus, RoomStatusEnum.STARTED.getValue()).count();
        if (roomIng>0){
            HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.FAILED_TO_CREATE_OR_ADD_ROOM)).build());
            return true;
        }
        return false;
    }


    public static Integer extractRoomId(String text) {
        if (text == null) return null;

        // 统一替换中文全角符号为英文符号
        text = text.replaceAll("[＝=]", "=").replaceAll("　", " ").trim();

        // 支持“加入房间6”或“加入房间=6”或“加入房间 6”
        String pattern = "加入房间\\s*=?\\s*(\\d+)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

}
