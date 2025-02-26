package com.qinglan.example.device_point.controller;

import com.qinglan.example.device_point.server.msg.ServerLBSInfo;
import com.qinglan.example.device_point.server.session.DeviceRegSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    DeviceRegSession deviceRegSession;

    /**
     * Subscribe to device data
     * @param uid
     * @return  Subscription device data
     * @throws InterruptedException
     */
    @GetMapping("/test")
    public String test(@RequestParam("uid") String uid) throws InterruptedException {
        Channel channel = deviceRegSession.isReg(uid);
        if (channel == null){
            return null;
        }
        String channelId = channel.id().asLongText();
        ServerLBSInfo.SetModeReq.Builder modeBuild = ServerLBSInfo.SetModeReq.newBuilder();
        int type = 26;
        String key = type + channelId;
        modeBuild.setSeq(26);
        modeBuild.setSeconds(30);
        ByteBuf buffer = channel.alloc().buffer();
        buffer.writeByte(26);
        buffer.writeBytes(modeBuild.build().toByteArray());
        deviceRegSession.initReceiveMsg(key);
        channel.writeAndFlush(buffer);
        String s = deviceRegSession.waitReceiveMsg(key);
        return s;
    }

    /**
     * Get device properties
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @GetMapping("/prop")
    public String getProp(@RequestParam("uid") String uid) throws InterruptedException {
        Channel channel = deviceRegSession.isReg(uid);
        if (channel == null){
            return null;
        }
        String channelId = channel.id().asLongText();
        int type = 11;
        String key = type + channelId;
        ByteBuf buffer = channel.alloc().buffer();
        System.out.println("before write and flush, buf.refCnt(): " + buffer.refCnt());
        buffer.writeByte(type);
        deviceRegSession.initReceiveMsg(key);
        ChannelFuture channelFuture = channel.writeAndFlush(buffer);
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                // After sending the data successfully, check the reference count again.
                System.out.println("after write and flush completed, buf.refCnt(): " + buffer.refCnt());
            }
        });
        String s = deviceRegSession.waitReceiveMsg(key);
        return s;
    }

}
