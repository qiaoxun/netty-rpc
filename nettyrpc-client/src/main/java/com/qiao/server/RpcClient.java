package com.qiao.server;

import com.alibaba.fastjson.JSON;
import com.qiao.bean.RpcRequest;
import com.qiao.zookeeper.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class RpcClient {

    @Autowired
    ServiceDiscovery serviceDiscovery;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private BlockingQueue<RpcRequest> queue = new LinkedBlockingQueue<>();

    public RpcClient() {
        System.out.println("construct");
    }

    @PostConstruct
    public void startNetty() {
        System.out.println("start method");
        new Thread(() -> {
            final NettyClientHandler nettyClientHandler = new NettyClientHandler();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 0, 30));
                                pipeline.addLast(nettyClientHandler);
                            }
                        });

                serviceDiscovery.connect();
                String serverAddress = serviceDiscovery.discoveryRegistedAddress();
                String[] addressArr = serverAddress.split(":");
                System.out.println("addressArr is " + Arrays.toString(addressArr));
                String host = addressArr[0];
                int port = Integer.parseInt(addressArr[1]);
                Channel channel = bootstrap.connect(host, port).channel();

                System.out.println("Netty Client Started");

                while (true) {
                    RpcRequest request = queue.take();
                    String jsonData = JSON.toJSONString(request);
                    System.out.println("queue take data : " + jsonData);
                    ChannelFuture future = channel.writeAndFlush(Unpooled.copiedBuffer(jsonData, CharsetUtil.UTF_8));
                    if (!future.isSuccess()) {
                        System.err.println("Failed because: " + future.cause());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    public void send(RpcRequest rpcRequest) {
        try {
            System.out.println("queue put data");
            queue.put(rpcRequest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
