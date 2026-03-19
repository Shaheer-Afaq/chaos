package chaos.systems;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

import java.util.function.Consumer;

import static chaos.game.GameManager.getServer;

public class ItemBuilder {
    private final ItemStack stack;
    private final Registry<Enchantment> registry;

    public ItemBuilder(Item item) {
        this.stack = new ItemStack(item);
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

    public ItemBuilder maxDamage(int amount) {
        stack.set(DataComponentTypes.MAX_DAMAGE, amount);
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