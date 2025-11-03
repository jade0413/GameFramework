package com.yp.gameframwrok.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yyp
 */
public class CardsDefine {
    /**
     * 十六进制表示法: 牌. 花色: 1~4(方块~黑桃) 数值: A~K(1点~13点) 表示方式: 16进制表示法，如, 黑桃K(0x4d=77), 解:
     * 黑桃: int color = 77 >> 4 = 4; K: int number = 77 & 0xf = 13;
     */
    public static List<Integer> cardGroup;

    static {
        int[] cardArray = new int[]{
                // 方块A~黑桃A
                0x11, 0x21, 0x31, 0x41,
                // 方块2~黑桃2
                0x12, 0x22, 0x32, 0x42,
                // 方块3~黑桃3
                0x13, 0x23, 0x33, 0x43,
                // 方块4~黑桃4
                0x14, 0x24, 0x34, 0x44,
                // 方块5~黑桃5
                0x15, 0x25, 0x35, 0x45,
                // 方块6~黑桃6
                0x16, 0x26, 0x36, 0x46,
                // 方块7~黑桃7
                0x17, 0x27, 0x37, 0x47,
                // 方块8~黑桃8
                0x18, 0x28, 0x38, 0x48,
                // 方块9~黑桃9
                0x19, 0x29, 0x39, 0x49,
                // 方块10~黑桃10
                0x1A, 0x2A, 0x3A, 0x4A,
                // 方块J~黑桃J
                0x1B, 0x2B, 0x3B, 0x4B,
                // 方块Q~黑桃Q
                0x1C, 0x2C, 0x3C, 0x4C,
                // 方块K~黑桃K
                0x1D, 0x2D, 0x3D, 0x4D};
        cardGroup = new ArrayList<>();
        for (int card : cardArray) {
            cardGroup.add(card);
        }
    }

    public static final int Card_A = 1;
    public static final int Card_2 = 2;
    public static final int Card_3 = 3;
    public static final int Card_4 = 4;
    public static final int Card_5 = 5;
    public static final int Card_6 = 6;
    public static final int Card_7 = 7;
    public static final int Card_8 = 8;
    public static final int Card_9 = 9;
    public static final int Card_10 = 10;
    public static final int Card_J = 11;
    public static final int Card_Q = 12;
    public static final int Card_K = 13;

    public static final int Color_FangKuai = 1;
    public static final int Color_MeiHua = 2;
    public static final int Color_HongTao = 3;
    public static final int Color_HeiTao = 4;

    public static int getCard(int card, int color) {
        return color << 4 | card;
    }

    /**
     * 获得牌的点数
     */
    public static int getWeight(int card) {
        return card & 0xf;
    }

    /**
     * 获取需要对A做特殊点数的。A作为14点，也就是最大点数使用
     */
    public static int getCardAMaxWeight(int card) {
        int weight = card & 0xf;
        return weight == 1 ? 14 : weight;
    }

    public static int getSuit(int card) {
        return card >> 4;
    }

    public static List<Integer> toList() {
        return new LinkedList<>(cardGroup);
    }

    public static List<Integer> getAndShuttleCardPool() {
        List<Integer> cardPool = new LinkedList<>(cardGroup);
        Collections.shuffle(cardPool);
        return cardPool;
    }

    public static void sortCards(List<Integer> cards, boolean reverseSort) {
        if (reverseSort) {
            Collections.sort(cards, Comparator.comparingInt(CardsDefine::getCardAMaxWeight).reversed());
        } else {
            Collections.sort(cards, Comparator.comparingInt(CardsDefine::getCardAMaxWeight));
        }
    }

    public static List<String> cards2StringCards(List<Integer> cards) {
        return cards2StringCards(cards, false);
    }

    public static List<String> cards2StringCards(List<Integer> cards, boolean noColor) {
        List<String> cds = new ArrayList<>();
        for (int card : cards) {
            int cardWight = getWeight(card);
            int color = getSuit(card);
            StringBuilder builder;
            if (noColor) {
                builder = new StringBuilder();
            } else {
                builder = new StringBuilder(color2String(color));
            }
            builder.append(cardWight);
            cds.add(builder.toString());
        }
        return cds;
    }

    public static String color2String(int color) {
        String colorStr = "";
        switch (color) {
            case Color_FangKuai:
                colorStr = "◆";
                break;

            case Color_HeiTao:
                colorStr = "♠";
                break;

            case Color_HongTao:
                colorStr = "♥";
                break;

            case Color_MeiHua:
                colorStr = "♣";
                break;

            default:
                break;
        }
        return colorStr;
    }
}
