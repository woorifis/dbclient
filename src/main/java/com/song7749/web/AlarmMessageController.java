package com.song7749.web;

import static com.song7749.util.LogMessageFormatter.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.song7749.base.WebSocketMessageVo;
import com.song7749.dbclient.type.AuthType;
import com.song7749.dbclient.value.LoginAuthVo;
import com.song7749.util.ObjectJsonUtil;
import com.song7749.util.crypto.CryptoAES;

@Controller
public class AlarmMessageController {

	Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 관리자 메세지 전송
	 * 보낸 사람이 관리자인가 검증해야 한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
    @MessageMapping("/send")
    @SendTo("/topic/recieve")
    public WebSocketMessageVo messageSend(WebSocketMessageVo vo) throws Exception {
    	logger.trace(format("{}", "Web Socket Send Message"),vo);
    	try {
        	LoginAuthVo lav = getLoginAuthVo(vo.getSenderApikey());
        	if(!AuthType.ADMIN.equals(lav.getAuthType())) {
        		throw new IllegalArgumentException("관리자가 아닙니다");
        	}
        	// 불필요한 정보는 제거
        	lav.setIp(null);
        	lav.setCreate(null);
        	lav.setRefresh(null);
        	// 본인에게 메세지가 가지 않도록 처리 한다.
        	vo.setContents(lav);
		} catch (Exception e) {
			vo.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			vo.setMessage(e.getMessage());
		}
    	// 관리자의 API 키 정보는 삭제 한다.
    	vo.setSenderApikey(null);
    	return vo;
    }

    /**
     * 알람 테스크
     * @param vo
     * @return
     * @throws Exception
     */
    @MessageMapping("/runAlarms")
    @SendTo("/topic/runAlarms")
    public WebSocketMessageVo runAlarms(WebSocketMessageVo vo) throws Exception {
    	return vo;
    }

    private LoginAuthVo getLoginAuthVo(String apikey) {
    	// 인증 객체로 변경 한다.
    	LoginAuthVo lav = null;
    	try {
    		lav = (LoginAuthVo) ObjectJsonUtil.getObjectByJsonString(CryptoAES.decrypt(apikey),LoginAuthVo.class);
    		logger.debug(format("{}","Login apikey 복호화 완료"),lav);
    	} catch (Exception e) {
    		throw new IllegalArgumentException("apikey 정보 복호화 실패. 관리자에게 문의 하시기 바랍니다.");
    	}
		return lav;
    }
}