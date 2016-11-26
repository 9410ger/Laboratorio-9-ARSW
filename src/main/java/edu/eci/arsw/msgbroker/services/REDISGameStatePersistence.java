/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.msgbroker.services;

import edu.eci.arsw.msgbroker.model.HangmanGame;
import edu.eci.arsw.util.JedisUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 *
 * @author 2105403
 */
@Service
public class REDISGameStatePersistence implements GameStatePersistence{

    @Override
    public void createGame(int id, String word) throws GameCreationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HangmanGame getGame(int gameid) throws GameNotFoundException {
        Jedis jedis = JedisUtil.getPool().getResource();
        Map<String,String> partidaRedis = jedis.hgetAll("partida:"+gameid);
        HangmanGame partida = new HangmanGame(partidaRedis.get("palabra")
                ,partidaRedis.get("adivinado"),partidaRedis.get("ganador")
                ,partidaRedis.get("partida").equals("true"));
        jedis.close();
     return partida;
    }

    @Override
    public void addLetter(int gameid, char c) throws GameNotFoundException {
        Jedis jedis = JedisUtil.getPool().getResource();
        Map<String,String> partidaRedis = jedis.hgetAll("partida:"+gameid);
        HangmanGame partida = new HangmanGame(partidaRedis.get("palabra")
                ,partidaRedis.get("adivinado"),partidaRedis.get("ganador")
                ,partidaRedis.get("partida").equals("true"));
        partida.addLetter(c);
        Map<String,String> partidaActualizar=new HashMap<>();
            partidaActualizar.put("palabra",partidaRedis.get("palabra"));
            partidaActualizar.put("adivinado",partida.getCurrentGuessedWord());
            partidaActualizar.put("ganador",partidaRedis.get("ganador"));
            partidaActualizar.put("partida",partidaRedis.get("partida"));
            jedis.hmset("partida:" + gameid, partidaActualizar);
        jedis.close();
    }

    @Override
    public boolean checkWordAndUpdateHangman(int gameid, String player, String word) throws GameNotFoundException {
        Jedis jedis = JedisUtil.getPool().getResource();
        boolean gano=false;
        Map<String,String> partidaRedis = jedis.hgetAll("partida:"+gameid);
        HangmanGame partida = new HangmanGame(partidaRedis.get("palabra")
                ,partidaRedis.get("adivinado"),partidaRedis.get("ganador")
                ,partidaRedis.get("partida").equals("true"));
        gano=partida.guessWord(player, word);
        if(gano){
            jedis.watch("adivinado", "ganador", "partida");
            Transaction t = jedis.multi();
            t.set(word, player,"true");
            t.exec();
            Map<String,String> partidaActualizar=new HashMap<>();
            partidaActualizar.put("palabra",partidaRedis.get("palabra"));
            partidaActualizar.put("adivinado",word);
            partidaActualizar.put("ganador",player);
            partidaActualizar.put("partida","true");
            jedis.hmset("partida:" + gameid, partidaActualizar);
       }
        jedis.close();
       return gano;
     }
  }
    

