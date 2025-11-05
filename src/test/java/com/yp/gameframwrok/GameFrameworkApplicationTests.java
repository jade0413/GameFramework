package com.yp.gameframwrok;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@SpringBootTest
class GameFrameworkApplicationTests {

    @Autowired
    private NettyConnectionTester connectionTester;
    @Test
    void contextLoads() throws InterruptedException {
        // 测试小规模连接
        NettyConnectionTester.ConnectionTestResult result =
                connectionTester.testConnections("127.0.0.1", 8081,
                        10, 10, 0);

        printTestResult("小规模连接测试", result);

//        assertThat(result.getSuccessRate()).isGreaterThan(95);
    }
    private void printTestResult(String testName, NettyConnectionTester.ConnectionTestResult result) {
        log.info("=== {} ===", testName);
        log.info("总尝试数: {}", result.getTotalCount());
        log.info("成功数: {}", result.getSuccessCount());
        log.info("失败数: {}", result.getFailedCount());
        log.info("成功率: {:.2f}%", result.getSuccessRate());
        log.info("耗时: {}ms", result.getDurationMs());
        log.info("连接速率: {:.2f} 连接/秒", result.getConnectionsPerSecond());
        log.info("当前活跃连接: {}", result.getCurrentConnections());
    }
}
