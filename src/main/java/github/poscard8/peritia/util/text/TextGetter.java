package github.poscard8.peritia.util.text;

import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.LevelRestriction;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.minecraft.AdvancementContext;
import github.poscard8.peritia.util.minecraft.StructureContext;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.xpsource.*;
import github.poscard8.peritia.xpsource.type.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public interface TextGetter extends Function<PeritiaClientHandler, List<Component>>
{
    static TextGetter empty() { return clientHandler -> List.of(); }

    static TextGetter profile()
    {
        return clientHandler ->
        {
            Player player = clientHandler.player();
            if (player == null) return List.of();

            ClientSkillData skillData = clientHandler.viewingSkillData();
            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.playerName(player, clientHandler.lookContext()));
            texts.add(PeritiaTexts.empty());
            texts.addAll(PeritiaTexts.$generalStats(skillData));
            texts.add(PeritiaTexts.empty());

            Map<Attribute, Double> attributeMap = new HashMap<>();
            for (SkillInstance instance : skillData.skillMap().values())
            {
                Map<Attribute, Double> skillAttributeMap = instance.attributeMap();
                for (Map.Entry<Attribute, Double> entry : skillAttributeMap.entrySet())
                {
                    Attribute attribute = entry.getKey();

                    double existingValue = attributeMap.getOrDefault(attribute, 0.0D);
                    double newValue = existingValue + entry.getValue();
                    attributeMap.put(attribute, newValue);
                }
            }

            for (Map.Entry<Attribute, Double> entry : skillData.legacy().attributeMap().entrySet())
            {
                Attribute attribute = entry.getKey();

                double existingValue = attributeMap.getOrDefault(attribute, 0.0D);
                double newValue = existingValue + entry.getValue();
                attributeMap.put(attribute, newValue);
            }

            List<Component> attributeTexts = PeritiaTexts.$attributes(attributeMap);
            if (!attributeTexts.isEmpty())
            {
                texts.add(PeritiaTexts.attributeBuffs());
                texts.addAll(attributeTexts);
                texts.add(PeritiaTexts.empty());
            }

            texts.add(PeritiaTexts.clickToView());
            return texts;
        };
    }

    static TextGetter single(Component text)
    {
        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();
            texts.add(text);
            return texts;
        };
    }

    static TextGetter skillMenu(Skill skill)
    {
        return clientHandler ->
        {
            SkillInstance instance = clientHandler.viewingSkillData().getSkill(skill);

            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.skillWithLevel(instance, ChatFormatting.GOLD));
            texts.addAll(instance.skill().descriptionTexts());

            texts.add(PeritiaTexts.empty());

            if (instance.isMaxLevel())
            {
                String xp = PeritiaTexts.format(instance.totalXp());
                Component totalXPComponent = Component.translatable("label.peritia.total_xp").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(xp).withStyle(ChatFormatting.DARK_AQUA));

                texts.add(totalXPComponent);
            }
            else
            {
                Component textComponent = Component.translatable("label.peritia.progress_to_level",  PeritiaTexts.level(instance.nextLevel())).withStyle(ChatFormatting.GRAY);

                texts.add(textComponent);
                texts.add(PeritiaTexts.progressBar(instance));
            }
            texts.add(PeritiaTexts.empty());
            texts.add(PeritiaTexts.clickToView());
            return texts;
        };
    }

    static TextGetter skillMilestone(Skill skill, int level)
    {
        return clientHandler ->
        {
            SkillInstance instance = clientHandler.viewingSkillData().getSkill(skill);
            SkillInstance.MilestoneStatus status = instance.milestoneStatus(level);
            boolean authorized = clientHandler.isScreenAuthorized();
            boolean claimed = instance.isRewardClaimed(level);
            boolean canClaim = false;
            boolean canPay;

            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.skillWithLevel(skill, level, ChatFormatting.DARK_GREEN));
            texts.add(PeritiaTexts.empty());

            List<RewardLike<?>> rewardLikes = skill.rewardLikesAt(level);

            if (!rewardLikes.isEmpty())
            {
                List<Component> rewardTexts = new ArrayList<>();

                for (RewardLike<?> rewardLike : rewardLikes)
                {
                    if (rewardLike.shouldDisplayText(skill, level)) rewardTexts.add(PeritiaTexts.space().append(rewardLike.getText(skill, level)));
                }

                MutableComponent rewardText = PeritiaTexts.rewards().copy();
                if (status.canClaim() && !claimed)
                {
                    canClaim = true;
                    rewardText.append(PeritiaTexts.glowExclamationMark());
                }

                if (!rewardTexts.isEmpty())
                {
                    texts.add(rewardText);
                    texts.addAll(rewardTexts);
                    texts.add(PeritiaTexts.empty());
                }
            }

            SkillInstance.MilestoneStatus previousStatus = instance.milestoneStatus(level - 1);
            List<LevelRestriction> restrictions = skill.levelRestrictionsAt(level);

            canPay = previousStatus == SkillInstance.MilestoneStatus.UNLOCKED && status == SkillInstance.MilestoneStatus.PENDING;

            if (status != SkillInstance.MilestoneStatus.UNLOCKED && (previousStatus == SkillInstance.MilestoneStatus.PENDING || !restrictions.isEmpty()))
            {
                List<LevelRestriction> previousRestrictions = skill.levelRestrictionsAt(level - 1);

                texts.add(PeritiaTexts.required());

                if ((!previousRestrictions.isEmpty() && previousStatus != SkillInstance.MilestoneStatus.UNLOCKED) || previousStatus.pending())
                {
                    ChatFormatting color = authorized ? ChatFormatting.RED : ChatFormatting.WHITE;
                    texts.add(PeritiaTexts.space().append(PeritiaTexts.skillWithLevel(skill, level - 1, color)));
                }
                Inventory inventory = clientHandler.player() == null ? null : Objects.requireNonNull(clientHandler.player()).getInventory();

                for (LevelRestriction restriction : restrictions)
                {
                    boolean playerHasItem = inventory != null && restriction.checkInventory(inventory);
                    ChatFormatting color = authorized ? playerHasItem ? ChatFormatting.GREEN : ChatFormatting.RED : ChatFormatting.WHITE;

                    Component text = PeritiaTexts.itemStack(restriction.itemStack(), color);
                    texts.add(PeritiaTexts.space().append(text));
                }
                texts.add(PeritiaTexts.empty());
            }
            texts.add(status.getText(instance, level));

            if (authorized)
            {
                if (canClaim) texts.add(PeritiaTexts.claimRewards());
                if (canPay) texts.add(PeritiaTexts.payRestrictions());
            }
            return texts;
        };
    }

    static TextGetter craftGuide(boolean unlocked)
    {
        return unlocked ? clientHandler ->
        {
            Component text = Screen.hasShiftDown() ? PeritiaTexts.craftStack() : PeritiaTexts.craftSingle();
            return List.of(PeritiaTexts.empty(), text);
        } : empty();
    }

    static TextGetter highestSkill()
    {
        return clientHandler ->
        {
            ClientSkillData skillData = clientHandler.viewingSkillData();
            Skill skill = skillData.highestSkill();
            SkillInstance instance = skillData.getSkill(skill);
            int level = instance.level();
            int highScore = skillData.getHighScore(skill);
            int totalXp = instance.totalXp();

            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.highestSkill().withStyle(ChatFormatting.GOLD));
            texts.add(PeritiaTexts.skill(skill, ChatFormatting.WHITE));
            texts.add(PeritiaTexts.empty());

            Component innerText = Component.translatable("label.peritia.all_time_highest", PeritiaTexts.level(highScore)).withStyle(ExtraTextColors.DARKER_AQUA);
            Component text = PeritiaTexts.levelPrefix()
                    .append(PeritiaTexts.level(level, ChatFormatting.DARK_AQUA))
                    .append(PeritiaTexts.space())
                    .append(innerText);

            Component text2 = PeritiaTexts.totalXp().append(PeritiaTexts.makeText(totalXp, ChatFormatting.DARK_AQUA));

            texts.add(text);
            texts.add(text2);
            return texts;
        };
    }

    static TextGetter timePlayed()
    {
        return clientHandler ->
        {
            int seconds = clientHandler.totalTimeInSeconds();
            int allTimeXp = clientHandler.viewingSkillData().allTimeXp();
            float xpPerHour = (allTimeXp * 3600F) / seconds;



            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.timePlayed());
            texts.add(PeritiaTexts.duration(seconds * 1000L, PeritiaTexts.empty(), ChatFormatting.WHITE));
            texts.add(PeritiaTexts.empty());
            texts.add(PeritiaTexts.xpGainRate().append(PeritiaTexts.makePerHour(xpPerHour, ChatFormatting.DARK_AQUA)));

            if (clientHandler.isScreenAuthorized())
            {
                int secondsThisSession = clientHandler.sessionTimeInSeconds();
                int xpThisSession = clientHandler.xpThisSession();
                float xpPerHourThisSession = (xpThisSession * 3600F) / secondsThisSession;

                Component innerText = PeritiaTexts.makePerHour(xpPerHourThisSession).withStyle(ExtraTextColors.DARKER_AQUA);
                Component text = PeritiaTexts.doubleSpace().append(Component.translatable("label.peritia.this_session", innerText).withStyle(ExtraTextColors.DARKER_AQUA));
                texts.add(text);
            }
            return texts;
        };
    }

    static TextGetter wisdom()
    {
        return clientHandler ->
        {
            double wisdom = clientHandler.viewingAttributeMap().valueOf(PeritiaAttributes.WISDOM.get());
            double added = wisdom + 100;

            List<Component> texts = new ArrayList<>();

            Component text = Component.translatable(PeritiaAttributes.WISDOM.get().getDescriptionId()).withStyle(ChatFormatting.GOLD)
                    .append(PeritiaTexts.space())
                    .append(PeritiaTexts.makeText(wisdom, ChatFormatting.WHITE));

            Component text2 = PeritiaTexts.xpMultiplier().append(PeritiaTexts.makePercentage(added, ChatFormatting.LIGHT_PURPLE));

            texts.add(text);
            texts.add(PeritiaTexts.wisdomInfo());
            texts.add(PeritiaTexts.empty());
            texts.add(text2);

            return texts;
        };
    }

    static TextGetter chestLuck()
    {
        return clientHandler ->
        {
            double chestLuck = clientHandler.viewingAttributeMap().valueOf(PeritiaAttributes.CHEST_LUCK.get());
            double added = chestLuck + 100;
            int lower = (int) (added / 100);
            int higher = lower + 1;

            double higherChance = chestLuck % 100;
            double lowerChance = 100 - higherChance;

            List<Component> texts = new ArrayList<>();

            Component text = Component.translatable(PeritiaAttributes.CHEST_LUCK.get().getDescriptionId()).withStyle(ChatFormatting.GOLD)
                    .append(PeritiaTexts.space())
                    .append(PeritiaTexts.makeText(chestLuck, ChatFormatting.WHITE));

            texts.add(text);
            texts.add(PeritiaTexts.chestLuckInfo());
            texts.add(PeritiaTexts.empty());

            if (lower == 1)
            {
                texts.add(PeritiaTexts.lootChance(higherChance, higher, ChatFormatting.LIGHT_PURPLE));
            }
            else
            {
                texts.add(PeritiaTexts.lootChance(lowerChance, lower, ChatFormatting.DARK_PURPLE));
                texts.add(PeritiaTexts.lootChance(higherChance, higher, ChatFormatting.LIGHT_PURPLE));
            }
            return texts;
        };
    }

    static TextGetter item(ItemInputs inputs) { return item(inputs, 16); }

    static TextGetter item(ItemInputs inputs, int textsPerPage)
    {
        if (inputs.acceptsAll()) return single(PeritiaTexts.anyItem());

        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();

            for (ItemInput input : inputs)
            {
                if (input instanceof ItemInput.Single single)
                {
                    Item item = single.item();
                    Component text = Component.translatable(item.getDescriptionId()).withStyle(ChatFormatting.GRAY);
                    texts.add(text);
                }
                else if (input instanceof ItemInput.Tag tag)
                {
                    ResourceLocation key = tag.tag().location();
                    String string = "#" + key;
                    Component text = Component.literal(string).withStyle(ChatFormatting.DARK_PURPLE);
                    texts.add(text);

                    if (Screen.hasShiftDown())
                    {
                        for (Item item : ForgeRegistries.ITEMS.getValues())
                        {
                            if (item.getDefaultInstance().is(tag.tag()))
                            {
                                Component text2 = PeritiaTexts.doubleSpace().append(Component.translatable(item.getDescriptionId()).withStyle(ChatFormatting.GRAY));
                                texts.add(text2);
                            }
                        }
                    }
                }
            }

            if (texts.size() <= textsPerPage) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + textsPerPage - 1) / textsPerPage;
            int currentPage = cycles % period;
            int fromIndex = currentPage * textsPerPage;
            int toIndex = Math.min(fromIndex + textsPerPage, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter block(BlockInputs inputs) { return block(inputs, 16); }

    static TextGetter block(BlockInputs inputs, int textsPerPage)
    {
        if (inputs.acceptsAll()) return single(PeritiaTexts.anyBlock());

        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();

            for (BlockInput input : inputs)
            {
                if (input instanceof BlockInput.Single single)
                {
                    Block block = single.block();
                    Component text = Component.translatable(block.getDescriptionId()).withStyle(ChatFormatting.GRAY);
                    texts.add(text);
                }
                else if (input instanceof BlockInput.Tag tag)
                {
                    ResourceLocation key = tag.tag().location();
                    String string = "#" + key;
                    Component text = Component.literal(string).withStyle(ChatFormatting.DARK_PURPLE);
                    texts.add(text);

                    if (Screen.hasShiftDown())
                    {
                        for (Block block : ForgeRegistries.BLOCKS.getValues())
                        {
                            if (block.defaultBlockState().is(tag.tag()))
                            {
                                Component text2 = PeritiaTexts.doubleSpace().append(Component.translatable(block.getDescriptionId()).withStyle(ChatFormatting.GRAY));
                                texts.add(text2);
                            }
                        }
                    }
                }
            }

            if (texts.size() <= textsPerPage) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + textsPerPage - 1) / textsPerPage;
            int currentPage = cycles % period;
            int fromIndex = currentPage * textsPerPage;
            int toIndex = Math.min(fromIndex + textsPerPage, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter entity(EntityInputs inputs)
    {
        if (inputs.acceptsAll()) return single(PeritiaTexts.anyEntity());

        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();

            for (EntityInput input : inputs)
            {
                if (input instanceof EntityInput.Single single)
                {
                    EntityType<?> entityType = single.entityTypeWithFallback();
                    Component text = Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY);
                    texts.add(text);
                }
                else if (input instanceof EntityInput.Tag tag)
                {
                    ResourceLocation key = tag.tag().location();
                    String string = "#" + key;
                    Component text = Component.literal(string).withStyle(ChatFormatting.DARK_PURPLE);
                    texts.add(text);

                    if (Screen.hasShiftDown())
                    {
                        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues())
                        {
                            if (entityType.is(tag.tag()))
                            {
                                Component text2 = PeritiaTexts.doubleSpace().append(Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY));
                                texts.add(text2);
                            }
                        }
                    }
                }
            }

            if (texts.size() <= 16) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + 15) / 16;
            int currentPage = cycles % period;
            int fromIndex = currentPage * 16;
            int toIndex = Math.min(fromIndex + 16, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter structure(StructureXpSource xpSource)
    {
        return clientHandler ->
        {
            if (xpSource.shouldUseHints()) return xpSource.hints().getTexts();
            if (xpSource.inputs().acceptsAll()) return List.of(PeritiaTexts.anyStructure());

            List<Component> texts = new ArrayList<>();
            StructureInputs inputs = xpSource.inputs();

            for (StructureInput input : inputs)
            {
                if (input instanceof StructureInput.Single single)
                {
                    Component text = Component.translatable(single.structureKey().toString()).withStyle(ChatFormatting.GRAY);
                    texts.add(text);
                }
                else if (input instanceof StructureInput.Tag tag)
                {
                    ResourceLocation key = tag.tag().location();
                    String string = "#" + key;
                    Component text = Component.literal(string).withStyle(ChatFormatting.DARK_PURPLE);
                    texts.add(text);

                    if (Screen.hasShiftDown())
                    {
                        StructureContext structureContext = clientHandler.gameContext().structureContext();
                        Set<ResourceLocation> keys = structureContext.keysForTag(tag.tag());

                        for (ResourceLocation structureKey : keys)
                        {
                            Component text2 = Component.translatable(structureKey.toString()).withStyle(ChatFormatting.GRAY);
                            texts.add(PeritiaTexts.doubleSpace().append(text2));
                        }
                    }
                }
            }

            if (texts.size() <= 12) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + 11) / 12;
            int currentPage = cycles % period;
            int fromIndex = currentPage * 12;
            int toIndex = Math.min(fromIndex + 12, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter advancement(ResourceInputs inputs)
    {
        if (inputs.acceptsAll()) return single(PeritiaTexts.anyAdvancement());

        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();
            AdvancementContext context = clientHandler.gameContext().advancementContext();

            for (ResourceInput input : inputs)
            {
                if (input instanceof ResourceInput.Single single)
                {
                    ResourceLocation key = single.key();
                    texts.add(context.nameOf(key));
                }
                else if (input instanceof ResourceInput.Contains contains)
                {
                    String string = contains.string();
                    String modId = contains.modId();
                    boolean display = false;

                    if (string != null)
                    {
                        String wrapped = "\"" + string + "\"";
                        texts.add(PeritiaTexts.relatedTo().copy().append(Component.literal(wrapped).withStyle(ChatFormatting.BLUE)));
                        display = true;
                    }
                    else if (modId != null)
                    {
                        texts.add(PeritiaTexts.anyAdvancementFrom().copy().append(PeritiaTexts.modName(modId)));
                        display = true;
                    }

                    if (Screen.hasShiftDown() && display)
                    {
                        for (ResourceLocation key : context.keys())
                        {
                            if (input.test(key)) texts.add(PeritiaTexts.doubleSpace().append(context.nameOf(key)));
                        }
                    }
                }
            }

            if (texts.size() <= 12) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + 11) / 12;
            int currentPage = cycles % period;
            int fromIndex = currentPage * 12;
            int toIndex = Math.min(fromIndex + 12, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter profession(TradeXpSource xpSource)
    {
        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();
            ResourceInputs inputs = xpSource.professionInputs();

            for (ResourceInput input : inputs)
            {
                if (input instanceof ResourceInput.Single single) texts.add(PeritiaTexts.profession(single.key()));
            }

            List<Component> subList;

            if (texts.size() <= 12)
            {
                subList = texts;
            }
            else
            {
                int cycles = clientHandler.screenTicks() / 24;
                int period = (texts.size() + 11) / 12;
                int currentPage = cycles % period;
                int fromIndex = currentPage * 12;
                int toIndex = Math.min(fromIndex + 12, texts.size());

                subList = new ArrayList<>(texts.subList(fromIndex, toIndex));
            }

            subList.add(0, PeritiaTexts.validProfessions());
            return subList;
        };
    }

    static TextGetter recipe(RecipeXpSource xpSource)
    {
        if (xpSource.shouldUseHints()) return clientHandler -> xpSource.hints().getTexts();
        return resource(xpSource.keyInputs(), ClientHandler.getRecipeManager().getRecipeIds().collect(Collectors.toSet()), PeritiaTexts.anyRecipeFrom(), PeritiaTexts.anyRecipe());
    }

    static TextGetter chest(ChestXpSource xpSource)
    {
        if (xpSource.shouldUseHints()) return clientHandler -> xpSource.hints().getTexts();
        return resource(xpSource.inputs(), ClientHandler.getGameContext().lootTableContext().chestKeys(), PeritiaTexts.anyChestFrom(), PeritiaTexts.anyChest());
    }

    static TextGetter resource(ResourceInputs inputs, Collection<ResourceLocation> keys, Component modText, Component anyText)
    {
        if (inputs.acceptsAll()) return single(anyText);

        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>();

            for (ResourceInput input : inputs)
            {
                if (input instanceof ResourceInput.Single single)
                {
                    texts.add(Component.literal(single.key().toString()).withStyle(ChatFormatting.GRAY));
                }
                else if (input instanceof ResourceInput.Contains contains)
                {
                    String string = contains.string();
                    String modId = contains.modId();
                    boolean display = false;

                    if (string != null)
                    {
                        String wrapped = "\"" + string + "\"";
                        texts.add(PeritiaTexts.relatedTo().copy().append(Component.literal(wrapped).withStyle(ChatFormatting.BLUE)));
                        display = true;
                    }
                    else if (modId != null)
                    {
                        texts.add(modText.copy().append(PeritiaTexts.modName(modId)));
                        display = true;
                    }

                    if (Screen.hasShiftDown() && display)
                    {
                        for (ResourceLocation key : keys)
                        {
                            if (input.test(key)) texts.add(PeritiaTexts.doubleSpace().append(Component.literal(key.toString()).withStyle(ChatFormatting.GRAY)));
                        }
                    }
                }
            }

            if (texts.size() <= 12) return texts;

            int cycles = clientHandler.screenTicks() / 24;
            int period = (texts.size() + 11) / 12;
            int currentPage = cycles % period;
            int fromIndex = currentPage * 12;
            int toIndex = Math.min(fromIndex + 12, texts.size());

            return texts.subList(fromIndex, toIndex);
        };
    }

    static TextGetter ingredient(ItemInputs inputs)
    {
        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>(item(inputs, 12).apply(clientHandler));
            texts.add(0, PeritiaTexts.validIngredients());
            return texts;
        };
    }

    static TextGetter anvilLeft(AnvilXpSource xpSource)
    {
        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>(item(xpSource.leftInputs(), 12).apply(clientHandler));
            texts.add(0, PeritiaTexts.leftSlot());
            return texts;
        };
    }

    static TextGetter anvilRight(AnvilXpSource xpSource)
    {
        return clientHandler ->
        {
            List<Component> texts = new ArrayList<>(item(xpSource.rightInputs(), 12).apply(clientHandler));
            texts.add(0, PeritiaTexts.rightSlot());
            return texts;
        };
    }

    static TextGetter remainingTimeLogin(SerializableDate nextValidLogin)
    {
        return clientHandler ->
        {
            if (nextValidLogin == null) return List.of();

            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.timeUntilNextXp());
            texts.add(PeritiaTexts.duration(nextValidLogin.offsetFromNow()));
            return texts;
        };
    }

    static TextGetter remainingTimeSocial(SocialXpSource xpSource, int playerCount)
    {
        return clientHandler ->
        {
            SerializableDate nextXp = xpSource.getNextXpClient();
            boolean shouldAward = xpSource.shouldAward(playerCount);
            ChatFormatting color = shouldAward ? ChatFormatting.WHITE : ChatFormatting.RED;
            Component readyText = shouldAward ? PeritiaTexts.ready() : PeritiaTexts.waitingForPlayers();

            if (nextXp == null) return List.of();

            List<Component> texts = new ArrayList<>();

            texts.add(PeritiaTexts.timeUntilNextXp());
            texts.add(PeritiaTexts.duration(nextXp.offsetFromNow(), readyText, color));

            texts.add(PeritiaTexts.onlinePlayers().copy().append(PeritiaTexts.makeText(playerCount, color)));

            if (shouldAward)
            {
                int multiplier = xpSource.multiplier(playerCount);

                Component xpText = PeritiaTexts.xpTitle(xpSource, multiplier, ChatFormatting.AQUA);
                texts.add(PeritiaTexts.reward().copy().append(xpText));
            }
            else texts.add(PeritiaTexts.notEnoughPlayers());

            return texts;
        };
    }

    static TextGetter enchantments(int xp)
    {
        return clientHandler ->
        {
            List<Enchantment> enchantments = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues());
            int newXp = Math.round(xp * (float) clientHandler.skillData().xpMultiplier());

            int cycles = clientHandler.screenTicks() / 48; // going slower since it's kinda unreadable
            int period = (enchantments.size() + 11) / 12;
            int currentPage = cycles % period;
            int fromIndex = currentPage * 12;
            int toIndex = Math.min(fromIndex + 12, enchantments.size());

            List<Enchantment> viewingEnchantments = enchantments.subList(fromIndex, toIndex);
            List<Component> texts = new ArrayList<>();

            Component xpTitle = PeritiaTexts.xpTitle(newXp * 5, ChatFormatting.DARK_AQUA);
            Component infoText = Component.translatable("xp_source.peritia.enchantment.info").withStyle(ChatFormatting.GRAY);
            Component infoText2 = Component.translatable("xp_source.peritia.enchantment.info_2", xpTitle).withStyle(ChatFormatting.GRAY);

            texts.add(infoText);
            texts.add(infoText2);

            if (Screen.hasShiftDown())
            {
                texts.add(PeritiaTexts.empty());

                for (Enchantment enchantment : viewingEnchantments) texts.add(PeritiaTexts.enchantmentXpInfo(enchantment, newXp));
            }
            return texts;
        };
    }

}
