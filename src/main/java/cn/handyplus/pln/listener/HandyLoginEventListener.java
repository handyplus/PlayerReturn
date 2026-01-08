package cn.handyplus.pln.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.internal.HandyLoginEvent;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.lib.util.HandyHttpUtil;
import cn.handyplus.pln.PlayerReturn;
import cn.handyplus.pln.api.PlayerReturnApi;
import cn.handyplus.pln.enter.ReturnPlayerInfo;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.event.PlayerReturnEvent;
import cn.handyplus.pln.service.ReturnPlayerInfoService;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.util.ReturnUtil;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * 登录事件
 *
 * @author handy
 */
@HandyListener
public class HandyLoginEventListener implements Listener {

    /**
     * 玩家进入
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(HandyLoginEvent event) {
        Player player = event.getPlayer();
        // 先设置登陆时间
        this.setLastJoin(player);
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            // 重设最后登录时间
            ReturnPlayerInfoService.getInstance().updateLastJoinTimeByPlayerName(player.getName(), new Date());
            // 判断是否回归
            setReturn(player);
        });
    }

    /**
     * op 进入服务器发送更新提醒
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpPlayerJoin(HandyLoginEvent event) {
        HandyHttpUtil.checkVersion(event.getPlayer());
    }

    /**
     * 设置登陆时间
     *
     * @param player 玩家
     */
    private void setLastJoin(Player player) {
        ReturnPlayerInfo returnPlayer = new ReturnPlayerInfo();
        returnPlayer.setPlayerName(player.getName());
        returnPlayer.setPlayerUuid(player.getUniqueId().toString());
        returnPlayer.setLastJoinTime(new Date());
        returnPlayer.setLastQuitTime(new Date());
        // 如果有authme，兼容历史数据
        if (PlayerReturn.USE_AUTH_ME) {
            if (AuthMeApi.getInstance().isRegistered(player.getName())) {
                Instant lastLoginTime = AuthMeApi.getInstance().getLastLoginTime(player.getName());
                returnPlayer.setLastQuitTime(Date.from(lastLoginTime));
            }
        }
        ReturnPlayerInfoService.getInstance().putIfAbsent(returnPlayer);
    }

    /**
     * 设置回归记录
     *
     * @param player 玩家
     */
    private void setReturn(Player player) {
        // 查询是否回归并绑定
        Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
        if (returnPlayerTimeOptional.isPresent()) {
            // 判断是否超过7天
            ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
            int day = PlayerReturnApi.returnDay(returnPlayerTime, ChronoUnit.DAYS);
            if (day > BaseConstants.CONFIG.getInt("endDay", 7)) {
                ReturnPlayerTimeService.getInstance().updateStatus(player.getName(), false);
                return;
            }
            // 已经是回归玩家发送回归提醒
            ReturnUtil.setRemind(player);
            return;
        }
        // 判断是否可以成为回归
        int leaveDay = PlayerReturnApi.leaveDay(player.getName());
        if (leaveDay < BaseConstants.CONFIG.getInt("returnDay", 7)) {
            return;
        }
        HandySchedulerUtil.runTask(() -> Bukkit.getServer().getPluginManager().callEvent(new PlayerReturnEvent(player, leaveDay)));
    }

}