package github.poscard8.peritia.util.text.insertion;

import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public interface Insertion extends Consumer<PeritiaClientHandler>, StringSerializable<Insertion>
{
    @Nullable
    static Insertion tryLoad(@Nullable String data)
    {
        if (data == null) return null;

        if (data.contains(ClaimRewardInsertion.PREFIX)) return ClaimRewardInsertion.tryLoad(data);
        if (data.contains(OpenSkillMenuInsertion.PREFIX)) return OpenSkillMenuInsertion.tryLoad(data);

        return null;
    }

    @Override
    default Insertion fallback() { return null; }

}
