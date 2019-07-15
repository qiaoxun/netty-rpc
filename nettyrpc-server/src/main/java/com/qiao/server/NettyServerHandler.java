package com.qiao.server;

import com.alibaba.fastjson.JSON;
import com.qiao.bean.RpcRequest;
import com.qiao.bean.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        ByteBuf buf = (ByteBuf) obj;
        String msg = buf.toString(CharsetUtil.UTF_8);
        if ("heartBeat".equals(msg.toString())) {
            System.out.println("heart beat");
        } else {
            System.err.println("msg is " + msg);
            RpcRequest rpcRequest = JSON.parseObject(msg.toString(), RpcRequest.class);
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            Object[] parameters = rpcRequest.getParameters();
            Object[] actualParameters = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Class<?> clz = parameterTypes[i];
                Object param = parameters[i];
                actualParameters[i] = JSON.parseObject(param.toString(), clz);
            }

            rpcRequest.setParameters(actualParameters);

            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(rpcRequest.getId());

            try {
                Object returnVal = handle(rpcRequest);
                rpcResponse.setData(returnVal);
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setCode(1);
                rpcResponse.setErrorMsg(e.getMessage());
            }
            ctx.writeAndFlush(rpcResponse);
        }
    }

    private Object handle(RpcRequest rpcRequest) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String className = rpcRequest.getClassName();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        Object serviceBean = serviceMap.get(className);
        if (null != serviceBean) {
            System.out.println("serviceBean is " + serviceBean);
            System.out.println("parameters is " + parameters[0]);
            Class clazz = serviceBean.getClass();
            Method method = clazz.getMethod(methodName, parameterTypes);
            Object object = method.invoke(serviceBean, parameters);
            return object;
        } else {
            throw new RuntimeException("class not found: " + className);
        }
    }

    private Object[] getParameters(Class<?>[] parameterTypes, Object[] parameters) {
        if (parameters.length == 0 || parameterTypes.length == 0) {
            return null;
        } else {
            Object[] params = new Object[parameters.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                params[i] = JSON.parseObject(parameters[i].toString(), parameterTypes[i]);
            }
            return params;
        }
    }

}
