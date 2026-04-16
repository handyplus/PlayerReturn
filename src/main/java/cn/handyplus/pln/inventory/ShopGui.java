package cn.handyplus.pln.inventory;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.db.Page;
import cn.handyplus.lib.inventory.HandyInventory;
import cn.handyplus.lib.inventory.HandyInventoryUtil;
import cn.handyplus.lib.util.ItemMetaUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.pln.constants.GuiTypeEnum;
import cn.handyplus.pln.constants.ReturnConstants;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.enter.ReturnShop;
import cn.handyplus.pln.enter.ReturnShopLog;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.service.ReturnShopLogService;
import cn.handyplus.pln.service.ReturnShopService;
import cn.handyplus.pln.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 回归商店
 *
 * @author handy
 */
public class ShopGui {

    private ShopGui() {
    }

    private final static ShopGui INSTANCE = new ShopGui();

    public static ShopGui getInstance() {
        return INSTANCE;
    }

    /**
     * 创建gui
     *
     * @param player 玩家
     * @return gui
     */
    public Inventory createGui(Player player) {
        String title = ConfigUtil.SHOP_CONFIG.getString("title");
        int size = ConfigUtil.SHOP_CONFIG.getInt("size", BaseConstants.GUI_SIZE_54);
        String sound = ConfigUtil.SHOP_CONFIG.getString("sound");
        HandyInventory handyInventory = new HandyInventory(GuiTypeEnum.SHOP.getType(), title, size, sound);
        // 设置数据
        handyInventory.setPageNum(1);
        handyInventory.setPlayer(player);
        this.setInventoryDate(handyInventory);
        return handyInventory.getInventory();
    }

    /**
     * 设置数据
     *
     * @param handyInventory gui
     */
    public void setInventoryDate(HandyInventory handyInventory) {
        // 基础设置
        handyInventory.setGuiType(GuiTypeEnum.SHOP.getType());
        // 1.刷新
        HandyInventoryUtil.refreshInventory(handyInventory.getInventory());
        // 1.设置功能性菜单
        this.setFunctionMenu(handyInventory);
        // 2.设置数据
        this.setDate(handyInventory);
    }

    /**
     * 设置数据
     *
     * @param handyInventory gui
     */
    private void setDate(HandyInventory handyInventory) {
        Inventory inventory = handyInventory.getInventory();
        Player player = handyInventory.getPlayer();
        Map<Integer, Integer> map = handyInventory.getIntMap();
        // 判断是否回归
        Optional<ReturnPlayerTime> returnPlayerTimeOptional = ReturnPlayerTimeService.getInstance().findByPlayerName(player.getName(), true);
        if (!returnPlayerTimeOptional.isPresent()) {
            return;
        }
        ReturnPlayerTime returnPlayerTime = returnPlayerTimeOptional.get();
        Page<ReturnShop> page = ReturnShopService.getInstance().page(handyInventory.getPageNum(), 10);
        if (page.getTotal() < 1) {
            return;
        }
        Integer money = returnPlayerTime.getNumber();
        int i = 0;
        List<Integer> guiIndexList = ReturnConstants.GUI_INDEX;
        for (ReturnShop record : page.getRecords()) {
            Integer index = guiIndexList.get(i++);
            ItemStack itemStack = ItemStackUtil.itemStackDeserialize(record.getItemStack());
            // 原版物品lore显示
            List<String> loreList = ConfigUtil.SHOP_CONFIG.getStringList("logo.info");
            List<String> lore = ItemStackUtil.getItemMeta(itemStack).getLore();
            if (CollUtil.isNotEmpty(lore)) {
                lore.addAll(loreList);
                loreList = lore;
            }
            List<String> newLoreList = ItemStackUtil.loreReplaceMap(loreList, this.replaceMap(money, record, returnPlayerTime));
            ItemMeta itemMeta = ItemStackUtil.getItemMeta(itemStack);
            ItemMetaUtil.setLore(itemMeta, newLoreList);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(index, itemStack);
            map.put(index, record.getId());
        }
    }

    /**
     * 设置功能性菜单
     *
     * @param handyInventory GUI
     */
    private void setFunctionMenu(HandyInventory handyInventory) {
        // 设置翻页按钮
        Map<String, String> replacePageMap = this.replacePageMap(handyInventory);
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "nextPage", replacePageMap);
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "previousPage", replacePageMap);
        // 关闭按钮
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "back");
        // 回归签到
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "signIn");
        // 回归任务
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "task");
        // 分隔板
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "pane");
        // 自定义按钮
        HandyInventoryUtil.setCustomButton(ConfigUtil.SHOP_CONFIG, handyInventory, "custom");
    }

    /**
     * 分页map
     *
     * @param handyInventory 当前页
     * @return 分页map
     */
    private Map<String, String> replacePageMap(HandyInventory handyInventory) {
        Integer pageNum = handyInventory.getPageNum();
        Map<String, String> map = new HashMap<>();
        int count = (int) Math.ceil(ReturnShopService.getInstance().findCount() / 10.0);
        handyInventory.setPageCount(count);
        map.put("count", count == 0 ? "1" : count + "");
        map.put("pageNum", pageNum + "");
        map.put("nextPage", (pageNum + 1) + "");
        map.put("previousPage", (pageNum - 1) < 1 ? "1" : (pageNum - 1) + "");
        return map;
    }

    /**
     * 变量map
     *
     * @param money            个人经济
     * @param returnShop       物品
     * @param returnPlayerTime 回归玩家
     * @return map
     */
    private Map<String, String> replaceMap(Integer money, ReturnShop returnShop, ReturnPlayerTime returnPlayerTime) {
        Integer price = returnShop.getPrice();
        Integer number = returnShop.getNumber();
        Map<String, String> map = new HashMap<>();
        String button = ConfigUtil.SHOP_CONFIG.getString("noButton");
        if (money >= price) {
            // 判断是否限购
            if (number == 0) {
                button = ConfigUtil.SHOP_CONFIG.getString("yesButton");
            } else {
                // 判断玩家购买该商品次数
                Optional<ReturnShopLog> guildShopLogOpt = ReturnShopLogService.getInstance().findByPlayerName(returnPlayerTime.getId(), returnPlayerTime.getPlayerName(), returnShop.getId());
                int num = guildShopLogOpt.isPresent() ? guildShopLogOpt.get().getNumber() : 0;
                if (number > num) {
                    button = ConfigUtil.SHOP_CONFIG.getString("limitButton", "").replace("${number}", (number - num) + "");
                } else {
                    button = ConfigUtil.SHOP_CONFIG.getString("noLimitButton");
                }
            }
        }
        String unlimited = ConfigUtil.SHOP_CONFIG.getString("unlimited", "&a无限制");
        map.put("price", price.toString());
        map.put("money", money.toString());
        map.put("number", number == 0 ? unlimited : number.toString());
        map.put("button", button);
        return map;
    }

}