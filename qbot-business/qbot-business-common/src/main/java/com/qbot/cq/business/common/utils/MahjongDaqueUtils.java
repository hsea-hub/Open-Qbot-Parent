package com.qbot.cq.business.common.utils;

import java.util.*;

public final class MahjongDaqueUtils {

    /* ──────────────────────────── 枚举 & 基础结构 ─────────────────────────── */

    /**
     * 四川麻将三门花色（升序：万→条→筒）
     */
    public enum Suit {WAN, TIAO, TONG}

    /**
     * 用户动作枚举
     */
    public enum Action { DISCARD, PENG, GANG, HU, DRAW }

    /**
     * 不可变单张麻将牌
     */
    public static final class Tile {
        public final Suit suit;   // 花色
        public final int rank;   // 点数 1–9

        private Tile(Suit suit, int rank) {
            this.suit = suit;
            this.rank = rank;
        }

        @Override
        public String toString() {
            return rank + (suit == Suit.WAN ? "万" : suit == Suit.TIAO ? "条" : "筒");
        }

        @Override
        public int hashCode() {
            return Objects.hash(suit, rank);
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Tile) && suit == ((Tile) o).suit && rank == ((Tile) o).rank;
        }
    }

    public static final class PlayerState {
        public final List<Tile> hand;   // 已排序手牌
        public final Suit missing;// 缺门

        private PlayerState(List<Tile> hand, Suit missing) {
            this.hand = Collections.unmodifiableList(hand);
            this.missing = missing;
        }
    }

    private MahjongDaqueUtils() {
    }

    /* ──────────────────────── 牌面字符串 ↔ Tile ─────────────────────── */
    public static Tile parseTile(String s) {
        Objects.requireNonNull(s, "tile string null");
        s = s.trim();
        if (s.length() != 2)
            throw new IllegalArgumentException("牌面格式须为数字+汉字，如 1万");
        char a = s.charAt(0), b = s.charAt(1);
        int rank;
        Suit suit;
        if (Character.isDigit(a)) {
            rank = a - '0';
            suit = charToSuit(b);
        } else if (Character.isDigit(b)) {
            rank = b - '0';
            suit = charToSuit(a);
        } else throw new IllegalArgumentException("缺少数字: " + s);
        if (rank < 1 || rank > 9)
            throw new IllegalArgumentException("点数须 1–9: " + s);
        return new Tile(suit, rank);
    }

    public static String tileToString(Tile t) {
        return t.toString();
    }

    /**
     * 动作字符串 → {@link Action}（未知/空 = DISCARD）
     */
    public static Action parseAction(String s) {
        if (s == null) return Action.DISCARD;
        switch (s.trim()) {
            case "碰": return Action.PENG;
            case "杠": return Action.GANG;
            case "胡": return Action.HU;
//            case "摸": return Action.DRAW;
            default :  return Action.DISCARD;
        }
    }

    private static Suit charToSuit(char c) {
        switch (c) {
            case '万':
                return Suit.WAN;
            case '条':
                return Suit.TIAO;
            case '筒':
                return Suit.TONG;
            default:
                throw new IllegalArgumentException("未知花色:" + c);
        }
    }

    /* ──────────────────────── 牌墙生成 / 持久化 ─────────────────────── */

    /**
     * 生成一副 108 张（万/条/筒 ×4×9）基础牌墙
     */
    public static List<Tile> buildWall() {
        List<Tile> wall = new ArrayList<>(108);
        for (Suit s : Suit.values())
            for (int r = 1; r <= 9; r++)
                for (int i = 0; i < 4; i++) wall.add(new Tile(s, r));
        return wall;
    }

    /**
     * 打乱牌墙，返回新 List
     */
    public static List<Tile> shuffle(Collection<Tile> raw) {
        List<Tile> copy = new ArrayList<>(raw);
        Collections.shuffle(copy);
        return copy;
    }

    /**
     * 数据库字符串列表
     */
    public static Deque<Tile> wallFromStrings(Collection<String> strs) {
        Deque<Tile> dq = new ArrayDeque<>();
        strs.forEach(s -> dq.add(parseTile(s)));
        return dq;
    }

    /**
     * 牌墙
     */
    public static List<String> wallToStrings(Deque<Tile> wall) {
        List<String> list = new ArrayList<>(wall.size());
        Iterator<Tile> it = wall.descendingIterator(); // top → bottom
        while (it.hasNext()) list.add(tileToString(it.next()));
        Collections.reverse(list);                     // 调整 list[0] = top
        return list;
    }

    /**
     * 发牌：庄家 14 张，其余 13 张；返回各家手牌
     */
    public static List<List<Tile>> deal(Deque<Tile> wall, int playerCnt, int dealerIdx) {
        List<List<Tile>> hands = new ArrayList<>(playerCnt);
        for (int i = 0; i < playerCnt; i++) hands.add(new ArrayList<>());
        for (int r = 0; r < 13; r++)
            for (int p = 0; p < playerCnt; p++) hands.get(p).add(wall.pop());
        hands.get(dealerIdx).add(wall.pop());
        hands.forEach(h -> h.sort(tileComparator()));
        return hands;
    }
    /**
     * 计算下一出牌玩家的索引。
     * 轮转顺序：当前玩家（0-based）→ 下一个（逆时针方向），到最后一位后回到 0。
     *
     * @param currentPlayer 当前出牌玩家索引（0–playerCount-1）
     * @param playerCount   总玩家数（通常为4）
     * @return               下一出牌玩家的索引
     */
    public static int nextPlayer(int currentPlayer, int playerCount) {
        if (playerCount <= 0) {
            throw new IllegalArgumentException("playerCount 必须大于 0");
        }
        return (currentPlayer + 1) % playerCount;
    }
    /* ────────────────────────── 玩家动作 ────────────────────────── */

    /**
     * 摸牌
     */
    public static Tile draw(List<Tile> hand, Deque<Tile> wall) {
        Tile t = wall.pop();
        hand.add(t);
        hand.sort(tileComparator());
        return t;
    }

    /**
     * 出牌：移除指定牌并返回新手牌
     */
    public static List<Tile> discard(List<Tile> hand, Tile tile) {
        if (!hand.remove(tile))
            throw new IllegalArgumentException("手中无此牌:" + tile);
        return new ArrayList<>(hand);
    }

    /**
     * 碰牌：手中需两张同牌
     */
    public static List<Tile> peng(List<Tile> hand, Tile tile) {
        long cnt = hand.stream().filter(tile::equals).count();
        if (cnt < 2) throw new IllegalStateException("无法碰:" + tile);
        for (int i = 0; i < 2; i++) hand.remove(tile);
        return new ArrayList<>(hand);
    }

    /**
     * 杠牌
     */
    public static List<Tile> gang(List<Tile> hand, Tile tile, boolean selfDraw) {
        long cnt = hand.stream().filter(tile::equals).count();
        if (selfDraw) {
            if (cnt != 4) throw new IllegalStateException("不能暗杠:" + tile);
            for (int i = 0; i < 4; i++) hand.remove(tile);
        } else {
            if (cnt != 3) throw new IllegalStateException("不能明杠:" + tile);
            for (int i = 0; i < 3; i++) hand.remove(tile);
        }
        return new ArrayList<>(hand);
    }

    /**
     * 快捷自摸胡牌判断
     */
    public static boolean hu(List<Tile> hand, Suit miss) {
        return canHu(new PlayerState(hand, miss), null);
    }

    /* ────────────────────────── 规则判定工具 ────────────────────────── */

    /**
     * 升序比较器
     */
    public static Comparator<Tile> tileComparator() {
        return Comparator.<Tile>comparingInt(t -> t.suit.ordinal()).thenComparingInt(t -> t.rank);
    }

    public static boolean canDiscard(PlayerState ps, Tile cand) {
        return ps.hand.contains(cand) && !(hasSuit(ps.hand, ps.missing) && cand.suit != ps.missing);
    }

    public static boolean canPeng(PlayerState ps, Tile in) {
        return ps.hand.stream().filter(in::equals).count() >= 2;
    }

    public static boolean canGang(PlayerState ps, Tile in, boolean selfDraw) {
        long c = ps.hand.stream().filter(in::equals).count();
        return selfDraw ? c == 4 : c == 3;
    }

    public static boolean canHu(PlayerState ps, Tile in) {
        if (hasSuit(ps.hand, ps.missing) || (in != null && in.suit == ps.missing)) return false;
        List<Tile> all = new ArrayList<>(ps.hand);
        if (in != null) all.add(in);
        all.sort(tileComparator());
        return isSevenPairs(all) || isStandardHu(all);
    }

    /* ──────────────────────────建议 & 缺门 & 快照 ────────────────────────── */

    /**
     * 建议出牌：
     *  1. 首选 缺门 花色的牌
     *  2. 其次 找“孤张”——手中只有 1 张且前后张都没有
     *  3. 最后 按照“连接数”（同刻 + 顺子可能性）最少原则
     *
     * @param hand    当前已排序手牌
     * @param missing 玩家缺门花色
     * @return        建议丢弃的牌 Tile
     */
    public static Tile suggestDiscard(List<Tile> hand, Suit missing) {
        // 1. 丢缺门
        for (Tile t : hand) {
            if (t.suit == missing) {
                return t;
            }
        }
        // 2. 丢孤张：无刻子也无顺子可能
        for (Tile t : hand) {
            if (countSame(hand, t) == 1
                    && find(hand, t.suit, t.rank - 1) == null
                    && find(hand, t.suit, t.rank + 1) == null) {
                return t;
            }
        }
        // 3. 按连接数最少原则：score = 同张数×2 + 邻张数
        Tile best = hand.get(0);
        int minScore = Integer.MAX_VALUE;
        for (Tile t : hand) {
            int same = countSame(hand, t);
            int adj  = (find(hand, t.suit, t.rank - 1) != null ? 1 : 0)
                    + (find(hand, t.suit, t.rank + 1) != null ? 1 : 0);
            int score = same * 2 + adj;
            if (score < minScore) {
                minScore = score;
                best = t;
            }
        }
        return best;
    }

    /**
     * 从手牌最少花色随机选择缺门
     */
    public static Suit suggestMissingSuit(Collection<Tile> hand) {
        EnumMap<Suit, Long> cnt = new EnumMap<>(Suit.class);
        for (Suit s : Suit.values()) cnt.put(s, 0L);
        hand.forEach(t -> cnt.merge(t.suit, 1L, Long::sum));
        long min = Collections.min(cnt.values());
        List<Suit> opts = new ArrayList<>();
        cnt.forEach((s, v) -> {
            if (v == min) opts.add(s);
        });
        return opts.get(new Random().nextInt(opts.size()));
    }

    /**
     * 数据库手牌字段 → {@link PlayerState}
     */
    public static PlayerState playerStateFromStrings(Collection<String> strs, Suit miss) {
        List<Tile> list = new ArrayList<>();
        strs.forEach(s -> list.add(parseTile(s)));
        list.sort(tileComparator());
        return new PlayerState(list, miss);
    }

    /* ────────────────────────── 内部拆牌算法 ────────────────────────── */

    private static boolean hasSuit(Collection<Tile> tiles, Suit s) {
        return tiles.stream().anyMatch(t -> t.suit == s);
    }

    private static boolean isSevenPairs(List<Tile> ts) {
        if (ts.size() != 14) return false;
        for (int i = 0; i < 14; i += 2) if (!ts.get(i).equals(ts.get(i + 1))) return false;
        return true;
    }

    private static boolean isStandardHu(List<Tile> ts) {
        return split(ts, false);
    }

    /**
     * 递归拆分 4 面子 1 将
     */
    private static boolean split(List<Tile> tiles, boolean hasPair) {
        if (tiles.isEmpty()) return hasPair;
        Tile f = tiles.get(0);
        // 将
        if (!hasPair && countSame(tiles, f) >= 2) {
            List<Tile> rest = new ArrayList<>(tiles);
            rest.remove(f);
            rest.remove(f);
            if (split(rest, true)) return true;
        }
        // 刻子
        if (countSame(tiles, f) >= 3) {
            List<Tile> rest = new ArrayList<>(tiles);
            for (int i = 0; i < 3; i++) rest.remove(f);
            if (split(rest, hasPair)) return true;
        }
        // 顺子
        Tile n2 = find(tiles, f.suit, f.rank + 1);
        Tile n3 = find(tiles, f.suit, f.rank + 2);
        if (n2 != null && n3 != null) {
            List<Tile> rest = new ArrayList<>(tiles);
            rest.remove(f);
            rest.remove(n2);
            rest.remove(n3);
            if (split(rest, hasPair)) return true;
        }
        return false;
    }

    private static int countSame(List<Tile> tiles, Tile ref) {
        return (int) tiles.stream().filter(ref::equals).count();
    }

    private static Tile find(List<Tile> tiles, Suit suit, int rank) {
        return tiles.stream().filter(t -> t.suit == suit && t.rank == rank).findFirst().orElse(null);
    }


    /* ───────────── ③ 改进版 handleCommand ───────────── */
    /**
     * 根据玩家字符串指令执行操作，并返回最新手牌
     *
     * 支持的简写：
     *   ● “摸”                 → 自家摸牌
     *   ● “碰 / 杠 / 胡”       → 直接对上一张外来牌操作
     *   ● “7条 出” / “7条”     → 打 7 条
     *   ● “1万 碰” / “3筒 杠”  → 显式带牌也兼容
     *
     * @param cmd       用户指令字符串
     * @param hand      当前玩家手牌（会被原地修改）
     * @param wall      牌墙（摸牌/暗杠需要，外部确保非空）
     * @param missing   玩家缺门
     * @param incoming  上家刚打出的牌（自摸时传 null）
     * @param selfDraw  true = 本轮为自摸回合（摸牌后可暗杠、胡自摸）
     * @return          新的手牌 List（已排序）
     */
    public static List<Tile> handleCommand(
            String      cmd,
            List<Tile>  hand,
            Deque<Tile> wall,
            Suit        missing,
            Tile        incoming,
            boolean     selfDraw) {

        // ① 解析指令
        String[] parts = cmd.trim().split("\\s+");
        Action action;
        Tile   tile = null;

        if (parts.length == 1) {                         // 只有一个单词
            action = parseAction(parts[0]);
            if (action == Action.DISCARD) {              // 单词其实是牌面（如“7条”）
                tile   = parseTile(parts[0]);
                action = Action.DISCARD;
            }  else {                                     // “碰 / 杠 / 胡”
                tile = Objects.requireNonNull(
                        incoming, "缺少上一张外来牌，无法执行 " + action);
            }
        } else {                                        // “牌 动作”格式
            tile   = parseTile(parts[0]);
            action = parseAction(parts[1]);
        }

        // ② 执行动作
        switch (action) {
            case DRAW:
                if (wall == null || wall.isEmpty())
                    throw new IllegalStateException("牌墙已空，无法摸牌");
                draw(hand, wall);
                break;

            case DISCARD:
                discard(hand, tile);
                break;

            case PENG:
                if (!canPeng(new PlayerState(hand, missing), tile))
                    throw new IllegalStateException("不能碰这张牌");
                peng(hand, tile);
                break;

            case GANG:
                if (!canGang(new PlayerState(hand, missing), tile, selfDraw))
                    throw new IllegalStateException("不能杠这张牌");
                gang(hand, tile, selfDraw);
                break;

            case HU:
                if (!canHu(new PlayerState(hand, missing), tile))
                    throw new IllegalStateException("现在还胡不了");
                break;

            default:
                throw new IllegalArgumentException("未知动作: " + cmd);
        }

        // 返回最新手牌（深拷贝）
        List<Tile> out = new ArrayList<>(hand);
        out.sort(tileComparator());
        return out;
    }

    public static void main(String[] args) {
        /*
         * 简易文字演示：
         * - 只控制东家（玩家 0），其余 3 家由程序随机打牌
         * - 支持指令：摸 / 碰 / 杠 / 胡 / 7条（出牌） / 7条 出
         * - 输入 exit 退出
         */
        Deque<Tile> wall = new ArrayDeque<>(shuffle(buildWall()));
        List<List<Tile>> hands = deal(wall, 4, 0);      // 4 人，东家 0
        List<Tile> myHand = hands.get(0);
        Suit miss = suggestMissingSuit(myHand);
        Tile incoming = null;   // 上家打出的牌

        System.out.println("=== 四川麻将文字演示 ===");
        System.out.println("你的缺门: " + miss);
        System.out.println("起手: " + myHand);
        System.out.println("指令示例: 碰 / 杠 / 胡 / 7条");

        try (java.util.Scanner sc = new java.util.Scanner(System.in)) {
            boolean selfDraw = true;   // 第一轮视为自摸
            while (true) {
                System.out.print(">>> ");
                String cmd = sc.nextLine().trim();
                if (cmd.equalsIgnoreCase("exit")) break;
                try {
                    myHand = handleCommand(cmd, myHand, wall, miss, incoming, selfDraw);
                    System.out.println("当前手牌: " + myHand);
                    if (cmd.contains("胡")) {
                        System.out.println("🎉 胡牌！游戏结束");
                        break;
                    }
                    // 简单模拟其它玩家打出一张随机牌
                    if (!wall.isEmpty()) {
                        incoming = wall.pop();
                        System.out.println("上家打出: " + incoming);
                    } else {
                        System.out.println("牌墙摸尽，流局");
                        break;
                    }
                    selfDraw = false;  // 之后回合非自摸
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
            }
        }
    }
}