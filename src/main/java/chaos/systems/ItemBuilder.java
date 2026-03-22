package chaos.systems;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static chaos.game.GameManager.getServer;
import static chaos.game.GameManager.getWorld;

public class ItemBuilder {
    private final ItemStack stack;
    private final Registry<Enchantment> registry;

    public ItemBuilder(Item item, int count) {
        this.stack = new ItemStack(item, count);
        this.registry = getServer().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    public ItemBuilder name(String name, Formatting color) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(color));
        return this;
    }

    public ItemBuilder enchant(RegistryKey<Enchantment> enchantment, int level) {
        stack.addEnchantment(registry.getOrThrow(enchantment), level);
        return this;
    }

    public <T> ItemBuilder component(ComponentType<T> type, T value) {
        stack.set(type, value);
        return this;
    }

    public ItemBuilder desc(String text, Formatting color) {
        LoreComponent currentLore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);

        List<Text> lines = new ArrayList<>(currentLore.lines());

        lines.add(Text.literal(text).formatted(color).styled(style -> style.withItalic(false)));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));

        return this;
    }

    public ItemBuilder maxDura(int amount) {
        stack.set(DataComponentTypes.MAX_DAMAGE, amount);
        return this;
    }

    public ItemBuilder stats(double damage, double speed) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        builder.add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("chaos", "damage"), damage - 1.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);

        builder.add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("chaos", "speed"), speed - 4.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        return this;
    }

    public ItemBuilder entityData(EntityType<?> type, Consumer<NbtCompound> nbtModifier) {
        NbtCompound nbt = new NbtCompound();
        nbtModifier.accept(nbt);
        TypedEntityData<EntityType<?>> typedData = TypedEntityData.create(type, NbtComponent.of(nbt).copyNbt());

        return component(DataComponentTypes.ENTITY_DATA, typedData);
    }

    public ItemBuilder unbreakable() {
        stack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
        return this;
    }

    public ItemStack build() {
        return this.stack;
    }
}