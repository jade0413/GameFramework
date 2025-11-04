package com.yp.gameframwrok.game.logic.zjh;

import com.yp.gameframwrok.game.CardsDefine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 炸金花比较器测试类
 */
public class ZjhUtilsTest {

    @Test
    public void testBaoZi() {
        // 豹子 AAA vs KKK，AAA更大
        int[] cards1 = {0x11, 0x21, 0x31}; // 三个A
        int[] cards2 = {0x1D, 0x2D, 0x3D}; // 三个K
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AAA应该大于KKK");
        
        System.out.println("豹子测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
        System.out.println("牌型: " + ZjhUtils.getCardTypeName(cards1) + " vs " + 
                          ZjhUtils.getCardTypeName(cards2));
    }

    @Test
    public void testShunJin() {
        // 顺金 AKQ vs QJT，AKQ更大
        int[] cards1 = {0x11, 0x1D, 0x1C}; // 方块 A K Q
        int[] cards2 = {0x1C, 0x1B, 0x1A}; // 方块 Q J 10
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AKQ顺金应该大于QJ10顺金");
        
        System.out.println("\n顺金测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
    }

    @Test
    public void testShunJinA23() {
        // 顺金 A23 vs 345，345更大（A23是最小的顺子）
        int[] cards1 = {0x11, 0x12, 0x13}; // 方块 A 2 3
        int[] cards2 = {0x13, 0x14, 0x15}; // 方块 3 4 5
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result < 0, "A23顺金应该小于345顺金");
        
        System.out.println("\n顺金A23测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
    }

    @Test
    public void testJinHua() {
        // 金花 AJ9 vs AJ8，AJ9更大
        int[] cards1 = {0x11, 0x1B, 0x19}; // 方块 A J 9
        int[] cards2 = {0x11, 0x1B, 0x18}; // 方块 A J 8
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AJ9金花应该大于AJ8金花");
        
        System.out.println("\n金花测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
    }

    @Test
    public void testShunZi() {
        // 顺子 KQJ vs 789
        int[] cards1 = {0x1D, 0x2C, 0x3B}; // K Q J 不同花色
        int[] cards2 = {0x17, 0x28, 0x39}; // 7 8 9 不同花色
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "KQJ顺子应该大于789顺子");
        
        System.out.println("\n顺子测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
        System.out.println("牌型: " + ZjhUtils.getCardTypeName(cards1) + " vs " + 
                          ZjhUtils.getCardTypeName(cards2));
    }

    @Test
    public void testDuiZi() {
        // 对子 AAK vs KKA，AAK更大
        int[] cards1 = {0x11, 0x21, 0x1D}; // A A K
        int[] cards2 = {0x1D, 0x2D, 0x11}; // K K A
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AAK对子应该大于KKA对子");
        
        System.out.println("\n对子测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
        System.out.println("牌型: " + ZjhUtils.getCardTypeName(cards1) + " vs " + 
                          ZjhUtils.getCardTypeName(cards2));
    }

    @Test
    public void testDuiZiSameValue() {
        // 对子 AAK vs AAQ，AAK更大
        int[] cards1 = {0x11, 0x21, 0x1D}; // A A K
        int[] cards2 = {0x31, 0x41, 0x1C}; // A A Q
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AAK对子应该大于AAQ对子（单牌K>Q）");
        
        System.out.println("\n对子单牌测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
    }

    @Test
    public void testSanPai() {
        // 散牌 AKJ vs AK9
        int[] cards1 = {0x11, 0x2D, 0x3B}; // A K J 不同花色
        int[] cards2 = {0x21, 0x1D, 0x29}; // A K 9 不同花色
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertTrue(result > 0, "AKJ散牌应该大于AK9散牌");
        
        System.out.println("\n散牌测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
        System.out.println("牌型: " + ZjhUtils.getCardTypeName(cards1) + " vs " + 
                          ZjhUtils.getCardTypeName(cards2));
    }

    @Test
    public void testDifferentTypes() {
        // 不同牌型比较：豹子 > 顺金 > 金花 > 顺子 > 对子 > 散牌
        int[] baoZi = {0x12, 0x22, 0x32};      // 三个2（豹子）
        int[] shunJin = {0x11, 0x1D, 0x1C};    // 方块AKQ（顺金）
        int[] jinHua = {0x11, 0x1B, 0x19};     // 方块AJ9（金花）
        int[] shunZi = {0x11, 0x2D, 0x3C};     // AKQ不同花（顺子）
        int[] duiZi = {0x1D, 0x2D, 0x11};      // KKA（对子）
        int[] sanPai = {0x11, 0x2C, 0x3A};     // AQ10不同花（散牌）
        
        assertTrue(ZjhUtils.comparerCard(baoZi, shunJin) > 0, "豹子 > 顺金");
        assertTrue(ZjhUtils.comparerCard(shunJin, jinHua) > 0, "顺金 > 金花");
        assertTrue(ZjhUtils.comparerCard(jinHua, shunZi) > 0, "金花 > 顺子");
        assertTrue(ZjhUtils.comparerCard(shunZi, duiZi) > 0, "顺子 > 对子");
        assertTrue(ZjhUtils.comparerCard(duiZi, sanPai) > 0, "对子 > 散牌");
        
        System.out.println("\n牌型大小测试:");
        System.out.println("豹子: " + ZjhUtils.printCards(baoZi));
        System.out.println("顺金: " + ZjhUtils.printCards(shunJin));
        System.out.println("金花: " + ZjhUtils.printCards(jinHua));
        System.out.println("顺子: " + ZjhUtils.printCards(shunZi));
        System.out.println("对子: " + ZjhUtils.printCards(duiZi));
        System.out.println("散牌: " + ZjhUtils.printCards(sanPai));
    }

    @Test
    public void testEqual() {
        // 相同的牌
        int[] cards1 = {0x11, 0x21, 0x31}; // 三个A
        int[] cards2 = {0x11, 0x21, 0x31}; // 三个A
        
        int result = ZjhUtils.comparerCard(cards1, cards2);
        assertEquals(0, result, "相同的牌应该返回0");
        
        System.out.println("\n相等测试: " + ZjhUtils.printCards(cards1) + " vs " + 
                          ZjhUtils.printCards(cards2) + " = " + result);
    }
}

