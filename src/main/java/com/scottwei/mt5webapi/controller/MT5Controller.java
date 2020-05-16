package com.scottwei.mt5webapi.controller;

import com.scottwei.mt5webapi.mt5Api.manager.ClientManager;
import com.scottwei.mt5webapi.mt5Api.nettyTcp.Message;
import com.scottwei.mt5webapi.common.Constant;
import com.scottwei.mt5webapi.common.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Scott Wei
 * @date 2019/7/30 19:23
 *
 * controller 处理器
 **/
@Controller
@RequestMapping("/mt5")
public class MT5Controller {

    //login auth
    @RequestMapping(value="/login", method = RequestMethod.POST)
    public String login(String account, String password) throws Exception {
        Message authStartMessage = new Message();
        authStartMessage.setBody(String.format(Constant.AUTH_START, account));//enable AES256OFB加密
        Map<String,String> authStartResult = ClientManager.getInstance().invoke(authStartMessage).get();
        Optional<String> hexPassword = Utils.getMD5Password(password, authStartResult.get("SRV_RAND"));
        if(hexPassword.isPresent()) {
            String myRand = UUID.randomUUID().toString().replaceAll("-","");
            Message authAnswerMessage = new Message();
            authAnswerMessage.setBody(String.format(Constant.AUTH_ANSWER, hexPassword.get(), myRand));
            Map<String,String> authAnswerResult = ClientManager.getInstance().invoke(authAnswerMessage).get();
            Utils.getCrypt(password, authAnswerResult.get("CRYPT_RAND"));
        }
        return null;
    }

}

