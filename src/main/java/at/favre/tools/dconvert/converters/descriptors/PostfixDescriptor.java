package at.favre.tools.dconvert.converters.descriptors;

/**
 * Created by PatrickF on 18.03.2016.
 */
public class PostfixDescriptor extends DensityDescriptor {
    public final String postFix;

    public PostfixDescriptor(float scale, String name, String postFix) {
        super(scale, name);
        this.postFix = postFix;
    }
}
