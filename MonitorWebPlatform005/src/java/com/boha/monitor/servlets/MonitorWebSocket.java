/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.servlets;

import com.boha.monitor.dto.transfer.RequestDTO;
import com.boha.monitor.dto.transfer.ResponseDTO;
import com.boha.monitor.utilx.DataException;
import com.boha.monitor.utilx.DataUtil;
import com.boha.monitor.utilx.ServerStatus;
import com.boha.monitor.utilx.SignInUtil;
import com.boha.monitor.utilx.StatusCode;
import com.boha.monitor.utilx.TrafficCop;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author aubreyM
 */
@Deprecated
@ServerEndpoint("/wsmonitor")
@Stateless
public class MonitorWebSocket {

    
    @Inject
    DataUtil dataUtil;
    @EJB
    SignInUtil signInUtil;
   

    static final String SOURCE = "MonitorWebSocket";
    public static final Set<Session> peers
            = Collections.synchronizedSet(new HashSet<Session>());

    @OnMessage
    public ByteBuffer onMessage(String message) {
        log.log(Level.WARNING, "...incoming socket: onMessage: {0}", message);
        ResponseDTO resp = new ResponseDTO();
        ByteBuffer bb = null;

        try {
            RequestDTO dto = gson.fromJson(message, RequestDTO.class);
            resp = TrafficCop.processRequest(dto, dataUtil, signInUtil);                      
        
        } catch (DataException e) {
            resp.setStatusCode(ServerStatus.ERROR_DATABASE);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));
            log.log(Level.SEVERE, resp.getMessage(), e);

        } catch (Exception e) {
            resp.setStatusCode(StatusCode.ERROR_SERVER);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));
            log.log(Level.SEVERE, resp.getMessage(), e);
            

        }
        bb = ByteBuffer.wrap(gson.toJson(resp).getBytes());
        return bb;
    }

    @OnOpen
    public void onOpen(Session session) {
        peers.add(session);
        try {
            ResponseDTO r = new ResponseDTO();
            r.setSessionID(session.getId());
            r.setStatusCode(0);
            session.getBasicRemote().sendText(gson.toJson(r));
            log.log(Level.WARNING, "########## onOpen...sent session id: {0}", session.getId());
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to send websocket sessionID", ex);
        }
    }

    @OnClose
    public void onClose(Session session
    ) {
        log.log(Level.WARNING, "onClose - remove session: {0}", session.getId());
        for (Session mSession : peers) {
            if (session.getId().equalsIgnoreCase(mSession.getId())) {
                peers.remove(mSession);
                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.log(Level.SEVERE, "### @OnError, websocket failed.......");
        try {
            ResponseDTO r = new ResponseDTO();
            r.setStatusCode(ServerStatus.ERROR_WEBSOCKET);
            r.setMessage(ServerStatus.getMessage(r.getStatusCode()));
            session.getBasicRemote().sendText(gson.toJson(r));
        } catch (IOException ex) {
            Logger.getLogger(MonitorWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    Gson gson = new Gson();
    static final Logger log = Logger.getLogger(MonitorWebSocket.class.getSimpleName());
}
