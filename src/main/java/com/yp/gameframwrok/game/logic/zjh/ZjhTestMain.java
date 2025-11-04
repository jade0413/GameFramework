package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.CardsDefine;

/**
 * 炸金花比较器手动测试类
 */
public class ZjhTestMain {

    public static void main(String[] args) {
        System.out.println("=================== 炸金花牌型比较器测试 ===================\n");

        // 测试1: 豹子 AAA vs KKK
        test("豹子测试", 
            new int[]{0x11, 0x21, 0x31}, // 三个A
            new int[]{0x1D, 0x2D, 0x3D}  // 三个K
        );

        // 测试2: 顺金 AKQ vs QJ10
        test("顺金测试",
            new int[]{0x11, 0x1D, 0x1C}, // 方块 A K Q
            new int[]{0x1C, 0x1B, 0x1A}  // 方块 Q J 10
        );

        // 测试3: 顺金 A23 vs 345
        test("顺金A23测试",
            new int[]{0x11, 0x12, 0x13}, // 方块 A 2 3
            new int[]{0x13, 0x14, 0x15}  // 方块 3 4 5
        );

        // 测试4: 金花 AJ9 vs AJ8
        test("金花测试",
            new int[]{0x11, 0x1B, 0x19}, // 方块 A J 9
            new int[]{0x21, 0x2B, 0x28}  // 梅花 A J 8
        );

        // 测试5: 顺子 KQJ vs 789
        test("顺子测试",
            new int[]{0x1D, 0x2C, 0x3B}, // K Q J 不同花色
            new int[]{0x17, 0x28, 0x39}  // 7 8 9 不同花色
        );

        // 测试6: 对子 AAK vs KKA
        test("对子测试",
            new int[]{0x11, 0x21, 0x1D}, // A A K
            new int[]{0x1D, 0x2D, 0x11}  // K K A
        );

        // 测试7: 对子 AAK vs AAQ
        test("对子单牌测试",
            new int[]{0x11, 0x21, 0x1D}, // A A K
            new int[]{0x31, 0x41, 0x1C}  // A A Q
        );

        // 测试8: 散牌 AKJ vs AK9
        test("散牌测试",
            new int[]{0x11, 0x2D, 0x3B}, // A K J 不同花色
            new int[]{0x21, 0x1D, 0x29}  // A K 9 不同花色
        );

        // 测试9: 不同牌型比较
        System.out.println("\n=============== 不同牌型大小测试 ===============");
        int[] baoZi = {0x12, 0x22, 0x32};      // 三个2（豹子）
        int[] shunJin = {0x11, 0x1D, 0x1C};    // 方块AKQ（顺金）
        int[] jinHua = {0x11, 0x1B, 0x19};     // 方块AJ9（金花）
        int[] shunZi = {0x11, 0x2D, 0x3C};     // AKQ不同花（顺子）
        int[] duiZi = {0x1D, 0x2D, 0x11};      // KKA（对子）
        int[] sanPai = {0x11, 0x2C, 0x3A};     // AQ10不同花（散牌）

        System.out.println("豹子: " + ZjhUtils.printCards(baoZi) + " - " + ZjhUtils.getCardTypeName(baoZi));
        System.out.println("顺金: " + ZjhUtils.printCards(shunJin) + " - " + ZjhUtils.getCardTypeName(shunJin));
        System.out.println("金花: " + ZjhUtils.printCards(jinHua) + " - " + ZjhUtils.getCardTypeName(jinHua));
        System.out.println("顺子: " + ZjhUtils.printCards(shunZi) + " - " + ZjhUtils.getCardTypeName(shunZi));
        System.out.println("对子: " + ZjhUtils.printCards(duiZi) + " - " + ZjhUtils.getCardTypeName(duiZi));
        System.out.println("散牌: " + ZjhUtils.printCards(sanPai) + " - " + ZjhUtils.getCardTypeName(sanPai));

        System.out.println("\n牌型大小关系:");
        System.out.println("豹子 > 顺金: " + (ZjhUtils.comparerCard(baoZi, shunJin) > 0));
        System.out.println("顺金 > 金花: " + (ZjhUtils.comparerCard(shunJin, jinHua) > 0));
        System.out.println("金花 > 顺子: " + (ZjhUtils.comparerCard(jinHua, shunZi) > 0));
        System.out.println("顺子 > 对子: " + (ZjhUtils.comparerCard(shunZi, duiZi) > 0));
        System.out.println("对子 > 散牌: " + (ZjhUtils.comparerCard(duiZi, sanPai) > 0));

        System.out.println("\n=================== 测试完成 ===================");
    }

    private static void test(String name, int[] cards1, int[] cards2) {
        System.out.println("【" + name + "】");
        System.out.println("  牌1: " + ZjhUtils.printCards(cards1) + " - " + ZjhUtils.getCardTypeName(cards1));
        System.out.println("  牌2: " + ZjhUtils.printCards(cards2) + " - " + ZjhUtils.getCardTypeName(cards2));
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        String resultStr;
        if (result > 0) {
            resultStr = "牌1 > 牌2 ✓";
        } else if (result < 0) {
            resultStr = "牌1 < 牌2";
        } else {
            resultStr = "牌1 = 牌2";
        }
        System.out.println("  结果: " + resultStr);
        System.out.println();
    }
}

