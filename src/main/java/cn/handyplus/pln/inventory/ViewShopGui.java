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
import cn.handyplus.pln.enter.ReturnShop;
import cn.handyplus.pln.service.ReturnShopService;
import cn.handyplus.pln.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回归商店管理
 *
 * @author handy
 */
public class ViewShopGui {

    private ViewShopGui() {
    }

    private final static ViewShopGui INSTANCE = new ViewShopGui();

    public static ViewShopGui getInstance() {
        return INSTANCE;
    }

    /**
     * 创建gui
     *
     * @param player 玩家
     * @return gui
     */
    public Inventory createGui(Player player) {
        String title = ConfigUtil.SHOP_CONFIG.getString("adminTitle");
        int size = ConfigUtil.SHOP_CONFIG.getInt("size", BaseConstants.GUI_SIZE_54);
        String sound = ConfigUtil.SHOP_CONFIG.getString("sound");
        HandyInventory handyInventory = new HandyInventory(GuiTypeEnum.VIEW_SHOP.getType(), title, size, sound);
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
        handyInventory.setGuiType(GuiTypeEnum.VIEW_SHOP.getType());
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
        Map<Integer, Integer> map = handyInventory.getIntMap();
        Page<ReturnShop> page = ReturnShopService.getInstance().page(handyInventory.getPageNum(), 10);
        if (page.getTotal() < 1) {
            return;
        }
        List<ReturnShop> records = page.getRecords();
        int i = 0;
        List<Integer> guiIndexList = ReturnConstants.GUI_INDEX;

        for (ReturnShop record : records) {
            Integer index = guiIndexList.get(i++);
            ItemStack itemStack = ItemStackUtil.itemStackDeserialize(record.getItemStack());
            // 原版物品lore显示
            List<String> loreList = ConfigUtil.SHOP_CONFIG.getStringList("logo.view");
            List<String> lore = ItemStackUtil.getItemMeta(itemStack).getLore();
            if (CollUtil.isNotEmpty(lore)) {
                lore.addAll(loreList);
                loreList = lore;
            }
            List<String> newLoreList = ItemStackUtil.loreReplaceMap(loreList, this.replaceMap(record));
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
        // 返回按钮
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "back");
        // 分隔板
        HandyInventoryUtil.setButton(ConfigUtil.SHOP_CONFIG, handyInventory, "pane");
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
     * @param guildShop 商品
     * @return map
     */
    private Map<String, String> replaceMap(ReturnShop guildShop) {
        Map<String, String> map = new HashMap<>();
        Integer number = guildShop.getNumber();
        String unlimited = ConfigUtil.SHOP_CONFIG.getString("unlimited", "&a无限制");
        map.put("number", number == 0 ? unlimited : number.toString());
        map.put("price", guildShop.getPrice().toString());
        return map;
    }

}