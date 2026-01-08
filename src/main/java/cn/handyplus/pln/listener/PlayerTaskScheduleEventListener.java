package cn.handyplus.pln.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.enter.ReturnTask;
import cn.handyplus.pln.enter.ReturnTaskDemand;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.service.ReturnTaskDemandService;
import cn.handyplus.pln.service.ReturnTaskService;
import com.handy.playertask.api.PlayerTaskApi;
import com.handy.playertask.event.PlayerTaskScheduleEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Optional;

/**
 * plk任务进度
 *
 * @author handy
 */
@HandyListener
public class PlayerTaskScheduleEventListener implements Listener {

    @EventHandler
    public void onPlayerTaskSchedule(PlayerTaskScheduleEvent event) {
        Player player = event.getPlayer();
        Integer amount = event.getAmount();
        String content = event.getContent();
        String demandType = event.getDemandType();
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
            if (!returnPlayerTimeOptional.isPresent()) {
                return;
            }
            ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
            List<ReturnTaskDemand> returnTaskDemandList = ReturnTaskDemandService.getInstance().findByReturnPlayerTimeIdAndContent(returnPlayerTime.getId(), content, demandType);
            if (CollUtil.isEmpty(returnTaskDemandList)) {
                return;
            }
            // 任务目前进度处理
            for (ReturnTaskDemand taskNpcPlayerDemand : returnTaskDemandList) {
                ReturnTaskDemandService.getInstance().updateCompletionAmount(taskNpcPlayerDemand.getId(), amount);
                // 发送进度消息
                sendTaskDemandScheduleMsg(taskNpcPlayerDemand, player, amount);
            }
        });
    }

    /**
     * 发送任务目标进度提醒
     *
     * @param taskNpcPlayerDemand 任务
     * @param player              玩家
     * @param amount              数量
     */
    private void sendTaskDemandScheduleMsg(ReturnTaskDemand taskNpcPlayerDemand, Player player, Integer amount) {
        // 当前进度消息
        int completionAmount = taskNpcPlayerDemand.getCompletionAmount() + amount;
        PlayerTaskApi.getInstance().sendCurrentProgressMsg(player, taskNpcPlayerDemand.getType(), taskNpcPlayerDemand.getAmount(), completionAmount, taskNpcPlayerDemand.getItemStack());
        // 判断是否需要发送其他消息
        if (completionAmount < taskNpcPlayerDemand.getAmount()) {
            return;
        }
        Optional<ReturnTask> returnTaskOptional = ReturnTaskService.getInstance().findById(taskNpcPlayerDemand.getReturnTaskId());
        if (!returnTaskOptional.isPresent()) {
            return;
        }
        ReturnTask returnTask = returnTaskOptional.get();
        // 发送任务小目标完成消息
        PlayerTaskApi.getInstance().sendTaskManagerMsg(player, returnTask.getTaskName());
        // 任务总进度消息提醒
        Boolean rst = ReturnTaskDemandService.getInstance().findCountByReturnTaskId(taskNpcPlayerDemand.getReturnTaskId());
        if (rst) {
            return;
        }
        // 设置任务目标完成
        ReturnTaskService.getInstance().updateDemandSuccess(taskNpcPlayerDemand.getReturnTaskId(), true);
        // 发送任务完成提醒
        PlayerTaskApi.getInstance().sendTaskFinishMsg(player, returnTask.getTaskName());
    }

}
