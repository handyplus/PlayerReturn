package cn.handyplus.pln.listener.gui;

import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.inventory.HandyInventory;
import cn.handyplus.lib.inventory.HandyInventoryUtil;
import cn.handyplus.lib.inventory.IHandyClickEvent;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.constants.GuiTypeEnum;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.enter.ReturnTask;
import cn.handyplus.pln.inventory.ShopGui;
import cn.handyplus.pln.inventory.SignInGui;
import cn.handyplus.pln.inventory.TaskGui;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.service.ReturnTaskService;
import cn.handyplus.pln.util.ConfigUtil;
import cn.handyplus.pln.util.ReturnUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 回归任务点击事件
 *
 * @author handy
 */
public class TaskClickEvent implements IHandyClickEvent {
    @Override
    public String guiType() {
        return GuiTypeEnum.TASK.getType();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void rawSlotClick(HandyInventory handyInventory, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        Integer pageNum = handyInventory.getPageNum();
        Integer pageCount = handyInventory.getPageCount();
        Player player = handyInventory.getPlayer();
        Map<Integer, Integer> map = handyInventory.getIntMap();

        // 关闭按钮
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.TASK_CONFIG, "back")) {
            handyInventory.syncClose();
            return;
        }
        // 回归商店
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.TASK_CONFIG, "shop")) {
            Inventory gui = ShopGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 回归签到
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.TASK_CONFIG, "signIn")) {
            Inventory gui = SignInGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 上一页
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.TASK_CONFIG, "previousPage")) {
            if (pageNum > 1) {
                handyInventory.setPageNum(handyInventory.getPageNum() - 1);
                TaskGui.getInstance().setInventoryDate(handyInventory);
            }
            return;
        }
        // 下一页
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.TASK_CONFIG, "nextPage")) {
            if (pageNum + 1 <= pageCount) {
                handyInventory.setPageNum(handyInventory.getPageNum() + 1);
                TaskGui.getInstance().setInventoryDate(handyInventory);
            }
        }
        // 自定义菜单处理
        Map<Integer, String> custom = HandyInventoryUtil.getCustomButton(ConfigUtil.TASK_CONFIG, "custom");
        String command = custom.get(rawSlot);
        if (StrUtil.isNotEmpty(command)) {
            PlayerSchedulerUtil.syncPerformReplaceCommand(player, command);
            return;
        }
        // 点击完成
        Integer id = map.get(rawSlot);
        if (id == null) {
            return;
        }
        Optional<ReturnTask> returnTaskOptional = ReturnTaskService.getInstance().findById(id);
        if (!returnTaskOptional.isPresent()) {
            return;
        }
        ReturnTask returnTask = returnTaskOptional.get();
        if (returnTask.getStatus()) {
            MessageUtil.sendMessage(player, ConfigUtil.TASK_CONFIG.getString("taskFinishMsg"));
            return;
        }
        if (!returnTask.getDemandSuccess()) {
            MessageUtil.sendMessage(player, ConfigUtil.TASK_CONFIG.getString("taskNoFinish"));
            return;
        }
        // 玩家经济
        Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
        if (!returnPlayerTimeOptional.isPresent()) {
            return;
        }
        ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
        // 完成任务
        ReturnTaskService.getInstance().updateStatusById(returnTask.getId(), true);
        // 发奖励
        int money = ConfigUtil.TASK_CONFIG.getInt("returnTask." + returnTask.getTaskId() + ".reward.money", 0);
        if (money > 0) {
            ReturnPlayerTimeService.getInstance().addNumber(returnPlayerTime.getId(), money);
        }
        List<String> commandList = ConfigUtil.TASK_CONFIG.getStringList("returnTask." + returnTask.getTaskId() + ".reward.command");
        ReturnUtil.command(player, commandList);
    }

}