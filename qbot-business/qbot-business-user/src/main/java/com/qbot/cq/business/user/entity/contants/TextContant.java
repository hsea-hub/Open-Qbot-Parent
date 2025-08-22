package com.qbot.cq.business.user.entity.contants;

public interface TextContant {
    String THE_INSTRUCTION_HAS_EXPIRED="【{}】- 输入错误";
    String FAILED_TO_CREATE_OR_ADD_ROOM="【{}】- 创建或者加入房间失败，您上把游戏还没结束";
    String REPLACE_INSTRUCTION="已退出[{}],已加入[{}]";
    String CREATE_SUCCESS="\uD83C\uDF89 【{}】 创建新房间成功！记得加我好友，否则发不了牌！\n\n房间ID是：[{}]\n\n快来一起开局吧~\n（示例：打麻将=加入房间{}）";
    String YOU_HAVE_ALREADY_JOINED_THE_ROOM="【{}】- 您已经加入了 {} 房间,请勿重复加入";
    String THE_ROOM_IS_FULL_OF_PEOPLE="【{}】- 房间 {} 人数已满";
    String TOO_MANY_PARTICIPANTS="【{}】- 参与人数太多了，请稍后再试";
    String ROOM_ADDED_SUCCESSFULLY="【{}】- 房间 {} 加入成功，还差{}人\nTip: 加我好友才能发牌";
    String THE_GAME_HAS_STARTED="【{}】- 房间 {} 游戏已开始\n请查看私聊，如果没有好友，请加好友再发送：[手牌]\n\n1.手牌\n2.碰\n3.杠\n4.胡\n5.缺万，缺筒，缺条(定缺)\n\n本局游戏玩家：\n1.{},2.{},3.{}.4.{}";
    String START_YOUR_HAND_IS_AS_FOLLOWS="【{}】- 房间 {}\n\n您的手牌如下：\n{}\n\n玩家全部定缺后游戏开打\nTip: 缺万，缺筒，缺条(定缺)，建议定缺：[{}]";
    String SUCCESSFUL_VACANCY_DETERMINATION="【系统】: 恭喜你定缺成功,缺：{}\n\n还有 {} 未定缺...";
    String SUCCESSFUL_VACANCY_DETERMINATION_OTHER="【{}】: 缺：{}\n\n还有 {} 未定缺...";

    String SYS_BASE_MESSAGE="【系统】: {}";
    String HU_GANG_PENG="【{}】: {} \n{}";
    String OTHER_PRODUCE="【{}】: {}";
    String SUCCESSFUL_CARD_PLAYING="【系统】: 出牌 {} 成功\n\n{}";
    String CONGRATULATIONS_ON_YOUR_SUCCESSFUL_HU_CARD="【系统】: 恭喜你{}牌成功\n\n{}";

    String WAITING_FOR_THE_CARD_TO_BE_PLAYED="【系统】: 请等待 {} 出牌";
    String IT_S_YOUR_TURN_TO_PLAY="【系统】: 该你出牌了";
    String PLAYING_CARDS="【系统】: 摸牌：{}\n手牌：{}\n该你出牌了！";
}
