package github.poscard8.peritia.command;

import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

public enum CommandPredicate
{
    ENABLED(() -> true),
    DISABLED(() -> false),
    ESSENTIAL_MOD_ONLY(() -> ModList.get().isLoaded("essential"));

    final Supplier<Boolean> testFunction;

    CommandPredicate(Supplier<Boolean> testFunction) { this.testFunction = testFunction; }

    public boolean test() { return testFunction.get(); }

}
