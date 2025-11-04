package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.CardsDefine;

import java.util.Arrays;

/**
 * 炸金花牌型比较工具类
 * 牌型从大到小：豹子(三条) > 顺金(同花顺) > 金花(同花) > 顺子 > 对子 > 散牌
 * 
 * @author yyp
 */
public class ZjhUtils {

    // 牌型定义
    private static final int TYPE_BAO_ZI = 6;      // 豹子（三条）
    private static final int TYPE_SHUN_JIN = 5;    // 顺金（同花顺）
    private static final int TYPE_JIN_HUA = 4;     // 金花（同花）
    private static final int TYPE_SHUN_ZI = 3;     // 顺子
    private static final int TYPE_DUI_ZI = 2;      // 对子
    private static final int TYPE_SAN_PAI = 1;     // 散牌

    /**
     * 比较两副牌的大小
     * 
     * @param cards1 第一副牌（3张）
     * @param cards2 第二副牌（3张）
     * @return 1: cards1 > cards2, -1: cards1 < cards2, 0: cards1 == cards2
     */
    public static int comparerCard(int[] cards1, int[] cards2) {
        if (cards1 == null || cards2 == null || cards1.length != 3 || cards2.length != 3) {
            throw new IllegalArgumentException("每副牌必须是3张");
        }

        // 获取两副牌的牌型和权重
        CardType type1 = getCardType(cards1);
        CardType type2 = getCardType(cards2);

        // 先比较牌型
        if (type1.type != type2.type) {
            return Integer.compare(type1.type, type2.type);
        }

        // 牌型相同，比较权重
        return compareWeights(type1.weights, type2.weights);
    }

    /**
     * 获取牌型和权重
     */
    private static CardType getCardType(int[] cards) {
        // 复制并排序（按A为最大的规则）
        Integer[] sortedCards = new Integer[3];
        for (int i = 0; i < 3; i++) {
            sortedCards[i] = cards[i];
        }
        Arrays.sort(sortedCards, (a, b) -> Integer.compare(
            CardsDefine.getCardAMaxWeight(b),
            CardsDefine.getCardAMaxWeight(a)
        ));
        // 点数数组（按A为最大的规则）
        int[] weights = new int[3];
        // 花色数组
        int[] colors = new int[3];
        for (int i = 0; i < 3; i++) {
            weights[i] = CardsDefine.getCardAMaxWeight(sortedCards[i]);
            colors[i] = CardsDefine.getSuit(sortedCards[i]);
        }

        boolean isSameColor = colors[0] == colors[1] && colors[1] == colors[2];
        boolean isSequence = checkSequence(weights);
        boolean isTriple = weights[0] == weights[1] && weights[1] == weights[2];
        boolean isPair = weights[0] == weights[1] || weights[1] == weights[2] || weights[0] == weights[2];

        // 判断牌型
        if (isTriple) {
            // 豹子（三条）
            return new CardType(TYPE_BAO_ZI, new int[]{weights[0]});
        } else if (isSameColor && isSequence) {
            // 顺金（同花顺）- 特殊处理 A23
            if (isA23Sequence(weights)) {
                return new CardType(TYPE_SHUN_JIN, new int[]{3, 2, 1}); // A23 最小
            }
            return new CardType(TYPE_SHUN_JIN, weights);
        } else if (isSameColor) {
            // 金花（同花）
            return new CardType(TYPE_JIN_HUA, weights);
        } else if (isSequence) {
            // 顺子 - 特殊处理 A23
            if (isA23Sequence(weights)) {
                return new CardType(TYPE_SHUN_ZI, new int[]{3, 2, 1}); // A23 最小
            }
            return new CardType(TYPE_SHUN_ZI, weights);
        } else if (isPair) {
            // 对子
            return getPairType(weights);
        } else {
            // 散牌
            return new CardType(TYPE_SAN_PAI, weights);
        }
    }

    /**
     * 检查是否是顺子（包括 A23 的特殊情况）
     */
    private static boolean checkSequence(int[] weights) {
        // 正常顺子：三张牌连续
        if (weights[0] - weights[1] == 1 && weights[1] - weights[2] == 1) {
            return true;
        }
        // 特殊顺子：A23 (14, 3, 2)
        return isA23Sequence(weights);
    }

    /**
     * 检查是否是 A23
     */
    private static boolean isA23Sequence(int[] weights) {
        int[] sorted = weights.clone();
        Arrays.sort(sorted);
        return sorted[0] == 2 && sorted[1] == 3 && sorted[2] == 14;
    }

    /**
     * 获取对子牌型（对子在前，单牌在后）
     */
    private static CardType getPairType(int[] weights) {
        int[] result = new int[3];
        
        if (weights[0] == weights[1]) {
            // 前两张是对子
            result[0] = weights[0];
            result[1] = weights[0];
            result[2] = weights[2];
        } else if (weights[1] == weights[2]) {
            // 后两张是对子
            result[0] = weights[1];
            result[1] = weights[1];
            result[2] = weights[0];
        } else if (weights[0] == weights[2]) {
            // 第一张和第三张是对子（不太可能，因为已排序）
            result[0] = weights[0];
            result[1] = weights[0];
            result[2] = weights[1];
        }
        
        return new CardType(TYPE_DUI_ZI, result);
    }

    /**
     * 比较权重数组
     */
    private static int compareWeights(int[] weights1, int[] weights2) {
        for (int i = 0; i < Math.min(weights1.length, weights2.length); i++) {
            if (weights1[i] != weights2[i]) {
                return Integer.compare(weights1[i], weights2[i]);
            }
        }
        return 0;
    }

    /**
     * 牌型类
     */
    private static class CardType {
        int type;       // 牌型
        int[] weights;  // 权重数组（从大到小）

        CardType(int type, int[] weights) {
            this.type = type;
            this.weights = weights;
        }
    }

    /**
     * 获取牌型名称（用于调试）
     */
    public static String getCardTypeName(int[] cards) {
        CardType type = getCardType(cards);
        switch (type.type) {
            case TYPE_BAO_ZI:
                return "豹子";
            case TYPE_SHUN_JIN:
                return "顺金";
            case TYPE_JIN_HUA:
                return "金花";
            case TYPE_SHUN_ZI:
                return "顺子";
            case TYPE_DUI_ZI:
                return "对子";
            case TYPE_SAN_PAI:
                return "散牌";
            default:
                return "未知";
        }
    }

    /**
     * 打印牌型详情（用于调试）
     */
    public static String printCards(int[] cards) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < cards.length; i++) {
            int color = CardsDefine.getSuit(cards[i]);
            int weight = CardsDefine.getWeight(cards[i]);
            sb.append(CardsDefine.color2String(color));
            if (weight == 1) {
                sb.append("A");
            } else if (weight == 11) {
                sb.append("J");
            } else if (weight == 12) {
                sb.append("Q");
            } else if (weight == 13) {
                sb.append("K");
            } else {
                sb.append(weight);
            }
            if (i < cards.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
