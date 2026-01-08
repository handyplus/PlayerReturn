package cn.handyplus.pln.listener.gui;

import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.inventory.HandyInventory;
import cn.handyplus.lib.inventory.HandyInventoryUtil;
import cn.handyplus.lib.inventory.IHandyClickEvent;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.api.PlayerReturnApi;
import cn.handyplus.pln.constants.GuiTypeEnum;
import cn.handyplus.pln.enter.ReturnPlayerSignIn;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.inventory.ShopGui;
import cn.handyplus.pln.inventory.SignInGui;
import cn.handyplus.pln.inventory.TaskGui;
import cn.handyplus.pln.service.ReturnPlayerSignInService;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.util.ConfigUtil;
import cn.handyplus.pln.util.ReturnUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 签到点击事件
 *
 * @author handy
 */
public class SignInClickEvent implements IHandyClickEvent {

    @Override
    public String guiType() {
        return GuiTypeEnum.SIGN_IN.getType();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void rawSlotClick(HandyInventory handyInventory, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        Player player = handyInventory.getPlayer();
        // 关闭按钮
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "back")) {
            handyInventory.syncClose();
            return;
        }
        // 回归商店
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "shop")) {
            Inventory gui = ShopGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 回归任务
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "task")) {
            Inventory gui = TaskGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 自定义菜单处理
        Map<Integer, String> custom = HandyInventoryUtil.getCustomButton(ConfigUtil.SIGN_IN_CONFIG, "custom");
        String command = custom.get(rawSlot);
        if (StrUtil.isNotEmpty(command)) {
            PlayerSchedulerUtil.syncPerformReplaceCommand(player, command);
            return;
        }
        int day = 0;
        List<String> commandList = new ArrayList<>();
        // 1
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "one")) {
            day = 1;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("one.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "two")) {
            day = 2;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("two.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "three")) {
            day = 3;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("three.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "four")) {
            day = 4;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("four.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "five")) {
            day = 5;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("five.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "six")) {
            day = 6;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("six.command");
        }
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SIGN_IN_CONFIG, "seven")) {
            day = 7;
            commandList = ConfigUtil.SIGN_IN_CONFIG.getStringList("seven.command");
        }
        if (day == 0) {
            return;
        }
        // 判断是否回归玩家
        Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
        if (!returnPlayerTimeOptional.isPresent()) {
            MessageUtil.sendMessage(player, BaseUtil.getLangMsg("noReturnMsg"));
            return;
        }
        ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
        int returnDay = PlayerReturnApi.returnDay(player.getName());
        if (day != returnDay) {
            MessageUtil.sendMessage(player, ConfigUtil.SIGN_IN_CONFIG.getString("noButton"));
            return;
        }
        // 判断是否签到过
        Optional<ReturnPlayerSignIn> playerSignInOptional = ReturnPlayerSignInService.getInstance().findByPlayerName(returnPlayerTime.getId(), player.getName(), day);
        if (playerSignInOptional.isPresent()) {
            MessageUtil.sendMessage(player, ConfigUtil.SIGN_IN_CONFIG.getString("signedInButton"));
            return;
        }
        // 记录签到信息
        ReturnPlayerSignIn playerSignIn = new ReturnPlayerSignIn();
        playerSignIn.setReturnPlayerTimeId(returnPlayerTime.getId());
        playerSignIn.setPlayerName(player.getName());
        playerSignIn.setPlayerUuid(player.getUniqueId().toString());
        playerSignIn.setSignInTime(new Date());
        playerSignIn.setDay(day);
        long rst = ReturnPlayerSignInService.getInstance().add(playerSignIn);
        // 执行签到指令
        if (rst > 0) {
            ReturnUtil.command(player, commandList);
        }
        // 刷新
        SignInGui.getInstance().setInventoryDate(handyInventory);
        MessageUtil.sendMessage(player, ConfigUtil.SIGN_IN_CONFIG.getString(rst > 0 ? "succeedMsg" : "failureMsg"));
    }

}