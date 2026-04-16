package cn.handyplus.pln.inventory;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.core.DateUtil;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.db.Page;
import cn.handyplus.lib.inventory.HandyInventory;
import cn.handyplus.lib.inventory.HandyInventoryUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.pln.constants.GuiTypeEnum;
import cn.handyplus.pln.constants.ReturnConstants;
import cn.handyplus.pln.enter.ReturnPlayerTime;
import cn.handyplus.pln.service.ReturnPlayerTimeService;
import cn.handyplus.pln.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回归玩家管理
 *
 * @author handy
 */
public class ViewReturnGui {

    private ViewReturnGui() {
    }

    private final static ViewReturnGui INSTANCE = new ViewReturnGui();

    public static ViewReturnGui getInstance() {
        return INSTANCE;
    }

    /**
     * 创建gui
     *
     * @param player 玩家
     * @return gui
     */
    public Inventory createGui(Player player) {
        String title = ConfigUtil.RETURN_CONFIG.getString("adminTitle");
        int size = ConfigUtil.RETURN_CONFIG.getInt("size", BaseConstants.GUI_SIZE_54);
        String sound = ConfigUtil.RETURN_CONFIG.getString("sound");
        HandyInventory handyInventory = new HandyInventory(GuiTypeEnum.VIEW_RETURN.getType(), title, size, sound);
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
        handyInventory.setGuiType(GuiTypeEnum.VIEW_RETURN.getType());
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
        Page<ReturnPlayerTime> page = ReturnPlayerTimeService.getInstance().page(handyInventory.getPageNum(), 10);
        if (page.getTotal() < 1) {
            return;
        }
        List<ReturnPlayerTime> records = page.getRecords();
        int i = 0;
        List<Integer> guiIndexList = ReturnConstants.GUI_INDEX;
        String name = ConfigUtil.RETURN_CONFIG.getString("return.name", "");
        String materialStr = ConfigUtil.RETURN_CONFIG.getString("return.material");
        List<String> loreList = ConfigUtil.RETURN_CONFIG.getStringList("return.lore");
        for (ReturnPlayerTime record : records) {
            Integer index = guiIndexList.get(i++);
            List<String> newLoreList = ItemStackUtil.loreReplaceMap(loreList, this.replaceMap(record));
            String newName = name.replace("${player_name}", record.getPlayerName());
            ItemStack itemStack = ItemStackUtil.getItemStack(materialStr, newName, newLoreList);
            inventory.setItem(index, itemStack);
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
        HandyInventoryUtil.setButton(ConfigUtil.RETURN_CONFIG, handyInventory, "nextPage", replacePageMap);
        HandyInventoryUtil.setButton(ConfigUtil.RETURN_CONFIG, handyInventory, "previousPage", replacePageMap);
        // 返回按钮
        HandyInventoryUtil.setButton(ConfigUtil.RETURN_CONFIG, handyInventory, "back");
        // 分隔板
        HandyInventoryUtil.setButton(ConfigUtil.RETURN_CONFIG, handyInventory, "pane");
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
        int count = (int) Math.ceil(ReturnPlayerTimeService.getInstance().findCount() / 10.0);
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
     * @param returnPlayerTime 回归玩家
     * @return map
     */
    private Map<String, String> replaceMap(ReturnPlayerTime returnPlayerTime) {
        Map<String, String> map = new HashMap<>();
        map.put("player_name", returnPlayerTime.getPlayerName());
        map.put("day", returnPlayerTime.getDay().toString());
        map.put("return_time", DateUtil.format(returnPlayerTime.getReturnTime(), DateUtil.YYYY));
        map.put("invitee_player_name", StrUtil.isNotEmpty(returnPlayerTime.getInviteePlayerName()) ? returnPlayerTime.getInviteePlayerName() : "");
        map.put("number", returnPlayerTime.getNumber().toString());
        return map;
    }

}
