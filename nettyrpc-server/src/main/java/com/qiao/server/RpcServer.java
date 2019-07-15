package com.qiao.server;

import com.qiao.zookeeper.ServiceRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private Map<String, Object> serviceMap = new HashMap<>();

    @Value("${rpc.server.address}")
    private String serverAddress;

    @Autowired
    ServiceRegister serviceRegister;

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    public RpcServer() {
        System.out.println("construct");
    }

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("================ setApplicationContext ==================");
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);
        beans.values().forEach(serviceBean -> {
            Class<?> clazz = serviceBean.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            Stream.of(interfaces).forEach(inter -> {
                String interfaceName = inter.getName();
                serviceMap.put(interfaceName, serviceBean);
            });
        });
    }

    @PostConstruct
    public void start() {
        System.out.println("start method");

        final NettyServerHandler nettyServerHandler = new NettyServerHandler(serviceMap);

        new Thread(() -> {
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 0, 60));
                                pipeline.addLast(nettyServerHandler);
                            }
                        });

                String[] addressArr = serverAddress.split(":");
                System.out.println("addressArr is " + Arrays.toString(addressArr));
                String host = addressArr[0];
                int port = Integer.parseInt(addressArr[1]);
                ChannelFuture future = serverBootstrap.bind(host, port).sync();
                serviceRegister.registerNode(serverAddress);
                future.channel().closeFuture().sync();
                System.out.println("Netty server Started");
            } catch (Exception e) {
                e.printStackTrace();
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }).start();
    }
}
