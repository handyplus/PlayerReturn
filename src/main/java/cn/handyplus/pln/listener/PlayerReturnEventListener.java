package cn.handyplus.pln.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.event.PlayerReturnEvent;
import cn.handyplus.pln.hook.PlayerTaskUtil;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.util.ReturnUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;

/**
 * 玩家回归事件
 *
 * @author handy
 */
@HandyListener
public class PlayerReturnEventListener implements Listener {

    /**
     * 玩家回归事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onEvent(PlayerReturnEvent event) {
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            Player player = event.getPlayer();
            // 清除之前回归记录
            ReturnPlayerTimeService.getInstance().updateStatus(player.getName(), false);
            // 添加新回归记录
            ReturnPlayerTime returnPlayerTime = new ReturnPlayerTime();
            returnPlayerTime.setPlayerName(player.getName());
            returnPlayerTime.setPlayerUuid(player.getUniqueId().toString());
            returnPlayerTime.setReturnTime(new Date());
            returnPlayerTime.setDay(event.getDay());
            returnPlayerTime.setNumber(0);
            returnPlayerTime.setStatus(true);
            int id = ReturnPlayerTimeService.getInstance().add(returnPlayerTime);
            returnPlayerTime.setId(id);
            // 创建回归任务
            PlayerTaskUtil.getInstance().createTask(returnPlayerTime);
            // 发送回归提醒
            ReturnUtil.setRemind(player);
        });
    }

}