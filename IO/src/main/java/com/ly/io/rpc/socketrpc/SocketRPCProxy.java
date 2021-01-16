package com.ly.io.rpc.socketrpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/7 0007 18:29
 */
public class SocketRPCProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(Object target) {

        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = new Socket("localhost", 9999);
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        try {
                            output.writeUTF(target.getClass().getName());
                            output.writeUTF(method.getName());
                            output.writeObject(method.getParameterTypes());
                            output.writeObject(args);
                            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                                Object result = input.readObject();
                                if (result instanceof Throwable) {
                                    throw (Throwable) result;
                                }
                                return result;
                            }
                        } finally {
                            output.close();
                            socket.close();
                        }
                    }
                });
    }
}