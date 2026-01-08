package cn.handyplus.pln.listener.gui;

import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.inventory.HandyInventory;
import cn.handyplus.lib.inventory.HandyInventoryUtil;
import cn.handyplus.lib.inventory.IHandyClickEvent;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.constants.GuiTypeEnum;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.enter.ReturnShop;
import cn.handyplus.pln.enter.ReturnShopLog;
import cn.handyplus.pln.inventory.ShopGui;
import cn.handyplus.pln.inventory.SignInGui;
import cn.handyplus.pln.inventory.TaskGui;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.service.ReturnShopLogService;
import cn.handyplus.pln.service.ReturnShopService;
import cn.handyplus.pln.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * 回归商店
 *
 * @author handy
 */
public class ShopClickEvent implements IHandyClickEvent {
    @Override
    public String guiType() {
        return GuiTypeEnum.SHOP.getType();
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
        // 返回按钮
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SHOP_CONFIG, "back")) {
            handyInventory.syncClose();
            return;
        }
        // 回归签到
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SHOP_CONFIG, "signIn")) {
            Inventory gui = SignInGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 回归任务
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SHOP_CONFIG, "task")) {
            Inventory gui = TaskGui.getInstance().createGui(player);
            handyInventory.syncOpen(gui);
            return;
        }
        // 上一页
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SHOP_CONFIG, "previousPage")) {
            if (pageNum > 1) {
                handyInventory.setPageNum(handyInventory.getPageNum() - 1);
                ShopGui.getInstance().setInventoryDate(handyInventory);
            }
            return;
        }
        // 下一页
        if (HandyInventoryUtil.isIndex(rawSlot, ConfigUtil.SHOP_CONFIG, "nextPage")) {
            if (pageNum + 1 <= pageCount) {
                handyInventory.setPageNum(handyInventory.getPageNum() + 1);
                ShopGui.getInstance().setInventoryDate(handyInventory);
            }
            return;
        }
        // 自定义菜单处理
        Map<Integer, String> custom = HandyInventoryUtil.getCustomButton(ConfigUtil.SHOP_CONFIG, "custom");
        String command = custom.get(rawSlot);
        if (StrUtil.isNotEmpty(command)) {
            PlayerSchedulerUtil.syncPerformReplaceCommand(player, command);
            return;
        }

        //  点击购买
        Integer id = map.get(rawSlot);
        if (id == null) {
            return;
        }
        // 商品信息
        Optional<ReturnShop> returnShopOptional = ReturnShopService.getInstance().findById(id);
        if (!returnShopOptional.isPresent()) {
            return;
        }
        ReturnShop returnShop = returnShopOptional.get();
        // 玩家经济
        Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
        if (!returnPlayerTimeOptional.isPresent()) {
            return;
        }
        ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
        // 判断贡献是否足够
        if (returnPlayerTime.getNumber() < returnShop.getPrice()) {
            MessageUtil.sendMessage(player, ConfigUtil.SHOP_CONFIG.getString("noButton"));
            return;
        }
        // 判断玩家购买该商品次数
        if (returnShop.getNumber() != 0) {
            Optional<ReturnShopLog> returnShopLogOptional = ReturnShopLogService.getInstance().findByPlayerName(returnPlayerTime.getId(), player.getName(), returnShop.getId());
            if (returnShopLogOptional.isPresent()) {
                ReturnShopLog returnShopLog = returnShopLogOptional.get();
                if (returnShop.getNumber() <= returnShopLog.getNumber()) {
                    MessageUtil.sendMessage(player, ConfigUtil.SHOP_CONFIG.getString("noLimitButton"));
                    return;
                }
                ReturnShopLogService.getInstance().addNumber(returnShopLog.getId(), 1);
            } else {
                ReturnShopLog returnShopLog = new ReturnShopLog();
                returnShopLog.setReturnPlayerTimeId(returnPlayerTime.getId());
                returnShopLog.setPlayerName(player.getName());
                returnShopLog.setPlayerUuid(player.getUniqueId().toString());
                returnShopLog.setReturnShopId(returnShop.getId());
                returnShopLog.setNumber(1);
                ReturnShopLogService.getInstance().add(returnShopLog);
            }
        }
        ReturnPlayerTimeService.getInstance().subtractNumber(returnPlayerTime.getId(), returnShop.getPrice());
        ItemStack itemStack = ItemStackUtil.itemStackDeserialize(returnShop.getItemStack());
        ItemStackUtil.addItem(player, itemStack, itemStack.getAmount());
        MessageUtil.sendMessage(player, ConfigUtil.SHOP_CONFIG.getString("buyMsg"));
        ShopGui.getInstance().setInventoryDate(handyInventory);
    }

}