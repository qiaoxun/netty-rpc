package com.qiao.factory;

import com.qiao.bean.RpcRequest;
import com.qiao.server.RpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Component
public class RpcServiceFactory implements InvocationHandler {

    @Autowired
    private RpcClient rpcClient;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameters(args);
        rpcRequest.setParameterTypes(method.getParameterTypes());

        rpcClient.send(rpcRequest);

        System.out.println("Dynamic invoke");

        return null;
    }
}
