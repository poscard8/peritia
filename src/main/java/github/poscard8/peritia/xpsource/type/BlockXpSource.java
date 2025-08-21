package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.BlockInputs;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlockXpSource extends DataXpSource
{
    protected final Map<String, Integer> debtMap = new HashMap<>();

    protected BlockInputs inputs = BlockInputs.empty();
    protected boolean allowFarming = false;
    protected boolean requireCorrectTool = false;

    public BlockXpSource(ResourceLocation key) { super(key); }

    public static BlockXpSource empty() { return new BlockXpSource(EMPTY_KEY); }

    @Nullable
    public static BlockXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (BlockXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.BLOCK.get(); }

    public BlockInputs inputs() { return inputs; }

    public boolean isFarmingAllowed() { return allowFarming; }

    public boolean requiresCorrectTool() { return requireCorrectTool; }

    @Override
    public boolean isValidDataSource() { return !isFarmingAllowed(); }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public int getDebtClient()
    {
        tryLoadClient();
        return debtMap.getOrDefault(ClientHandler.getPlayerUUID(), 0);
    }

    public int getDebt(ServerPlayer player)
    {
        tryLoad(player);
        return debtMap.getOrDefault(player.getStringUUID(), 0);
    }

    public boolean hasDebt(ServerPlayer player) { return getDebt(player) > 0; }

    public void setDebt(ServerPlayer player, int debt)
    {
        debtMap.put(player.getStringUUID(), Math.max(0, debt));
        update(player);
    }

    public void incrementDebt(ServerPlayer player) { setDebt(player, getDebt(player) + 1); }

    public void decrementDebt(ServerPlayer player) { setDebt(player, getDebt(player) - 1); }

    public boolean hasCorrectTool(ServerPlayer player, BlockState state) { return isCorrectTool(player.getMainHandItem(), state); }

    public boolean isCorrectTool(ItemStack tool, BlockState state) { return !requiresCorrectTool() || tool.isCorrectToolForDrops(state); }

    public void handleBlockBreak(ServerPlayer player, BlockState state)
    {
        if (inputs().test(state) && hasCorrectTool(player, state) && canPlayerGainXp(player))
        {
            if (hasDebt(player) && !isFarmingAllowed())
            {
                decrementDebt(player);
            }
            else award(player);
        }
    }

    public void handleBlockPlace(ServerPlayer player, BlockState state)
    {
        if (inputs().test(state) && canPlayerGainXp(player) && !isFarmingAllowed()) incrementDebt(player);
    }

    @Override
    public void loadData(JsonObject data)
    {
        debtMap.clear();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) debtMap.put(entry.getKey(), entry.getValue().getAsInt());
    }

    @Override
    public JsonObject saveData()
    {
        JsonObject data = new JsonObject();

        for (String key : debtMap.keySet())
        {
            int value = debtMap.getOrDefault(key, 0);
            JsonHelper.write(data, key, value);
        }

        return data;
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.inputs = JsonHelper.readElementSerializable(data, "inputs", BlockInputs::tryLoad, inputs);
        this.allowFarming = JsonHelper.readBoolean(data, "allowFarming", allowFarming);
        this.requireCorrectTool = JsonHelper.readBoolean(data, "requireCorrectTool", requireCorrectTool);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
        JsonHelper.write(data, "allowFarming", allowFarming);
        JsonHelper.write(data, "requireCorrectTool", requireCorrectTool);
    }


}
